package net.nullved.pmweatherapi.radar.storage;

import dev.protomanly.pmweather.multiblock.wsr88d.WSR88DCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.client.radar.WSRClientStorage;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.network.S2CRadarPacket;
import net.nullved.pmweatherapi.network.S2CStoragePacket;
import net.nullved.pmweatherapi.network.S2CWSRPacket;
import net.nullved.pmweatherapi.storage.IServerStorage;
import net.nullved.pmweatherapi.storage.ISyncServerStorage;

/**
 * {@link IServerStorage} for {@link WSR88DCore}s
 * <br><br>
 * You should not create a {@link WSRServerStorage}, instead, use {@link PMWStorages#wsrs()}
 *
 * @since 0.15.3.3
 * @see WSRStorage
 * @see WSRClientStorage
 */
public class WSRServerStorage extends WSRStorage implements ISyncServerStorage<WSRStorageData> {
    private final ServerLevel level;

    /**
     * <strong>DO NOT CALL THIS CONSTRUCTOR!!!</strong>
     * <br>
     * Get a radar storage from {@link PMWStorages#wsrs()}
     * @param level The level to create this storage for
     * @since 0.15.3.3
     */
    public WSRServerStorage(ServerLevel level) {
        super(level.dimension());
        this.level = level;
    }

    @Override
    public ServerLevel getLevel() {
        return level;
    }

    @Override
    public S2CStoragePacket<? extends IClientStorage<WSRStorageData>> packet(CompoundTag tag) {
        return new S2CWSRPacket(tag);
    }
}
