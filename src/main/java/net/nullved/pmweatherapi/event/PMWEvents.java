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
import net.nullved.pmweatherapi.command.StoragesCommand;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.radar.RadarServerStorage;
import net.nullved.pmweatherapi.storage.IServerStorage;
import net.nullved.pmweatherapi.storage.ISyncServerStorage;

import java.util.*;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = PMWeatherAPI.MODID)
public class PMWEvents {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ResourceKey<Level> dimension = event.getEntity().level().dimension();

        PMWStorages.getAll().forEach(si -> {
            IServerStorage storage = si.get(dimension);
            if (storage != null) {
                if (storage instanceof ISyncServerStorage isss) isss.syncAllToPlayer(event.getEntity());
            }
        });

        PMWeatherAPI.LOGGER.info("Synced all sync-storages to joined player {}", event.getEntity().getDisplayName().getString());
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        PMWStorages.getForDimension(event.getTo()).forEach(iss -> {
            if (iss != null) iss.syncAllToPlayer(event.getEntity());
        });
    }

    @SubscribeEvent
    public static void onChunkSentEvent(ChunkWatchEvent.Sent event) {
        RadarServerStorage radarStorage = PMWStorages.radars().getOrCreate(event.getLevel());
        if (radarStorage.shouldRecalculate(event.getChunk().getPos())) {
            List<BlockPos> radars = new ArrayList<>();
            LevelAccessor level = event.getLevel();
            Set<BlockPos> blockEntities = event.getChunk().getBlockEntitiesPos();

            for (BlockPos pos : blockEntities) {
                if (level.getBlockState(pos).getBlock() instanceof RadarBlock) radars.add(pos);
            }

            radarStorage.addAndSync(radars);
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
            PMWStorages.generateForDimension(slevel);
            PMWeatherAPI.LOGGER.info("Loaded storages for dimension {}", slevel.dimension().location());
        }
    }

    @SubscribeEvent
    public static void onLevelUnloadEvent(LevelEvent.Unload event) {
        LevelAccessor level =  event.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel slevel) {
            PMWStorages.removeForDimension(slevel.dimension());
            PMWeatherAPI.LOGGER.info("Unloaded storages for dimension {}", slevel.dimension().location());
        }
    }
}
