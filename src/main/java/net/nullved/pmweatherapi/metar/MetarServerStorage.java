package net.nullved.pmweatherapi.metar;

import dev.protomanly.pmweather.block.MetarBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.client.metar.MetarClientStorage;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.network.S2CMetarPacket;
import net.nullved.pmweatherapi.network.S2CStoragePacket;
import net.nullved.pmweatherapi.storage.IServerStorage;
import net.nullved.pmweatherapi.storage.ISyncServerStorage;

/**
 * {@link IServerStorage} for {@link MetarBlock}s
 * <br><br>
 * You should not create a {@link MetarServerStorage}, instead, use {@link PMWStorages#metars()}
 *
 * @since 0.15.3.3
 * @see MetarStorage
 * @see MetarClientStorage
 */
public class MetarServerStorage extends MetarStorage implements ISyncServerStorage<MetarStorageData> {
    private final ServerLevel level;

    public MetarServerStorage(ServerLevel level) {
        super(level.dimension());
        this.level = level;
    }

    @Override
    public ServerLevel getLevel() {
        return level;
    }

    @Override
    public S2CStoragePacket<? extends IClientStorage<MetarStorageData>> packet(CompoundTag tag) {
        return new S2CMetarPacket(tag);
    }
}
