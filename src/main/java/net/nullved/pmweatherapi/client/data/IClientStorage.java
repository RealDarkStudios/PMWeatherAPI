package net.nullved.pmweatherapi.client.data;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.network.S2CStoragePacket;
import net.nullved.pmweatherapi.storage.IStorage;

import java.util.stream.Collectors;

/**
 * The interface defining the client-side implementation of a Storage such as {@link RadarClientStorage}
 * <br><br>
 * Every time the client changes dimension, a new {@link IClientStorage} is created for each storage.
 * @since 0.15.1.1
 */
public interface IClientStorage extends IStorage {
    ClientLevel getLevel();

    default void syncAll(CompoundTag tag) {
        clean();
        syncAdd(tag);
    }

    /**
     * Syncs data from a {@link S2CStoragePacket} with operation {@code add} into this storage's memory
     * @since 0.15.1.1
     */
    default void syncAdd(CompoundTag tag) {
        if (tag.contains("list") && tag.getBoolean("list")) {
            // list format
            ListTag list = tag.getList("data", ListTag.TAG_INT_ARRAY);
            add(list.stream().map(t -> {
                if (t instanceof IntArrayTag iat) {
                    return new BlockPos(iat.get(0).getAsInt(), iat.get(1).getAsInt(), iat.get(2).getAsInt());
                } else return null;
            }).collect(Collectors.toSet()));
        } else {
            // not list format
            add(NbtUtils.readBlockPos(tag, "data").orElseThrow());
        }
    }

    /**
     * Syncs data from a {@link S2CStoragePacket} with operation {@code remove} into this storage's memory
     * @since 0.15.1.1
     */
    default void syncRemove(CompoundTag tag) {
        if (tag.contains("list") && tag.getBoolean("list")) {
            // list format
            ListTag list = tag.getList("data", ListTag.TAG_INT_ARRAY);
            remove(list.stream().map(t -> {
                if (t instanceof IntArrayTag iat) {
                    return new BlockPos(iat.get(0).getAsInt(), iat.get(1).getAsInt(), iat.get(2).getAsInt());
                } else return null;
            }).collect(Collectors.toSet()));
        } else {
            // not list format
            remove(NbtUtils.readBlockPos(tag, "data").orElseThrow());
        }
    }
}
