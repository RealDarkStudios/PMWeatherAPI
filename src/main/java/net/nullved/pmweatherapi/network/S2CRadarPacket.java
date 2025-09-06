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
 * @since 0.15.3.3
 */
public class S2CRadarPacket extends S2CStoragePacket<RadarClientStorage> {
    public static final CustomPacketPayload.Type<S2CRadarPacket> TYPE = new Type<>(PMWeatherAPI.rl("s2c_radar"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CRadarPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, S2CRadarPacket::tag, S2CRadarPacket::new);

    /**
     * Creates a new {@link S2CRadarPacket}
     * @param tag The {@link CompoundTag} to send with the packet
     * @since 0.15.3.3
     */
    public S2CRadarPacket(CompoundTag tag) {
        super(tag);
    }

    /**
     * Gets the {@link RadarClientStorage} that is receiving data
     * @return The {@link RadarClientStorage}
     * @since 0.15.3.3
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
