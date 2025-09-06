package net.nullved.pmweatherapi.mixin;

import dev.protomanly.pmweather.multiblock.wsr88d.WSR88DCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.radar.storage.WSRStorageData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WSR88DCore.class)
public class WSR88DCoreMixin {
    @Inject(method = "completionChanged", at = @At("HEAD"))
    public void completionChanged(boolean completed, Level level, BlockState blockState, BlockPos pos, CallbackInfo ci) {
        PMWStorages.wsrs().get(level.dimension()).addAndSync(new WSRStorageData(pos, completed));
    }
}
