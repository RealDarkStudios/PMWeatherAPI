package net.nullved.pmweatherapi.radar.storage;

import dev.protomanly.pmweather.multiblock.wsr88d.WSR88DCore;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.radar.WSRClientStorage;
import net.nullved.pmweatherapi.storage.PMWStorage;

/**
 * {@link PMWStorage} for {@link WSR88DCore}s
 *
 * @since 0.15.3.3
 * @see PMWStorage
 * @see WSRServerStorage
 * @see WSRClientStorage
 */
public abstract class WSRStorage extends PMWStorage<WSRStorageData> {
    public static final int VERSION = 1;
    public static final ResourceLocation ID = PMWeatherAPI.rl("wsrs");

    public WSRStorage(ResourceKey<Level> dimension) {
        super(dimension);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ResourceLocation getExpectedDataType() {
        return WSRStorageData.ID;
    }

    @Override
    public int version() {
        return VERSION;
    }
}
