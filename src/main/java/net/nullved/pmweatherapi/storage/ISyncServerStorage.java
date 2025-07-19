package net.nullved.pmweatherapi.storage;

import net.minecraft.core.BlockPos;

import java.util.Collection;

public interface ISyncServerStorage extends IServerStorage {
    default void addAndSync(BlockPos pos) {
        this.add(pos);
        this.syncAdd(pos);
    }

    default void addAndSync(Collection<BlockPos> pos) {
        this.add(pos);
        this.syncAdd(pos);
    }

    default void removeAndSync(BlockPos pos) {
        this.remove(pos);
        this.syncRemove(pos);
    }

    default void removeAndSync(Collection<BlockPos> pos) {
        this.remove(pos);
        this.syncRemove(pos);
    }
}
