package net.nullved.pmweatherapi.example;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.protomanly.pmweather.block.RadarBlock;
import dev.protomanly.pmweather.config.ClientConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.render.IRadarOverlay;
import net.nullved.pmweatherapi.client.render.RenderData;
import net.nullved.pmweatherapi.radar.NearbyRadars;
import net.nullved.pmweatherapi.storm.NearbyStorms;
import org.joml.Vector3f;

/**
 * This is an example overlay that draws a dot at every nearby radar's position
 */
public class ExampleOverlay implements IRadarOverlay {
    public static final ExampleOverlay INSTANCE = new ExampleOverlay();

    @Override
    public void render(RenderData renderData, BufferBuilder bufferBuilder) {
        BlockEntity blockEntity = renderData.blockEntity();
        BlockPos pos = blockEntity.getBlockPos();
        RadarBlock.Mode mode = blockEntity.getBlockState().getValue(RadarBlock.RADAR_MODE);
        if (mode == RadarBlock.Mode.REFLECTIVITY) {
            NearbyRadars.client().forRadarNearBlock(pos, 2048,
                p -> renderMarker(bufferBuilder, p.offset(-pos.getX(), -pos.getY(), -pos.getZ()).getCenter()));
        } else if (mode == RadarBlock.Mode.VELOCITY) {
            NearbyStorms.client().forStormNearBlock(pos, 2048,
                s -> renderMarker(bufferBuilder, s.position.add(-pos.getX(), -pos.getY(), -pos.getZ())));
        }
    }

    @Override
    public String getModID() {
        return PMWeatherAPI.MODID + "_test";
    }

    private static void renderMarker(BufferBuilder bufferBuilder, Vec3 relative) {
        float resolution = ClientConfig.radarResolution;
        Vector3f radarPos = relative.add(0.5, 0.5, 0.5).toVector3f().mul(3 / (2 * resolution)).div(2048, 0, 2048).div(1.0F / resolution, 0.0F, 1.0F / resolution);
        Vector3f topLeft = (new Vector3f(-1.0F, 0.0F, -1.0F)).mul(0.015F).add(radarPos.x, 0.005F, radarPos.z);
        Vector3f bottomLeft = (new Vector3f(-1.0F, 0.0F, 1.0F)).mul(0.015F).add(radarPos.x, 0.005F, radarPos.z);
        Vector3f bottomRight = (new Vector3f(1.0F, 0.0F, 1.0F)).mul(0.015F).add(radarPos.x, 0.005F, radarPos.z);
        Vector3f topRight = (new Vector3f(1.0F, 0.0F, -1.0F)).mul(0.015F).add(radarPos.x, 0.005F, radarPos.z);
        int color = 0xFF777777;
        bufferBuilder.addVertex(topLeft).setColor(color).addVertex(bottomLeft).setColor(color).addVertex(bottomRight).setColor(color).addVertex(topRight).setColor(color);
    }
}
