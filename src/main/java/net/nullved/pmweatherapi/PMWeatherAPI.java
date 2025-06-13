package net.nullved.pmweatherapi;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.nullved.pmweatherapi.client.render.RadarOverlays;
import net.nullved.pmweatherapi.example.ExampleOverlay;
import net.nullved.pmweatherapi.network.PMWNetworking;
import org.slf4j.Logger;

@Mod(PMWeatherAPI.MODID)
public class PMWeatherAPI {
    public static final String MODID = "pmweatherapi";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PMWeatherAPI(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing PMWAPI...");

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        modEventBus.addListener(this::clientSetup);

        LOGGER.info("Initialized PMWAPI");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("pmweather")) {
            throw new RuntimeException("ProtoManly's Weather not detected!");
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PMWNetworking.register(event.registrar("1"));
    }

    private void clientSetup(FMLClientSetupEvent event) {
        RadarOverlays.registerOverlay(() -> ExampleOverlay.INSTANCE);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
