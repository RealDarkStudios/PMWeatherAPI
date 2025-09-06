package net.nullved.pmweatherapi.radar.storage;

import dev.protomanly.pmweather.multiblock.wsr88d.WSR88DCore;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.storage.data.StorageData;

/**
 * {@link StorageData} for {@link WSR88DCore}s.
 * Includes position and completion data
 *
 * @since 0.15.3.3
 * @see StorageData
 */
public class WSRStorageData extends StorageData {
    public static final ResourceLocation ID = PMWeatherAPI.rl("wsr");
    private final boolean completed;

    public WSRStorageData(BlockPos pos, boolean completed) {
        super(pos);
        this.completed = completed;
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public CompoundTag serializeToNBT() {
        CompoundTag tag = super.serializeToNBT();
        tag.putBoolean("completed", completed);
        return tag;
    }

    public static WSRStorageData deserializeFromNBT(CompoundTag tag, int version) {
        BlockPos bp = deserializeBlockPos(tag);
        if (bp != null) {
            boolean completed = tag.getBoolean("completed");
            return new WSRStorageData(bp, completed);
        }
        else throw new IllegalArgumentException("Could not read BlockPos in WSRStorageData!");
    }
}
