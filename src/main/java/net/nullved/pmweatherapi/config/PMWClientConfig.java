package net.nullved.pmweatherapi.config;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.nullved.pmweatherapi.PMWeatherAPI;

@EventBusSubscriber(modid = PMWeatherAPI.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PMWClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue DISABLE_CUSTOM_RADAR_MODE_RENDERING;
    public static boolean disableCustomRadarModeRendering;
    public static final ModConfigSpec SPEC;

    @SubscribeEvent
    private static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC && !(event instanceof ModConfigEvent.Unloading)) {
            PMWeatherAPI.LOGGER.info("Loading Client PMWeatherAPI Configs");
            disableCustomRadarModeRendering = DISABLE_CUSTOM_RADAR_MODE_RENDERING.getAsBoolean();
        }
    }

    static {
        DISABLE_CUSTOM_RADAR_MODE_RENDERING = BUILDER.comment("Disables custom radar mode rendering").define("disable_custom_radar_mode_rendering", false);
        SPEC = BUILDER.build();
    }
}
