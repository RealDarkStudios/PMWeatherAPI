package net.nullved.pmweatherapi.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.protomanly.pmweather.render.RadarRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nullved.pmweatherapi.client.render.RadarOverlays;
import net.nullved.pmweatherapi.client.render.RenderData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(RadarRenderer.class)
public class RadarRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;mul(Lorg/joml/Matrix4fc;)Lorg/joml/Matrix4f;", ordinal = 1))
    private static void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource,
                               int combinedLightIn, int combinedOverlayIn, CallbackInfo ci, @Local BufferBuilder bufferBuilder, @Local(ordinal = 0) boolean canRender) {
        RenderData renderData = new RenderData(blockEntity, partialTicks, poseStack, multiBufferSource, combinedLightIn, combinedOverlayIn);
        RadarOverlays.renderOverlays(renderData, bufferBuilder, canRender);
    }
}
