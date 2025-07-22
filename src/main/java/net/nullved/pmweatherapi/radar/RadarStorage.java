package net.nullved.pmweatherapi.radar;


import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.storage.IStorage;
import net.nullved.pmweatherapi.storage.PMWStorage;

/**
 * A {@link IStorage} implementation for {@link RadarBlock}s
 *
 * @since 0.15.1.1
 */
public abstract class RadarStorage extends PMWStorage {
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
    public int version() {
        return 1;
    }
}
