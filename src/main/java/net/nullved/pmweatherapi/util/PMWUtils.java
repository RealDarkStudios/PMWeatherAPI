package net.nullved.pmweatherapi.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.radar.NearbyRadars;
import net.nullved.pmweatherapi.storage.IStorage;
import net.nullved.pmweatherapi.storage.data.IStorageData;
import net.nullved.pmweatherapi.storage.data.StorageData;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class PMWUtils {
    /**
     * Checks if two {@link BlockPos} are corner-adjacent
     * @param a A {@link BlockPos}
     * @param b Another {@link BlockPos}
     * @return {@code true} if they are corner-adjacent
     */
    public static boolean isCornerAdjacent(BlockPos a, BlockPos b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());

        return (dx <= 1 && dy <= 1 && dz <= 1) && (dx + dy + dz > 0);
    }

    /**
     * Performs a {@link Function} for each {@link BlockPos} around a {@link BlockPos}
     * @param pos The base {@link BlockPos}
     * @param test The test
     * @return A {@link Set} of {@link BlockPos} that passed the test
     */
    public static Set<BlockPos> testAround(BlockPos pos, Function<BlockPos, Boolean> test) {
        HashSet<BlockPos> set = new HashSet<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    if (test.apply(pos.offset(x, y, z))) set.add(pos.offset(x, y, z));
                }
            }
        }

        return set;
    }

    /**
     * Gets all {@link BlockPos} in a {@link IStorage} that are corner-adjacent to the base {@link BlockPos}
     * @param storage The {@link IStorage} to check in
     * @param pos The base {@link BlockPos}
     * @return A {@link Set} of {@link BlockPos} that are in the {@link IStorage} around the base {@link BlockPos}
     * @since 0.15.3.3
     */
    public static <D extends IStorageData> Set<D> storageCornerAdjacent(IStorage<D> storage, BlockPos pos) {
        HashSet<D> set = new HashSet<>();

        for (D data: storage.getInAdjacentChunks(new ChunkPos(pos))) {
            if (isCornerAdjacent(data.getPos(), pos)) set.add(data);
        }

        return set;
    }

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

    /**
     * Determines if a radar is within 1 block, including diagonally, to the given {@link BlockPos}
     * @param dim The dimension to search in
     * @param pos The {@link BlockPos} to look by
     * @return {@code true} if there is a radar adjacent to this block, {@code false} otherwise
     * @since 0.15.3.3
     */
    public static boolean isRadarCornerAdjacent(ResourceKey<Level> dim, BlockPos pos) {
        Set<BlockPos> nearby = NearbyRadars.get(dim).radarsNearBlock(pos, 3);

        boolean adj = false;
        for (BlockPos nearbyPos : nearby) {
            adj |= isCornerAdjacent(nearbyPos, pos);
        }

        return adj;
    }

    /**
     * Determines if a radar is within 1 block, including diagonally, to the given {@link BlockPos}.
     * @param level The {@link Level} to search in
     * @param pos The {@link BlockPos} to look by
     * @return {@code true} if there is a radar adjacent to this block, {@code false} otherwise
     * @since 0.15.3.3
     */
    public static boolean isRadarCornerAdjacent(Level level, BlockPos pos) {
        return isRadarCornerAdjacent(level.dimension(), pos);
    }
}
