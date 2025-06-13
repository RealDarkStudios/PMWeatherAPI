package net.nullved.pmweatherapi.radar;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.nullved.pmweatherapi.network.PMWNetworking;
import net.nullved.pmweatherapi.data.PMWStorages;

import java.util.Collection;

/**
 * A Radar Storage for the server side.
 * There is a {@code RadarServerStorage} for each dimension of a save
 * <br>
 * You should not create a {@code RadarServerStorage}, instead, use {@link PMWStorages#getRadar}.
 * @since 0.14.15.3
 */
public class RadarServerStorage extends RadarStorage {
    private final ServerLevel level;

    /**
     * <strong>DO NOT CALL THIS CONSTRUCTOR!!!</strong>
     * <br>
     * Get a radar storage from {@link PMWStorages#getRadar}
     * @param level The level to create this storage for
     * @since 0.14.15.3
     */
    public RadarServerStorage(ServerLevel level) {
        super(level.dimension());
        this.level = level;
    }

    /**
     * Gets the level associated with this {@code RadarServerStorage}
     * @return A {@link ServerLevel}
     * @since 0.14.15.3
     */
    @Override
    public ServerLevel getLevel() {
        return level;
    }

    /**
     * Syncs all radars to the given player
     * @param player The {@link Player} to sync all radars to
     * @since 0.14.15.3
     */
    public void syncAllToPlayer(Player player) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "add");
        tag.putBoolean("list", true);

        ListTag list = new ListTag();
        for (BlockPos pos: getAllRadars()) {
            list.add(NbtUtils.writeBlockPos(pos));
        }

        tag.put("data", list);

        PMWNetworking.serverSendRadarsToPlayer(tag, player);
    }

    /**
     * Syncs a new radar to all clients
     * @param pos The {@link BlockPos} of the new radar
     * @since 0.14.15.3
     */
    public void syncAdd(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "add");
        tag.put("data", NbtUtils.writeBlockPos(pos));

        PMWNetworking.serverSendRadarsToAll(tag);
    }

    /**
     * Syncs multiple new radars to all clients
     * @param posList A {@link Collection} of {@link BlockPos} to sync
     * @since 0.14.15.3
     */
    public void syncAdd(Collection<BlockPos> posList) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "add");
        tag.putBoolean("list", true);

        ListTag list = new ListTag();
        for (BlockPos pos: posList) {
            list.add(NbtUtils.writeBlockPos(pos));
        }

        tag.put("data", list);

        PMWNetworking.serverSendRadarsToAll(tag);
    }

    /**
     * Syncs a radar removal to all clients
     * @param pos The {@link BlockPos} of the radar to remove
     * @since 0.14.15.3
     */
    public void syncRemove(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "remove");
        tag.put("data", NbtUtils.writeBlockPos(pos));

        PMWNetworking.serverSendRadarsToAll(tag);
    }

    /**
     * Syncs multiple radar removals to all clients
     * @param posList A {@link Collection} of {@link BlockPos} to sync
     * @since 0.14.15.3
     */
    public void syncRemove(Collection<BlockPos> posList) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "remove");
        tag.putBoolean("list", true);

        ListTag list = new ListTag();
        for (BlockPos pos: posList) {
            list.add(NbtUtils.writeBlockPos(pos));
        }

        tag.put("data", list);

        PMWNetworking.serverSendRadarsToAll(tag);
    }
}
