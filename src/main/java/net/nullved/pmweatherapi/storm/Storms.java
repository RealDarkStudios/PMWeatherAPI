package net.nullved.pmweatherapi.storm;

import dev.protomanly.pmweather.event.GameBusEvents;
import dev.protomanly.pmweather.weather.Storm;
import dev.protomanly.pmweather.weather.WeatherHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Storms {
    public static final Storms INSTANCE = new Storms();
    private WeatherHandler handler;

    private Storms() {
        handler = GameBusEvents.MANAGERS.get(Level.OVERWORLD);
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
}
