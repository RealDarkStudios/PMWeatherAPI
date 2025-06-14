package net.nullved.pmweatherapi.radar;

import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.data.PMWStorages;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Get all the radars within a given radius around a {@link BlockPos} or {@link ChunkPos}
 * @since 0.14.15.0
 */
public class NearbyRadars {
    private static final HashMap<ResourceKey<Level>, NearbyRadars> DIMENSION_MAP = new HashMap<>();
    private final RadarStorage storage;

    /**
     * Creates a {@link NearbyRadars} instance for the given {@link RadarStorage}
     * @param storage The {@link RadarStorage} to get radars from
     * @since 0.14.15.3
     */
    private NearbyRadars(RadarStorage storage) {
        this.storage = storage;
    }

    /**
     * Get the {@link NearbyRadars} instance for the client
     * @return The client-specific {@link NearbyRadars} instance
     * @since 0.14.15.3
     */
    @OnlyIn(Dist.CLIENT)
    public static NearbyRadars client() {
        return new NearbyRadars(PMWClientStorages.getRadars());
    }

    /**
     * Get {@link NearbyRadars} for the given dimension
     * @param dim The {@link ResourceKey} of the dimension
     * @return A {@link NearbyRadars} instance
     * @since 0.14.15.3
     */
    public static NearbyRadars get(ResourceKey<Level> dim) {
        return DIMENSION_MAP.computeIfAbsent(dim, d -> new NearbyRadars(PMWStorages.getRadar(d)));
    }

    /**
     * Get {@link NearbyRadars} for the given level
     * @param level The {@link Level} with the storms
     * @return A {@link NearbyRadars} instance
     * @since 0.14.15.3
     */
    public static NearbyRadars get(Level level) {
        return get(level.dimension());
    }

    /**
     * Returns a {@link Set} of the {@link BlockPos} of {@link RadarBlock}s in a defined radius around the block
     * @param pos The {@link BlockPos} of the block at the center of the search area
     * @param radius The radius of the search area
     * @return A {@link Set} of {@link BlockPos} around, but not including, the given {@link BlockPos}
     * @since 0.14.15.1
     */
    public Set<BlockPos> radarsNearBlock(BlockPos pos, double radius) {
        Set<BlockPos> radarList = new HashSet<>();

        for (BlockPos radar: storage.getAllRadars()) {
            if (radar.distToCenterSqr(pos.getX(), pos.getY(), pos.getZ()) <= radius * radius)  radarList.add(radar);
        }

        radarList.remove(pos);

        return radarList;
    }

    /**
     * Returns a {@link Set} of the {@link BlockPos} of {@link RadarBlock}s in a defined radius around the center of the chunk
     * @param pos The {@link ChunkPos} of the chunk
     * @param radius The radius of the search area
     * @return A {@link Set} of {@link BlockPos}
     * @since 0.14.15.1
     */
    public Set<BlockPos> radarsNearChunk(ChunkPos pos, double radius) {
        Set<BlockPos> radarList = new HashSet<>();

        for (BlockPos radar: storage.getAllRadars()) {
            if (radar.distToCenterSqr(pos.getMiddleBlockX(), radar.getY(), pos.getMiddleBlockZ()) <= radius * radius)  radarList.add(radar);
        }

        return radarList;
    }

    /**
     * Returns a {@link Set} of the {@link BlockPos} of {@link RadarBlock}s in a defined radius around the {@link Player}
     * @param player The {@link Player} to search around
     * @param radius The radius of the search area
     * @return A {@link Set} of {@link BlockPos} around, but not including, the given {@link BlockPos}
     * @since 0.14.15.4
     */
    public Set<BlockPos> radarsNearPlayer(Player player, double radius) {
        Set<BlockPos> radarList = new HashSet<>();

        for (BlockPos radar: storage.getAllRadars()) {
            if (radar.distToCenterSqr(player.getX(), player.getY(), player.getZ()) <= radius * radius)  radarList.add(radar);
        }

        return radarList;
    }


    /**
     * Executes the given {@link Consumer} for each {@link BlockPos} of a {@link RadarBlock} in a defined radius around the block
     * @param block The {@link BlockPos} of the block at the center of the search area
     * @param radius The radius of the search area
     * @param consumer The {@link Consumer} to execute for each {@link BlockPos}
     * @since 0.14.15.1
     */
    public void forRadarNearBlock(BlockPos block, double radius, Consumer<BlockPos> consumer) {
        Set<BlockPos> radars = radarsNearBlock(block, radius);
        for (BlockPos radar: radars) consumer.accept(radar);
    }

    /**
     * Executes the given {@link Consumer} for each {@link BlockPos} of a {@link RadarBlock} in a defined radius around the center of the chunk
     * @param chunk The {@link ChunkPos} of the chunk at the center of the search area.
     * @param radius The radius of the search area
     * @param consumer The {@link Consumer} to execute for each {@link BlockPos}
     * @since 0.14.15.0
     */
    public void forRadarNearChunk(ChunkPos chunk, double radius, Consumer<BlockPos> consumer) {
        Set<BlockPos> radars = radarsNearChunk(chunk, radius);
        for (BlockPos radar: radars) consumer.accept(radar);
    }

    /**
     * Executes the given {@link Consumer} for each {@link BlockPos} of a {@link RadarBlock} in a defined radius around the {@link Player}
     * @param player The {@link Player} to search around
     * @param radius The radius of the search area
     * @param consumer The {@link Consumer} to execute for each {@link BlockPos}
     * @since 0.14.15.4
     */
    public void forRadarNearPlayer(Player player, double radius, Consumer<BlockPos> consumer) {
        Set<BlockPos> radars = radarsNearPlayer(player, radius);
        for (BlockPos radar: radars) consumer.accept(radar);
    }
}
