package net.nullved.pmweatherapi;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.client.metar.MetarClientStorage;
import net.nullved.pmweatherapi.client.radar.RadarClientStorage;
import net.nullved.pmweatherapi.client.radar.WSRClientStorage;
import net.nullved.pmweatherapi.client.render.IDOverlay;
import net.nullved.pmweatherapi.client.render.RadarOverlays;
import net.nullved.pmweatherapi.config.PMWClientConfig;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.example.ExampleOverlay;
import net.nullved.pmweatherapi.metar.MetarServerStorage;
import net.nullved.pmweatherapi.metar.MetarStorage;
import net.nullved.pmweatherapi.metar.MetarStorageData;
import net.nullved.pmweatherapi.network.PMWNetworking;
import net.nullved.pmweatherapi.radar.RadarMode;
import net.nullved.pmweatherapi.radar.storage.*;
import net.nullved.pmweatherapi.storage.data.BlockPosData;
import net.nullved.pmweatherapi.storage.data.StorageDataManager;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;

import java.awt.*;

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

        if (ModList.get().isLoaded("pmweather")) {
            ModContainer pmweather = ModList.get().getModContainerById("pmweather").get();

            ArtifactVersion pmweatherVer = pmweather.getModInfo().getVersion();
            String pmweatherVerS = pmweatherVer.getMajorVersion() + "." + pmweatherVer.getMinorVersion() + "." + pmweatherVer.getIncrementalVersion();
            String pmweatherExpected = "0.15.3-1.21.1-alpha";
            int pmweatherCompared = pmweatherVer.compareTo(new DefaultArtifactVersion(pmweatherExpected));

            if (pmweatherCompared == 0) {
                PMWeatherAPI.LOGGER.info("Found PMWeather Version {}", pmweatherExpected.split("-")[0]);
            } else if (pmweatherCompared > 0) {
                PMWeatherAPI.LOGGER.warn("Found newer PMWeather Version {} than supported ({})! Support is NOT guaranteed!", pmweatherVerS, pmweatherExpected.split("-")[0]);
            } else {
                PMWeatherAPI.LOGGER.info("Found old PMWeather Version {}! Please update PMWeather to version {}!", pmweatherVerS, pmweatherExpected.split("-")[0]);
            }
        }

        if (FMLEnvironment.dist.isClient()) {
            modContainer.registerConfig(ModConfig.Type.CLIENT, PMWClientConfig.SPEC);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        StorageDataManager.register(BlockPosData.ID, BlockPosData::deserializeFromNBT);
        StorageDataManager.register(RadarStorageData.ID, RadarStorageData::deserializeFromNBT);
        StorageDataManager.register(MetarStorageData.ID, MetarStorageData::deserializeFromNBT);
        StorageDataManager.register(WSRStorageData.ID, WSRStorageData::deserializeFromNBT);

        PMWStorages.registerStorage(RadarStorage.ID, RadarServerStorage.class, RadarServerStorage::new);
        PMWStorages.registerStorage(MetarStorage.ID, MetarServerStorage.class, MetarServerStorage::new);
        PMWStorages.registerStorage(WSRStorage.ID, WSRServerStorage.class, WSRServerStorage::new);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PMWNetworking.register(event.registrar("1"));
    }

    private void clientSetup(FMLClientSetupEvent event) {
        //RadarMode.removeBaseRendering(true);

        PMWClientStorages.registerStorage(RadarStorage.ID, RadarClientStorage.class, RadarClientStorage::new);
        PMWClientStorages.registerStorage(MetarStorage.ID, MetarClientStorage.class, MetarClientStorage::new);
        PMWClientStorages.registerStorage(WSRStorage.ID, WSRClientStorage.class, WSRClientStorage::new);

        RadarOverlays.registerOverlay(() -> IDOverlay.INSTANCE);
        //RadarOverlays.registerOverlay(() -> ExampleOverlay.INSTANCE);
    }
    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
