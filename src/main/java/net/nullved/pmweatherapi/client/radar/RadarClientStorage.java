package net.nullved.pmweatherapi.client.radar;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.network.S2CRadarsPacket;
import net.nullved.pmweatherapi.radar.RadarStorage;

import java.util.stream.Collectors;

/**
 * A Radar Storage for the client side.
 * Every time the client changes dimension, a new {@code RadarClientStorage} will be created
 * <br>
 * You should not create a {@code RadarClientStorage} directly, instead, use {@link PMWClientStorages#getRadars()}
 * @since 0.14.15.3
 */
public class RadarClientStorage extends RadarStorage {
    /**
     * <strong>DO NOT CALL THIS CONSTRUCTOR!!!</strong>
     * <br>
     * Get a radar storage from {@link PMWClientStorages#getRadars()}
     * @param dimension The dimension to create this storage for
     * @since 0.14.15.3
     */
    public RadarClientStorage(ResourceKey<Level> dimension) {
        super(dimension);
    }

    /**
     * Gets the level associated with this {@code RadarClientStorage}
     * @return The {@link Minecraft} instance {@link Level}
     * @since 0.14.15.3
     */
    @Override
    public Level getLevel() {
        return Minecraft.getInstance().level;
    }

    /**
     * Syncs data from a {@link S2CRadarsPacket} with operation {@code add} into this storage's memory
     * @since 0.14.15.3
     */
    public void syncAdd(CompoundTag tag) {
        if (tag.contains("list") && tag.getBoolean("list")) {
            // list format
            ListTag list = tag.getList("data", ListTag.TAG_INT_ARRAY);
            addRadars(list.stream().map(t -> {
                if (t instanceof IntArrayTag iat) {
                    return new BlockPos(iat.get(0).getAsInt(), iat.get(1).getAsInt(), iat.get(2).getAsInt());
                } else return null;
            }).collect(Collectors.toSet()));
        } else {
            // not list format
            addRadar(NbtUtils.readBlockPos(tag, "data").orElseThrow());
        }
    }

    /**
     * Syncs data from a {@link S2CRadarsPacket} with operation {@code remove} into this storage's memory
     * @since 0.14.15.3
     */
    public  void syncRemove(CompoundTag tag) {
        if (tag.contains("list") && tag.getBoolean("list")) {
            // list format
            ListTag list = tag.getList("data", ListTag.TAG_INT_ARRAY);
            removeRadars(list.stream().map(t -> {
                if (t instanceof IntArrayTag iat) {
                    return new BlockPos(iat.get(0).getAsInt(), iat.get(1).getAsInt(), iat.get(2).getAsInt());
                } else return null;
            }).collect(Collectors.toSet()));
        } else {
            // not list format
            removeRadar(NbtUtils.readBlockPos(tag, "data").orElseThrow());
        }
    }
}
