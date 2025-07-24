package net.nullved.pmweatherapi.data;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.radar.RadarServerStorage;
import net.nullved.pmweatherapi.radar.RadarStorage;
import net.nullved.pmweatherapi.storage.IServerStorage;
import net.nullved.pmweatherapi.storage.StorageInstance;

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
    public static final Map<ResourceLocation, StorageInstance<?>> STORAGE_INSTANCES = new HashMap<>();


    /**
     * Gets the map of {@link RadarServerStorage}s
     * @return The associated dimension's {@link RadarServerStorage}
     * @since 0.14.15.3
     */
    public static StorageInstance<RadarServerStorage> radars() {
        return get(RadarStorage.ID, RadarServerStorage.class).orElseThrow();
    }

    public static StorageInstance<?> get(ResourceLocation location) {
        if (!STORAGE_INSTANCES.containsKey(location)) {
            PMWeatherAPI.LOGGER.error("No storage instance found for location {}", location);
        }
        return STORAGE_INSTANCES.get(location);
    }

    public static void set(ResourceLocation location, StorageInstance<?> instance) {
        STORAGE_INSTANCES.put(location, instance);
    }

    public static <T extends IServerStorage> Optional<StorageInstance<T>> get(ResourceLocation location, Class<T> clazz) {
        return STORAGE_INSTANCES.get(location).cast(clazz);
    }

    public static Collection<? extends StorageInstance<?>> getAll() {
        return STORAGE_INSTANCES.values();
    }

    public static Collection<? extends IServerStorage> getForDimension(ResourceKey<Level> dimension) {
        return getAll().stream().map(si -> si.get(dimension)).toList();
    }

    public static void generateForDimension(ServerLevel dimension) {
        STORAGE_INSTANCES.forEach((rl, si) -> si.load(dimension));
    }

    public static void removeForDimension(ResourceKey<Level> dimension) {
        STORAGE_INSTANCES.forEach((rl, si) -> si.remove(dimension));
    }

    public static <S extends IServerStorage> void registerStorage(ResourceLocation id, Class<S> clazz, Function<ServerLevel, S> creator) {
        StorageInstance<S> instance = new StorageInstance<>(id, clazz, creator);
        STORAGE_INSTANCES.put(id, instance);
    }
}
