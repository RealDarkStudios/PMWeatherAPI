package net.nullved.pmweatherapi.data;

import net.minecraft.core.BlockPos;
import net.nullved.pmweatherapi.radar.RadarMode;
import net.nullved.pmweatherapi.util.StringProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * A class storing extra things used by varying parts of the API
 * @since 0.14.15.6
 */
public class PMWExtras {
    public static StringProperty RADAR_MODE = new StringProperty("radarmode", RadarMode.values());
    public static final Map<BlockPos, BlockPos> RADAR_WSR_88D_LOOKUP = new HashMap<>();
}
