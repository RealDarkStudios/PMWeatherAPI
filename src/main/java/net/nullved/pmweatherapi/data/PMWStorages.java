package net.nullved.pmweatherapi.data;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.radar.RadarServerStorage;
import net.nullved.pmweatherapi.radar.RadarStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * A class holding maps of dimensions to different storages
 * @since 0.14.15.3
 */
public class PMWStorages {
    public static final Map<ResourceKey<Level>, RadarServerStorage> RADARS = new HashMap<>();

    /**
     * Gets a {@link RadarServerStorage} for the given dimension
     * @param dim The dimension to get the radar storage for
     * @return The associated dimension's {@link RadarServerStorage}
     * @since 0.14.15.3
     */
    public static RadarServerStorage getRadar(ResourceKey<Level> dim) {
        return RADARS.get(dim);
    }

    /**
     * Gets a {@link RadarServerStorage} for the given dimension
     * @param dim The dimension to get the radar storage for
     * @return The associated dimension's {@link RadarServerStorage}
     * @since 0.14.15.3
     */
    public static RadarServerStorage getRadar(Level dim) {
        return getRadar(dim.dimension());
    }
}
