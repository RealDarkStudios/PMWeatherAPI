package net.nullved.pmweatherapi.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * An interface defining a radar overlay
 * To implement this, you must override {@link #render(RenderData, BufferBuilder)} and {@link #getModID()}
 * @since 0.14.15.2
 */
public interface IRadarOverlay {
    /**
     * Renders objects on top of the radar
     * @param renderData The data used to call {@link BlockEntityRenderer#render(BlockEntity, float, PoseStack, MultiBufferSource, int, int)}
     * @param bufferBuilder The {@link BufferBuilder} that gets drawn to the radar
     * @since 0.14.15.2
     */
    void render(RenderData renderData, BufferBuilder bufferBuilder);

    /**
     * @return The Mod ID of the mod that registered this overlay
     * @since 0.14.15.2
     */
    String getModID();

    /**
     * If not overriden, this method returns the class name of the implementor converted to snake case
     * @return The path to use for this overlay's ID
     * @since 0.14.15.2
     */
    default String getIDPath() {
        return this.getClass().getSimpleName().replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    /**
     * The ID of this overlay, defined by {@link #getModID()} and {@link #getIDPath()}
     * @return The {@link ResourceLocation} of this overlay
     * @since 0.14.15.2
     */
    default ResourceLocation getID() {
        // by default, returns
        return ResourceLocation.fromNamespaceAndPath(getModID(), getIDPath());
    }
}
