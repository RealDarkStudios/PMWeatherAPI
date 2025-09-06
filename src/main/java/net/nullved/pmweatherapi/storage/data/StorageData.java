package net.nullved.pmweatherapi.storage.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.metar.MetarStorageData;
import net.nullved.pmweatherapi.radar.storage.RadarStorageData;
import net.nullved.pmweatherapi.radar.storage.WSRStorageData;
import net.nullved.pmweatherapi.storage.IServerStorage;
import net.nullved.pmweatherapi.storage.IStorage;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A basic {@link IStorageData} implementation that stores and handles a {@link BlockPos} by default.
 * <br><br>
 * By extending this class, you can add additional data that will be saved as part of the "Storages" system.
 * <br><br>
 * To register your data for use, you must first register it with {@link StorageDataManager#register(ResourceLocation, BiFunction)}
 * <br><br>
 * When serializing, you should get the {@link CompoundTag} from calling {@link StorageData#serializeToNBT()}.
 * This makes sure that you have both the type and blockpos saved.
 * <br><br>
 * When deserializing, you can call {@link #deserializeBlockPos(CompoundTag)} to automatically read the saved {@link BlockPos}
 * <br><br>
 * For some example implementations, view {@link RadarStorageData}, {@link MetarStorageData}, and {@link WSRStorageData}
 *
 * @since 0.15.3.3
 * @see IStorageData
 * @see StorageDataManager
 * @see IStorage
 * @see IServerStorage
 * @see IClientStorage
 */
public abstract class StorageData implements IStorageData {
    protected final BlockPos pos;

    public StorageData(BlockPos pos) {
        this.pos = pos;
    }

    /**
     * Get the position saved in this data
     * @return A {@link BlockPos}
     * @since 0.15.3.3
     */
    @Override
    public BlockPos getPos() {
        return pos;
    }

    /**
     * Serialize this storage data to NBT
     * @return A {@link CompoundTag} of the serialized data
     * @since 0.15.3.3
     */
    @Override
    public CompoundTag serializeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", getId().toString());
        tag.put("blockpos", NbtUtils.writeBlockPos(pos));
        return tag;
    }

    /**
     * Deserialize a {@link BlockPos} from a {@link CompoundTag}
     * @param tag The {@link CompoundTag} to get the {@link BlockPos} from
     * @return The found {@link BlockPos}, or {@code null}
     * @since 0.15.3.3
     */
    public static BlockPos deserializeBlockPos(CompoundTag tag) {
        Optional<BlockPos> bp = NbtUtils.readBlockPos(tag, "blockpos");
        if (bp.isPresent()) return bp.get();
        else PMWeatherAPI.LOGGER.warn("Could not deserialize BlockPos! Tag: {}", NbtUtils.toPrettyComponent(tag));
        return null;
    }

    /**
     * Shorthand for {@link StorageDataManager#get(CompoundTag, int)}
     * @param tag The {@link CompoundTag}
     * @param version The version of the data
     * @return An {@link IStorageData} instance
     * @param <D> The type of {@link IStorageData}
     * @since 0.15.3.3
     */
    public static <D extends IStorageData> D deserializeFromNBT(CompoundTag tag, int version) {
        return StorageDataManager.get(tag, version);
    }
}
