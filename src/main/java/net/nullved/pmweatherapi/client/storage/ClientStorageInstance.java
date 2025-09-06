package net.nullved.pmweatherapi.client.storage;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.storage.StorageInstance;
import net.nullved.pmweatherapi.storage.data.IStorageData;
import net.nullved.pmweatherapi.storage.data.StorageData;

import java.util.Optional;
import java.util.function.Function;

/**
 * A client version of {@link StorageInstance}s, but only holds one {@link IClientStorage}
 * <br><br>
 * You should not create {@link ClientStorageInstance}s yourself.
 * Instead, get them from {@link PMWClientStorages#get}
 *
 * @param <D> The {@link IStorageData} of the {@link IClientStorage}
 * @param <C> The {@link IClientStorage}
 * @since 0.15.3.3
 */
public class ClientStorageInstance<D extends StorageData, C extends IClientStorage<D>> {
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

    public <F extends StorageData, O extends IClientStorage<F>> Optional<ClientStorageInstance<F, O>> cast(Class<O> oclazz) {
        if (oclazz.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            ClientStorageInstance<F, O> casted = new ClientStorageInstance<>(id(), oclazz, cl -> (O) creator.apply(cl));

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
