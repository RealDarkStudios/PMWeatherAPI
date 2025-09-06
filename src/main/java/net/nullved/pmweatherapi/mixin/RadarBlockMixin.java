package net.nullved.pmweatherapi.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.protomanly.pmweather.block.RadarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.nullved.pmweatherapi.data.PMWExtras;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.radar.RadarMode;
import net.nullved.pmweatherapi.radar.storage.RadarStorageData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RadarBlock.class)
public class RadarBlockMixin {
    @Shadow public static EnumProperty<RadarBlock.Mode> RADAR_MODE;
    @Shadow public static BooleanProperty ON;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ldev/protomanly/pmweather/block/RadarBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void init(RadarBlock instance, BlockState state) {
        instance.registerDefaultState(instance.defaultBlockState().setValue(PMWExtras.RADAR_MODE, RadarMode.REFLECTIVITY).setValue(RADAR_MODE, RadarBlock.Mode.REFLECTIVITY));
    }

    @Inject(method = "createBlockStateDefinition", at = @At(value = "TAIL"))
    private void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(PMWExtras.RADAR_MODE);
    }

    @WrapMethod(method = "useWithoutItem")
    private InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, Operation<InteractionResult> original) {
        original.call(state, level, pos, player, hitResult);

        if (player.isCrouching()) {
            level.setBlockAndUpdate(pos, state.setValue(ON, !state.getValue(ON)));
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }

        if (!level.isClientSide()) {
            RadarMode currentMode = state.getValue(PMWExtras.RADAR_MODE);
            RadarMode newMode = currentMode.cycle();
            PMWStorages.radars().get(level.dimension()).addAndSync(new RadarStorageData(pos, newMode));
            level.setBlockAndUpdate(pos, state.setValue(PMWExtras.RADAR_MODE, newMode));
        }

        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
}
