package net.nullved.pmweatherapi.metar;

import dev.protomanly.pmweather.block.MetarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.storage.data.StorageData;

/**
 * {@link StorageData} for {@link MetarBlock}s.
 * Includes position, temp, dewpoint, wind, and risk data
 *
 * @since 0.15.3.3
 * @see StorageData
 */
public class MetarStorageData extends StorageData {
    public static final ResourceLocation ID = PMWeatherAPI.rl("metar");
    private float temp, dew, windAngle, windspeed, risk;

    public MetarStorageData(BlockPos pos, float temp, float dew, float windAngle, float windspeed, float risk) {
        super(pos);
        this.temp = temp;
        this.dew = dew;
        this.windAngle = windAngle;
        this.windspeed = windspeed;
        this.risk = risk;
    }

    public float getTemp() {
        return temp;
    }

    public float getDew() {
        return dew;
    }

    public float getWindAngle() {
        return windAngle;
    }

    public float getWindspeed() {
        return windspeed;
    }

    public float getRisk() {
        return risk;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public CompoundTag serializeToNBT() {
        CompoundTag tag = super.serializeToNBT();
        tag.putFloat("temp", temp);
        tag.putFloat("dew", dew);
        tag.putFloat("wind_angle", windAngle);
        tag.putFloat("windspeed", windspeed);
        tag.putFloat("risk", risk);
        return tag;
    }

    public static MetarStorageData deserializeFromNBT(CompoundTag tag, int version) {
        BlockPos bp = deserializeBlockPos(tag);

        if (bp == null) {
            throw new IllegalArgumentException("Could not read BlockPos in MetarStorageData!");
        }

        float temp = tag.getFloat("temp");
        float dew = tag.getFloat("dew");
        float windAngle = tag.getFloat("wind_angle");
        float windspeed = tag.getFloat("windspeed");
        float risk = tag.getFloat("risk");
        return new MetarStorageData(bp, temp, dew, windAngle, windspeed, risk);
    }
}
