package net.nullved.pmweatherapi.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.config.PMWClientConfig;
import net.nullved.pmweatherapi.radar.RadarMode;

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
        RadarMode mode = getRadarMode(renderData);
        PoseStack poseStack = renderData.poseStack();
        PMWClientConfig.RadarModeIDSide side = PMWClientConfig.radarModeIDSide;

        poseStack.translate(side.x, 1.055f, side.z);
        poseStack.mulPose(Axis.ZP.rotationDegrees(side.rotation));
        poseStack.scale(0.01f, 0.01f, 0.01f);

        renderText(Component.literal(mode.getId().toString()), renderData, poseStack);

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
