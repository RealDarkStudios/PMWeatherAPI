package net.nullved.pmweatherapi.storage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.PMWeatherAPI;

import java.util.*;
import java.util.function.Function;

public class StorageInstance<S extends IServerStorage> {
    private final ResourceLocation id;
    private final HashMap<ResourceKey<Level>, S> map = new HashMap<>();
    private final Function<ServerLevel, S> creator;

    public StorageInstance(ResourceLocation id, Function<ServerLevel, S> creator) {
        this.id = id;
        this.creator = creator;
    }

    public ResourceLocation id() {
        return id;
    }

    public HashMap<ResourceKey<Level>, S> getBackingMap() {
        return map;
    }

    public void clear() {
        this.map.clear();
    }

    public Set<ResourceKey<Level>> keySet() {
        return map.keySet();
    }

    public Collection<S> values() {
        return map.values();
    }

    public Set<Map.Entry<ResourceKey<Level>, S>> entrySet() {
        return map.entrySet();
    }

    public S get(ResourceKey<Level> dimension) {
        return map.get(dimension);
    }

    public S getOrCreate(ServerLevel level) {
        return map.computeIfAbsent(level.dimension(), dim -> creator.apply(level));
    }

    public <O extends IServerStorage> Optional<StorageInstance<O>> cast(Class<O> clazz) {
        if (map.isEmpty()) return Optional.of(new StorageInstance<>(id(), sl ->  (O) creator.apply(sl)));

        S val = map.values().stream().findFirst().orElseThrow();
        if (clazz.isAssignableFrom(val.getClass())) {
            // almost unevitable to
            @SuppressWarnings("unchecked")
            StorageInstance<O> casted = new StorageInstance<>(id(), sl -> (O) creator.apply(sl));
            HashMap<ResourceKey<Level>, O> backingMap = casted.getBackingMap();

            try {

                for (Map.Entry<ResourceKey<Level>, S> entry : this.map.entrySet()) {
                    backingMap.put(entry.getKey(), clazz.cast(entry.getValue()));
                }

                return Optional.of(casted);
            } catch (ClassCastException e) {
                PMWeatherAPI.LOGGER.error("Could not cast {} to {}", val.getClass().getSimpleName(), clazz.getSimpleName());
                return Optional.empty();
            }
        } else return Optional.empty();
    }

    public void load(ServerLevel level) {
        S storage = creator.apply(level);
        storage.read();
        map.put(level.dimension(), storage);
    }

    public void remove(ResourceKey<Level> dimension) {
        S storage = map.get(dimension);
        map.remove(dimension);
    }
}
