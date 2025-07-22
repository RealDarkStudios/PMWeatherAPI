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
import dev.protomanly.pmweather.weather.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.client.render.PixelRenderData;
import net.nullved.pmweatherapi.client.render.RadarOverlays;
import net.nullved.pmweatherapi.client.render.RenderData;
import net.nullved.pmweatherapi.config.PMWClientConfig;
import net.nullved.pmweatherapi.data.PMWExtras;
import net.nullved.pmweatherapi.radar.RadarMode;
import net.nullved.pmweatherapi.util.StormType;
import org.joml.Matrix4fStack;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static dev.protomanly.pmweather.render.RadarRenderer.FBM;

@OnlyIn(Dist.CLIENT)
@Mixin(RadarRenderer.class)
public class RadarRendererMixin {
    @Shadow
    public static int RenderedRadars = 0;

    @WrapMethod(method = "render")
    private void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLightIn, int combinedOverlayIn, Operation<Void> original) {
        if (!(blockEntity instanceof RadarBlockEntity radarBlockEntity)) return;
        if (Minecraft.getInstance().player.position().distanceTo(blockEntity.getBlockPos().getCenter()) > (double) 20.0F || RenderedRadars > 2) return;

        ++RenderedRadars;
        boolean canRender = true;
        BlockPos pos = radarBlockEntity.getBlockPos();
        float sizeRenderDiameter = 3.0F;
        float simSize = 2048.0F;

        if (radarBlockEntity.hasRangeUpgrade) {
            simSize *= 4.0F;
            if (!ClientConfig._3X3Radar) {
                sizeRenderDiameter = 6.0F;
            }
        }

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
        List<Storm> storms = new ArrayList<>(radarBlockEntity.storms);
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

        float size = sizeRenderDiameter / (float) resolution;

        RenderData renderData = new RenderData(blockEntity, sizeRenderDiameter, partialTicks, poseStack, multiBufferSource, combinedLightIn, combinedOverlayIn);
        RadarMode radarMode = blockEntity.getBlockState().getValue(PMWExtras.RADAR_MODE);
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

                Vector3f pixelPos = (new Vector3f((float) x, 0.0F, (float) z)).mul(1.0F / (float) resolution).mul(sizeRenderDiameter / 2.0F);
                Vec3 worldPos = (new Vec3(x, 0.0F, z)).multiply(1.0F / (float) resolution, 0.0F, (1.0F / (float) resolution)).multiply(simSize, 0.0F, simSize).add(pos.getCenter());

                if (update) {
                    float clouds = Clouds.getCloudDensity(GameBusClientEvents.weatherHandler, new Vector2f((float)worldPos.x, (float)worldPos.z), 0.0F);

                    dbz = 0.0F;
                    temp = 0.0F;
                    Vec2 f = (new Vec2((float)x, (float)z)).normalized();
                    Vec3 wind = WindEngine.getWind(new Vec3(worldPos.x, blockEntity.getLevel().getMaxBuildHeight() + 1, worldPos.z), blockEntity.getLevel(), false, false, false);
                    Vec2 w = new Vec2((float)wind.x, (float)wind.z);
                    vel = f.dot(w);

                    for(Storm storm : storms) {
                        if (!storm.visualOnly) {
                            double stormSize = ServerConfig.stormSize * (double) 2.0F;
                            if (storm.stormType == 0) {
                                stormSize *= 1.5F;
                            }

                            double scale = stormSize / (double)1200.0F;
                            if (storm.stormType == 2) {
                                scale = (float) storm.maxWidth / 3000.0F;
                                scale *= 0.5F;
                            }

                            double shapeNoise = radarBlockEntity.noise.getValue(((float)radarBlockEntity.tickCount / 8000.0F), worldPos.x / ((double)750.0F * scale), worldPos.z / ((double)750.0F * scale));
                            float fineShapeNoise = FBM(radarBlockEntity.noise, new Vec3((float)radarBlockEntity.tickCount / 8000.0F, worldPos.x / ((double)500.0F * scale), worldPos.z / ((double)500.0F * scale)), 10, 2.0F, 0.75F, 1.0F);
                            double shapeNoise2 = radarBlockEntity.noise.getValue((float)radarBlockEntity.tickCount / 8000.0F, worldPos.z / ((double)750.0F * scale), worldPos.x / ((double)750.0F * scale));
                            double shapeNoise3 = radarBlockEntity.noise.getValue(((float)radarBlockEntity.tickCount / 16000.0F), worldPos.x / ((double)4000.0F * scale), worldPos.z / ((double)4000.0F * scale));
                            double shapeNoise4 = radarBlockEntity.noise.getValue(((float)radarBlockEntity.tickCount / 8000.0F), worldPos.z / ((double)250.0F * scale), worldPos.x / ((double)250.0F * scale));
                            shapeNoise *= (double)0.5F;
                            shapeNoise2 *= (double)0.5F;
                            shapeNoise4 *= (double)0.5F;
                            shapeNoise += (double)0.5F;
                            shapeNoise2 += (double)0.5F;
                            shapeNoise4 += (double)0.5F;
                            float localDBZ = 0.0F;
                            float smoothStage = (float)storm.stage + (float)storm.energy / 100.0F;

                            if (storm.stormType == StormType.CYCLONE.idx()) {
                                Vec3 wPos = worldPos;
                                Vec3 cPos = storm.position.multiply(1.0F, 0.0F, 1.0F);

                                for(Vorticy vorticy : storm.vorticies) {
                                    Vec3 vPos = vorticy.getPosition();
                                    float width = vorticy.getWidth() * 0.35F;
                                    double d = wPos.multiply(1.0F, 0.0F, 1.0F).distanceTo(vPos.multiply(1.0F, 0.0F, 1.0F));
                                    if (d < (double) width) {
                                        double angle = Math.pow((double) 1.0F - Math.clamp(d / (double) width, 0.0F, 1.0F), 3.75F);
                                        angle *= ((float) Math.PI / 10F);
                                        angle *= Math.min(vorticy.windspeedMult * (float) storm.windspeed, 6.0F);
                                        wPos = Util.rotatePoint(wPos, vPos, angle);
                                    }
                                }

                                double rawDist = wPos.multiply(1.0F, 0.0F, 1.0F).distanceTo(storm.position.multiply(1.0F, 0.0F, 1.0F));
                                rawDist *= (double) 1.0F + shapeNoise3 * (double) 0.2F;
                                float intensity = (float) Math.pow(Math.clamp((float) storm.windspeed / 65.0F, 0.0F, 1.0F), 0.25F);
                                Vec3 relPos = cPos.subtract(wPos).multiply(scale, 0.0F, scale);
                                double d = ((float) storm.maxWidth / (3.0F + (float) storm.windspeed / 12.0F));
                                double d2 = ((float) storm.maxWidth / (1.15F + (float) storm.windspeed / 12.0F));
                                double dE = ((float) storm.maxWidth * 0.65F / (1.75F + (float) storm.windspeed / 12.0F));
                                double fac = (double) 1.0F + Math.max((rawDist - (double) ((float) storm.maxWidth * 0.2F)) / (double) storm.maxWidth, 0.0F) * (double) 2.0F;
                                d *= fac;
                                d2 *= fac;
                                double angle = Math.atan2(relPos.z, relPos.x) - rawDist / d;
                                double angle2 = Math.atan2(relPos.z, relPos.x) - rawDist / d2;
                                double angleE = Math.atan2(relPos.z, relPos.x) - rawDist / dE;
                                float weak = 0.0F;
                                float strong = 0.0F;
                                float intense = 0.0F;
                                float staticBands = (float) Math.sin(angle - (Math.PI / 2D));
                                staticBands *= (float) Math.pow(Math.clamp(rawDist / (double) ((float) storm.maxWidth * 0.25F), 0.0F, 1.0F), 0.1F);
                                staticBands *= 1.25F * (float) Math.pow(intensity, 0.75F);
                                if (staticBands < 0.0F) {
                                    weak += Math.abs(staticBands);
                                } else {
                                    weak += Math.abs(staticBands) * (float) Math.pow((double) 1.0F - Math.clamp(rawDist / (double) ((float) storm.maxWidth * 0.65F), 0.0F, 1.0F), 0.5F);
                                    weak *= Math.clamp(((float) storm.windspeed - 70.0F) / 40.0F, 0.0F, 1.0F);
                                }

                                float rotatingBands = (float) Math.sin((angle2 + Math.toRadians(((float) storm.tickCount / 8.0F))) * (double) 6.0F);
                                rotatingBands *= (float) Math.pow(Math.clamp(rawDist / (double) ((float) storm.maxWidth * 0.25F), 0.0F, 1.0F), 0.1F);
                                rotatingBands *= 1.25F * (float) Math.pow(intensity, 0.75F);
                                strong += Mth.lerp(0.45F, Math.abs(rotatingBands) * 0.3F + 0.7F, weak);
                                intense += Mth.lerp(0.3F, Math.abs(rotatingBands) * 0.2F + 0.8F, weak);
                                weak = (Math.abs(rotatingBands) * 0.3F + 0.6F) * weak;
                                localDBZ += Mth.lerp(Math.clamp(((float) storm.windspeed - 120.0F) / 60.0F, 0.0F, 1.0F), Mth.lerp(Math.clamp(((float) storm.windspeed - 40.0F) / 90.0F, 0.0F, 1.0F), weak, strong), intense);
                                float eye = (float) Math.sin((angleE + Math.toRadians(((float) storm.tickCount / 4.0F))) * (double) 2.0F);
                                float efc = Mth.lerp(Math.clamp(((float) storm.windspeed - 100.0F) / 50.0F, 0.0F, 1.0F), 0.15F, 0.4F);
                                localDBZ = Math.max((float) Math.pow((double) 1.0F - Math.clamp(rawDist / (double) ((float) storm.maxWidth * efc), 0.0F, 1.0F), 0.5F) * (Math.abs(eye * 0.1F) + 0.9F) * 1.35F * intensity, localDBZ);
                                localDBZ *= (float) Math.pow((double) 1.0F - Math.clamp(rawDist / (double) storm.maxWidth, 0.0F, 1.0F), 0.5F);
                                localDBZ *= Mth.lerp(0.5F + Math.clamp(((float) storm.windspeed - 65.0F) / 40.0F, 0.0F, 1.0F) * 0.5F, 1.0F, (float) Math.pow(Math.clamp(rawDist / (double)((float) storm.maxWidth * 0.1F), 0.0F, 1.0F), 2.0F));
                                localDBZ *= Mth.lerp(Math.clamp(((float) storm.windspeed - 75.0F) / 50.0F, 0.0F, 1.0F), 0.8F + (float) shapeNoise2 * 0.4F, 1.0F);
                                localDBZ *= 0.8F + (float) shapeNoise * 0.4F;
                                localDBZ *= 1.0F + fineShapeNoise * Mth.lerp((float)Math.pow(Math.clamp(rawDist / (double) storm.maxWidth, 0.0F, 1.0F), 1.5F), 0.05F, 0.15F);
                                localDBZ = (float) Math.pow(localDBZ, 1.75F);
                                if (localDBZ > 0.8F) {
                                    float dif = (localDBZ - 0.8F) / 1.25F;
                                    localDBZ -= dif;
                                }
                            }

                            if (storm.stormType == StormType.SQUALL.idx()) {
                                double rawDist = worldPos.multiply(1.0F, 0.0F, 1.0F).distanceTo(storm.position.multiply(1.0F, 0.0F, 1.0F));
                                Vec2 v2fWorldPos = new Vec2((float) worldPos.x, (float) worldPos.z);
                                Vec2 stormVel = new Vec2((float) storm.velocity.x, (float) storm.velocity.z);
                                Vec2 v2fStormPos = new Vec2((float) storm.position.x, (float) storm.position.z);
                                Vec2 right = (new Vec2(stormVel.y, -stormVel.x)).normalized();
                                Vec2 fwd = stormVel.normalized();
                                Vec2 le = Util.mulVec2(right, -3000.0F * (float) scale);
                                Vec2 ri = Util.mulVec2(right, 3000.0F * (float) scale);
                                Vec2 off = Util.mulVec2(fwd, -((float) Math.pow(Mth.clamp(rawDist / ((double) 3000.0F * scale), 0.0F, 1.0F), 2.0F)) * 900.0F * (float) scale);
                                le = le.add(off);
                                ri = ri.add(off);
                                le = le.add(v2fStormPos);
                                ri = ri.add(v2fStormPos);
                                float dist = Util.minimumDistance(le, ri, v2fWorldPos);

                                float intensity = switch (storm.stage) {
                                    case 1 -> 0.1F + (float) storm.energy / 100.0F * 0.7F;
                                    case 2 -> 0.8F + (float) storm.energy / 100.0F * 0.4F;
                                    case 3 -> 1.2F + (float) storm.energy / 100.0F;
                                    default -> (float) storm.energy / 100.0F * 0.1F;
                                };

                                if (intensity > 0.8F) {
                                    intensity = 0.8F + (intensity - 0.8F) / 1.5F;
                                }

                                Vec2 nearPoint = Util.nearestPoint(le, ri, v2fWorldPos);
                                Vec2 facing = v2fWorldPos.add(nearPoint.negated());
                                float behind = -facing.dot(fwd);
                                behind += (float) shapeNoise * 600.0F * (float) scale * 0.2F;
                                float sze = 600.0F * (float) scale * 1.5F * 3.0F;
                                behind += (float) stormSize / 2.0F;
                                if (behind > 0.0F) {
                                    sze *= Mth.lerp(Mth.clamp(smoothStage - 1.0F, 0.0F, 1.0F), 1.0F, 4.0F);
                                    float p = Mth.clamp(Math.abs(behind) / sze, 0.0F, 1.0F);
                                    float start = 0.06F;
                                    if (p <= start) {
                                        p /= start;
                                        localDBZ += (float) Math.pow(p, 2.0F);
                                    } else {
                                        p = 1.0F - (p - start) / (1.0F - start);
                                        localDBZ += (float) Math.pow(p, 4.0F);
                                    }
                                }

                                localDBZ *= Mth.sqrt(1.0F - Mth.clamp(dist / sze, 0.0F, 1.0F));
                                if (smoothStage > 3.0F) {
                                    float p = Mth.clamp((smoothStage - 3.0F) / 2.0F, 0.0F, 0.5F);
                                    localDBZ *= 0.8F + (float) shapeNoise2 * 0.4F * (1.0F - p);
                                    localDBZ *= 0.8F + (float) shapeNoise * 0.4F * (1.0F - p);
                                    localDBZ *= 1.0F + p * 0.25F;
                                } else {
                                    localDBZ *= 0.8F + (float) shapeNoise2 * 0.4F;
                                    localDBZ *= 0.8F + (float) shapeNoise * 0.4F;
                                }

                                localDBZ *= Mth.sqrt(intensity);
                            }

                            if (storm.stormType == StormType.SUPERCELL.idx()) {
                                double dist = worldPos.multiply(1.0F, 0.0F, 1.0F).distanceTo(storm.position.multiply(1.0F, 0.0F, 1.0F));
                                if (dist > stormSize * (double) 4.0F) {
                                    continue;
                                }

                                float intensity = switch (storm.stage) {
                                    case 1 -> 0.1F + (float) storm.energy / 100.0F * 0.7F;
                                    case 2 -> 0.8F + (float) storm.energy / 100.0F * 0.4F;
                                    case 3 -> 1.2F + (float) storm.energy / 100.0F;
                                    default -> (float) Math.pow(storm.energy / 100.0F, 2.0F) * 0.1F;
                                };

                                if (intensity > 0.8F) {
                                    intensity = 0.8F + (intensity - 0.8F) / 4.0F;
                                }

                                float windspeed = switch (storm.stage) {
                                    case 2 -> (float) storm.energy / 100.0F * 40.0F;
                                    case 3 -> 40.0F + (float) storm.windspeed;
                                    default -> 0.0F;
                                };

                                if (windspeed > 60.0F) {
                                    windspeed -= (windspeed - 60.0F) * 0.2F;
                                }

                                Vec3 torPos = storm.position.multiply(1.0F, 0.0F, 1.0F);
                                Vec3 corePos = torPos.add((double)100.0F * scale * (double)2.5F * (double)Math.clamp(intensity * 1.5F, 0.0F, 1.0F), 0.0F, (double)-350.0F * scale * (double)2.5F * (double)Math.clamp(intensity * 1.5F, 0.0F, 1.0F));
                                float xM = 1.75F;
                                if (worldPos.x > corePos.x) {
                                    xM = 1.0F;
                                }

                                double coreDist = Math.sqrt(Math.pow((worldPos.x - corePos.x) * (double) xM, 2.0F) + Math.pow((worldPos.z - corePos.z) * (double) 1.5F, 2.0F)) / scale;
                                dist /= scale;
                                coreDist *= 0.9 + shapeNoise * 0.3;
                                Vec3 relPos = torPos.subtract(worldPos).multiply(scale, 0.0F, scale);
                                double d = (double) 150.0F + dist / (double) 3.0F;
                                double d2 = (double) 75.0F + dist / (double) 3.0F;
                                double angle = Math.atan2(relPos.z, relPos.x) - dist / d;
                                double angle2 = Math.atan2(relPos.z, relPos.x) - dist / d2;
                                double angle3 = Math.atan2(relPos.z, relPos.x) - dist / d2 / (double) 2.0F;
                                angle += Math.toRadians(180.0F);
                                angle2 += Math.toRadians(180.0F);
                                angle3 += Math.toRadians(180.0F);
                                double angleMod = Math.toRadians(40.0F) * ((double) 1.0F - Math.clamp(Math.pow((double) windspeed / (double) 100.0F, 2.0F), 0.0F, 0.9));
                                double noise = (shapeNoise4 - (double) 0.5F) * Math.toRadians(10.0F);
                                angle += angleMod + noise;
                                angle2 += angleMod + noise;
                                angle3 += angleMod + noise;
                                double inflow = Math.sin(angle - Math.toRadians(15.0F));
                                inflow = Math.pow(Math.abs(inflow), 0.5F) * Math.sin(inflow);
                                inflow *= (double) 1.0F - Math.clamp(dist / (double) 2400.0F, 0.0F, 1.0F);
                                if (inflow < (double) 0.0F) {
                                    localDBZ += (float) (inflow * (double) 2.0F * Math.pow(Math.clamp((double) (windspeed - 15.0F) / (double) 50.0F, 0.0F, 1.0F), 2.0F));
                                }

                                double surge = Math.sin(angle2 - Math.toRadians(60.0F));
                                surge = Math.abs(surge) * Math.sin(surge);
                                surge *= ((double) 1.0F - Math.pow(Math.clamp(dist / (double) 1200.0F, 0.0F, 1.0F), 1.5F)) * ((double) 1.0F - Math.clamp(dist / (double) 200.0F, 0.0F, 0.3));
                                if (surge > (double) 0.0F) {
                                    double n = 0.8 * ((double) 1.0F - Math.clamp(Math.pow((double) windspeed / (double) 80.0F, 2.0F), 0.0F, 1.0F));
                                    double m = (double) 1.0F - shapeNoise4 * n;
                                    localDBZ += (float) (surge * (double)1.5F * Math.clamp(dist / (double) 500.0F, 0.0F, 1.0F) * Math.sqrt(Math.clamp((double) (windspeed - 20.0F) / (double) 50.0F, 0.0F, 1.0F)) * m);
                                }

                                double shield = Math.sin(angle3 - Math.toRadians(60.0F));
                                shield = Math.abs(shield) * Math.sin(shield);
                                shield *= (double) 1.0F - Math.pow(Math.clamp(dist / (double) 2400.0F, 0.0F, 1.0F), 2.0F);
                                if (shield > (double) 0.0F) {
                                    localDBZ -= (float) (shield * (double) 2.0F * Math.clamp(dist / (double) 1000.0F, 0.0F, 1.0F) * Math.sqrt(Math.clamp((double) (windspeed - 30.0F) / (double) 80.0F, 0.0F, 1.0F)));
                                }

                                double coreIntensity = ((double) 1.0F - Math.clamp(coreDist / (double) 1800.0F, 0.0F, 1.0F)) * ((double) 1.5F - shapeNoise2 * (double) 0.5F) * Math.sqrt(Math.clamp((double) intensity / (double) 2.0F, 0.0F, 1.0F)) * Math.clamp(dist / (double) 300.0F, 0.5F, 1.0F) * 1.2;
                                localDBZ += (float) Math.pow(coreIntensity, 0.65);
                            }

                            dbz = Math.max(dbz, localDBZ);
                        }
                    }

                    float v = Math.max(clouds - 0.15F, 0.0F) * 4.0F;
                    if (v > 0.4F) {
                        float dif = (v - 0.4F) / 2.0F;
                        v -= dif;
                    }

                    dbz = Math.max(dbz, v);
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
                    if (!PMWClientConfig.disableCustomRadarModeRendering) {
                        PixelRenderData pixelRenderData = new PixelRenderData(canRender, dbz * 60.0F, vel, temp, x, z, resolution, worldPos, renderData);
                        color = radarMode.getColorForPixel(pixelRenderData);
                        PMWClientStorages.RADAR_MODE_COLORS.get(radarMode).put(id, color);
                    }
                }

                float rdbz = dbz * 60.0F;
                Color startColor = radarBlockEntity.terrainMap.getOrDefault(longID, Color.BLACK);
                if (radarBlockEntity.init && update) {
                    Holder<Biome> biome = radarBlockEntity.getNearestBiome(new BlockPos((int) worldPos.x, (int) worldPos.y, (int) worldPos.z));
                    String rn = biome.getRegisteredName().toLowerCase();
                    if (rn.contains("ocean") || rn.contains("river")) startColor = new Color(biome.value().getWaterColor());
                    else if (rn.contains("beach") || rn.contains("desert")) {
                        if (rn.contains("badlands")) startColor = new Color(214, 111, 42);
                        else startColor = new Color(biome.value().getGrassColor(worldPos.x, worldPos.z));
                    } else startColor = new Color(227, 198, 150);
                }

                if (PMWClientConfig.disableCustomRadarModeRendering) {
                    color = ColorTables.getReflectivity(rdbz, startColor);

                    if (rdbz > 5.0F && !radarBlockEntity.hasRangeUpgrade) {
                        if (temp < 3.0F && temp > -1.0F) {
                            color = ColorTables.getMixedReflectivity(rdbz);
                        } else if (temp <= -1.0F) {
                            color = ColorTables.getSnowReflectivity(rdbz);
                        }
                    }

                    if (radarMode == RadarMode.VELOCITY) {
                        color = new Color(0, 0, 0);
                        vel /= 1.75F;
                        color = ColorTables.lerp(Mth.clamp(Math.max(rdbz, (Mth.abs(vel) - 18.0F) / 0.65F) / 12.0F, 0.0F, 1.0F), color, ColorTables.getVelocity(vel));
                    }

                    if (radarMode == RadarMode.IR) {
                        float ir = rdbz * 10.0F;
                        if (rdbz > 10.0F) {
                            ir = 100.0F + (rdbz - 10.0F) * 2.5F;
                        }

                        if (rdbz > 50.0F) {
                            ir += (rdbz - 50.0F) * 5.0F;
                        }

                        color = ColorTables.getIR(ir);
                    }

                }

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

                    if (clientRadarMode == ClientConfig.RadarMode.SST) {
                        Float t = ThermodynamicEngine.GetSST(GameBusClientEvents.weatherHandler, worldPos, blockEntity.getLevel(), radarBlockEntity, 0);
                        if (t == null) {
                            dbg = new Color(0, 0, 0);
                        } else {
                            dbg = ColorTables.getSST(t);
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

                    if (clientRadarMode == ClientConfig.RadarMode.GLOBALWINDS && GameBusClientEvents.weatherHandler != null) {
                        int height = GameBusClientEvents.weatherHandler.getWorld().getHeight(Heightmap.Types.MOTION_BLOCKING, (int)worldPos.x, (int)worldPos.z);
                        float wind = (float)WindEngine.getWind(new Vec3(worldPos.x, (double)height, worldPos.z), GameBusClientEvents.weatherHandler.getWorld(), false, false, false, true).length();
                        dbg = ColorTables.getHurricaneWindspeed(wind);
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

        int color = radarMode.getDotColor().getRGB();
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
