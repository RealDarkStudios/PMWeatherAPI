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
    private final RadarMode radarMode;
    private final boolean on;

    public RadarStorageData(BlockPos pos, RadarMode radarMode, boolean on) {
        super(pos);
        this.radarMode = radarMode;
        this.on = on;
    }

    public RadarMode getRadarMode() {
        return radarMode;
    }

    public boolean isOn() {
        return on;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public CompoundTag serializeToNBT() {
        CompoundTag tag = super.serializeToNBT();
        tag.putString("radar_mode", radarMode.getSerializedName());
        tag.putBoolean("on", on);
        return tag;
    }

    public static RadarStorageData deserializeFromNBT(CompoundTag tag, int version) {
        BlockPos bp = deserializeBlockPos(tag);

        if (bp != null) {
            RadarMode mode = RadarMode.get(tag.getString("radar_mode"));
            boolean on = tag.getBoolean("on");
            return new RadarStorageData(bp, mode, on);
        } else {
            return new RadarStorageData(NbtUtils.readBlockPos(tag, "").orElseThrow(() -> new IllegalArgumentException("Could not read BlockPos in RadarStorageData!")), RadarMode.get(tag.getString("radar_mode")), !tag.contains("on") || tag.getBoolean("on"));
        }
    }
}
