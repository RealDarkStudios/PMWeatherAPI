package net.nullved.pmweatherapi.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.client.storage.ClientStorageInstance;
import net.nullved.pmweatherapi.radar.RadarMode;
import net.nullved.pmweatherapi.radar.RadarStorage;

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

    public static final Map<ResourceLocation, ClientStorageInstance<?>> STORAGE_INSTANCES = new HashMap<>();

    private static ClientLevel lastLevel;

    public static ClientStorageInstance<RadarClientStorage> radars() {
        return get(RadarStorage.ID, RadarClientStorage.class).orElseThrow();
    }

    public static ClientStorageInstance<?> get(ResourceLocation location) {
        if (!STORAGE_INSTANCES.containsKey(location)) {
            PMWeatherAPI.LOGGER.error("No storage instance found for location {}", location);
        }

        ClientLevel curLevel = Minecraft.getInstance().level;
        if (curLevel != null && curLevel != lastLevel) {
            loadNewLevel(curLevel);
        }

        ClientStorageInstance<?> csi = STORAGE_INSTANCES.get(location);
        if (csi.get() == null) {
            csi.load(curLevel);
        }

        return csi;
    }

    public static void set(ResourceLocation location, ClientStorageInstance<?> instance) {
        STORAGE_INSTANCES.put(location, instance);
    }

    public static <T extends IClientStorage> Optional<ClientStorageInstance<T>> get(ResourceLocation location, Class<T> clazz) {
        return get(location).cast(clazz);
    }

    public static Collection<? extends ClientStorageInstance<?>> getAll() {
        return STORAGE_INSTANCES.values();
    }

    public static void resetAll() {
        STORAGE_INSTANCES.forEach((location, instance) -> instance.clear());
    }

    public static void loadNewLevel(ClientLevel level) {
        lastLevel = level;
        STORAGE_INSTANCES.forEach((location, instance) -> instance.load(level));
    }

    public static <C extends IClientStorage> void registerStorage(ResourceLocation id, Class<C> clazz, Function<ClientLevel, C> creator) {
        ClientStorageInstance<C> instance = new ClientStorageInstance<>(id, clazz, creator);
        STORAGE_INSTANCES.put(id, instance);
    }
}
