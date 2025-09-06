package net.nullved.pmweatherapi.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.client.metar.MetarClientStorage;
import net.nullved.pmweatherapi.client.radar.WSRClientStorage;

/**
 * The packet that syncs wsrs from the server to the client, using the Storages system
 * @since 0.15.3.3
 */
public class S2CWSRPacket extends S2CStoragePacket<WSRClientStorage> {
    public static final Type<S2CWSRPacket> TYPE = new Type<>(PMWeatherAPI.rl("s2c_wsr"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CWSRPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, S2CWSRPacket::tag, S2CWSRPacket::new);

    /**
     * Creates a new {@link S2CWSRPacket}
     * @param tag The {@link CompoundTag} to send with the packet
     * @since 0.15.3.3
     */
    public S2CWSRPacket(CompoundTag tag) {
        super(tag);
    }

    /**
     * Gets the {@link WSRClientStorage} that is receiving data
     * @return The {@link WSRClientStorage}
     * @since 0.15.3.3
     */
    @Override
    public WSRClientStorage getStorage() {
        return PMWClientStorages.wsrs().get();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
