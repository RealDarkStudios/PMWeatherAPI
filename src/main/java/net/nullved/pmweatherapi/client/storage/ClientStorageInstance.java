package net.nullved.pmweatherapi.client.storage;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.data.IClientStorage;

import java.util.Optional;
import java.util.function.Function;

public class ClientStorageInstance<C extends IClientStorage> {
    private final ResourceLocation id;
    private final Class<C> clazz;
    private final Function<ClientLevel, C> creator;
    private C storage;

    public ClientStorageInstance(ResourceLocation id, Class<C> clazz, Function<ClientLevel, C> creator) {
        this.id = id;
        this.clazz = clazz;
        this.creator = creator;
    }

    public ResourceLocation id() {
        return id;
    }

    public C get() {
        return storage;
    }

    public void set(C storage) {
        this.storage = storage;
    }

    public <O extends IClientStorage> Optional<ClientStorageInstance<O>> cast(Class<O> oclazz) {
        if (oclazz.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            ClientStorageInstance<O> casted = new ClientStorageInstance<>(id(), oclazz, cl -> (O) creator.apply(cl));

            try {
                casted.set((O) storage);
                return Optional.of(casted);
            } catch (ClassCastException e) {
                PMWeatherAPI.LOGGER.error("Could not cast {} to {}", clazz.getSimpleName(), oclazz.getSimpleName());
                return Optional.empty();
            }
        } else return Optional.empty();
    }

    public void load(ClientLevel level) {
        storage = creator.apply(level);
    }

    public void clear() {
        storage = null;
    }
}
