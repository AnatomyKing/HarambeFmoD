package net.anatomyworld.harambefmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.PortalShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Optional;
import java.util.function.Consumer;

public final class PortalIgnitionHandler {
    private PortalIgnitionHandler() {}

    public static void register() {
        NeoForge.EVENT_BUS.register(PortalIgnitionHandler.class);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        if (e.getLevel().isClientSide()) return;
        Level level = (Level) e.getLevel();
        if (!net.anatomyworld.harambefmod.Config.isOverworldLike(level.dimension())) return;

        ItemStack stack = e.getItemStack();
        boolean flint = stack.is(Items.FLINT_AND_STEEL);
        boolean charge = stack.is(Items.FIRE_CHARGE);
        if (!flint && !charge) return;

        BlockPos target = e.getPos().relative(e.getFace());
        if (!level.getBlockState(target).canBeReplaced()) return;

        // Axis from player yaw -> better than hardcoding X then Z
        Direction.Axis yawAxis = Direction.fromYRot(e.getEntity().getYRot()).getAxis();

        Optional<PortalShape> shape =
                tryFindEmptyPortal(level, target, yawAxis)
                        .or(() -> tryFindEmptyPortal(level, target, (yawAxis == Direction.Axis.X) ? Direction.Axis.Z : Direction.Axis.X));
        if (shape.isEmpty()) return;

        // cancel vanilla fire and report success so client animates
        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.SUCCESS_SERVER);

        Player player = e.getEntity();
        boolean creative = player.getAbilities().instabuild;

        if (flint && !creative) {
            Consumer<Item> onBreak = it -> {};
            stack.hurtAndBreak(1, (ServerLevel) level,
                    (player instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null),
                    onBreak);
        } else if (charge && !creative) {
            stack.shrink(1);
        }

        placePortalBlocks(shape.get(), level);
        NeoForge.EVENT_BUS.post(new BlockEvent.PortalSpawnEvent(level, target, Blocks.NETHER_PORTAL.defaultBlockState(), shape.get()));
    }

    private static Optional<PortalShape> tryFindEmptyPortal(LevelAccessor level, BlockPos pos, Direction.Axis axis) {
        try { return PortalShape.findEmptyPortalShape(level, pos, axis); }
        catch (Throwable t) { return Optional.empty(); }
    }

    // Works across minor mapping changes (createPortalBlocks vs placePortalBlocks)
    private static void placePortalBlocks(PortalShape shape, Level level) {
        try {
            PortalShape.class.getMethod("createPortalBlocks", LevelAccessor.class).invoke(shape, level);
        } catch (NoSuchMethodException e) {
            try { PortalShape.class.getMethod("placePortalBlocks", LevelAccessor.class).invoke(shape, level); }
            catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }
}
