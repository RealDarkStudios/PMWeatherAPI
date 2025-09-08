package net.nullved.pmweatherapi.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.metar.MetarClientStorage;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.client.radar.WSRClientStorage;
import net.nullved.pmweatherapi.client.storage.ClientStorageInstance;
import net.nullved.pmweatherapi.metar.MetarStorage;
import net.nullved.pmweatherapi.metar.MetarStorageData;
import net.nullved.pmweatherapi.radar.RadarMode;
import net.nullved.pmweatherapi.radar.storage.RadarStorage;
import net.nullved.pmweatherapi.radar.storage.WSRStorage;
import net.nullved.pmweatherapi.radar.storage.WSRStorageData;
import net.nullved.pmweatherapi.storage.data.IStorageData;
import net.nullved.pmweatherapi.storage.data.StorageData;
import net.nullved.pmweatherapi.radar.storage.RadarStorageData;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * A class holding the specific storage instances for the client
 * @since 0.14.15.3
 */
@OnlyIn(Dist.CLIENT)
public class PMWClientStorages {
    /**
     * A {@link Map} of {@link RadarMode}s to {@link Map}s of pixel ids and their {@link Color}
     * @since 0.14.15.6
     */
    public static Map<BlockPos, Map<RadarMode, Map<Integer, Color>>> RADAR_MODE_COLORS = new HashMap<>();

    public static final Map<ResourceLocation, ClientStorageInstance<?, ?>> STORAGE_INSTANCES = new HashMap<>();

    private static ClientLevel lastLevel;

    /**
     * Gets the {@link ClientStorageInstance} of the {@link RadarClientStorage}
     * @return The {@link ClientStorageInstance}
     * @since 0.14.15.3
     */
    public static ClientStorageInstance<RadarStorageData, RadarClientStorage> radars() {
        return get(RadarStorage.ID, RadarClientStorage.class).orElseThrow();
    }

    /**
     * Gets the {@link ClientStorageInstance} of the {@link MetarClientStorage}
     * @return The {@link ClientStorageInstance}
     * @since 0.15.3.3
     */
    public static ClientStorageInstance<MetarStorageData, MetarClientStorage> metars() {
        return get(MetarStorage.ID, MetarClientStorage.class).orElseThrow();
    }

    /**
     * Gets the {@link ClientStorageInstance} of the {@link WSRClientStorage}
     * @return The {@link ClientStorageInstance}
     * @since 0.15.3.3
     */
    public static ClientStorageInstance<WSRStorageData, WSRClientStorage> wsrs() {
        return get(WSRStorage.ID, WSRClientStorage.class).orElseThrow();
    }

    /**
     * Get a {@link ClientStorageInstance} for a given {@link ResourceLocation} ID
     * @param location The ID of the storage
     * @return A {@link ClientStorageInstance}
     * @since 0.15.3.3
     */
    public static ClientStorageInstance<?, ?> get(ResourceLocation location) {
        if (!STORAGE_INSTANCES.containsKey(location)) {
            PMWeatherAPI.LOGGER.error("No storage instance found for location {}", location);
        }

        ClientLevel curLevel = Minecraft.getInstance().level;
        if (curLevel != null && curLevel != lastLevel) {
            loadDimension(curLevel);
        }

        ClientStorageInstance<?, ?> csi = STORAGE_INSTANCES.get(location);
        if (csi.get() == null) {
            csi.load(curLevel);
        }

        return csi;
    }

    /**
     * Overwrite a {@link ClientStorageInstance}
     * @param location The ID {@link ResourceLocation}
     * @param instance The new {@link ClientStorageInstance}
     * @since 0.15.3.3
     */
    public static void set(ResourceLocation location, ClientStorageInstance<?, ?> instance) {
        STORAGE_INSTANCES.put(location, instance);
    }

    /**
     * Casts the {@link ClientStorageInstance} to the specified {@link IClientStorage} class after retrieval
     * @param location The ID {@link ResourceLocation}
     * @param clazz The {@link Class} of an {@link IClientStorage} to cast to
     * @return The casted {@link ClientStorageInstance}
     * @param <D> The {@link IStorageData} of the {@link IClientStorage}
     * @param <T> The {@link IClientStorage}
     * @since 0.15.3.3
     */
    public static <D extends IStorageData, T extends IClientStorage<D>> Optional<ClientStorageInstance<D, T>> get(ResourceLocation location, Class<T> clazz) {
        return get(location).cast(clazz);
    }

    /**
     * Gets all {@link ClientStorageInstance}s
     * @return A {@link Collection} of all {@link ClientStorageInstance}s
     * @since 0.15.3.3
     */
    public static Collection<? extends ClientStorageInstance<?, ?>> getAll() {
        return STORAGE_INSTANCES.values();
    }

    /**
     * Resets all data for all {@link ClientStorageInstance}s
     * @since 0.15.3.3
     */
    public static void resetAll() {
        getAll().forEach(ClientStorageInstance::clear);
    }

    /**
     * Loads a new {@link ClientLevel} for all {@link ClientStorageInstance}s
     * @param clientLevel The new {@link ClientLevel} to load
     * @since 0.15.3.3
     */
    public static void loadDimension(ClientLevel clientLevel) {
        lastLevel = clientLevel;
        STORAGE_INSTANCES.forEach((location, instance) -> instance.load(clientLevel));
    }

    /**
     * Register a new {@link IClientStorage}
     * @param id The {@link ResourceLocation} to save this {@link IClientStorage} as
     * @param clazz The {@link Class} of the {@link IClientStorage}
     * @param creator A function creating another {@link IClientStorage} for the given {@link ClientLevel}
     * @param <D> The {@link IStorageData} of the {@link IClientStorage}
     * @param <C> The {@link IClientStorage}
     * @since 0.15.3.3
     */
    public static <D extends IStorageData, C extends IClientStorage<D>> void registerStorage(ResourceLocation id, Class<C> clazz, Function<ClientLevel, C> creator) {
        ClientStorageInstance<D, C> instance = new ClientStorageInstance<>(id, clazz, creator);
        STORAGE_INSTANCES.put(id, instance);
    }
}
