package net.nullved.pmweatherapi.mixin;

import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.radar.RadarServerStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @Inject(method = "onPlace", at = @At("HEAD"))
    private static void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston, CallbackInfo ci) {
        if (state.getBlock() instanceof RadarBlock) {
            RadarServerStorage radarStorage = PMWStorages.getRadar(level);
            radarStorage.addRadar(pos);
            radarStorage.syncAdd(pos);
        }
    }

    @Inject(method = "onRemove", at = @At("HEAD"))
    private static void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston, CallbackInfo ci) {
        if (state.getBlock() instanceof RadarBlock) {
            RadarServerStorage radarStorage = PMWStorages.getRadar(level);
            radarStorage.removeRadar(pos);
            radarStorage.syncRemove(pos);
        }
    }
}
