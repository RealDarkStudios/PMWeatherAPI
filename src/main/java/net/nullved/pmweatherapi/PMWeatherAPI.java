package net.nullved.pmweatherapi;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(PMWeatherAPI.MODID)
public class PMWeatherAPI {
    public static final String MODID = "pmweatherapi";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PMWeatherAPI(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing PMWAPI...");
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("Initialized PMWAPI");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("pmweather")) {
            throw new RuntimeException("ProtoManly's Weather not detected!");
        }
    }
}
