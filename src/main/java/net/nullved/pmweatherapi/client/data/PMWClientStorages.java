package net.nullved.pmweatherapi.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.radar.RadarMode;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A class holding the specific storage instances for the client
 * @since 0.14.15.3
 */
@OnlyIn(Dist.CLIENT)
public class PMWClientStorages {
    /**
     * A {@link Map} of {@link RadarMode}s to {@link Map}s of pixel ids and their {@link Color}
     * @since 0.14.15.6
     */
    public static Map<BlockPos, Map<RadarMode, Map<Integer, Color>>> RADAR_MODE_COLORS = new HashMap<>();

    private static Level lastLevel;
    private static RadarClientStorage radar;

    /**
     * Resets this client's internal radar storage
     * @since 0.14.15.3
     */
    public static void resetRadars() {
        radar = null;
    }

    /**
     * Gets the radar storage for the dimension this client is currently in
     * @return A {@link RadarClientStorage}
     * @since 0.14.15.3
     */
    public static RadarClientStorage getRadars() {
        try {
            Level level = Minecraft.getInstance().level;
            if (radar == null || level != lastLevel) {
                init(level);
            }
        } catch (Exception e) {
            PMWeatherAPI.LOGGER.error(e.getMessage(), e);
        }

        return radar;
    }

    /**
     * Initializes this client's storages to be the storages for the given level
     * @param level The {@link Level} to initialize storages for
     * @since 0.14.15.3
     */
    public static void init(Level level) {
        lastLevel = level;
        if (level != null) {
            radar = new RadarClientStorage(level.dimension());
        }
    }
}
