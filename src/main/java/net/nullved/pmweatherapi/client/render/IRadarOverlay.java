package net.nullved.pmweatherapi.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.nullved.pmweatherapi.data.PMWExtras;
import net.nullved.pmweatherapi.radar.RadarMode;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * An interface defining a radar overlay
 * To implement this, you must override {@link #render(boolean, RenderData, BufferBuilder, Object...)} and {@link #getModID()}
 * @since 0.14.15.2
 */
public interface IRadarOverlay {
    /**
     * Renders objects on top of the radar
     * @param canRender {@code true} if either the server doesn't require WSR-88D or a WSR-88D is complete within 4 chunks of the radar
     * @param renderData The data used to call {@link BlockEntityRenderer#render(BlockEntity, float, PoseStack, MultiBufferSource, int, int)}
     * @param bufferBuilder The {@link BufferBuilder} that gets drawn to the radar
     * @param args The arguments to pass to the Radar Overlay
     * @since 0.14.15.2
     */
    void render(boolean canRender, RenderData renderData, BufferBuilder bufferBuilder, Object... args);

    /**
     * Get the {@link RadarMode} of the radar
     * @param renderData The {@link RenderData}
     * @return The radar's {@link RadarMode}
     * @since 0.14.16.2
     */
    default RadarMode getRadarMode(RenderData renderData) {
        return renderData.blockEntity().getBlockState().getValue(PMWExtras.RADAR_MODE);
    }

    /**
     * Render a texture at the given {@link ResourceLocation}
     * @param texture The {@link ResourceLocation} of the texture
     * @param renderData The {@link RenderData}
     * @param poseStack The {@link PoseStack} to render with
     * @param color The color
     * @since 0.15.3.3
     */
    default void renderTexture(ResourceLocation texture, RenderData renderData, PoseStack poseStack, int color) {
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = renderData.multiBufferSource().getBuffer(RenderType.entityCutout(texture));

        consumer.addVertex(pose, -0.5f, -0.5f, 0.0f)
                .setColor(color)
                .setUv(0.0F, 0.0F)
                .setOverlay(renderData.combinedOverlayIn())
                .setLight(0xF000F0)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);

        consumer.addVertex(pose, 0.5f, -0.5f, 0.0f)
                .setColor(color)
                .setUv(1.0F, 0.0F)
                .setOverlay(renderData.combinedOverlayIn())
                .setLight(0xF000F0)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);

        consumer.addVertex(pose, 0.5f, 0.5f, 0.0f)
                .setColor(color)
                .setUv(1.0F, 1.0F)
                .setOverlay(renderData.combinedOverlayIn())
                .setLight(0xF000F0)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);

        consumer.addVertex(pose, -0.5f, 0.5f, 0.0f)
                .setColor(color)
                .setUv(0.0F, 1.0F)
                .setOverlay(renderData.combinedOverlayIn())
                .setLight(0xF000F0)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);
    }

    /**
     * Render a texture at the given {@link ResourceLocation}
     * @param texture The {@link ResourceLocation} of the texture
     * @param renderData The {@link RenderData}
     * @param poseStack The {@link PoseStack} to render with
     * @since 0.15.3.3
     */
    default void renderTexture(ResourceLocation texture, RenderData renderData, PoseStack poseStack) {
        renderTexture(texture, renderData, poseStack, 0xFFFFFFFF);
    }

    /**
     * Render a texture at the given {@link ResourceLocation}
     * @param texture The {@link ResourceLocation} of the texture
     * @param renderData The {@link RenderData}
     * @param color The color
     * @since 0.15.3.3
     */
    default void renderTexture(ResourceLocation texture, RenderData renderData, int color) {
        renderTexture(texture, renderData, renderData.poseStack(), color);
    }

    /**
     * Render a texture at the given {@link ResourceLocation}
     * @param texture The {@link ResourceLocation} of the texture
     * @param renderData The {@link RenderData}
     * @since 0.15.3.3
     */
    default void renderTexture(ResourceLocation texture, RenderData renderData) {
        renderTexture(texture, renderData, renderData.poseStack(), 0xFFFFFFFF);
    }

    /**
     * Render the text given in the given {@link Component}
     * @param component The {@link Component} to render
     * @param renderData The {@link RenderData}
     * @param poseStack The {@link PoseStack} to render with
     * @since 0.14.16.2
     */
    default void renderText(Component component, RenderData renderData, PoseStack poseStack) {
        Font font = Minecraft.getInstance().font;
        font.drawInBatch(component, 0, 0, 0xFFFFFF, false, poseStack.last().pose(), renderData.multiBufferSource(), Font.DisplayMode.NORMAL, 0xFFFFFF, 0xFFFFFF);
    }

    /**
     * Render the text given in the given {@link Component}
     * @param component The {@link Component} to render
     * @param renderData The {@link RenderData}
     * @since 0.14.16.2
     */
    default void renderText(Component component, RenderData renderData) {
        this.renderText(component, renderData, renderData.poseStack());
    }

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
