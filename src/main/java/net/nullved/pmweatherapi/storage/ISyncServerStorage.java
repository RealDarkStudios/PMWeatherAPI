package net.nullved.pmweatherapi.storage;

import net.minecraft.core.BlockPos;
import net.nullved.pmweatherapi.storage.data.IStorageData;
import net.nullved.pmweatherapi.storage.data.StorageData;

import java.util.Collection;

public interface ISyncServerStorage<D extends IStorageData> extends IServerStorage<D> {
    /**
     * Shorthand for calling {@link #add(IStorageData)} and {@link #syncAdd(IStorageData)}
     * @param data The {@link IStorageData} to add and sync
     * @since 0.15.3.3
     */
    default void addAndSync(D data) {
        this.add(data);
        this.syncAdd(data);
    }

    /**
     * Shorthand for calling {@link #add(Collection)} and {@link #syncAdd(Collection)}
     * @param datum The {@link Collection} of {@link IStorageData} to add and sync
     * @since 0.15.3.3
     */
    default void addAndSync(Collection<D> datum) {
        this.add(datum);
        this.syncAdd(datum);
    }

    /**
     * Shorthand for calling {@link #remove(BlockPos)} and {@link #remove(BlockPos)}
     * @param pos The {@link BlockPos} to remove and sync
     * @since 0.15.3.3
     */
    default void removeAndSync(BlockPos pos) {
        this.remove(pos);
        this.syncRemove(pos);
    }

    /**
     * Shorthand for calling {@link #removeByPos(Collection)} and {@link #syncRemoveByPos(Collection)}
     * @param pos The {@link Collection} of {@link BlockPos} to remove and sync
     * @since 0.15.3.3
     */
    default void removeAndSyncByPos(Collection<BlockPos> pos) {
        this.removeByPos(pos);
        this.syncRemoveByPos(pos);
    }

    /**
     * Shorthand for calling {@link #remove(IStorageData)} and {@link #syncRemove(IStorageData)}
     * @param data The {@link IStorageData} to remove and sync
     * @since 0.15.3.3
     */
    default void removeAndSync(D data) {
        this.remove(data);
        this.syncRemove(data);
    }

    /**
     * Shorthand for calling {@link #removeByData(Collection)} and {@link #syncRemoveByData(Collection)}
     * @param datum The {@link Collection} of {@link IStorageData} to remove and sync
     * @since 0.15.3.3
     */
    default void removeAndSyncByData(Collection<D> datum) {
        this.removeByData(datum);
        this.syncRemoveByData(datum);
    }
}
