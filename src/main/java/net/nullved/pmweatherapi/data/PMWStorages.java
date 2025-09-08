package net.nullved.pmweatherapi.data;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.metar.MetarServerStorage;
import net.nullved.pmweatherapi.metar.MetarStorage;
import net.nullved.pmweatherapi.metar.MetarStorageData;
import net.nullved.pmweatherapi.radar.storage.*;
import net.nullved.pmweatherapi.storage.IServerStorage;
import net.nullved.pmweatherapi.storage.StorageInstance;
import net.nullved.pmweatherapi.storage.data.IStorageData;
import net.nullved.pmweatherapi.storage.data.StorageData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * A class holding maps of dimensions to different storages
 * @since 0.14.15.3
 */
public class PMWStorages {
    public static final Map<ResourceLocation, StorageInstance<?, ?>> STORAGE_INSTANCES = new HashMap<>();

    /**
     * Gets the map of {@link RadarServerStorage}s
     * @return The associated dimension's {@link RadarServerStorage}
     * @since 0.14.15.3
     */
    public static StorageInstance<RadarStorageData, RadarServerStorage> radars() {
        return get(RadarStorage.ID, RadarServerStorage.class).orElseThrow();
    }

    /**
     * Gets the map of {@link MetarServerStorage}s
     * @return The associated dimension's {@link MetarServerStorage}
     * @since 0.15.3.3
     */
    public static StorageInstance<MetarStorageData, MetarServerStorage> metars() {
        return get(MetarStorage.ID, MetarServerStorage.class).orElseThrow();
    }

    /**
     * Gets the map of {@link WSRServerStorage}s
     * @return The associated dimension's {@link WSRServerStorage}
     * @since 0.15.3.3
     */
    public static StorageInstance<WSRStorageData, WSRServerStorage> wsrs() {
        return get(WSRStorage.ID, WSRServerStorage.class).orElseThrow();
    }

    /**
     * Get a {@link StorageInstance} for a given {@link ResourceLocation} ID
     * @param location The ID of the storage
     * @return A {@link StorageInstance}
     * @since 0.15.3.3
     */
    public static StorageInstance<?, ?> get(ResourceLocation location) {
        if (!STORAGE_INSTANCES.containsKey(location)) {
            PMWeatherAPI.LOGGER.error("No storage instance found for location {}", location);
        }
        return STORAGE_INSTANCES.get(location);
    }

    /**
     * Overwrite a {@link StorageInstance}
     * @param location The ID {@link ResourceLocation}
     * @param instance The new {@link StorageInstance}
     * @since 0.15.3.3
     */
    public static void set(ResourceLocation location, StorageInstance<?, ?> instance) {
        STORAGE_INSTANCES.put(location, instance);
    }

    /**
     * Casts the {@link StorageInstance} to the specified {@link IServerStorage} class after retrieval
     * @param location The ID {@link ResourceLocation}
     * @param clazz The {@link Class} of an {@link IServerStorage} to cast to
     * @return The casted {@link StorageInstance}
     * @param <D> The {@link IStorageData} of the {@link IServerStorage}
     * @param <T> The {@link IServerStorage}
     * @since 0.15.3.3
     */
    public static <D extends IStorageData, T extends IServerStorage<D>> Optional<StorageInstance<D, T>> get(ResourceLocation location, Class<T> clazz) {
        return STORAGE_INSTANCES.get(location).cast(clazz);
    }

    /**
     * Resets all data for all {@link StorageInstance}s
     * @since 0.15.3.3
     */
    public static void resetAll() {
        getAll().forEach(StorageInstance::clear);
    }

    /**
     * Gets all {@link StorageInstance}s
     * @return A {@link Collection} of all {@link StorageInstance}s
     * @since 0.15.3.3
     */
    public static Collection<? extends StorageInstance<?, ?>> getAll() {
        return STORAGE_INSTANCES.values();
    }

    /**
     * Gets all {@link IServerStorage}s for a given dimension
     * @param dimension The {@link ResourceKey} of the dimension to get
     * @return A {@link Collection} of {@link IServerStorage} for the given dimension
     * @since 0.15.3.3
     */
    public static Collection<? extends IServerStorage<?>> getForDimension(ResourceKey<Level> dimension) {
        return getAll().stream().map(si -> si.get(dimension)).toList();
    }

    /**
     * Loads a new {@link ServerLevel} for all {@link StorageInstance}s
     * @param serverLevel The new {@link ServerLevel} to load
     * @since 0.15.3.3
     */
    public static void loadDimension(ServerLevel serverLevel) {
        STORAGE_INSTANCES.forEach((rl, si) -> si.load(serverLevel));
    }

    /**
     * Removes a dimension from all {@link StorageInstance}s
     * @param dimension The {@link ResourceKey} of the dimension to remove
     * @since 0.15.3.3
     */
    public static void removeDimension(ResourceKey<Level> dimension) {
        STORAGE_INSTANCES.forEach((rl, si) -> si.remove(dimension));
    }

    /**
     * Register a new {@link IServerStorage}
     * @param id The {@link ResourceLocation} to save this {@link IServerStorage} as
     * @param clazz The {@link Class} of the {@link IServerStorage}
     * @param creator A function creating another {@link IServerStorage} for the given {@link ServerLevel}
     * @param <D> The {@link IStorageData} of the {@link IServerStorage}
     * @param <S> The {@link IServerStorage}
     * @since 0.15.3.3
     */
    public static <D extends StorageData, S extends IServerStorage<D>> void registerStorage(ResourceLocation id, Class<S> clazz, Function<ServerLevel, S> creator) {
        StorageInstance<D, S> instance = new StorageInstance<>(id, clazz, creator);
        STORAGE_INSTANCES.put(id, instance);
    }
}
