package net.nullved.pmweatherapi.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.client.metar.MetarClientStorage;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;

/**
 * The packet that syncs metars from the server to the client, using the Storages system
 * @since 0.15.3.3
 */
public class S2CMetarPacket extends S2CStoragePacket<MetarClientStorage> {
    public static final Type<S2CMetarPacket> TYPE = new Type<>(PMWeatherAPI.rl("s2c_metar"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMetarPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, S2CMetarPacket::tag, S2CMetarPacket::new);

    /**
     * Creates a new {@link S2CMetarPacket}
     * @param tag The {@link CompoundTag} to send with the packet
     * @since 0.15.3.3
     */
    public S2CMetarPacket(CompoundTag tag) {
        super(tag);
    }

    /**
     * Gets the {@link MetarClientStorage} that is receiving data
     * @return The {@link MetarClientStorage}
     * @since 0.15.3.3
     */
    @Override
    public MetarClientStorage getStorage() {
        return PMWClientStorages.metars().get();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
