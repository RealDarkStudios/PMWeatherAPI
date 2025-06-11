package net.nullved.pmweatherapi.radar;

import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class NearbyRadars {
    /**
     * Returns a {@link Set} of the {@link BlockPos} of {@link RadarBlock}s in a defined radius around the block
     * @param level The {@link Level} to search
     * @param pos The {@link BlockPos} of the block at the center of the search area
     * @param radius The radius of the search area
     * @return A {@link Set} of {@link BlockPos}
     * @since 0.14.15.0
     */
    public static Set<BlockPos> radarsNearBlock(Level level, BlockPos pos, double radius) {
        return radarsNearBlock(level.dimension(), pos, radius);
    }

    /**
     * Returns a {@link Set} of the {@link BlockPos} of {@link RadarBlock}s in a defined radius around the block
     * @param level The {@link ResourceKey} for the {@link Level} to search
     * @param pos The {@link BlockPos} of the block at the center of the search area
     * @param radius The radius of the search area
     * @return A {@link Set} of {@link BlockPos}
     * @since 0.14.15.1
     */
    public static Set<BlockPos> radarsNearBlock(ResourceKey<Level> level, BlockPos pos, double radius) {
        Set<BlockPos> radarList = new HashSet<>();

        for (BlockPos radar: Radars.get(level).getRadars()) {
            if (radar.distToCenterSqr(pos.getX(), pos.getY(), pos.getZ()) <= radius * radius)  radarList.add(radar);
        }

        radarList.remove(pos);

        return radarList;
    }

    /**
     * Returns a {@link Set} of the {@link BlockPos} of {@link RadarBlock}s in a defined radius around the center of the chunk
     * @param level The {@link Level} to search
     * @param pos The {@link ChunkPos} of the chunk
     * @param radius The radius of the search area
     * @return A {@link Set} of {@link BlockPos}
     * @since 0.14.15.0
     */
    public static Set<BlockPos> radarsNearChunk(Level level, ChunkPos pos, double radius) {
        return radarsNearChunk(level.dimension(), pos, radius);
    }

    /**
     * Returns a {@link Set} of the {@link BlockPos} of {@link RadarBlock}s in a defined radius around the center of the chunk
     * @param level The {@link ResourceKey} for the{@link Level} to search
     * @param pos The {@link ChunkPos} of the chunk
     * @param radius The radius of the search area
     * @return A {@link Set} of {@link BlockPos}
     * @since 0.14.15.1
     */
    public static Set<BlockPos> radarsNearChunk(ResourceKey<Level> level, ChunkPos pos, double radius) {
        Set<BlockPos> radarList = new HashSet<>();

        for (BlockPos radar: Radars.get(level).getRadars()) {
            if (radar.distToCenterSqr(pos.getMiddleBlockX(), radar.getY(), pos.getMiddleBlockZ()) <= radius * radius)  radarList.add(radar);
        }

        return radarList;
    }

    /**
     * Executes the given {@link Consumer} for each {@link BlockPos} of a {@link RadarBlock} in a defined radius around the block
     * @param level The {@link Level} to search
     * @param block The {@link BlockPos} of the block at the center of the search area
     * @param radius The radius of the search area
     * @param consumer The {@link Consumer} to execute for each {@link BlockPos}
     * @since 0.14.15.0
     */
    public static void forRadarNearBlock(Level level, BlockPos block, double radius, Consumer<BlockPos> consumer) {
        Set<BlockPos> radars = radarsNearBlock(level, block, radius);
        for (BlockPos radar: radars) consumer.accept(radar);
    }

    /**
     * Executes the given {@link Consumer} for each {@link BlockPos} of a {@link RadarBlock} in a defined radius around the block
     * @param level The {@link ResourceKey} of a {@link Level} to search
     * @param block The {@link BlockPos} of the block at the center of the search area
     * @param radius The radius of the search area
     * @param consumer The {@link Consumer} to execute for each {@link BlockPos}
     * @since 0.14.15.1
     */
    public static void forRadarNearBlock(ResourceKey<Level> level, BlockPos block, double radius, Consumer<BlockPos> consumer) {
        Set<BlockPos> radars = radarsNearBlock(level, block, radius);
        for (BlockPos radar: radars) consumer.accept(radar);
    }

    /**
     * Executes the given {@link Consumer} for each {@link BlockPos} of a {@link RadarBlock} in a defined radius around the center of the chunk
     * @param level The {@link Level} to search
     * @param chunk The {@link ChunkPos} of the chunk at the center of the search area.
     * @param radius The radius of the search area
     * @param consumer The {@link Consumer} to execute for each {@link BlockPos}
     * @since 0.14.15.0
     */
    public static void forRadarNearChunk(Level level, ChunkPos chunk, double radius, Consumer<BlockPos> consumer) {
        Set<BlockPos> radars = radarsNearChunk(level, chunk, radius);
        for (BlockPos radar: radars) consumer.accept(radar);
    }

    /**
     * Executes the given {@link Consumer} for each {@link BlockPos} of a {@link RadarBlock} in a defined radius around the center of the chunk
     * @param level The {@link ResourceKey} of a {@link Level} to search
     * @param chunk The {@link ChunkPos} of the chunk at the center of the search area.
     * @param radius The radius of the search area
     * @param consumer The {@link Consumer} to execute for each {@link BlockPos}
     * @since 0.14.15.0
     */
    public static void forRadarNearChunk(ResourceKey<Level> level, ChunkPos chunk, double radius, Consumer<BlockPos> consumer) {
        Set<BlockPos> radars = radarsNearChunk(level, chunk, radius);
        for (BlockPos radar: radars) consumer.accept(radar);
    }
}
