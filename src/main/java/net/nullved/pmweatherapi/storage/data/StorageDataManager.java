package net.nullved.pmweatherapi.storage.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.nullved.pmweatherapi.PMWeatherAPI;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A manager for {@link IStorageData}.
 * <br><br>
 * To register a {@link IStorageData}, you must pass a {@link ResourceLocation} of the id, and the deserialization function
 *
 * @since 0.15.3.3
 * @see IStorageData
 * @see StorageData
 */
public class StorageDataManager {
    public static final HashMap<ResourceLocation, BiFunction<CompoundTag, Integer, ? extends IStorageData>> map = new HashMap<>();

    public static <D extends IStorageData> void register(ResourceLocation id, BiFunction<CompoundTag, Integer, D> deserializer) {
        map.put(id, deserializer);
    }

    public static <D extends IStorageData> D get(CompoundTag tag, int version) {
        try {
            //PMWeatherAPI.LOGGER.info("Getting storage data for type {}", tag.getString("type"));
            if (tag.getString("type").isEmpty()) {
                Optional<BlockPos> bp = NbtUtils.readBlockPos(tag, "blockpos");
                if (bp.isPresent()) return (D) new BlockPosData(bp.get());
                else throw new IllegalArgumentException("No type given and does not meet BlockPos criteria");
            } else return (D) map.get(ResourceLocation.parse(tag.getString("type"))).apply(tag, version);
        } catch (Exception e) {
            PMWeatherAPI.LOGGER.error("Could not deserialize tag {} of type {}: {}", NbtUtils.toPrettyComponent(tag), tag.get("type"), e.getMessage());
            return null;
        }
    }
}
