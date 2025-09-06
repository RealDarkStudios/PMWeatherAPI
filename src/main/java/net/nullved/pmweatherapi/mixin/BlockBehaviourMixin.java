package net.nullved.pmweatherapi.mixin;

import dev.protomanly.pmweather.block.MetarBlock;
import dev.protomanly.pmweather.block.RadarBlock;
import dev.protomanly.pmweather.block.entity.RadarBlockEntity;
import dev.protomanly.pmweather.event.GameBusEvents;
import dev.protomanly.pmweather.multiblock.wsr88d.WSR88DCore;
import dev.protomanly.pmweather.weather.Sounding;
import dev.protomanly.pmweather.weather.ThermodynamicEngine;
import dev.protomanly.pmweather.weather.WeatherHandler;
import dev.protomanly.pmweather.weather.WindEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nullved.pmweatherapi.data.PMWExtras;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.metar.MetarServerStorage;
import net.nullved.pmweatherapi.metar.MetarStorageData;
import net.nullved.pmweatherapi.radar.RadarMode;
import net.nullved.pmweatherapi.radar.storage.RadarServerStorage;
import net.nullved.pmweatherapi.radar.storage.RadarStorageData;
import net.nullved.pmweatherapi.radar.storage.WSRStorageData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @Inject(method = "onPlace", at = @At("HEAD"))
    private static void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston, CallbackInfo ci) {
        if (state.getBlock() instanceof RadarBlock) {
            RadarServerStorage radarStorage = PMWStorages.radars().get(level.dimension());
            radarStorage.addAndSync(new RadarStorageData(pos, state.getValue(PMWExtras.RADAR_MODE)));
        } else if (state.getBlock() instanceof MetarBlock) {
            // Get Metar data
            WeatherHandler weatherHandler = GameBusEvents.MANAGERS.get(level.dimension());
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

            MetarServerStorage metarStorage = PMWStorages.metars().get(level.dimension());
            metarStorage.addAndSync(new MetarStorageData(pos, temp, dew, (float) windAngle, (float) windspeed, riskV));
        } else if (state.getBlock() instanceof WSR88DCore wsr) {
            PMWStorages.wsrs().get(level.dimension()).addAndSync(new WSRStorageData(pos, wsr.isComplete(state)));
        }
    }

    @Inject(method = "onRemove", at = @At("HEAD"))
    private static void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston, CallbackInfo ci) {
        if (state.getBlock() instanceof RadarBlock) {
            RadarServerStorage radarStorage = PMWStorages.radars().get(level.dimension());
            radarStorage.removeAndSync(pos);
        } else if (state.getBlock() instanceof MetarBlock) {
            MetarServerStorage metarStorage = PMWStorages.metars().get(level.dimension());
            metarStorage.removeAndSync(pos);
        }else if (state.getBlock() instanceof WSR88DCore wsr) {
            PMWStorages.wsrs().get(level.dimension()).removeAndSync(pos);
        }
    }
}
