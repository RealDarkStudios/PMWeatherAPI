package net.nullved.pmweatherapi.event;

import dev.protomanly.pmweather.event.GameBusEvents;
import dev.protomanly.pmweather.weather.Sounding;
import dev.protomanly.pmweather.weather.ThermodynamicEngine;
import dev.protomanly.pmweather.weather.WeatherHandler;
import dev.protomanly.pmweather.weather.WindEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.command.StoragesCommand;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.metar.MetarStorageData;
import net.nullved.pmweatherapi.storage.IServerStorage;
import net.nullved.pmweatherapi.storage.ISyncServerStorage;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = PMWeatherAPI.MODID)
public class PMWEvents {
    private static int ticks = 0;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ResourceKey<Level> dimension = event.getEntity().level().dimension();

        PMWStorages.getAll().forEach(si -> {
            IServerStorage<?> storage = si.get(dimension);
            if (storage != null) {
                if (storage instanceof ISyncServerStorage<?> isss) {
                    PMWeatherAPI.LOGGER.debug("Syncing stoage {}", isss.getId().toString());
                    isss.syncAllToPlayer(event.getEntity());
                }
            }
        });

        PMWeatherAPI.LOGGER.info("Synced all sync-storages to joined player {}", event.getEntity().getDisplayName().getString());
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        PMWStorages.getForDimension(event.getTo()).forEach(iss -> {
            if (iss instanceof ISyncServerStorage<?> isss) isss.syncAllToPlayer(event.getEntity());
        });
    }

//    @SubscribeEvent
//    public static void onChunkSentEvent(ChunkWatchEvent.Sent event) {
//        RadarServerStorage radarStorage = PMWStorages.radars().getOrCreate(event.getLevel());
//        if (radarStorage.shouldRecalculate(event.getChunk().getPos())) {
//            List<RadarStorageData> radars = new ArrayList<>();
//            LevelAccessor level = event.getLevel();
//            Set<BlockPos> blockEntities = event.getChunk().getBlockEntitiesPos();
//
//            for (BlockPos pos : blockEntities) {
//                if (level.getBlockState(pos).getBlock() instanceof RadarBlock) radars.add(new RadarStorageData(pos));
//            }
//
//            radarStorage.addAndSync(radars);
//        }
//    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Pre event) {
        ticks += 1;
        if (ticks % 1200 == 0) {
            ticks = 0;

            PMWeatherAPI.LOGGER.info("Saving metar data!");
            PMWStorages.metars().entrySet().forEach(entry -> {
                WeatherHandler weatherHandler = GameBusEvents.MANAGERS.get(entry.getKey());

                List<MetarStorageData> updated = new ArrayList<>();
                entry.getValue().getAll().forEach(msd -> {
                    BlockPos pos = msd.getPos();
                    Level level = weatherHandler.getWorld();

                    Vec3 wind = WindEngine.getWind(pos, level);
                    int windAngle = Math.floorMod((int)Math.toDegrees(Math.atan2(wind.x, -wind.z)), 360);
                    double windspeed = wind.length();
                    ThermodynamicEngine.AtmosphericDataPoint sfc = ThermodynamicEngine.samplePoint(weatherHandler, pos.getCenter(), level, null, 0);
                    float temp = sfc.temperature();
                    float dew = sfc.dewpoint();
                    float riskV = 0.0F;
                    for(int i = 0; i < 24000; i += 200) {
                        Sounding sounding = new Sounding(weatherHandler, pos.getCenter(), level, 250, 16000, i);
                        float r = sounding.getRisk(i);
                        if (r > riskV) {
                            riskV = r;
                        }
                    }

                    MetarStorageData newMsd = new MetarStorageData(pos, temp, dew, (float) windAngle, (float) windspeed, riskV);
                    updated.add(newMsd);
                });
                entry.getValue().addAndSync(updated);
            });
        }
    }

    @SubscribeEvent
    public static void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        PMWeatherAPI.LOGGER.info("Registering PMWeatherAPI Commands");
        StoragesCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLevelLoadEvent(LevelEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel slevel) {
            PMWStorages.loadDimension(slevel);
            PMWeatherAPI.LOGGER.info("Loaded storages for dimension {}", slevel.dimension().location());
        }
    }

    @SubscribeEvent
    public static void onLevelUnloadEvent(LevelEvent.Unload event) {
        LevelAccessor level =  event.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel slevel) {
            PMWStorages.removeDimension(slevel.dimension());
            PMWeatherAPI.LOGGER.info("Unloaded storages for dimension {}", slevel.dimension().location());
        }
    }
}
