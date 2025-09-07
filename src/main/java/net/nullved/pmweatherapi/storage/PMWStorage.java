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
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.data.PMWStorageSavedData;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.event.PMWEvents;
import net.nullved.pmweatherapi.radar.storage.RadarServerStorage;
import net.nullved.pmweatherapi.radar.storage.RadarStorage;
import net.nullved.pmweatherapi.storage.data.IStorageData;
import net.nullved.pmweatherapi.storage.data.StorageData;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * A basic {@link IStorage} implementation that should cover most, if not all, use-cases.
 * <br><br>
 * A "Storage" saves and maintains a list of {@link IStorageData} on the {@link Level} that can be reloaded on world load.
 * It does this by separating each {@link IStorageData} by chunk (more specifically, by {@link ChunkPos})
 * <br><br>
 * Any {@link IStorageData} can be saved, regardless of type, however, both server and clients must expect the same data structure.
 * For example, {@link RadarStorage} is meant to store the positions of {@link RadarBlock}s in the world
 * <br><br>
 * {@link PMWStorage} does not handle syncing radars from the server to the client, instead,
 * implement {@link ISyncServerStorage} on a Server Storage and {@link IClientStorage} on a Client Storage.
 * To sync using your own method, implement {@link IServerStorage} on the Server Storage instead.
 * <br><br>
 * For your storage to be saved, you must first register it using {@link PMWStorages#registerStorage(ResourceLocation, Class, Function)} on both sides,
 * and {@link PMWClientStorages#registerStorage(ResourceLocation, Class, Function)} on the client-side only
 * <br><br>
 * For a full implementation example, see {@link RadarStorage}, {@link RadarServerStorage}, and {@link RadarClientStorage}
 *
 * @see IStorage
 * @see IServerStorage
 * @see IClientStorage
 * @since 0.15.3.3
 */
public abstract class PMWStorage<D extends StorageData> implements IStorage<D> {

    /**
     * A {@link Set} of {@link IStorageData} split up by {@link ChunkPos}
     * @since 0.15.3.3
     */
    private final Map<ChunkPos, Set<D>> data = new HashMap<>();
    /**
     * The times each {@link ChunkPos} was last checked
     * @since 0.15.3.3
     */
    private final Map<ChunkPos, Long> checkTimes = new HashMap<>();
    /**
     * The dimension to store {@link BlockPos} for
     * @since 0.15.3.3
     */
    private final ResourceKey<Level> dimension;

    @Override
    public void clean() {
        data.clear();
        checkTimes.clear();
    }

    public abstract ResourceLocation getExpectedDataType();

    /**
     * Gets the level associated with this {@link IStorage}.
     * For the client side, it returns the {@link ClientLevel}.
     * For the server side, it returns a {@link ServerLevel}.
     *
     * @return A {@link Level} instance
     * @since 0.15.3.3
     */
    public abstract Level getLevel();

    /**
     * The {@link ResourceLocation} ID of this {@link IStorage}.
     * Used primarily for saving to the file at {@code data/<namespace>_<path>.dat}.
     *
     * @return A {@link ResourceLocation}
     * @since 0.15.3.3
     */
    public abstract ResourceLocation getId();

    /**
     * The version of this {@link IStorage}.
     * To disable version data from being saved, return {@code -1}
     *
     * @return The version of the saved data
     * @since 0.15.3.3
     */
    public abstract int version();

    /**
     * The base constructor
     *
     * @param dimension The dimension of the {@link IStorage}
     * @since 0.15.3.3
     */
    public PMWStorage(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    /**
     * Gets a {@link Set} of every {@link IStorageData} saved in this {@link IStorage}, regardless of {@link ChunkPos}
     *
     * @return Every saved {@link IStorageData}
     * @since 0.15.3.3
     */
    public Set<D> getAll() {
        return data.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Gets a {@link Set} of every {@link IStorageData} within a given radius of the base {@link BlockPos}
     *
     * @param base The base {@link BlockPos}
     * @param radius The radius of the search range
     * @return All {@link IStorageData} within {@code radius} blocks of the base {@link BlockPos}
     * @since 0.15.3.3
     */
    @Override
    public Set<D> getAllWithinRange(BlockPos base, double radius) {
        int chunks = (int) Math.ceil(radius / 16.0F) + 1;
        ChunkPos cpos = new ChunkPos(base);

        HashSet<D> set = new HashSet<>();
        for (int x = -chunks; x <= chunks; x++) {
            for (int z = -chunks; z <= chunks; z++) {
                for (D candidate: getInChunk(new ChunkPos(cpos.x + x, cpos.z + z))) {
                    if (Math.abs(base.distToCenterSqr(candidate.getPos().getX(), candidate.getPos().getY(), candidate.getPos().getZ())) <= radius * radius) set.add(candidate);
                }
            }
        }

        return set;
    }

    /**
     * Gets the {@link Set} of {@link IStorageData} for this {@link ChunkPos}
     *
     * @param pos The {@link ChunkPos} to search
     * @return A {@link Set} of the {@link IStorageData} in this chunk
     * @since 0.15.3.3
     */
    public Set<D> getInChunk(ChunkPos pos) {
        return data.getOrDefault(pos, Set.of());
    }

    /**
     * Gets the {@link Set} of {@link IStorageData} within this and adjacent {@link ChunkPos}'
     *
     * @param pos The middle {@link ChunkPos}
     * @return A {@link Set} of the {@link IStorageData} in this and adjacent chunks
     * @since 0.15.3.3
     */
    @Override
    public Set<D> getInAdjacentChunks(ChunkPos pos) {
        Set<D> set = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                set.addAll(getInChunk(new ChunkPos(pos.x + x, pos.z + z)));
            }
        }
        return set;
    }


    /**
     * Executes a {@link Consumer} for every {@link IStorageData} saved in this {@link IStorage}, regardless of {@link ChunkPos}
     *
     * @param consumer The function to run for each {@link IStorageData}
     * @since 0.15.3.3-rc2
     */
    @Override
    public void forAll(Consumer<D> consumer) {
        getAll().forEach(consumer);
    }

    /**
     * Executes a {@link Consumer} for every {@link IStorageData} within a given radius of the base {@link BlockPos}
     *
     * @param base The base {@link BlockPos}
     * @param radius The radius of the search range
     * @param consumer The function to run for each {@link IStorageData}
     * @since 0.15.3.3-rc2
     */
    @Override
    public void forAllWithinRange(BlockPos base, double radius, Consumer<D> consumer) {
        getAllWithinRange(base, radius).forEach(consumer);
    }

    /**
     * Executes a {@link Consumer} for each {@link IStorageData} in this {@link ChunkPos}
     *
     * @param pos The {@link ChunkPos} to search
     * @param consumer The function to run for each {@link IStorageData}
     * @since 0.15.3.3-rc2
     */
    @Override
    public void forInChunk(ChunkPos pos, Consumer<D> consumer) {
        getInChunk(pos).forEach(consumer);
    }

    /**
     * Executes a {@link Consumer} for each {@link IStorageData} within this and adjacent {@link ChunkPos}'
     *
     * @param pos The middle {@link ChunkPos}
     * @param consumer The function to run for each {@link IStorageData}
     * @since 0.15.3.3-rc2
     */
    @Override
    public void forInAdjacentChunks(ChunkPos pos, Consumer<D> consumer) {
        getInAdjacentChunks(pos).forEach(consumer);
    }

    /**
     * Determines if the data for the given {@link ChunkPos} is older than 30 seconds or does not exist.
     * Intended to be used while listening to a {@link ChunkWatchEvent.Sent} event (See {@link PMWEvents})
     *
     * @param pos The {@link ChunkPos} to check
     * @return Whether the data should be recalculated or not
     * @since 0.15.3.3
     */
    public boolean shouldRecalculate(ChunkPos pos) {
        if (!checkTimes.containsKey(pos)) {
            checkTimes.put(pos, System.currentTimeMillis());
            return true;
        }

        return checkTimes.get(pos) - System.currentTimeMillis() > 30000L;
    }

    /**
     * Adds a single {@link IStorageData} to the {@link IStorage}
     *
     * @param addData The new {@link IStorageData}
     * @since 0.15.3.3
     */
    public void add(D addData) {
        ChunkPos chunkPos = new ChunkPos(addData.getPos());
        Set<D> set = data.computeIfAbsent(chunkPos, c -> new HashSet<>());

        Set<BlockPos> exist = set.stream().map(IStorageData::getPos).filter(c -> c.equals(addData.getPos())).collect(Collectors.toSet());
        if (!exist.isEmpty()) {
            removeByPos(exist);
        }

        set.add(addData);
        data.put(chunkPos, set);
    }

    /**
     * Adds multiple new {@link IStorageData} to the {@link IStorage}
     *
     * @param datum A {@link Collection} of new {@link IStorageData}
     * @since 0.15.3.3
     */
    public void add(Collection<D> datum) {
        datum.forEach(this::add);
    }

    /**
     * Removes a single {@link BlockPos} from the {@link IStorage}
     *
     * @param pos The {@link BlockPos} to remove
     * @since 0.15.3.3
     */
    public void remove(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<D> set = data.computeIfAbsent(chunkPos, c -> new HashSet<>());

        Set<D> exist = set.stream().filter(c -> c.getPos().equals(pos)).collect(Collectors.toSet());
        if (!exist.isEmpty()) {
            exist.forEach(set::remove);
        }

        data.put(chunkPos, set);
    }

    /**
     * Removes multiple {@link BlockPos} from the {@link IStorage}
     *
     * @param pos A {@link Collection} of {@link BlockPos} to remove
     * @since 0.15.3.3
     */
    public void removeByPos(Collection<BlockPos> pos) {
        pos.forEach(this::remove);
    }

    /**
     * Removes a single {@link IStorageData} from the {@link IStorage}
     *
     * @param removedData The {@link IStorageData} to remove
     * @since 0.15.3.3
     */
    public void remove(D removedData) {
        ChunkPos chunkPos = new ChunkPos(removedData.getPos());
        Set<D> set = data.computeIfAbsent(chunkPos, c -> new HashSet<>());
        set.remove(removedData);
        data.put(chunkPos, set);
    }

    /**
     * Removes multiple {@link IStorageData} from the {@link IStorage}
     *
     * @param datum A {@link Collection} of {@link IStorageData} to remove
     * @since 0.15.3.3
     */
    public void removeByData(Collection<D> datum) {
        datum.forEach(this::remove);
    }

    /**
     * Saves the data of this {@link IStorage} to a {@link CompoundTag}
     *
     * @param tag The pre-existing {@link CompoundTag}
     * @return A {@link CompoundTag} with storage data
     * @since 0.15.3.3
     */
    public CompoundTag save(CompoundTag tag) {
        PMWeatherAPI.LOGGER.info("Saving storage {} to level...", getId());
        if (version() != -1) tag.putInt("version", version());
        tag.putLong("saveTime", System.currentTimeMillis());

        final ResourceLocation[] type = {null};
        for (Map.Entry<ChunkPos, Set<D>> entry : data.entrySet()) {
            ListTag list = new ListTag();
            entry.getValue().forEach(storageData -> {
                CompoundTag ctag = storageData.serializeToNBT();
                if (type[0] == null) {
                    type[0] = ResourceLocation.parse(ctag.getString("type"));
                }
                ctag.remove("type");
                list.add(ctag);
            });
            tag.put(String.valueOf(entry.getKey().toLong()), list);
        }

        if (type[0] != null) tag.putString("type", type[0].toString());

        PMWeatherAPI.LOGGER.info("Saved storage {} to level", getId());
        return tag;
    }

    /**
     * Reads the saved data from the {@link Level} and initializes this {@link IStorage} with the data
     * @since 0.15.3.3
     */
    public void read() {
        PMWStorageSavedData savedData = ((ServerLevel) this.getLevel()).getDataStorage().computeIfAbsent(PMWStorageSavedData.factory(), getId().toString().replace(":", "_"));
        savedData.setStorage(this);
        PMWeatherAPI.LOGGER.info("Reading storage {} from level...", getId());
        CompoundTag data = savedData.getTag();
        String type = this.getExpectedDataType().toString();
        int version = data.getInt("version");
        Set<String> chunks = data.getAllKeys();
        chunks.removeAll(Set.of("version", "saveTime", "type"));

        for (String chunk : chunks) {
            Set<D> blocks = new HashSet<>();
            ListTag list = (ListTag) data.get(chunk);
            for (int i = 0; i < list.size(); i++) {
                try {
                    if (!list.get(i).getType().equals(CompoundTag.TYPE)) {
                        // not a compound
                        CompoundTag ctag = new CompoundTag();
                        ctag.put("blockpos", list.get(i));

                        if (NbtUtils.readBlockPos(ctag, "blockpos").isPresent()) {
                            blocks.add(StorageData.deserializeFromNBT(ctag, version));
                        } else {
                            PMWeatherAPI.LOGGER.error("Could not deserialize tag {}! No type data and not a blockpos!", NbtUtils.toPrettyComponent(ctag.get("blockpos")));
                        }
                    } else {
                        CompoundTag ctag = list.getCompound(i);
                        if (!type.isEmpty()) ctag.putString("type", type);
                        blocks.add(StorageData.deserializeFromNBT(ctag, version));
                    }
                } catch (ClassCastException e) {
                    PMWeatherAPI.LOGGER.warn("Invalid data entry in storage {} at chunk {}: {}", getId(), chunk, e.getMessage());
                }
            }

            this.data.put(new ChunkPos(Long.parseLong(chunk)), blocks);
        }
    }
}