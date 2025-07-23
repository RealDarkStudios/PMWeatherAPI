package net.nullved.pmweatherapi.example;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.protomanly.pmweather.block.RadarBlock;
import dev.protomanly.pmweather.config.ClientConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.render.IRadarOverlay;
import net.nullved.pmweatherapi.client.render.RenderData;
import net.nullved.pmweatherapi.radar.NearbyRadars;
import net.nullved.pmweatherapi.radar.RadarMode;
import net.nullved.pmweatherapi.storm.NearbyStorms;
import org.joml.Vector3f;

/**
 * This is an example overlay that draws a dot at every lightning strike and fades out
 * @see IRadarOverlay
 */
@OnlyIn(Dist.CLIENT)
public class ExampleOverlay implements IRadarOverlay {
    public static final ExampleOverlay INSTANCE = new ExampleOverlay();

    @Override
    public void render(boolean canRender, RenderData renderData, BufferBuilder bufferBuilder, Object... args) {
        if (!canRender) return;
        BlockEntity blockEntity = renderData.blockEntity();
        BlockPos pos = blockEntity.getBlockPos();
        RadarMode mode = getRadarMode(renderData);
        if (mode == RadarMode.REFLECTIVITY) {
            NearbyRadars.client().forRadarNearBlock(pos, 2048,
                p -> renderMarker(bufferBuilder, p.offset(-pos.getX(), -pos.getY(), -pos.getZ()).getCenter()));
        } else if (mode == RadarMode.VELOCITY) {
            NearbyStorms.client().forStormNearBlock(pos, 2048,
                s -> renderMarker(bufferBuilder, s.position.add(-pos.getX(), -pos.getY(), -pos.getZ())));
        }
    }

    @Override
    public String getModID() {
        return "example";
    }

    private static void renderMarker(BufferBuilder bufferBuilder, Vec3 relative) {
        float resolution = ClientConfig.radarResolution;
        Vector3f radarPos = relative.add(0.5, 0.5, 0.5).toVector3f().mul(3 / (2 * resolution)).div(2048, 0, 2048).div(1.0F / resolution, 0.0F, 1.0F / resolution);
        Vector3f topLeft = (new Vector3f(-1.0F, 0.0F, -1.0F)).mul(0.015F).add(radarPos.x, 0.005F, radarPos.z);
        Vector3f bottomLeft = (new Vector3f(-1.0F, 0.0F, 1.0F)).mul(0.015F).add(radarPos.x, 0.005F, radarPos.z);
        Vector3f bottomRight = (new Vector3f(1.0F, 0.0F, 1.0F)).mul(0.015F).add(radarPos.x, 0.005F, radarPos.z);
        Vector3f topRight = (new Vector3f(1.0F, 0.0F, -1.0F)).mul(0.015F).add(radarPos.x, 0.005F, radarPos.z);
        int color = 0xFFAAAAAA;
        bufferBuilder.addVertex(topLeft).setColor(color).addVertex(bottomLeft).setColor(color).addVertex(bottomRight).setColor(color).addVertex(topRight).setColor(color);
    }
}
