package net.nullved.pmweatherapi.config;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.radar.RadarMode;

@EventBusSubscriber(modid = PMWeatherAPI.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PMWClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue DISABLE_CUSTOM_RADAR_MODE_RENDERING;
    public static boolean disableCustomRadarModeRendering;
    private static final ModConfigSpec.BooleanValue DISABLE_OVERLAYS_WHEN_DEBUGGING;
    public static boolean disableOverlaysWhenDebugging;
    private static final ModConfigSpec.BooleanValue SHOW_RADAR_MODE_ID;
    public static boolean showRadarModeId;
    private static final ModConfigSpec.EnumValue<RadarModeIDSide> RADAR_MODE_ID_SIDE;
    public static RadarModeIDSide radarModeIDSide;
    public static final ModConfigSpec SPEC;

    @SubscribeEvent
    private static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC && !(event instanceof ModConfigEvent.Unloading)) {
            PMWeatherAPI.LOGGER.info("Loading Client PMWeatherAPI Configs");
            disableCustomRadarModeRendering = DISABLE_CUSTOM_RADAR_MODE_RENDERING.getAsBoolean();
            disableOverlaysWhenDebugging = DISABLE_OVERLAYS_WHEN_DEBUGGING.getAsBoolean();
            showRadarModeId = SHOW_RADAR_MODE_ID.getAsBoolean();
            radarModeIDSide = RADAR_MODE_ID_SIDE.get();
        }
    }

    static {
        DISABLE_CUSTOM_RADAR_MODE_RENDERING = BUILDER.comment("Disables custom radar mode rendering").define("disable_custom_radar_mode_rendering", false);
        DISABLE_OVERLAYS_WHEN_DEBUGGING = BUILDER.comment("Disables all overlays when client radar debugging is on").define("disable_overlays_when_debugging", true);
        SHOW_RADAR_MODE_ID = BUILDER.comment("Shows the radar mode ID").define("show_radar_mode_id", false);
        RADAR_MODE_ID_SIDE = BUILDER.comment("The side to render the radar mode ID on").defineEnum("radar_mode_id_side", RadarModeIDSide.NORTH);
        SPEC = BUILDER.build();
    }

    public enum RadarModeIDSide {
        NORTH(0, -1, -1),
        EAST(90, 2, -1),
        SOUTH(180, 2, 2),
        WEST(-90, -1, 2);

        public final int rotation, x, z;

        RadarModeIDSide(int rotation, int x, int z) {
            this.rotation = rotation;
            this.x = x;
            this.z = z;
        }
    }
}
