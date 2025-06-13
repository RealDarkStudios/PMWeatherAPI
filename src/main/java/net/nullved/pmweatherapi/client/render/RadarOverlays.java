package net.nullved.pmweatherapi.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.nullved.pmweatherapi.PMWeatherAPI;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A class to manage radar overlays.
 * <br>
 * To register an overlay, use {@link #registerOverlay(Supplier)}
 * @since 0.14.15.0
 */
public class RadarOverlays {
    private static final HashSet<Supplier<? extends IRadarOverlay>> OVERLAYS = new HashSet<>();

    /**
     * @return The {@link Set} of all overlay instances to render to
     */
    public static Set<Supplier<? extends IRadarOverlay>> getOverlays() {
        return OVERLAYS;
    }

    /**
     * Renders all overlays
     * @param blockEntity The {@link BlockEntity} associated with the render call
     * @param partialTicks The time, in partial ticks, since last full tick
     * @param poseStack The {@link PoseStack}
     * @param multiBufferSource The {@link MultiBufferSource}
     * @param combinedLightIn The current light value on the block entity
     * @param combinedOverlayIn The current overlay of the block entity
     * @param bufferBuilder The {@link BufferBuilder} to render overlays to
     * @since 0.14.15.0
     */
    public static void renderOverlays(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLightIn, int combinedOverlayIn, BufferBuilder bufferBuilder) {
        RenderData renderData = new RenderData(blockEntity, partialTicks, poseStack, multiBufferSource, combinedLightIn, combinedOverlayIn);

        OVERLAYS.forEach(overlay -> overlay.get().render(renderData, bufferBuilder));
    }

    /**
     * Registers an overlay to be rendered.
     * @param overlay A {@link Supplier} returning an instance of an {@link IRadarOverlay}
     * @since 0.14.15.2 (in it's current form)
     */
    public static void registerOverlay(Supplier<? extends IRadarOverlay> overlay) {
        PMWeatherAPI.LOGGER.info("Registering overlay {}", overlay.get().getID());
        OVERLAYS.add(overlay);
    }
}
