package net.nullved.pmweatherapi.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.config.PMWClientConfig;
import net.nullved.pmweatherapi.radar.RadarMode;

import java.util.function.Supplier;

/**
 * The overlay for {@link RadarMode} IDs.
 * <br>
 * To enable, you must enable {@code Show Radar Mode IDs} in PMWeatherAPI's Client Config
 * @since 0.14.16.2
 */
@OnlyIn(Dist.CLIENT)
public class IDOverlay implements IRadarOverlay {
    public static final IRadarOverlay INSTANCE = new IDOverlay();

    @Override
    public void render(boolean canRender, RenderData renderData, BufferBuilder bufferBuilder, Object... args) {
        if (!PMWClientConfig.showRadarModeId) return;
        if (!Minecraft.getInstance().player.isCrouching()) return;

        RadarMode mode = getRadarMode(renderData);
        PoseStack poseStack = renderData.poseStack();
        PMWClientConfig.RadarModeIDSide side = PMWClientConfig.radarModeIDSide;

        float scale = renderData.sizeRenderDiameter() / 3.0F;

        poseStack.pushPose();
        poseStack.translate((side.x * scale) - 0.5F * (scale - 1), 1.055f, (side.z * scale) - 0.5F * (scale - 1));
        poseStack.mulPose(Axis.YN.rotationDegrees(side.rotation));
        poseStack.scale(0.01f, 0.01f, 0.01f);

        renderText(Component.literal(mode.getId().toString()), renderData, poseStack);

        float lineHeight = 8.0f;
        float offset = lineHeight;
        for (Supplier<? extends IRadarOverlay> overlay: RadarOverlays.getOverlays()) {
            poseStack.pushPose();
            poseStack.translate(0, 0, offset);
            poseStack.scale(0.6f, 0.6f, 0.6f);

            renderText(Component.literal(overlay.get().getID().toString()).withColor(0x888888), renderData, poseStack);

            poseStack.popPose();
            offset += lineHeight * 0.6f;
        }

        poseStack.popPose();

        // While I could reset the pose, it is not strictly necessary
        // poseStack.scale(100f, 100f, 100f);
        // poseStack.mulPose(Axis.ZP.rotationDegrees(-side.rotation));
        // poseStack.translate(-side.x, -1.055f, -side.z);
    }

    @Override
    public String getModID() {
        return PMWeatherAPI.MODID;
    }

    @Override
    public String getIDPath() {
        return "id";
    }
}
