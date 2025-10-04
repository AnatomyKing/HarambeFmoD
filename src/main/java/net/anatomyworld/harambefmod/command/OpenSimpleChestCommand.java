package net.anatomyworld.harambefmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.menu.SimpleChestMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;

public final class OpenSimpleChestCommand {
    private OpenSimpleChestCommand() {}

    private static final int CHEST_SIZE = 54; // 6x9 large chest

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("simplechest")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> open(ctx, defaultTexture()))
                .then(Commands.argument("texture", ResourceLocationArgument.id())
                        .executes(ctx -> open(ctx, ResourceLocationArgument.getId(ctx, "texture")))));
    }

    private static int open(CommandContext<CommandSourceStack> ctx, ResourceLocation texture) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        var provider = new SimpleMenuProvider(
                (containerId, inv, p) -> new SimpleChestMenu(containerId, inv, new SimpleContainer(CHEST_SIZE), texture),
                Component.translatable("menu." + HarambeCore.MOD_ID + ".simple_chest")
        );

        // Send the texture id to the client constructor
        player.openMenu(provider, buf -> buf.writeResourceLocation(texture));
        return 1;
    }

    private static ResourceLocation defaultTexture() {
        // 176x222 UI area on a 256x256 sheet (vanilla large chest proportions)
        return ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "textures/gui/container/simple_chest.png");
    }
}
