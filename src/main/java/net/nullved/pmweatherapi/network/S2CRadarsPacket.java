package net.nullved.pmweatherapi.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;

/**
 * The packet that syncs radars from the server to the client, using the Storages system
 * @since 0.15.1.1
 */
public class S2CRadarsPacket extends S2CStoragePacket<RadarClientStorage> {
    public static final CustomPacketPayload.Type<S2CRadarsPacket> TYPE = new Type<>(PMWeatherAPI.rl("s2c_radars"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CRadarsPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, S2CRadarsPacket::tag, S2CRadarsPacket::new);

    /**
     * Creates a new {@link S2CRadarsPacket}
     * @param tag The {@link CompoundTag} to send with the packet
     * @since 0.15.1.1
     */
    public S2CRadarsPacket(CompoundTag tag) {
        super(tag);
    }

    /**
     * Gets the {@link RadarClientStorage} that is receiving data
     * @return The {@link RadarClientStorage}
     * @since 0.15.1.1
     */
    @Override
    public RadarClientStorage getStorage() {
        return PMWClientStorages.radars().get();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
