package net.nullved.pmweatherapi.radar;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gets and manages all the radars for a given dimension
 */
public class Radars {
    private static final HashMap<ResourceKey<Level>, Radars> DIMENSION_MAP = new HashMap<>();

    private Radars(ResourceKey<Level> level) {}

    public static void readFromStorage() {
        Map<ResourceKey<Level>, Map<ChunkPos, Set<BlockPos>>> map = RadarStorage.readAllRadars();
        for (Map.Entry<ResourceKey<Level>, Map<ChunkPos, Set<BlockPos>>> entry : map.entrySet()) {
            Radars radars = get(entry.getKey());
            radars.loadFromStorage(entry.getValue());
        }
    }

    public static void clear() {
        DIMENSION_MAP.clear();
    }

    public static Radars get(ResourceKey<Level> level) {
        return DIMENSION_MAP.computeIfAbsent(level, Radars::new);
    }

    public static Radars get(Level level) {
        return get(level.dimension());
    }

    public static Map<ResourceKey<Level>, Radars> getAllDimensions() {
        return DIMENSION_MAP;
    }

    private final HashMap<ChunkPos, Set<BlockPos>> RADARS = new HashMap<>();
    private final HashMap<ChunkPos, Long> RADAR_CHECK_TIMES = new HashMap<>();

    private void loadFromStorage(Map<ChunkPos, Set<BlockPos>> radars) {
        RADARS.putAll(radars);
    }

    public Map<ChunkPos, Set<BlockPos>> getRadarMap() {
        return RADARS;
    }

    public Set<ChunkPos> getChunksWithRadars() {
        return RADARS.keySet();
    }

    public Set<BlockPos> getRadars() {
        return RADARS.values().parallelStream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    public void registerRadar(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos>  radars = RADARS.computeIfAbsent(chunkPos, p -> {
            RADAR_CHECK_TIMES.put(p, System.currentTimeMillis());
            return new HashSet<>();
        });
        radars.add(pos);
        RADARS.put(chunkPos, radars);
    }

    public void registerRadars(List<BlockPos> radars) {
        for (BlockPos radar : radars) {
            registerRadar(radar);
        }
    }

    public void unregisterRadar(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos>  radars = RADARS.computeIfAbsent(chunkPos, p -> {
            RADAR_CHECK_TIMES.put(p, System.currentTimeMillis());
            return new HashSet<>();
        });
        radars.remove(pos);
        RADARS.put(chunkPos, radars);
    }
    public void unregisterRadars(List<BlockPos> radars) {
        for (BlockPos radar : radars) {
            unregisterRadar(radar);
        }
    }

    public boolean shouldRecalculate(LevelChunk chunk) {
        if (!RADAR_CHECK_TIMES.containsKey(chunk.getPos())) {
            RADAR_CHECK_TIMES.put(chunk.getPos(), System.currentTimeMillis());
            return true;
        }

        return RADAR_CHECK_TIMES.get(chunk.getPos()) > 30000L;
    }
}
