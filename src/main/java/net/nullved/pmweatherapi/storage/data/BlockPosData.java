package net.nullved.pmweatherapi.storage.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.nullved.pmweatherapi.PMWeatherAPI;

/**
 * A wrapper around {@link StorageData} to give it an ID
 *
 * @see StorageData
 * @since 0.15.3.3
 */
public class BlockPosData extends StorageData {
    public static final ResourceLocation ID = PMWeatherAPI.rl("blockpos");

    public BlockPosData(BlockPos pos) {
        super(pos);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static BlockPosData deserializeFromNBT(CompoundTag tag, int version) {
        return new BlockPosData(deserializeBlockPos(tag));
    }
}
