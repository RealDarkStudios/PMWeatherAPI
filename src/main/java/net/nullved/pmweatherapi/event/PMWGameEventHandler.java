package net.nullved.pmweatherapi.event;

import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.command.NearbyRadarsCommand;
import net.nullved.pmweatherapi.radar.RadarStorage;
import net.nullved.pmweatherapi.radar.Radars;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = PMWeatherAPI.MODID)
public class PMWGameEventHandler {
    @SubscribeEvent
    public static void onChunkLoadEvent(ChunkWatchEvent.Sent event) {
        if (Radars.get(event.getLevel()).shouldRecalculate(event.getChunk())) {
            List<BlockPos> radars = new ArrayList<>();
            LevelAccessor level = event.getLevel();
            Set<BlockPos> blockEntities = event.getChunk().getBlockEntitiesPos();

            for (BlockPos pos : blockEntities) {
                if (level.getBlockState(pos).getBlock() instanceof RadarBlock) radars.add(pos);
            }

            Radars.get(event.getLevel()).registerRadars(radars);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        PMWeatherAPI.LOGGER.info("Registering PMWeatherAPI Commands");
        NearbyRadarsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLevelUnloadEvent(LevelEvent.Save event) {
        LevelAccessor level =  event.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel slevel) {
            RadarStorage.saveAllRadars();
            //Radars.clear();
        }
    }

    @SubscribeEvent
    public static void onLevelLoadEvent(LevelEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (!level.isClientSide()) {
            Radars.readFromStorage();
        }
    }
}
