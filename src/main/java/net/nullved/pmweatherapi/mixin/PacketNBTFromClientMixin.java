package net.nullved.pmweatherapi.mixin;

import dev.protomanly.pmweather.networking.PacketNBTFromClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.nullved.pmweatherapi.data.PMWStorages;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketNBTFromClient.class)
public class PacketNBTFromClientMixin {
    @Shadow @Final private CompoundTag compoundTag;

    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Ldev/protomanly/pmweather/block/entity/RadarBlockEntity;playerRequestsSync(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;)V"))
    private void onHandle(Player player, CallbackInfo ci) {
        PMWStorages.getRadar(player.level()).addRadar(NbtUtils.readBlockPos(this.compoundTag, "blockPos").get());
    }
}
