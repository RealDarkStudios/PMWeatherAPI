package net.nullved.pmweatherapi.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.protomanly.pmweather.PMWeather;
import dev.protomanly.pmweather.block.entity.RadarBlockEntity;
import dev.protomanly.pmweather.config.ClientConfig;
import dev.protomanly.pmweather.config.ServerConfig;
import dev.protomanly.pmweather.event.GameBusClientEvents;
import dev.protomanly.pmweather.multiblock.wsr88d.WSR88DCore;
import dev.protomanly.pmweather.render.RadarRenderer;
import dev.protomanly.pmweather.util.ColorTables;
import dev.protomanly.pmweather.util.Util;
import dev.protomanly.pmweather.weather.Sounding;
import dev.protomanly.pmweather.weather.Storm;
import dev.protomanly.pmweather.weather.ThermodynamicEngine;
import dev.protomanly.pmweather.weather.WindEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.client.render.PixelRenderData;
import net.nullved.pmweatherapi.client.render.RadarOverlays;
import net.nullved.pmweatherapi.client.render.RenderData;
import net.nullved.pmweatherapi.data.PMWExtras;
import net.nullved.pmweatherapi.radar.RadarMode;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mixin(RadarRenderer.class)
public class RadarRendererMixin {
    @WrapMethod(method = "render")
    private void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLightIn, int combinedOverlayIn, Operation<Void> original) {
        if (!(blockEntity instanceof RadarBlockEntity radarBlockEntity)) return;
        if (Minecraft.getInstance().player.position().distanceTo(blockEntity.getBlockPos().getCenter()) > (double)25.0F) return;

        boolean canRender = true;
        BlockPos pos = radarBlockEntity.getBlockPos();
        float sizeRenderDiameter = 3.0F;
        float simSize = 2048.0F;
        int resolution = ClientConfig.radarResolution;
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(poseStack.last().pose());
        matrix4fStack.translate(0.5F, 1.05F, 0.5F);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        List<Storm> storms = new ArrayList(radarBlockEntity.storms);
        boolean update = false;

        ClientConfig.RadarMode clientRadarMode = ClientConfig.radarMode;
        if (radarBlockEntity.lastUpdate < radarBlockEntity.tickCount) {
            radarBlockEntity.lastUpdate = radarBlockEntity.tickCount + 60;
            update = true;
        }

        if (ServerConfig.requireWSR88D && update) {
            canRender = false;
            int searchrange = 64;
            Level level = blockEntity.getLevel();

            // PMWeatherAPI: Minor optimization, Create Radar -> WSR-88D lookup to not do up to 64^3 level#getBlockState calls EVERY 3 SECONDS
            if (PMWExtras.RADAR_WSR_88D_LOOKUP.containsKey(pos)) {
                BlockEntity wsr88D = level.getBlockEntity(PMWExtras.RADAR_WSR_88D_LOOKUP.get(pos));
                if (wsr88D.getBlockState().getBlock() instanceof WSR88DCore wsr88DCore) {
                    if (wsr88DCore.isComplete(wsr88D.getBlockState())) {
                        canRender = true;
                    } else {
                        PMWExtras.RADAR_WSR_88D_LOOKUP.remove(pos);
                    }
                }
            } else {
                for (int x = -searchrange; x <= searchrange && !canRender; ++x) {
                    for (int y = -searchrange; y <= searchrange && !canRender; ++y) {
                        for (int z = -searchrange * 2; z <= searchrange * 2; ++z) {
                            BlockState state = level.getBlockState(pos.offset(x, y, z));
                            Block var26 = state.getBlock();
                            if (var26 instanceof WSR88DCore core) {
                                if (core.isComplete(state)) {
                                    canRender = true;
                                    PMWExtras.RADAR_WSR_88D_LOOKUP.put(pos, pos.offset(x, y, z));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        float size = sizeRenderDiameter / (float)resolution;

        RenderData renderData = new RenderData(blockEntity, partialTicks, poseStack, multiBufferSource, combinedLightIn, combinedOverlayIn);
        RadarMode radarMode = RadarMode.get(blockEntity.getBlockState().getValue(PMWExtras.RADAR_MODE).value());
        if (!PMWClientStorages.RADAR_MODE_COLORS.containsKey(radarMode)) update = true;

        for(int x = -resolution; x <= resolution; ++x) {
            for(int z = -resolution; z <= resolution; ++z) {
                float r, g, b, a;

                // PMWeatherAPI: Change ids to be integers instead of strings (also removes the String.format nightmare that was causing lag)
                int id = ((short) x + resolution) << 8 | ((short) z + resolution);
                long longID = (long)(x + resolution + 1) + (long)(z + resolution + 1) * ((long)resolution * 2L + 1L);;

                float dbz = radarBlockEntity.reflectivityMap.getOrDefault(longID, 0.0F);
                float temp = radarBlockEntity.temperatureMap.getOrDefault(longID, 15.0F);
                float vel = radarBlockEntity.velocityMap.getOrDefault(longID, 0.0F);
                Color color = PMWClientStorages.RADAR_MODE_COLORS.computeIfAbsent(radarMode, rm -> new HashMap<>()).getOrDefault(id, new Color(1.0F, 0, 1.0F));
                Color dbg = radarBlockEntity.debugMap.getOrDefault(longID, new Color(0, 0, 0));

                Vector3f pixelPos = (new Vector3f((float)x, 0.0F, (float)z)).mul(1.0F / (float)resolution).mul(sizeRenderDiameter / 2.0F);
                Vec3 worldPos = (new Vec3(x, 0.0F, z)).multiply(1.0F / (float)resolution, 0.0F, (1.0F / (float)resolution)).multiply(simSize, 0.0F, simSize).add(pos.getCenter());

                if (update) {
                    dbz = 0.0F;
                    temp = 0.0F;
                    Vec2 f = (new Vec2((float)x, (float)z)).normalized();
                    Vec3 wind = WindEngine.getWind(new Vec3(worldPos.x, blockEntity.getLevel().getMaxBuildHeight() + 1, worldPos.z), blockEntity.getLevel(), false, false, false);
                    Vec2 w = new Vec2((float)wind.x, (float)wind.z);
                    vel = f.dot(w);

                    for(Storm storm : storms) {
                        if (!storm.visualOnly) {
                            double stormSize = ServerConfig.stormSize * (double)2.0F;
                            if (storm.stormType == 0) {
                                stormSize *= 1.5F;
                            }

                            double scale = stormSize / (double)1200.0F;
                            double shapeNoise = radarBlockEntity.noise.getValue((float)radarBlockEntity.tickCount / 1200.0F, worldPos.x / ((double)750.0F * scale), worldPos.z / ((double)750.0F * scale));
                            double shapeNoise2 = radarBlockEntity.noise.getValue((float)radarBlockEntity.tickCount / 1200.0F, worldPos.z / ((double)750.0F * scale), worldPos.x / ((double)750.0F * scale));
                            double shapeNoise4 = radarBlockEntity.noise.getValue((float)radarBlockEntity.tickCount / 1200.0F, worldPos.z / ((double)250.0F * scale), worldPos.x / ((double)250.0F * scale));
                            shapeNoise *= 0.5F;
                            shapeNoise2 *= 0.5F;
                            shapeNoise4 *= 0.5F;
                            shapeNoise += 0.5F;
                            shapeNoise2 += 0.5F;
                            shapeNoise4 += 0.5F;
                            float localDBZ = 0.0F;
                            float smoothStage = (float)storm.stage + (float)storm.energy / 100.0F;

                            if (storm.stormType == 1) {
                                double rawDist = worldPos.distanceTo(storm.position.multiply(1.0F, 0.0F, 1.0F));
                                Vec2 v2fWorldPos = new Vec2((float)worldPos.x, (float)worldPos.z);
                                Vec2 stormVel = new Vec2((float)storm.velocity.x, (float)storm.velocity.z);
                                Vec2 v2fStormPos = new Vec2((float)storm.position.x, (float)storm.position.z);
                                Vec2 right = (new Vec2(stormVel.y, -stormVel.x)).normalized();
                                Vec2 fwd = stormVel.normalized();
                                Vec2 le = Util.mulVec2(right, -3000.0F * (float)scale);
                                Vec2 ri = Util.mulVec2(right, 3000.0F * (float)scale);
                                Vec2 off = Util.mulVec2(fwd, -((float)Math.pow(Mth.clamp(rawDist / ((double)3000.0F * scale), 0.0F, 1.0F), 2.0F)) * 900.0F * (float)scale);
                                le = le.add(off);
                                ri = ri.add(off);
                                le = le.add(v2fStormPos);
                                ri = ri.add(v2fStormPos);
                                float dist = Util.minimumDistance(le, ri, v2fWorldPos);

                                float intensity = switch (storm.stage) {
                                    case 1 -> 0.1F + (float)storm.energy / 100.0F * 0.7F;
                                    case 2 -> 0.8F + (float)storm.energy / 100.0F * 0.4F;
                                    case 3 -> 1.2F + (float)storm.energy / 100.0F;
                                    default -> (float)storm.energy / 100.0F * 0.1F;
                                };

                                if (intensity > 0.8F) {
                                    intensity = 0.8F + (intensity - 0.8F) / 1.5F;
                                }

                                Vec2 nearPoint = Util.nearestPoint(le, ri, v2fWorldPos);
                                Vec2 facing = v2fWorldPos.add(nearPoint.negated());
                                float behind = -facing.dot(fwd);
                                behind += (float)shapeNoise * 600.0F * (float)scale * 0.2F;
                                float sze = 600.0F * (float)scale * 1.5F * 3.0F;
                                behind += (float)stormSize / 2.0F;
                                if (behind > 0.0F) {
                                    sze *= Mth.lerp(Mth.clamp(smoothStage - 1.0F, 0.0F, 1.0F), 1.0F, 4.0F);
                                    float p = Mth.clamp(Math.abs(behind) / sze, 0.0F, 1.0F);
                                    float start = 0.06F;
                                    if (p <= start) {
                                        p /= start;
                                        localDBZ += (float)Math.pow(p, 2.0F);
                                    } else {
                                        p = 1.0F - (p - start) / (1.0F - start);
                                        localDBZ += (float)Math.pow(p, 4.0F);
                                    }
                                }

                                localDBZ *= Mth.sqrt(1.0F - Mth.clamp(dist / sze, 0.0F, 1.0F));
                                if (smoothStage > 3.0F) {
                                    float p = Mth.clamp((smoothStage - 3.0F) / 2.0F, 0.0F, 0.5F);
                                    localDBZ *= 0.8F + (float)shapeNoise2 * 0.4F * (1.0F - p);
                                    localDBZ *= 0.8F + (float)shapeNoise * 0.4F * (1.0F - p);
                                    localDBZ *= 1.0F + p * 0.25F;
                                } else {
                                    localDBZ *= 0.8F + (float)shapeNoise2 * 0.4F;
                                    localDBZ *= 0.8F + (float)shapeNoise * 0.4F;
                                }

                                localDBZ *= Mth.sqrt(intensity);
                            }

                            if (storm.stormType == 0) {
                                double dist = worldPos.distanceTo(storm.position.multiply(1.0F, 0.0F, 1.0F));
                                if (dist > stormSize * (double)4.0F) {
                                    continue;
                                }

                                float var178;
                                switch (storm.stage) {
                                    case 1 -> var178 = 0.1F + (float)storm.energy / 100.0F * 0.7F;
                                    case 2 -> var178 = 0.8F + (float)storm.energy / 100.0F * 0.4F;
                                    case 3 -> var178 = 1.2F + (float)storm.windspeed / 100.0F;
                                    default -> var178 = (float)Math.pow((float)storm.energy / 100.0F, 2.0F) * 0.1F;
                                }

                                float intensity = var178;
                                if (intensity > 0.8F) {
                                    intensity = 0.8F + (intensity - 0.8F) / 4.0F;
                                }

                                switch (storm.stage) {
                                    case 2 -> var178 = (float)storm.energy / 100.0F * 40.0F;
                                    case 3 -> var178 = 40.0F + (float)storm.windspeed;
                                    default -> var178 = 0.0F;
                                }

                                float windspeed = var178;
                                if (windspeed > 60.0F) {
                                    windspeed -= (windspeed - 60.0F) * 0.2F;
                                }

                                Vec3 torPos = storm.position.multiply(1.0F, 0.0F, 1.0F);
                                Vec3 corePos = torPos.add((double)100.0F * scale * (double)2.5F * (double)Math.clamp(intensity * 1.5F, 0.0F, 1.0F), 0.0F, (double)-350.0F * scale * (double)2.5F * (double)Math.clamp(intensity * 1.5F, 0.0F, 1.0F));
                                float xM = 1.75F;
                                if (worldPos.x > corePos.x) {
                                    xM = 1.0F;
                                }

                                double coreDist = Math.sqrt(Math.pow((worldPos.x - corePos.x) * (double)xM, 2.0F) + Math.pow((worldPos.z - corePos.z) * (double)1.5F, 2.0F)) / scale;
                                dist /= scale;
                                coreDist *= 0.9 + shapeNoise * 0.3;
                                Vec3 relPos = torPos.subtract(worldPos).multiply(scale, 0.0F, scale);
                                double d = (double)150.0F + dist / (double)3.0F;
                                double d2 = (double)75.0F + dist / (double)3.0F;
                                double angle = Math.atan2(relPos.z, relPos.x) - dist / d;
                                double angle2 = Math.atan2(relPos.z, relPos.x) - dist / d2;
                                double angle3 = Math.atan2(relPos.z, relPos.x) - dist / d2 / (double)2.0F;
                                angle += Math.toRadians(180.0F);
                                angle2 += Math.toRadians(180.0F);
                                angle3 += Math.toRadians(180.0F);
                                double angleMod = Math.toRadians(40.0F) * ((double)1.0F - Math.clamp(Math.pow((double)windspeed / (double)100.0F, 2.0F), 0.0F, 0.9));
                                double noise = (shapeNoise4 - (double)0.5F) * Math.toRadians(10.0F);
                                angle += angleMod + noise;
                                angle2 += angleMod + noise;
                                angle3 += angleMod + noise;
                                double inflow = Math.sin(angle - Math.toRadians(15.0F));
                                inflow = Math.pow(Math.abs(inflow), 0.5F) * Math.sin(inflow);
                                inflow *= (double)1.0F - Math.clamp(dist / (double)2400.0F, 0.0F, 1.0F);
                                if (inflow < (double)0.0F) {
                                    localDBZ += (float)(inflow * (double)2.0F * Math.pow(Math.clamp((double)(windspeed - 15.0F) / (double)50.0F, 0.0F, 1.0F), 2.0F));
                                }

                                double surge = Math.sin(angle2 - Math.toRadians(60.0F));
                                surge = Math.abs(surge) * Math.sin(surge);
                                surge *= ((double)1.0F - Math.pow(Math.clamp(dist / (double)1200.0F, 0.0F, 1.0F), 1.5F)) * ((double)1.0F - Math.clamp(dist / (double)200.0F, 0.0F, 0.3));
                                if (surge > (double)0.0F) {
                                    double n = 0.8 * ((double)1.0F - Math.clamp(Math.pow((double)windspeed / (double)80.0F, 2.0F), 0.0F, 1.0F));
                                    double m = (double)1.0F - shapeNoise4 * n;
                                    localDBZ += (float)(surge * (double)1.5F * Math.clamp(dist / (double)500.0F, 0.0F, 1.0F) * Math.sqrt(Math.clamp((double)(windspeed - 20.0F) / (double)50.0F, 0.0F, 1.0F)) * m);
                                }

                                double shield = Math.sin(angle3 - Math.toRadians(60.0F));
                                shield = Math.abs(shield) * Math.sin(shield);
                                shield *= (double)1.0F - Math.pow(Math.clamp(dist / (double)2400.0F, 0.0F, 1.0F), 2.0F);
                                if (shield > (double)0.0F) {
                                    localDBZ -= (float)(shield * (double)2.0F * Math.clamp(dist / (double)1000.0F, 0.0F, 1.0F) * Math.sqrt(Math.clamp((double)(windspeed - 30.0F) / (double)80.0F, 0.0F, 1.0F)));
                                }

                                double coreIntensity = ((double)1.0F - Math.clamp(coreDist / (double)1800.0F, 0.0F, 1.0F)) * ((double)1.5F - shapeNoise2 * (double)0.5F) * Math.sqrt(Math.clamp((double)intensity / (double)2.0F, 0.0F, 1.0F)) * Math.clamp(dist / (double)300.0F, 0.5F, 1.0F) * 1.2;
                                localDBZ += (float)Math.pow(coreIntensity, 0.65);
                            }

                            dbz = Math.max(dbz, localDBZ);
                        }
                    }

                    dbz += (PMWeather.RANDOM.nextFloat() - 0.5F) * 5.0F / 60.0F;
                    vel += (PMWeather.RANDOM.nextFloat() - 0.5F) * 3.0F;
                    if (dbz > 1.0F) {
                        dbz = (dbz - 1.0F) / 3.0F + 1.0F;
                    }

                    if (!canRender) {
                        dbz = PMWeather.RANDOM.nextFloat() * 1.2F;
                        vel = (PMWeather.RANDOM.nextFloat() - 0.5F) * 300.0F;
                        temp = 15.0F;
                    } else {
                        temp = ThermodynamicEngine.samplePoint(GameBusClientEvents.weatherHandler, worldPos, blockEntity.getLevel(), radarBlockEntity, 0).temperature();
                    }

                    radarBlockEntity.reflectivityMap.put(longID, dbz);
                    radarBlockEntity.temperatureMap.put(longID, temp);
                    radarBlockEntity.velocityMap.put(longID, vel);

                    // PMWeatherAPI: Support custom radar modes
                    PixelRenderData pixelRenderData = new PixelRenderData(canRender, dbz * 60.0F, vel, temp, x, z, resolution, renderData);
                    color = radarMode.getColorForPixel(pixelRenderData);
                    PMWClientStorages.RADAR_MODE_COLORS.get(radarMode).put(id, color);
                }

                float rdbz = dbz * 60.0F;

//                Color color = ColorTables.getReflectivity(rdbz);
//                RadarBlock.Mode mode = blockEntity.getBlockState().getValue(RadarBlock.RADAR_MODE);
//                if (mode == dev.protomanly.pmweather.block.RadarBlock.Mode.VELOCITY) {
//                    color = new Color(0, 0, 0);
//                    vel /= 1.75F;
//                    color = ColorTables.lerp(Mth.clamp(Math.max(rdbz, (Mth.abs(vel) - 18.0F) / 0.65F) / 12.0F, 0.0F, 1.0F), color, ColorTables.getVelocity(vel));
//                }

                if (ClientConfig.radarDebugging && update) {
                    if (clientRadarMode == ClientConfig.RadarMode.TEMPERATURE) {
                        float t = ThermodynamicEngine.samplePoint(GameBusClientEvents.weatherHandler, worldPos, blockEntity.getLevel(), radarBlockEntity, 0).temperature();
                        if (t <= 0.0F) {
                            dbg = ColorTables.lerp(Math.clamp(t / -40.0F, 0.0F, 1.0F), new Color(153, 226, 251, 255), new Color(29, 53, 221, 255));
                        } else if (t < 15.0F) {
                            dbg = ColorTables.lerp(Math.clamp(t / 15.0F, 0.0F, 1.0F), new Color(255, 255, 255, 255), new Color(225, 174, 46, 255));
                        } else {
                            dbg = ColorTables.lerp(Math.clamp((t - 15.0F) / 25.0F, 0.0F, 1.0F), new Color(225, 174, 46, 255), new Color(232, 53, 14, 255));
                        }
                    }

                    if (clientRadarMode == ClientConfig.RadarMode.WINDFIELDS && GameBusClientEvents.weatherHandler != null) {
                        Vec3 wP = (new Vec3(x, 0.0F, z)).multiply(1.0F / (float)resolution, 0.0F, 1.0F / (float)resolution).multiply(256.0F, 0.0F, 256.0F).add(pos.getCenter());
                        float wind = 0.0F;

                        for(Storm storm : storms) {
                            wind += storm.getWind(wP);
                        }

                        dbg = ColorTables.getWindspeed(wind);
                    }

                    if (clientRadarMode == ClientConfig.RadarMode.CAPE) {
                        Sounding sounding = new Sounding(GameBusClientEvents.weatherHandler, worldPos, blockEntity.getLevel(), 500, 12000, radarBlockEntity);
                        Sounding.CAPE CAPE = sounding.getCAPE(sounding.getSBParcel());
                        dbg = ColorTables.lerp(Mth.clamp(CAPE.CAPE() / 6000.0F, 0.0F, 1.0F), new Color(0, 0, 0), new Color(255, 0, 0));
                    }

                    if (clientRadarMode == ClientConfig.RadarMode.CAPE3KM) {
                        Sounding sounding = new Sounding(GameBusClientEvents.weatherHandler, worldPos, blockEntity.getLevel(), 250, 4000, radarBlockEntity);
                        Sounding.CAPE CAPE = sounding.getCAPE(sounding.getSBParcel());
                        dbg = ColorTables.lerp(Mth.clamp(CAPE.CAPE3() / 1000.0F, 0.0F, 1.0F), new Color(0, 0, 0), new Color(255, 0, 0));
                    }

                    if (clientRadarMode == ClientConfig.RadarMode.CINH) {
                        Sounding sounding = new Sounding(GameBusClientEvents.weatherHandler, worldPos, blockEntity.getLevel(), 500, 12000, radarBlockEntity);
                        Sounding.CAPE CAPE = sounding.getCAPE(sounding.getSBParcel());
                        dbg = ColorTables.lerp(Mth.clamp(CAPE.CINH() / -250.0F, 0.0F, 1.0F), new Color(0, 0, 0), new Color(0, 0, 255));
                    }

                    if (clientRadarMode == ClientConfig.RadarMode.LAPSERATE03) {
                        Sounding sounding = new Sounding(GameBusClientEvents.weatherHandler, worldPos, blockEntity.getLevel(), 250, 4000, radarBlockEntity);
                        float lapse = (float)Math.floor(sounding.getLapseRate(0, 3000) * 2.0F) / 2.0F;
                        if (lapse > 5.0F) {
                            dbg = ColorTables.lerp(Mth.clamp((lapse - 5.0F) / 5.0F, 0.0F, 1.0F), new Color(255, 255, 0), new Color(255, 0, 0));
                        } else {
                            dbg = ColorTables.lerp(Mth.clamp(lapse / 5.0F, 0.0F, 1.0F), new Color(0, 255, 0), new Color(255, 255, 0));
                        }
                    }

                    if (clientRadarMode == ClientConfig.RadarMode.LAPSERATE36) {
                        Sounding sounding = new Sounding(GameBusClientEvents.weatherHandler, worldPos, blockEntity.getLevel(), 250, 7000, radarBlockEntity);
                        float lapse = (float)Math.floor(sounding.getLapseRate(3000, 6000) * 2.0F) / 2.0F;
                        if (lapse > 5.0F) {
                            dbg = ColorTables.lerp(Mth.clamp((lapse - 5.0F) / 5.0F, 0.0F, 1.0F), new Color(255, 255, 0), new Color(255, 0, 0));
                        } else {
                            dbg = ColorTables.lerp(Mth.clamp(lapse / 5.0F, 0.0F, 1.0F), new Color(0, 255, 0), new Color(255, 255, 0));
                        }
                    }

                    radarBlockEntity.debugMap.put(longID, dbg);
                }
                if (ClientConfig.radarDebugging) {
                    color = dbg;
                }

                r = (float)color.getRed() / 255.0F;
                g = (float)color.getGreen() / 255.0F;
                b = (float)color.getBlue() / 255.0F;
                a = (float)color.getAlpha() / 255.0F * 0.75F + 0.25F;

                Vector3f topLeft = (new Vector3f(-1.0F, 0.0F, -1.0F)).mul(size / 4.0F).add(pixelPos);
                Vector3f bottomLeft = (new Vector3f(-1.0F, 0.0F, 1.0F)).mul(size / 4.0F).add(pixelPos);
                Vector3f bottomRight = (new Vector3f(1.0F, 0.0F, 1.0F)).mul(size / 4.0F).add(pixelPos);
                Vector3f topRight = (new Vector3f(1.0F, 0.0F, -1.0F)).mul(size / 4.0F).add(pixelPos);

                bufferBuilder.addVertex(topLeft).setColor(r, g, b, a).addVertex(bottomLeft).setColor(r, g, b, a).addVertex(bottomRight).setColor(r, g, b, a).addVertex(topRight).setColor(r, g, b, a);
            }
        }

        int color = radarMode.getDotColor().hashCode();
        Vector3f topLeft = (new Vector3f(-1.0F, 0.0F, -1.0F)).mul(0.015F).add(0.0F, 0.01F, 0.0F);
        Vector3f bottomLeft = (new Vector3f(-1.0F, 0.0F, 1.0F)).mul(0.015F).add(0.0F, 0.01F, 0.0F);
        Vector3f bottomRight = (new Vector3f(1.0F, 0.0F, 1.0F)).mul(0.015F).add(0.0F, 0.01F, 0.0F);
        Vector3f topRight = (new Vector3f(1.0F, 0.0F, -1.0F)).mul(0.015F).add(0.0F, 0.01F, 0.0F);
        bufferBuilder.addVertex(topLeft).setColor(color).addVertex(bottomLeft).setColor(color).addVertex(bottomRight).setColor(color).addVertex(topRight).setColor(color);

        // PMWeatherAPI: RadarOverlays callback
        RadarOverlays.renderOverlays(renderData, bufferBuilder, canRender);

        matrix4fStack.mul(poseStack.last().pose().invert());
        matrix4fStack.translate(-0.5F, -1.05F, -0.5F);
        matrix4fStack.popMatrix();
        MeshData meshData = bufferBuilder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }

        RenderSystem.applyModelViewMatrix();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }
}
