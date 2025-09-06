package net.nullved.pmweatherapi.client.metar;

import dev.protomanly.pmweather.block.MetarBlock;
import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.metar.MetarStorage;
import net.nullved.pmweatherapi.metar.MetarStorageData;
import net.nullved.pmweatherapi.radar.storage.RadarStorage;
import net.nullved.pmweatherapi.radar.storage.RadarStorageData;

/**
 * A {@link IClientStorage} implementation for {@link MetarBlock}s
 * <br><br>
 * You should not create a {@link MetarClientStorage}, instead, use {@link PMWClientStorages#metars()}
 * @since 0.15.3.3
 */
public class MetarClientStorage extends MetarStorage implements IClientStorage<MetarStorageData> {
    /**
     * <strong>DO NOT CALL THIS CONSTRUCTOR!!!</strong>
     * <br>
     * Get a radar storage from {@link PMWClientStorages#metars()}
     * @param clientLevel The {@link ClientLevel} to create this storage for
     * @since 0.15.3.3
     */
    public MetarClientStorage(ClientLevel clientLevel) {
        super(clientLevel.dimension());
    }

    /**
     * Gets the level associated with this {@link MetarClientStorage}
     * @return The {@link Minecraft} {@link ClientLevel}
     * @since 0.15.3.3
     */
    @Override
    public ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }
}