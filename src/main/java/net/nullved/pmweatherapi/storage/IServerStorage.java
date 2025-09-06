package net.nullved.pmweatherapi.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.network.PMWNetworking;
import net.nullved.pmweatherapi.network.S2CStoragePacket;
import net.nullved.pmweatherapi.storage.data.IStorageData;
import net.nullved.pmweatherapi.storage.data.StorageData;

import java.util.Collection;

/**
 * The Server Storage interface.
 * There is a {@link IServerStorage} for each dimension of a save
 * <br>
 * You should only create a {@link IServerStorage} for a level once, instead, use {@link PMWStorages} if built-in or a custom storage handler.
 * @since 0.15.3.3
 */
public interface IServerStorage<D extends IStorageData> extends IStorage<D> {
    /**
     * Gets the level associated with this {@link IServerStorage}
     * @return A {@link ServerLevel}
     * @since 0.15.3.3
     */
    ServerLevel getLevel();

    /**
     * Generates a {@link S2CStoragePacket} to be sent to the client
     * @param tag The {@link CompoundTag} to be sent
     * @return A {@link S2CStoragePacket} instance
     * @since 0.15.3.3
     */
    S2CStoragePacket<? extends IClientStorage<D>> packet(CompoundTag tag);

    /**
     * Syncs all {@link IStorageData} to all players
     * @since 0.15.3.3
     */
    default void syncAllToAll() {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "overwrite");
        tag.putBoolean("list", true);

        ListTag list = new ListTag();
        getAll().forEach(data -> list.add(data.serializeToNBT()));

        tag.put("data", list);

        PMWNetworking.serverSendStorageToAll(tag, this::packet);
    }

    /**
     * Syncs all {@link IStorageData} from the storage to the given player
     * @param player The {@link Player} to sync all data to
     * @since 0.15.3.3
     */
    default void syncAllToPlayer(Player player) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "overwrite");
        tag.putBoolean("list", true);

        ListTag list = new ListTag();
        getAll().forEach(data -> list.add(data.serializeToNBT()));

        tag.put("data", list);

        PMWNetworking.serverSendStorageToPlayer(tag, this::packet, player);
    }

    /**
     * Syncs new {@link IStorageData} to all clients
     * @param data The new {@link IStorageData}
     * @since 0.15.3.3
     */
    default void syncAdd(D data) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "add");
        tag.put("data", data.serializeToNBT());

        PMWNetworking.serverSendStorageToAll(tag, this::packet);
    }

    /**
     * Syncs multiple new {@link IStorageData} to all clients
     * @param datum A {@link Collection} of {@link IStorageData} to sync
     * @since 0.15.3.3
     */
    default void syncAdd(Collection<D> datum) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "add");
        tag.putBoolean("list", true);

        ListTag list = new ListTag();
        datum.forEach(data -> list.add(data.serializeToNBT()));

        tag.put("data", list);

        PMWNetworking.serverSendStorageToAll(tag, this::packet);
    }

    /**
     * Syncs a {@link BlockPos} removal to all clients
     * @param pos The {@link BlockPos} of the radar to remove
     * @since 0.15.3.3
     */
    default void syncRemove(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "remove");
        tag.putString("format", "blockpos");
        tag.put("data", NbtUtils.writeBlockPos(pos));

        PMWNetworking.serverSendStorageToAll(tag, this::packet);
    }

    /**
     * Syncs multiple {@link BlockPos} removals to all clients
     * @param posList A {@link Collection} of {@link BlockPos} to sync
     * @since 0.15.3.3
     */
    default void syncRemoveByPos(Collection<BlockPos> posList) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "remove");
        tag.putString("format", "blockpos");
        tag.putBoolean("list", true);

        ListTag list = new ListTag();
        posList.forEach(pos -> list.remove(NbtUtils.writeBlockPos(pos)));

        tag.put("data", list);

        PMWNetworking.serverSendStorageToAll(tag, this::packet);
    }

    /**
     * Syncs a {@link IStorageData} removal to all clients
     * @param data The {@link IStorageData} of the radar to remove
     * @since 0.15.3.3
     */
    default void syncRemove(D data) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "remove");
        tag.put("data", data.serializeToNBT());

        PMWNetworking.serverSendStorageToAll(tag, this::packet);
    }

    /**
     * Syncs multiple {@link IStorageData} removals to all clients
     * @param datum A {@link Collection} of {@link IStorageData} to sync
     * @since 0.15.3.3
     */
    default void syncRemoveByData(Collection<D> datum) {
        CompoundTag tag = new CompoundTag();
        tag.putString("operation", "remove");
        tag.putBoolean("list", true);

        ListTag list = new ListTag();
        datum.forEach(data -> list.remove(data.serializeToNBT()));

        tag.put("data", list);

        PMWNetworking.serverSendStorageToAll(tag, this::packet);
    }
}
