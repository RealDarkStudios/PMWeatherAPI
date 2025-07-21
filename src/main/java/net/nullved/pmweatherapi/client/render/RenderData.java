package net.nullved.pmweatherapi.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A wrapper class to be passed to {@link RenderData}
 * @param blockEntity The {@link BlockEntity} associated with the render call
 * @param sizeRenderDiameter The size in blocks of the radar
 * @param partialTicks The time, in partial ticks, since last full tick
 * @param poseStack The {@link PoseStack}
 * @param multiBufferSource The {@link MultiBufferSource}
 * @param combinedLightIn The current light value on the block entity
 * @param combinedOverlayIn The current overlay of the block entity
 * @since 0.14.15.2
 */
public record RenderData(BlockEntity blockEntity, float sizeRenderDiameter, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLightIn, int combinedOverlayIn) {
}
