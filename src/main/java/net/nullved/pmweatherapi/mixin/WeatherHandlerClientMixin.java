package net.nullved.pmweatherapi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.protomanly.pmweather.weather.Lightning;
import dev.protomanly.pmweather.weather.WeatherHandlerClient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.nullved.pmweatherapi.client.event.LightningEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@OnlyIn(Dist.CLIENT)
@Mixin(WeatherHandlerClient.class)
public class WeatherHandlerClientMixin {
    @WrapOperation(method = "strike", at = @At(value = "NEW", target = "(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/Level;)Ldev/protomanly/pmweather/weather/Lightning;"))
    public Lightning newLightning(Vec3 position, Level level, Operation<Lightning> original) {
        Lightning lightning = original.call(position, level);

        NeoForge.EVENT_BUS.post(new LightningEvent(lightning));

        return lightning;
    }
}
