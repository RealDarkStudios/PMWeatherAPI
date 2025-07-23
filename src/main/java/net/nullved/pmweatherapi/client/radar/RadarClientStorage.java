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
 * You should not create a {@link RadarClientStorage}, instead, use {@link PMWClientStorages#getRadars()}
 * @since 0.14.16.3
 */
public class RadarClientStorage extends RadarStorage implements IClientStorage {
    /**
     * <strong>DO NOT CALL THIS CONSTRUCTOR!!!</strong>
     * <br>
     * Get a radar storage from {@link PMWClientStorages#getRadars()}
     * @param dimension The dimension to create this storage for
     * @since 0.14.16.3
     */
    public RadarClientStorage(ResourceKey<Level> dimension) {
        super(dimension);
    }

    /**
     * Gets the level associated with this {@link RadarClientStorage}
     * @return The {@link Minecraft} {@link ClientLevel}
     * @since 0.14.16.3
     */
    @Override
    public ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }
}