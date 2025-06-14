package net.nullved.pmweatherapi.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
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
     * @param canRender {@code true} if either the server doesn't require WSR-88D or a WSR-88D is complete within 4 chunks of the radar
     * @param renderData The data used to call {@link BlockEntityRenderer#render(BlockEntity, float, PoseStack, MultiBufferSource, int, int)}
     * @param bufferBuilder The {@link BufferBuilder} to render overlays to
     * @since 0.14.15.0
     */
    public static void renderOverlays(RenderData renderData, BufferBuilder bufferBuilder, boolean canRender) {
        OVERLAYS.forEach(overlay -> overlay.get().render(canRender, renderData, bufferBuilder));
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
