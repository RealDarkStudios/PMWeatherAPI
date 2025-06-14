package net.nullved.pmweatherapi.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;

/**
 * A packet for sending radar information from Server -> Client (S2C)
 * @param tag The {@link CompoundTag} to send
 * @since 0.14.15.3
 */
public record S2CRadarsPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CRadarsPacket> TYPE = new CustomPacketPayload.Type<>(PMWeatherAPI.rl("s2c_radars"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CRadarsPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, S2CRadarsPacket::tag, S2CRadarsPacket::new);

    /**
     * Creates a new {@link S2CRadarsPacket} from a {@link RegistryFriendlyByteBuf}
     * @param buf The {@link RegistryFriendlyByteBuf} to read a {@link CompoundTag} from
     * @since 0.14.15.3
     */
    public S2CRadarsPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    /**
     * Writes the data into the given {@link FriendlyByteBuf}
     * @param buf The {@link FriendlyByteBuf} to write into
     * @since 0.14.15.3
     */
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    /**
     * Handles the packet on the <strong>CLIENT SIDE</strong>
     * @param player The player the packet was sent to
     * @since 0.14.15.3
     */
    public void handle(Player player) {
        try {
            String operation = tag.getString("operation");
            RadarClientStorage radarStorage = PMWClientStorages.getRadars();

            if (operation.equals("add")) {
                radarStorage.syncAdd(tag);
            } else if (operation.equals("remove")) {
                radarStorage.syncRemove(tag);
            } else {
                PMWeatherAPI.LOGGER.error("Unknown S2CRadarsPacket operation: {}", operation);
            }
        } catch (Exception e) {
            PMWeatherAPI.LOGGER.error("An error occurred when trying to write packet", e);
        }
    }

    /**
     * Gets the packet type
     * @return The packet type
     * @since 0.14.15.3
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
