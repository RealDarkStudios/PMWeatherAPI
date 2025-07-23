package net.nullved.pmweatherapi.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.storage.IServerStorage;
import net.nullved.pmweatherapi.util.PMWUtils;

import java.util.Set;
import java.util.function.BiFunction;

public class StoragesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("storages")
                .then(Commands.argument("storage", ResourceLocationArgument.id())
                    .suggests((ctx, builder) -> {
                        PMWStorages.getAll().forEach(si -> builder.suggest(si.id().toString()));
                        return builder.buildFuture();
                    })
                    .then(Commands.literal("all")
                        .then(Commands.argument("radius", IntegerArgumentType.integer(0, 2048))
                            .executes(StoragesCommand::storageAll))
                        .executes(StoragesCommand::storageAll))
                    .then(Commands.literal("adjacentChunks")
                        .executes(StoragesCommand::storageAdjacentChunks))
//                    .then(Commands.argument("radius", IntegerArgumentType.integer(1, 2048))
//                        .executes(StoragesCommand::exec))
                    .executes(StoragesCommand::storageAll)
                )
        );
    }

    private static int storageAll(CommandContext<CommandSourceStack> ctx) {
        BiFunction<IServerStorage, Player, Set<BlockPos>> func;
        try {
            final int radius = ctx.getArgument("radius", Integer.class);
            func = (srv, plr) -> srv.getAllWithinRange(plr.blockPosition(), radius);
        } catch (Exception e) {
            func = (srv, plr) -> srv.getAll();
        }

        return exec(ctx, func);
    }

    private static int storageAdjacentChunks(CommandContext<CommandSourceStack> ctx) {
        return exec(ctx, (srv, plr) -> srv.getInAdjacentChunks(new ChunkPos(plr.blockPosition())));
    }

    private static int exec(CommandContext<CommandSourceStack> ctx, BiFunction<IServerStorage, Player, Set<BlockPos>> blocksFunction) {
        PMWeatherAPI.LOGGER.info("Checking for nearby storages...");

        Player plr = ctx.getSource().getPlayer();
        ResourceLocation storage = ResourceLocationArgument.getId(ctx, "storage");
        if (!ctx.getSource().isPlayer()) {
            ctx.getSource().sendSystemMessage(Component.translatable("commands.pmweatherapi.non_player"));
            return Command.SINGLE_SUCCESS;
        }

        long startTimeMillis = System.currentTimeMillis();
        IServerStorage srv = PMWStorages.get(storage).get(plr.level().dimension());

        Set<BlockPos> blocks = Set.of();
        if (srv != null) {
            blocks = blocksFunction.apply(srv, plr);
        }
        long elapsedTime = System.currentTimeMillis() - startTimeMillis;

        StringBuilder sb = new StringBuilder("Found ").append(blocks.size()).append(" block positions in ").append(elapsedTime / 1000.0F).append("s");
        for (BlockPos blockPos : blocks) {
            sb.append("\nPos: ").append(blockPos.toShortString());

            if (PMWUtils.isRadarCornerAdjacent(plr.level(), blockPos)) {
                sb.append(" (RA)");
            }
        }

        if (PMWUtils.isRadarCornerAdjacent(plr.level(), plr.blockPosition())) {
            sb.append("\nYou are next to a radar!");
        }

        plr.sendSystemMessage(Component.literal(sb.toString()).withColor(ChatFormatting.GOLD.getColor()));
        return Command.SINGLE_SUCCESS;
    }
}
