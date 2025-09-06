package net.nullved.pmweatherapi.client.event;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;

@EventBusSubscriber(modid = PMWeatherAPI.MODID, value = Dist.CLIENT)
public class PMWClientEvents {
    @SubscribeEvent
    public static void onLevelLoadEvent(LevelEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (level.isClientSide() && level instanceof ClientLevel clevel) {
            PMWeatherAPI.LOGGER.info("Loaded client storages for dimension {}", clevel.dimension().location());
            PMWClientStorages.loadDimension(clevel);
        }
    }

    @SubscribeEvent
    public static void onLevelUnloadEvent(LevelEvent.Unload event) {
        LevelAccessor level =  event.getLevel();
        if (level.isClientSide() && level instanceof ClientLevel clevel) {
            PMWeatherAPI.LOGGER.info("Unloaded client storages for dimension {}", clevel.dimension().location());
        }
    }
}
