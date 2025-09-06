package net.nullved.pmweatherapi.storage.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.nullved.pmweatherapi.radar.storage.RadarStorageData;

/**
 * The interface defining Storage Data such as {@link RadarStorageData}
 * <br><br>
 * For method definitions, see {@link StorageData}
 *
 * @see StorageData
 * @since 0.15.3.3
 */
public interface IStorageData {
    ResourceLocation getId();
    BlockPos getPos();
    CompoundTag serializeToNBT();
}
