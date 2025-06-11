package net.nullved.pmweatherapi.example;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.protomanly.pmweather.config.ClientConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.nullved.pmweatherapi.storm.Storms;
import org.joml.Vector3f;

/**
 * This is an example overlay that draws a dot at every nearby radar's position
 */
public class ExampleOverlay {
    public static void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource,
                              int combinedLightIn, int combinedOverlayIn, BufferBuilder bufferBuilder) {
        BlockPos pos = blockEntity.getBlockPos();
        Storms.get(blockEntity.getLevel()).forStormNearBlock(pos, 2048,
            s -> renderStormMarker(bufferBuilder, s.position.add(-pos.getX(), -pos.getY(), -pos.getZ())));
    }

    private static void renderStormMarker(BufferBuilder bufferBuilder, Vec3 relative) {
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
