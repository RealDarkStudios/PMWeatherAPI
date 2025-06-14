package net.nullved.pmweatherapi.storm;

import dev.protomanly.pmweather.event.GameBusClientEvents;
import dev.protomanly.pmweather.event.GameBusEvents;
import dev.protomanly.pmweather.weather.Storm;
import dev.protomanly.pmweather.weather.WeatherHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * Get all the storms within a given radius around a {@link BlockPos} or {@link ChunkPos}
 * @since 0.14.15.0
 */
public class NearbyStorms {
    private static final HashMap<ResourceKey<Level>, NearbyStorms> DIMENSION_MAP = new HashMap<>();
    private final WeatherHandler handler;

    /**
     * Creates a {@link NearbyStorms} instance for the given {@link WeatherHandler}
     * @param handler The {@link WeatherHandler} to get radars from
     * @since 0.14.15.2
     */
    private NearbyStorms(WeatherHandler handler) {
        this.handler = handler;
    }

    /**
     * Get the {@link NearbyStorms} instance for the client
     * @return The client-specific {@link NearbyStorms} instance
     * @since 0.14.15.3
     */
    public static NearbyStorms client() {
        return new NearbyStorms(GameBusClientEvents.getClientWeather());
    }

    /**
     * Get {@link NearbyStorms} for the given dimension
     * @param dim The {@link ResourceKey} of the dimension
     * @return A {@link NearbyStorms} instance
     */
    public static NearbyStorms get(ResourceKey<Level> dim) {
        return DIMENSION_MAP.computeIfAbsent(dim, d -> new NearbyStorms(GameBusEvents.MANAGERS.get(d)));
    }

    /**
     * Get {@link NearbyStorms} for the given level
     * @param level The {@link Level} with the storms
     * @return A {@link NearbyStorms} instance
     */
    public static NearbyStorms get(Level level) {
        return get(level.dimension());
    }

    /**
     * Returns a {@link List} of {@link Storm}s in a defined radius around the block
     * @param block The {@link BlockPos} of the block at the center of the search area
     * @param radius The radius of the search area
     * @return A {@link List} of {@link Storm}s
     * @since 0.14.15.0
     */
    public List<Storm> stormsNearBlock(BlockPos block, double radius) {
        List<Storm> allStorms = handler.getStorms();
        List<Storm> nearStorms = new ArrayList<>();

        for (Storm storm: allStorms) {
            Vec3 pos = storm.position;
            if (pos.distanceTo(block.getCenter()) <= radius) nearStorms.add(storm);
        }

        return nearStorms;
    }

    /**
     * Returns a {@link List} of {@link Storm}s in a defined radius around the center of the chunk
     * @param chunk The {@link ChunkPos} of the chunk at the center of the search area.
     * @param radius The radius of the search area
     * @return A {@link List} of {@link Storm}s
     * @since 0.14.15.0
     */
    public List<Storm> stormsNearChunk(ChunkPos chunk, double radius) {
        List<Storm> allStorms = handler.getStorms();
        List<Storm> nearStorms = new ArrayList<>();

        for (Storm storm: allStorms) {
            Vec3 pos = storm.position;
            if (pos.distanceTo(chunk.getWorldPosition().getCenter()) <= radius) nearStorms.add(storm);
        }

        return nearStorms;
    }

    /**
     * Returns a {@link List} of {@link Storm}s in a defined radius around the given {@link Player}
     * @param player The {@link Player} to search around
     * @param radius The radius of the search area
     * @return A {@link List} of {@link Storm}s
     * @since 0.14.15.4
     */
    public List<Storm> stormsNearPlayer(Player player, double radius) {
        List<Storm> allStorms = handler.getStorms();
        List<Storm> nearStorms = new ArrayList<>();

        for (Storm storm: allStorms) {
            Vec3 pos = storm.position;
            if (pos.distanceTo(player.position()) <= radius) nearStorms.add(storm);
        }

        return nearStorms;
    }

    /**
     * Executes the given {@link Consumer} for each {@link Storm} in a defined radius around the block
     * @param block The {@link BlockPos} of the block at the center of the search area
     * @param radius The radius of the search area
     * @param consumer The {@link Consumer} to execute for each {@link Storm}
     * @since 0.14.15.0
     */
    public void forStormNearBlock(BlockPos block, double radius, Consumer<Storm> consumer) {
        List<Storm> storms = stormsNearBlock(block, radius);
        for (Storm storm: storms) consumer.accept(storm);
    }

    /**
     * Executes the given {@link Consumer} for each {@link Storm} in a defined radius around the center of the chunk
     * @param chunk The {@link ChunkPos} of the chunk at the center of the search area.
     * @param radius The radius of the search area
     * @param consumer The {@link Consumer} to execute for each {@link Storm}
     * @since 0.14.15.0
     */
    public void forStormNearChunk(ChunkPos chunk, double radius, Consumer<Storm> consumer) {
        List<Storm> storms = stormsNearChunk(chunk, radius);
        for (Storm storm: storms) consumer.accept(storm);
    }

    /**
     * Executes the given {@link Consumer} for each {@link Storm} in a defined radius around the given {@link Player}
     * @param player The {@link Player} to search around
     * @param radius The radius of the search area
     * @param consumer The {@link Consumer} to execute for each {@link Storm}
     * @since 0.14.15.4
     */
    public void forStormNearPlayer(Player player, double radius, Consumer<Storm> consumer) {
        List<Storm> storms = stormsNearPlayer(player, radius);
        for (Storm storm: storms) consumer.accept(storm);
    }
}
