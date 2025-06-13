package net.nullved.pmweatherapi.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.radar.NearbyRadars;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NearbyRadarsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("nearbyradars")
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 2048))
                    .executes(NearbyRadarsCommand::nearbyRadars))
                .executes(NearbyRadarsCommand::nearbyRadars)
        );
    }

    private static int nearbyRadars(CommandContext<CommandSourceStack> ctx) {
        PMWeatherAPI.LOGGER.info("Checking for nearby radars...");

        if (!ctx.getSource().isPlayer()) {
            ctx.getSource().sendSystemMessage(Component.translatable("commands.pmweatherapi.non_player"));
            return Command.SINGLE_SUCCESS;
        }

        Player plr = ctx.getSource().getPlayer();
        int radius;
        try {
            radius = ctx.getArgument("radius", Integer.class);
        } catch (Exception e) {
            radius = 512;
        };

        long startTimeMillis = System.currentTimeMillis();
        Set<BlockPos> blocks = NearbyRadars.get(plr.level()).radarsNearBlock(plr.blockPosition(), radius);
        long elapsedTime = System.currentTimeMillis() - startTimeMillis;

        StringBuilder sb = new StringBuilder("Found ").append(blocks.size()).append(" radars in ").append(elapsedTime / 1000.0F).append("s");
        for (BlockPos blockPos : blocks) {
            sb.append("\nPos: ").append(blockPos.toShortString());
        }

        plr.sendSystemMessage(Component.literal(sb.toString()).withColor(ChatFormatting.GOLD.getColor()));
        return Command.SINGLE_SUCCESS;
    }
}
