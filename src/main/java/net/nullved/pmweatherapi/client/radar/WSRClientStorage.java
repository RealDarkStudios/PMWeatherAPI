package net.nullved.pmweatherapi.client.radar;

import dev.protomanly.pmweather.block.RadarBlock;
import dev.protomanly.pmweather.multiblock.wsr88d.WSR88DCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.radar.storage.RadarStorage;
import net.nullved.pmweatherapi.radar.storage.RadarStorageData;
import net.nullved.pmweatherapi.radar.storage.WSRStorage;
import net.nullved.pmweatherapi.radar.storage.WSRStorageData;

/**
 * A {@link IClientStorage} implementation for {@link WSR88DCore}s
 * <br><br>
 * You should not create a {@link WSRClientStorage}, instead, use {@link PMWClientStorages#wsrs()}
 * @since 0.15.3.3
 */
public class WSRClientStorage extends WSRStorage implements IClientStorage<WSRStorageData> {
    /**
     * <strong>DO NOT CALL THIS CONSTRUCTOR!!!</strong>
     * <br>
     * Get a radar storage from {@link PMWClientStorages#wsrs()}
     * @param clientLevel The {@link ClientLevel} to create this storage for
     * @since 0.15.3.3
     */
    public WSRClientStorage(ClientLevel clientLevel) {
        super(clientLevel.dimension());
    }

    /**
     * Gets the level associated with this {@link WSRClientStorage}
     * @return The {@link Minecraft} {@link ClientLevel}
     * @since 0.15.3.3
     */
    @Override
    public ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }
}