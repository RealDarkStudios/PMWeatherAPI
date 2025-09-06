package net.nullved.pmweatherapi.radar.storage;


import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.storage.IStorage;
import net.nullved.pmweatherapi.storage.PMWStorage;

/**
 * {@link PMWStorage} for {@link RadarBlock}s
 *
 * @since 0.15.3.3
 * @see PMWStorage
 * @see RadarServerStorage
 * @see RadarClientStorage
 */
public abstract class RadarStorage extends PMWStorage<RadarStorageData> {
    public static final int VERSION = 2;
    public static final ResourceLocation ID = PMWeatherAPI.rl("radars");

    public RadarStorage(ResourceKey<Level> dimension) {
        super(dimension);
    }

    public abstract Level getLevel();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ResourceLocation getExpectedDataType() {
        return RadarStorageData.ID;
    }

    @Override
    public int version() {
        return VERSION;
    }
}
