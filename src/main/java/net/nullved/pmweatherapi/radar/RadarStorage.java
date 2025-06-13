package net.nullved.pmweatherapi.radar;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.data.PMWSavedData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Saves all the radars to a file to be saved and loaded from
 */
public abstract class RadarStorage {
    private Map<ChunkPos, Set<BlockPos>> radars = new HashMap<>();
    private Map<ChunkPos, Long> checkTimes = new HashMap<>();
    private ResourceKey<Level> dimension;

    public abstract Level getLevel();

    public RadarStorage(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public Set<BlockPos> getAllRadars() {
        return radars.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public Set<BlockPos> getRadarsInChunk(ChunkPos pos) {
        return radars.getOrDefault(pos, Set.of());
    }

    public boolean shouldRecalculate(ChunkPos pos) {
        if (!checkTimes.containsKey(pos)) {
            checkTimes.put(pos, System.currentTimeMillis());
            return true;
        }

        return checkTimes.get(pos) - System.currentTimeMillis() > 30000L;
    }

    public void addRadar(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> set = radars.computeIfAbsent(chunkPos, c -> new HashSet<>());
        set.add(pos);
        radars.put(chunkPos, set);
    }

    public void addRadars(Collection<BlockPos> pos) {
        pos.forEach(this::addRadar);
    }

    public void removeRadar(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> set = radars.computeIfAbsent(chunkPos, c -> new HashSet<>());
        set.remove(pos);
        radars.put(chunkPos, set);
    }

    public void removeRadars(Collection<BlockPos> pos) {
        pos.forEach(this::removeRadar);
    }

    public CompoundTag save(CompoundTag tag) {
        PMWeatherAPI.LOGGER.info("Saving radars to level...");
        tag.putInt("version", 1);
        tag.putLong("saveTime", System.currentTimeMillis());

        for (Map.Entry<ChunkPos, Set<BlockPos>> entry : radars.entrySet()) {
            ListTag blockList = new ListTag();
            entry.getValue().forEach(blockPos -> blockList.add(NbtUtils.writeBlockPos(blockPos)));
            tag.put(String.valueOf(entry.getKey().toLong()), blockList);
        }

        PMWeatherAPI.LOGGER.info("Saved radars to level");
        return tag;
    }

    public void read() {
        PMWSavedData savedData = ((ServerLevel) this.getLevel()).getDataStorage().computeIfAbsent(PMWSavedData.factory(), "pmweatherapi_radars");
        savedData.setRadarStorage(this);
        PMWeatherAPI.LOGGER.info("Reading radars from level...");
        CompoundTag data = savedData.getTag();
        Set<String> chunks = data.getAllKeys();
        chunks.removeAll(Set.of("version", "saveTime"));

        for (String chunk: chunks) {
            Set<BlockPos> radars = new HashSet<>();

            ListTag blockList = (ListTag) data.get(chunk);
            for (int i = 0; i < blockList.size(); i++) {
                int[] bp = blockList.getIntArray(i);
                radars.add(new BlockPos(bp[0], bp[1], bp[2]));
            }

            this.radars.put(new ChunkPos(Long.parseLong(chunk)), radars);
        }
    }

//    private static final String DATA_FOLDER = "data/pmweatherapi";
//    private static final String DATA_FILE = "radars.json";
//    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
//
//    /**
//     * Reads all radars from a save or server
//     * <br>
//     * For each dimension ({@link ResourceKey<Level>}), there is a {@link Map} mapping a {@link ChunkPos} to all the saved radar's {@link BlockPos}'s
//     * @return All saved radars across all dimensions
//     */
//    public static Map<ResourceKey<Level>, Map<ChunkPos, Set<BlockPos>>> readAllRadars() {
//        Radars.clear();
//        Map<ResourceKey<Level>, Map<ChunkPos, Set<BlockPos>>> dimensionToRadar = new HashMap<>();
//
//        try {
//            Path worldDir = getWorldSaveDirectory();
//            Path modDir = worldDir.resolve(DATA_FOLDER);
//            Path file = modDir.resolve(DATA_FILE);
//
//            if (!file.toFile().exists()) {
//                Radars.clear();
//                PMWeatherAPI.LOGGER.warn("No radars file for this save!");
//                return dimensionToRadar;
//            }
//
//            JsonObject root;
//            try (FileReader fr = new FileReader(file.toFile())) {
//                 root = JsonParser.parseReader(fr).getAsJsonObject();
//            } catch (IOException e) {
//                PMWeatherAPI.LOGGER.warn("Failed to read radars.json! Error:", e);
//                return dimensionToRadar;
//            }
//
//            int version = root.has("version") ? root.get("version").getAsInt() : 1;
//            if (version > 1) {
//                PMWeatherAPI.LOGGER.info("Radars file has newer version {} than supported (1), attemping load regardless", version);
//            }
//
//            root.entrySet()
//                .stream()
//                .filter(e -> !e.getKey().equals("version") && !e.getKey().equals("saveTime"))
//                .forEach(e -> {
//                    ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(e.getKey()));
//                    Map<ChunkPos, Set<BlockPos>> radarMap = readRadarsForDimension(new HashMap<>(), e.getValue());
//                    dimensionToRadar.put(dim, radarMap);
//                });
//            PMWeatherAPI.LOGGER.info("Loaded saved radar data!");
//        } catch (Exception e) {
//            PMWeatherAPI.LOGGER.warn("Unable to load saved radar data! Error:", e);
//        }
//
//        return dimensionToRadar;
//    }
//
//    /**
//     * Saves all radars to data/pmweatherapi/radars.json
//     */
//    public static void saveAllRadars() {
//        try {
//            Path worldDir = getWorldSaveDirectory();
//            Path modDir = worldDir.resolve(DATA_FOLDER);
//            Files.createDirectories(modDir);
//            Path file = modDir.resolve(DATA_FILE);
//
//            JsonObject root = new JsonObject();
//            root.addProperty("version", 1);
//            root.addProperty("saveTime", System.currentTimeMillis());
//
//            for (Map.Entry<ResourceKey<Level>, Radars> radars: Radars.getAllDimensions().entrySet()) {
//                root.add(radars.getKey().location().toString(), saveRadarsForDimension(radars.getKey(), radars.getValue()));
//            }
//
//            Path tmp = modDir.resolve(DATA_FILE + ".tmp");
//
//            try (FileWriter writer = new FileWriter(tmp.toFile())) {
//                GSON.toJson(root, writer);
//                writer.flush();
//            }
//
//            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
//            PMWeatherAPI.LOGGER.info("Saved radars for all dimensions to {}/{}", DATA_FOLDER, DATA_FILE);
//        } catch (IOException e) {
//            PMWeatherAPI.LOGGER.warn("Failed to save radars.json! Error:", e);
//        }
//    }
//
//    protected static JsonObject saveRadarsForDimension(ResourceKey<Level> dimension, Radars radars) {
//        JsonObject root = new JsonObject();
//        Map<ChunkPos, Set<BlockPos>> radarMap = radars.getRadarMap();
//
//        for (Map.Entry<ChunkPos, Set<BlockPos>> entry: radarMap.entrySet()) {
//            if (entry.getValue().isEmpty()) continue;
//            JsonArray array = new JsonArray();
//
//            for (BlockPos pos: entry.getValue()) {
//                JsonObject obj = new JsonObject();
//                obj.addProperty("x", pos.getX());
//                obj.addProperty("y", pos.getY());
//                obj.addProperty("z", pos.getZ());
//                array.add(obj);
//            }
//
//            root.add(String.valueOf(entry.getKey().toLong()), array);
//        }
//
//        return root;
//    }
//
//    protected static Map<ChunkPos, Set<BlockPos>> readRadarsForDimension(Map<ChunkPos, Set<BlockPos>> radarMap, JsonElement json) {
//        if (json.isJsonObject()) {
//            JsonObject root = json.getAsJsonObject();
//
//            for (Map.Entry<String, JsonElement> entry: root.entrySet()) {
//                JsonArray array = entry.getValue().getAsJsonArray();
//                if (array.isEmpty()) continue;
//
//                ChunkPos chunkPos = new ChunkPos(Long.parseLong(entry.getKey()));
//                Set<BlockPos> radars =  new HashSet<>();
//
//                for (JsonElement e: array) {
//                    JsonObject obj = e.getAsJsonObject();
//                    int x = obj.get("x").getAsInt();
//                    int y = obj.get("y").getAsInt();
//                    int z = obj.get("z").getAsInt();
//                    radars.add(new BlockPos(x, y, z));
//                }
//
//                radarMap.put(chunkPos, radars);
//            }
//        }
//
//        return radarMap;
//    }
}
