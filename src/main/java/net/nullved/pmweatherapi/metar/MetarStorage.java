package net.nullved.pmweatherapi.metar;

import dev.protomanly.pmweather.block.MetarBlock;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.metar.MetarClientStorage;
import net.nullved.pmweatherapi.storage.PMWStorage;

/**
 * {@link PMWStorage} for {@link MetarBlock}s
 *
 * @since 0.15.3.3
 * @see PMWStorage
 * @see MetarServerStorage
 * @see MetarClientStorage
 */
public abstract class MetarStorage extends PMWStorage<MetarStorageData> {
    public static final int VERSION = 1;
    public static final ResourceLocation ID = PMWeatherAPI.rl("metars");

    public MetarStorage(ResourceKey<Level> dimension) {
        super(dimension);
    }

    public abstract Level getLevel();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ResourceLocation getExpectedDataType() {
        return MetarStorageData.ID;
    }

    @Override
    public int version() {
        return VERSION;
    }
}
