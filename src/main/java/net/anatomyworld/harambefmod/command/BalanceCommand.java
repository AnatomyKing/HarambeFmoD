package net.anatomyworld.harambefmod.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.anatomyworld.harambefmod.economy.Economy;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;

/**
 * /balance <player>
 * /balance <player> add <amount>
 * /balance <player> remove <amount>
 *
 * OP-only (permission level 2).
 */
public final class BalanceCommand {
    private BalanceCommand() {}

    private static final SimpleCommandExceptionType ERROR_EXPECTED_SINGLE =
            new SimpleCommandExceptionType(Component.literal("Expected exactly one player."));

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("balance")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(ctx -> {
                            GameProfile gp = requireSingle(GameProfileArgument.getGameProfiles(ctx, "player"));
                            long bal = Economy.get(ctx.getSource().getServer(), gp);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal(nameOf(gp) + "'s balance: ").withStyle(ChatFormatting.GRAY)
                                            .append(Component.literal(Long.toString(bal)).withStyle(ChatFormatting.GOLD)),
                                    false
                            );
                            return 1;
                        })
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                        .executes(ctx -> {
                                            GameProfile gp = requireSingle(GameProfileArgument.getGameProfiles(ctx, "player"));
                                            long amount = LongArgumentType.getLong(ctx, "amount");
                                            MinecraftServer srv = ctx.getSource().getServer();
                                            long after = Economy.add(srv, gp, amount);

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Added ").withStyle(ChatFormatting.GRAY)
                                                            .append(Component.literal(Long.toString(amount)).withStyle(ChatFormatting.GREEN))
                                                            .append(Component.literal(" to " + nameOf(gp) + ". New balance: ").withStyle(ChatFormatting.GRAY))
                                                            .append(Component.literal(Long.toString(after)).withStyle(ChatFormatting.GOLD)),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                        .executes(ctx -> {
                                            GameProfile gp = requireSingle(GameProfileArgument.getGameProfiles(ctx, "player"));
                                            long amount = LongArgumentType.getLong(ctx, "amount");
                                            MinecraftServer srv = ctx.getSource().getServer();
                                            long after = Economy.remove(srv, gp, amount);

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Removed ").withStyle(ChatFormatting.GRAY)
                                                            .append(Component.literal(Long.toString(amount)).withStyle(ChatFormatting.RED))
                                                            .append(Component.literal(" from " + nameOf(gp) + ". New balance: ").withStyle(ChatFormatting.GRAY))
                                                            .append(Component.literal(Long.toString(after)).withStyle(ChatFormatting.GOLD)),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }

    private static GameProfile requireSingle(Collection<GameProfile> profiles) throws CommandSyntaxException {
        if (profiles.size() != 1) throw ERROR_EXPECTED_SINGLE.create();
        return profiles.iterator().next();
    }

    private static String nameOf(GameProfile gp) {
        return gp.getName() != null ? gp.getName() : "unknown";
    }
}
