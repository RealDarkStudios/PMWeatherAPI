package net.nullved.pmweatherapi.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.radar.NearbyRadars;

public class PMWUtils {
    /**
     * Determines if a radar is next to the given {@link BlockPos}
     * @param dim The dimension to search in
     * @param pos The {@link BlockPos} to look by
     * @return {@code true} if there is a radar adjacent to this block, {@code false} otherwise
     * @since 0.14.16.2
     */
    public static boolean isRadarAdjacent(ResourceKey<Level> dim, BlockPos pos) {
        return !NearbyRadars.get(dim).radarsNearBlock(pos, 1).isEmpty();
    }

    /**
     * Determines if a radar is next to the given {@link BlockPos}
     * @param level The {@link Level} to search in
     * @param pos The {@link BlockPos} to look by
     * @return {@code true} if there is a radar adjacent to this block, {@code false} otherwise
     * @since 0.14.16.2
     */
    public static boolean isRadarAdjacent(Level level, BlockPos pos) {
        return isRadarAdjacent(level.dimension(), pos);
    }
}
