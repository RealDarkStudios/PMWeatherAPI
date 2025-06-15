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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.nullved.pmweatherapi.data.PMWExtras;
import net.nullved.pmweatherapi.radar.RadarMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RadarBlock.class)
public class RadarBlockMixin {
    @Shadow public static EnumProperty<RadarBlock.Mode> RADAR_MODE;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ldev/protomanly/pmweather/block/RadarBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void init(RadarBlock instance, BlockState state) {
        instance.registerDefaultState(instance.defaultBlockState().setValue(PMWExtras.RADAR_MODE, RadarMode.REFLECTIVITY.stringValue()).setValue(RADAR_MODE, RadarBlock.Mode.REFLECTIVITY));
    }

    @Inject(method = "createBlockStateDefinition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/StateDefinition$Builder;add([Lnet/minecraft/world/level/block/state/properties/Property;)Lnet/minecraft/world/level/block/state/StateDefinition$Builder;"))
    private void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(PMWExtras.RADAR_MODE);
    }

    @WrapMethod(method = "useWithoutItem")
    private InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, Operation<InteractionResult> original) {
        original.call(state, level, pos, player, hitResult);

        if (!level.isClientSide()) {
            RadarMode currentMode = RadarMode.get(state.getValue(PMWExtras.RADAR_MODE).value());
            RadarMode newMode = currentMode.cycle();
            level.setBlockAndUpdate(pos, state.setValue(PMWExtras.RADAR_MODE, newMode.stringValue()));
        }

        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
}
