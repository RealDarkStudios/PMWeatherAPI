package net.nullved.pmweatherapi.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.nullved.pmweatherapi.storage.IStorage;

/**
 * A {@link SavedData} instance for {@link IStorage}s
 *
 * @since 0.14.16.3
 */
public class PMWStorageSavedData extends SavedData {
    private final CompoundTag tag;
    private IStorage storage;

    /**
     * Gets the factory for loading the {@link SavedData}
     * @return A {@link SavedData.Factory} for {@link PMWStorageSavedData}
     * @since 0.14.16.3
     */
    public static SavedData.Factory<PMWStorageSavedData> factory() {
        return new SavedData.Factory<>(PMWStorageSavedData::new, PMWStorageSavedData::load, null);
    }

    public PMWStorageSavedData() {
        this.tag = new CompoundTag();
    }

    public PMWStorageSavedData(CompoundTag tag) {
        this.tag = tag;
    }

    private static PMWStorageSavedData load(CompoundTag compoundTag, HolderLookup.Provider registries) {
        return new PMWStorageSavedData(compoundTag);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return storage.save(compoundTag);
    }

    /**
     * Sets the {@link IStorage} associated with this {@link Level} or dimension
     * @param storage The {@link IStorage}
     * @since 0.14.16.3
     */
    public void setStorage(IStorage storage) {
        this.storage = storage;
    }

    /**
     * Gets the {@link SavedData} from the {@link Level}
     * @return A data {@link CompoundTag}
     * @since 0.14.16.3
     */
    public CompoundTag getTag() {
        return tag;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
