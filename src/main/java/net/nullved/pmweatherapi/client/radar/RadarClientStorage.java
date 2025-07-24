package net.nullved.pmweatherapi.client.radar;

import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.radar.RadarStorage;

/**
 * A {@link IClientStorage} implementation for {@link RadarBlock}s
 * <br><br>
 * You should not create a {@link RadarClientStorage}, instead, use {@link PMWClientStorages#radars()}
 * @since 0.15.1.1
 */
public class RadarClientStorage extends RadarStorage implements IClientStorage {
    /**
     * <strong>DO NOT CALL THIS CONSTRUCTOR!!!</strong>
     * <br>
     * Get a radar storage from {@link PMWClientStorages#radars()}
     * @param clientLevel The {@link ClientLevel} to create this storage for
     * @since 0.15.1.1
     */
    public RadarClientStorage(ClientLevel clientLevel) {
        super(clientLevel.dimension());
    }

    /**
     * Gets the level associated with this {@link RadarClientStorage}
     * @return The {@link Minecraft} {@link ClientLevel}
     * @since 0.15.1.1
     */
    @Override
    public ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }
}