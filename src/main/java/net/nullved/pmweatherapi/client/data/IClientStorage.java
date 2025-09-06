package net.nullved.pmweatherapi.client.data;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.network.S2CStoragePacket;
import net.nullved.pmweatherapi.storage.IStorage;
import net.nullved.pmweatherapi.storage.data.IStorageData;
import net.nullved.pmweatherapi.storage.data.StorageData;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The interface defining the client-side implementation of a Storage such as {@link RadarClientStorage}
 * <br><br>
 * Every time the client changes dimension, a new {@link IClientStorage} is created for each storage.
 * @since 0.15.3.3
 */
public interface IClientStorage<D extends IStorageData> extends IStorage<D> {
    ClientLevel getLevel();

    /**
     * Syncs all data from a {@link S2CStoragePacket} with operation {@code overwrite} into this storage's memory
     * @param tag The {@link CompoundTag} of the data
     * @since 0.15.3.3
     */
    default void syncAll(CompoundTag tag) {
        clean();
        syncAdd(tag);
    }

    /**
     * Syncs data from a {@link S2CStoragePacket} with operation {@code add} into this storage's memory
     * @param tag The {@link CompoundTag} of the data
     * @since 0.15.3.3
     */
    default void syncAdd(CompoundTag tag) {
        int version = tag.getInt("version");
        if (tag.contains("list") && tag.getBoolean("list")) {
            // list format
            ListTag list = tag.getList("data", ListTag.TAG_COMPOUND);
            add(list.stream().map((t -> {
                try {
                    return (D) StorageData.deserializeFromNBT((CompoundTag) t, version);
                } catch (ClassCastException e) {
                    PMWeatherAPI.LOGGER.info("Invalid data entry in NBT: {}", e.getMessage());
                    return null;
                }
            })).filter(Objects::nonNull).collect(Collectors.toSet()));
        } else {
            try {
                add((D) StorageData.deserializeFromNBT(tag.getCompound("data"), version));
            } catch (ClassCastException e) {
                PMWeatherAPI.LOGGER.info("Invalid data entry in NBT: {}", e.getMessage());
            }
        }
    }

    /**
     * Syncs data from a {@link S2CStoragePacket} with operation {@code remove} into this storage's memory
     * @param tag The {@link CompoundTag} of the data
     * @since 0.15.3.3
     */
    default void syncRemove(CompoundTag tag) {
        int version = tag.getInt("version");
        if (!tag.contains("format")) {
            // data
            if (tag.contains("list") && tag.getBoolean("list")) {
                // list format
                ListTag list = tag.getList("data", ListTag.TAG_COMPOUND);
                removeByData(list.stream().map((t -> {
                    try {
                        return (D) StorageData.deserializeFromNBT((CompoundTag) t, version);
                    } catch (ClassCastException e) {
                        PMWeatherAPI.LOGGER.info("Invalid data entry in NBT: {}", e.getMessage());
                        return null;
                    }
                })).filter(Objects::nonNull).collect(Collectors.toSet()));
            } else {
                // not list format
                try {
                    remove((D) StorageData.deserializeFromNBT(tag.getCompound("data"), version));
                } catch (ClassCastException e) {
                    PMWeatherAPI.LOGGER.info("Invalid data entry in NBT: {}", e.getMessage());
                }
            }
        } else if (tag.getString("format").equals("blockpos")) {
            if (tag.contains("list") && tag.getBoolean("list")) {
                // list format
                ListTag list = tag.getList("data", ListTag.TAG_INT_ARRAY);
                removeByPos(list.stream().map(t -> {
                    if (t instanceof IntArrayTag iat) {
                        return new BlockPos(iat.get(0).getAsInt(), iat.get(1).getAsInt(), iat.get(2).getAsInt());
                    } else return null;
                }).collect(Collectors.toSet()));
            } else {
                // not list format
                remove(NbtUtils.readBlockPos(tag, "data").orElseThrow());
            }
        } else {
            PMWeatherAPI.LOGGER.info("Invalid data format for packet: '{}'!", tag.getString("format"));
        }
    }
}
