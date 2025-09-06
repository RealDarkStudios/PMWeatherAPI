package net.nullved.pmweatherapi.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.nullved.pmweatherapi.radar.storage.RadarStorage;

/**
 * The {@link SavedData} for PMWeatherAPI
 * @since 0.14.15.3
 * @deprecated Since 0.15.3.3 | For removal in 0.16.0.0 | Using new Storages system
 */
@Deprecated(since = "0.15.3.3", forRemoval = true)
public class PMWSavedData extends SavedData {
    private CompoundTag tag;
    private RadarStorage radarStorage;

    /**
     * Gets the factory for loading the saved data
     * @return A {@link SavedData.Factory} for {@link PMWSavedData}
     * @since 0.14.15.3
     */
    public static SavedData.Factory<PMWSavedData> factory() {
        return new SavedData.Factory<>(PMWSavedData::new, PMWSavedData::load, null);
    }

    public PMWSavedData() {
        this.tag = new CompoundTag();
    }

    public PMWSavedData(CompoundTag tag) {
        this.tag = tag;
    }

    private static PMWSavedData load(CompoundTag compoundTag, HolderLookup.Provider registries) {
        return new PMWSavedData(compoundTag);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return radarStorage.save(compoundTag);
    }

    /**
     * Sets the radar storage associated with this level
     * @param radarStorage The associated level's {@link RadarStorage}
     * @since 0.14.15.3
     */
    public void setRadarStorage(RadarStorage radarStorage) {
        this.radarStorage = radarStorage;
    }

    /**
     * Gets the data loaded from the level
     * @since 0.14.15.3
     */
    public CompoundTag getTag() {
        return tag;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
