package net.nullved.pmweatherapi.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.nullved.pmweatherapi.PMWeatherAPI;

import java.util.HashMap;
import java.util.Map;

public class RadarOverlays {
    private static final HashMap<ResourceLocation, RenderFunction> OVERLAYS = new HashMap<>();

    public static Map<ResourceLocation, RenderFunction> getOverlays() {
        return OVERLAYS;
    }

    public static void renderOverlays(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLightIn, int combinedOverlayIn, BufferBuilder bufferBuilder) {
        OVERLAYS.forEach((rl, rf) -> rf.apply(blockEntity, partialTicks, poseStack,  multiBufferSource, combinedLightIn, combinedOverlayIn, bufferBuilder));
    }

    public static void registerOverlay(ResourceLocation resourceLocation, RenderFunction renderFunction) {
        PMWeatherAPI.LOGGER.info("Registering overlay {}", resourceLocation);
        OVERLAYS.put(resourceLocation, renderFunction);
    }
}
