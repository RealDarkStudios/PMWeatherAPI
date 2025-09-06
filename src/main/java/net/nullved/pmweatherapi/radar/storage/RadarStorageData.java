package net.nullved.pmweatherapi.radar.storage;

import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.radar.RadarMode;
import net.nullved.pmweatherapi.storage.data.StorageData;

/**
 * {@link StorageData} for {@link RadarBlock}s.
 * Includes position and radar mode data
 *
 * @since 0.15.3.3
 * @see StorageData
 */
public class RadarStorageData extends StorageData {
    public static final ResourceLocation ID = PMWeatherAPI.rl("radar");
    private RadarMode radarMode;

    public RadarStorageData(BlockPos pos, RadarMode radarMode) {
        super(pos);
        this.radarMode = radarMode;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public CompoundTag serializeToNBT() {
        CompoundTag tag = super.serializeToNBT();
        tag.putString("radar_mode", radarMode.getSerializedName());
        return tag;
    }

    public static RadarStorageData deserializeFromNBT(CompoundTag tag, int version) {
        BlockPos bp = deserializeBlockPos(tag);

        if (bp != null) {
            RadarMode mode = RadarMode.get(tag.getString("radar_mode"));
            return new RadarStorageData(bp, mode);
        } else {
            return new RadarStorageData(NbtUtils.readBlockPos(tag, "").orElseThrow(() -> new IllegalArgumentException("Could not read BlockPos in RadarStorageData!")), RadarMode.get(tag.getString("radar_mode")));
        }
    }
}
