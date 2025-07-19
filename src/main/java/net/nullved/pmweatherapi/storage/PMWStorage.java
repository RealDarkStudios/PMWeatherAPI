package net.nullved.pmweatherapi.storage;

import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.data.PMWStorageSavedData;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.event.PMWEvents;
import net.nullved.pmweatherapi.radar.RadarServerStorage;
import net.nullved.pmweatherapi.radar.RadarStorage;

import java.util.*;
import java.util.stream.Collectors;


/**
 * The basic {@link IStorage} implementation that should cover most, if not all, use-cases.
 * <br><br>
 * Using this class automatically adds your storage to {@link PMWStorages},
 * which allows for easy retrieval using {@link PMWStorages#get(ResourceLocation)}
 * <br><br>
 * A "Storage" saves and maintains a list of {@link BlockPos} to the {@link Level} that can be reloaded on world load.
 * It does this by separating each {@link BlockPos} by chunk (more specifically, by {@link ChunkPos})
 * <br><br>
 * Any {@link BlockPos} can be saved, regardless of the type of {@link Block}.
 * The use case for {@link RadarStorage} specifically is to store the positions of {@link RadarBlock}s in the world
 * <br><br>
 * {@link PMWStorage} does not handle syncing radars from the server to the client, instead,
 * implement {@link IServerStorage} on a Server Storage and {@link IClientStorage} on a Client Storage
 * <br><br>
 * For a full implementation example, see {@link RadarStorage}, {@link RadarServerStorage}, and {@link RadarClientStorage}
 *
 * @see IStorage
 * @see IServerStorage
 * @see IClientStorage
 * @since 0.14.16.3
 */
public abstract class PMWStorage implements IStorage {
    /**
     * A {@link Set} of {@link BlockPos} split up by {@link ChunkPos}
     * @since 0.14.16.3
     */
    private Map<ChunkPos, Set<BlockPos>> positions = new HashMap<>();
    /**
     * The times each {@link ChunkPos} was last checked
     * @since 0.14.16.3
     */
    private Map<ChunkPos, Long> checkTimes = new HashMap<>();
    /**
     * The dimension to store {@link BlockPos} for
     * @since 0.14.16.3
     */
    private ResourceKey<Level> dimension;

    @Override
    public void clean() {
        positions.clear();
        checkTimes.clear();
    }

    /**
     * Gets the level associated with this {@link IStorage}.
     * For the client side, it returns the {@link ClientLevel}.
     * For the server side, it returns a {@link ServerLevel}.
     *
     * @return A {@link Level} instance
     * @since 0.14.16.3
     */
    public abstract Level getLevel();

    /**
     * The {@link ResourceLocation} ID of this {@link IStorage}.
     * Used primarily for saving to the file at {@code data/<namespace>_<path>.dat}.
     *
     * @return A {@link ResourceLocation}
     * @since 0.14.16.3
     */
    public abstract ResourceLocation getId();

    /**
     * The version of this {@link IStorage}.
     * To disable version data from being saved, return {@code -1}
     *
     * @return The version of the saved data
     * @since 0.14.16.3
     */
    public abstract int version();

    /**
     * The base constructor
     *
     * @param dimension The dimension of the {@link IStorage}
     * @since 0.14.16.3
     */
    public PMWStorage(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    /**
     * Gets a {@link Set} of every {@link BlockPos} saved in this {@link IStorage}, regardless of {@link ChunkPos}
     *
     * @return Every saved {@link BlockPos}
     * @since 0.14.16.3
     */
    public Set<BlockPos> getAll() {
        return positions.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public Set<BlockPos> getAllWithinRange(BlockPos base, double radius) {
        int chunks = (int) Math.ceil(radius / 16.0F) + 1;
        ChunkPos cpos = new ChunkPos(base);

        HashSet<BlockPos> set = new HashSet<>();
        for (int x = -chunks; x <= chunks; x++) {
            for (int z = -chunks; z <= chunks; z++) {
                for (BlockPos candidate: getInChunk(new ChunkPos(cpos.x + x, cpos.z + z))) {
                    if (Math.abs(base.distToCenterSqr(candidate.getX(), candidate.getY(), candidate.getZ())) <= radius * radius) set.add(candidate);
                }
            }
        }

        return set;
    }

    /**
     * Gets the {@link Set} of {@link BlockPos} for this {@link ChunkPos}
     *
     * @param pos The {@link ChunkPos} to search
     * @return A {@link Set} of the {@link BlockPos} in this chunk
     * @since 0.14.16.3
     */
    public Set<BlockPos> getInChunk(ChunkPos pos) {
        return positions.getOrDefault(pos, Set.of());
    }

    @Override
    public Set<BlockPos> getInAdjacentChunks(ChunkPos pos) {
        Set<BlockPos> set = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                set.addAll(getInChunk(new ChunkPos(pos.x + x, pos.z + z)));
            }
        }
        return set;
    }

    /**
     * Determines if the data for the given {@link ChunkPos} is older than 30 seconds or does not exist.
     * Intended to be used while listening to a {@link ChunkWatchEvent.Sent} event (See {@link PMWEvents})
     *
     * @param pos The {@link ChunkPos} to check
     * @return Whether the data should be recalculated or not
     * @since 0.14.16.3
     */
    public boolean shouldRecalculate(ChunkPos pos) {
        if (!checkTimes.containsKey(pos)) {
            checkTimes.put(pos, System.currentTimeMillis());
            return true;
        }

        return checkTimes.get(pos) - System.currentTimeMillis() > 30000L;
    }

    /**
     * Adds a single {@link BlockPos} to the {@link IStorage}
     *
     * @param pos The new {@link BlockPos}
     * @since 0.14.16.3
     */
    public void add(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> set = positions.computeIfAbsent(chunkPos, c -> new HashSet<>());
        set.add(pos);
        positions.put(chunkPos, set);
    }

    /**
     * Adds multiple new {@link BlockPos} to the {@link IStorage}
     *
     * @param pos A {@link Collection} of new {@link BlockPos}
     * @since 0.14.16.3
     */
    public void add(Collection<BlockPos> pos) {
        pos.forEach(this::add);
    }

    /**
     * Removes a single {@link BlockPos} from the {@link IStorage}
     *
     * @param pos The {@link BlockPos} to remove
     * @since 0.14.16.3
     */
    public void remove(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> set = positions.computeIfAbsent(chunkPos, c -> new HashSet<>());
        set.remove(pos);
        positions.put(chunkPos, set);
    }

    /**
     * Removes multiple {@link BlockPos} from the {@link IStorage}
     *
     * @param pos A {@link Collection} of {@link BlockPos} to remove
     * @since 0.14.16.3
     */
    public void remove(Collection<BlockPos> pos) {
        pos.forEach(this::remove);
    }

    /**
     * Saves the data of this {@link IStorage} to a {@link CompoundTag}
     *
     * @param tag The pre-existing {@link CompoundTag}
     * @return A {@link CompoundTag} with storage data
     * @since 0.14.16.3
     */
    public CompoundTag save(CompoundTag tag) {
        PMWeatherAPI.LOGGER.info("Saving storage {} to level...", getId());
        if (version() != -1) tag.putInt("version", version());
        tag.putLong("saveTime", System.currentTimeMillis());

        for (Map.Entry<ChunkPos, Set<BlockPos>> entry : positions.entrySet()) {
            ListTag blockList = new ListTag();
            entry.getValue().forEach(blockPos -> blockList.add(NbtUtils.writeBlockPos(blockPos)));
            tag.put(String.valueOf(entry.getKey().toLong()), blockList);
        }

        PMWeatherAPI.LOGGER.info("Saved storage {} to level", getId());
        return tag;
    }

    /**
     * Reads the saved data from the {@link Level} and initializes this {@link IStorage} with the data
     * @since 0.14.16.3
     */
    public void read() {
        PMWStorageSavedData savedData = ((ServerLevel) this.getLevel()).getDataStorage().computeIfAbsent(PMWStorageSavedData.factory(), getId().toString().replace(":", "_"));
        savedData.setStorage(this);
        PMWeatherAPI.LOGGER.info("Reading storage {} from level...", getId());
        CompoundTag data = savedData.getTag();
        Set<String> chunks = data.getAllKeys();
        chunks.removeAll(Set.of("version", "saveTime"));

        for (String chunk : chunks) {
            Set<BlockPos> radars = new HashSet<>();

            ListTag blockList = (ListTag) data.get(chunk);
            for (int i = 0; i < blockList.size(); i++) {
                int[] bp = blockList.getIntArray(i);
                radars.add(new BlockPos(bp[0], bp[1], bp[2]));
            }

            this.positions.put(new ChunkPos(Long.parseLong(chunk)), radars);
        }
    }
}