package net.nullved.pmweatherapi.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.nullved.pmweatherapi.PMWeatherAPI;
import net.nullved.pmweatherapi.client.data.IClientStorage;
import net.nullved.pmweatherapi.client.data.PMWClientStorages;
import net.nullved.pmweatherapi.data.PMWStorages;
import net.nullved.pmweatherapi.storage.IServerStorage;
import net.nullved.pmweatherapi.storage.IStorage;
import net.nullved.pmweatherapi.storage.data.IStorageData;
import net.nullved.pmweatherapi.storage.data.StorageData;
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
                    .then(Commands.literal("client")
                        .then(Commands.literal("all")
                            .then(Commands.argument("radius", IntegerArgumentType.integer(1, 2048))
                                .executes(StoragesCommand::clientAll))
                            .executes(StoragesCommand::clientAll))
                        .then(Commands.literal("adjacentChunks")
                            .executes(StoragesCommand::clientAdjacentChunks))
                        .executes(StoragesCommand::clientAll))
                    .then(Commands.literal("all")
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 2048))
                            .executes(StoragesCommand::serverAll))
                        .executes(StoragesCommand::serverAll))
                    .then(Commands.literal("adjacentChunks")
                        .executes(StoragesCommand::serverAdjacentChunks))
                    .executes(StoragesCommand::serverAll)
                )
        );
    }

    private static <D extends IStorageData> int clientAll(CommandContext<CommandSourceStack> ctx) {
        BiFunction<IStorage<D>, Player, Set<D>> func;
        try {
            final int radius = ctx.getArgument("radius", Integer.class);
            func = (stg, plr) -> stg.getAllWithinRange(plr.blockPosition(), radius);
        } catch (Exception e) {
            func = (stg, plr) -> stg.getAll();
        }

        return execClient(ctx, func);
    }

    private static <D extends IStorageData> int serverAll(CommandContext<CommandSourceStack> ctx) {
        BiFunction<IStorage<D>, Player, Set<D>> func;
        try {
            final int radius = ctx.getArgument("radius", Integer.class);
            func = (stg, plr) -> stg.getAllWithinRange(plr.blockPosition(), radius);
        } catch (Exception e) {
            func = (stg, plr) -> stg.getAll();
        }

        return exec(ctx, func);
    }

    private static int clientAdjacentChunks(CommandContext<CommandSourceStack> ctx) {
        return execClient(ctx, (stg, plr) -> stg.getInAdjacentChunks(new ChunkPos(plr.blockPosition())));
    }

    private static int serverAdjacentChunks(CommandContext<CommandSourceStack> ctx) {
        return exec(ctx, (stg, plr) -> stg.getInAdjacentChunks(new ChunkPos(plr.blockPosition())));
    }

    private static <D extends IStorageData> int exec(CommandContext<CommandSourceStack> ctx, BiFunction<IStorage<D>, Player, Set<D>> blocksFunction) {
        PMWeatherAPI.LOGGER.info("Checking for nearby storages...");

        Player plr = ctx.getSource().getPlayer();
        ResourceLocation storage = ResourceLocationArgument.getId(ctx, "storage");
        if (!ctx.getSource().isPlayer()) {
            ctx.getSource().sendSystemMessage(Component.translatable("commands.pmweatherapi.non_player"));
            return Command.SINGLE_SUCCESS;
        }

        long startTimeMillis = System.currentTimeMillis();
        IServerStorage<D> stg = (IServerStorage<D>) PMWStorages.get(storage).get(plr.level().dimension());

        Set<D> blocks = Set.of();
        if (stg != null) {
            blocks = blocksFunction.apply(stg, plr);
        }
        long elapsedTime = System.currentTimeMillis() - startTimeMillis;

        StringBuilder sb = new StringBuilder("Found ").append(blocks.size()).append(" server block positions in ").append(elapsedTime / 1000.0F).append("s");
        for (D data : blocks) {
            sb.append("\nPos: ").append(data.getPos().toShortString());

            if (PMWUtils.isRadarCornerAdjacent(plr.level(), data.getPos())) {
                sb.append(" (RA)");
            }
        }

        if (PMWUtils.isRadarCornerAdjacent(plr.level(), plr.blockPosition())) {
            sb.append("\nYou are next to a radar!");
        }

        plr.sendSystemMessage(Component.literal(sb.toString()).withColor(ChatFormatting.GOLD.getColor()));
        return Command.SINGLE_SUCCESS;
    }

    private static <D extends IStorageData> int execClient(CommandContext<CommandSourceStack> ctx, BiFunction<IStorage<D>, Player, Set<D>> blocksFunction) {
        PMWeatherAPI.LOGGER.info("Checking for nearby storages...");

        Player plr = ctx.getSource().getPlayer();
        ResourceLocation storage = ResourceLocationArgument.getId(ctx, "storage");
        if (!ctx.getSource().isPlayer()) {
            ctx.getSource().sendSystemMessage(Component.translatable("commands.pmweatherapi.non_player"));
            return Command.SINGLE_SUCCESS;
        }

        long startTimeMillis = System.currentTimeMillis();
        IClientStorage<D> stg = (IClientStorage<D>) PMWClientStorages.get(storage).get();

        Set<D> blocks = Set.of();
        if (stg != null) {
            blocks = blocksFunction.apply(stg, plr);
        }
        long elapsedTime = System.currentTimeMillis() - startTimeMillis;

        StringBuilder sb = new StringBuilder("Found ").append(blocks.size()).append(" client block positions in ").append(elapsedTime / 1000.0F).append("s");
        for (D data : blocks) {
            sb.append("\nPos: ").append(data.getPos().toShortString());

            if (PMWUtils.isRadarCornerAdjacent(plr.level(), data.getPos())) {
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
