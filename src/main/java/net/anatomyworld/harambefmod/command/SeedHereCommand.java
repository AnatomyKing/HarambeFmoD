package net.anatomyworld.harambefmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.anatomyworld.harambefmod.worldgen.api.SeedOverrideSupport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
                    long result = lvl.getSeed(); // default
                    ChunkGenerator gen = lvl.getChunkSource().getGenerator();
                    if (gen instanceof NoiseBasedChunkGenerator nb) {
                        OptionalLong o = ((SeedOverrideSupport)(Object)nb).harambefmod$getSeedOverride();
                        if (o.isPresent()) result = o.getAsLong();
                    }
                    long finalResult = result;
                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                            "Seed for " + lvl.dimension().location() + ": " + finalResult
                    ), false);
                    return 1;
                })
        );
    }
}
