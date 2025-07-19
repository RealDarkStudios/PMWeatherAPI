package net.nullved.pmweatherapi.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.storage.IStorage;

/**
 * A base packet for the Storages system that syncs data from the Server -> Client (S2C)
 * @param <T> The {@link IClientStorage} that will be synced to
 * @since 0.14.16.3
 */
public abstract class S2CStoragePacket<T extends IClientStorage> implements CustomPacketPayload {
    public CompoundTag tag;

    /**
     * Gets the {@link IClientStorage} that will be synced to.
     * @return The storage to be synced.
     * @since 0.14.16.3
     */
    public abstract T getStorage();

    public S2CStoragePacket(CompoundTag tag) {
        this.tag = tag;
    }

    /**
     * Creates a new {@link S2CStoragePacket} from a {@link RegistryFriendlyByteBuf}
     * @param buf The {@link RegistryFriendlyByteBuf} to read a {@link CompoundTag} from
     * @since 0.14.16.3
     */
    public S2CStoragePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    /**
     * Gets the {@link CompoundTag} send with the packet
     * @return The packet data
     * @since 0.14.16.3
     */
    public CompoundTag tag() {
        return tag;
    }

    /**
     * Writes the data into the given {@link FriendlyByteBuf}
     * @param buf The {@link FriendlyByteBuf} to write into
     * @since 0.14.16.3
     */
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    /**
     * Handles the packet on the <strong>CLIENT SIDE</strong>
     * @param player The player the packet was sent to
     * @since 0.14.16.3
     */
    public void handle(Player player) {
        try {
            String operation = tag.getString("operation");
            T storage = getStorage();

            if (operation.equals("overwrite")) {
                storage.syncAll(tag);
            } else if (operation.equals("add")) {
                storage.syncAdd(tag);
            } else if (operation.equals("remove")) {
                storage.syncRemove(tag);
            } else {
                PMWeatherAPI.LOGGER.error("Unknown S2CRadarsPacket operation: {}", operation);
            }
        } catch (Exception e) {
            PMWeatherAPI.LOGGER.error("An error occurred when trying to write packet", e);
        }
    }
}
