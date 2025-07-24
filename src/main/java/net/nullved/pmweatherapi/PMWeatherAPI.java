package net.nullved.pmweatherapi;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.client.render.IDOverlay;
import net.nullved.pmweatherapi.client.render.RadarOverlays;
import net.nullved.pmweatherapi.config.PMWClientConfig;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.network.PMWNetworking;
import net.nullved.pmweatherapi.radar.RadarServerStorage;
import net.nullved.pmweatherapi.radar.RadarStorage;
import org.slf4j.Logger;

@Mod(PMWeatherAPI.MODID)
public class PMWeatherAPI {
    public static final String MODID = "pmweatherapi";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PMWeatherAPI(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing PMWAPI...");

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerPayloads);

        LOGGER.info("Initialized PMWAPI");

        if (FMLEnvironment.dist.isClient()) {
            modContainer.registerConfig(ModConfig.Type.CLIENT, PMWClientConfig.SPEC);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        PMWStorages.registerStorage(RadarStorage.ID, RadarServerStorage.class, RadarServerStorage::new);

//        if (!ModList.get().isLoaded("pmweather")) {
//            throw new RuntimeException("ProtoManly's Weather not detected!");
//        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PMWNetworking.register(event.registrar("1"));
    }

    private void clientSetup(FMLClientSetupEvent event) {
        PMWClientStorages.registerStorage(RadarStorage.ID, RadarClientStorage.class, RadarClientStorage::new);

        RadarOverlays.registerOverlay(() -> IDOverlay.INSTANCE);
//        RadarOverlays.registerOverlay(() -> ExampleOverlay.INSTANCE);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
