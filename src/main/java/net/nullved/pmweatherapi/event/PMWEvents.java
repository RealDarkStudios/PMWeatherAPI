package net.nullved.pmweatherapi.event;

import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.command.NearbyRadarsCommand;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.radar.RadarServerStorage;

import java.util.*;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = PMWeatherAPI.MODID)
public class PMWEvents {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        PMWStorages.getRadar(event.getEntity().level()).syncAllToPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onChunkLoadEvent(ChunkWatchEvent.Sent event) {
        RadarServerStorage radarStorage = PMWStorages.getRadar(event.getLevel());
        if (radarStorage.shouldRecalculate(event.getChunk().getPos())) {
            List<BlockPos> radars = new ArrayList<>();
            LevelAccessor level = event.getLevel();
            Set<BlockPos> blockEntities = event.getChunk().getBlockEntitiesPos();

            for (BlockPos pos : blockEntities) {
                if (level.getBlockState(pos).getBlock() instanceof RadarBlock) radars.add(pos);
            }

            radarStorage.addRadars(radars);
            radarStorage.syncAdd(radars);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        PMWeatherAPI.LOGGER.info("Registering PMWeatherAPI Commands");
        NearbyRadarsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLevelLoadEvent(LevelEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel slevel) {
            ResourceKey<Level> dimension = slevel.dimension();
            RadarServerStorage radars = new RadarServerStorage(slevel);
            radars.read();
            PMWStorages.RADARS.put(dimension, radars);
            PMWeatherAPI.LOGGER.info("Loaded radars for dimension {}", slevel.dimension().location());
        }
    }

    @SubscribeEvent
    public static void onLevelUnloadEvent(LevelEvent.Unload event) {
        LevelAccessor level =  event.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel slevel) {
            PMWStorages.RADARS.remove(slevel.dimension());
        }
    }
}
