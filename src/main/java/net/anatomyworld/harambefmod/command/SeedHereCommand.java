package net.anatomyworld.harambefmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.anatomyworld.harambefmod.worldgen.api.SeedOverrideSupport;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

import java.util.OptionalLong;

public final class SeedHereCommand {
    private SeedHereCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("seedhere")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    ServerLevel lvl = ctx.getSource().getLevel();

                    long result = lvl.getSeed(); // default: world seed
                    ChunkGenerator gen = lvl.getChunkSource().getGenerator();
                    if (gen instanceof NoiseBasedChunkGenerator nb) {
                        OptionalLong o = ((SeedOverrideSupport)(Object) nb).harambefmod$getSeedOverride();
                        if (o.isPresent()) result = o.getAsLong();
                    }

                    String seedStr = Long.toString(result);
                    String dimStr = lvl.dimension().location().toString(); // e.g. "minecraft:overworld"

                    // Clickable green seed: copy-to-clipboard + hover hint + insertion (vanilla UX)
                    Component clickableSeed = Component.literal(seedStr).withStyle(s -> s
                            .withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent.CopyToClipboard(seedStr))
                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.copy.click")))
                            .withInsertion(seedStr)
                    );

                    // Seed: <namespace:dimension> [<seed>]
                    Component payload = Component.literal(dimStr + " ")
                            .append(ComponentUtils.wrapInSquareBrackets(clickableSeed));

                    // Use the vanilla translation key: "Seed: %s"
                    Component msg = Component.translatable("commands.seed.success", payload);

                    ctx.getSource().sendSuccess(() -> msg, false);
                    return 1;
                })
        );
    }
}
