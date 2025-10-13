package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.entity.FactionCatalystBlockEntity;
import net.anatomyworld.harambefmod.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Sneak-right-click on the catalyst toggles the zone overlay for everyone.
 * Non-sneaking right-click is left as PASS for future features.
 */
public abstract class FactionCatalystBlock extends SculkCatalystBlock implements EntityBlock {
    protected FactionCatalystBlock(Properties props) { super(props); }

    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return level.isClientSide ? null
                : (lvl, p, st, be) -> {
            if (be instanceof FactionCatalystBlockEntity f) {
                FactionCatalystBlockEntity.serverTick(lvl, p, st, f);
            }
        };
    }

    /* ===================== 1.21 interaction overrides ===================== */

    /** Item-in-hand right click. We handle sneak-click here too so it works even while holding items. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            // We don't consume non-sneak interactions; future features can handle normal right-click.
            return InteractionResult.PASS;
        }

        // Toggle overlay on server, mirror a "success" on client for hand swing.
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FactionCatalystBlockEntity fbe) {
                boolean newVisible = !fbe.isOverlayVisible();
                fbe.setOverlayVisible(newVisible);
                fbe.setChanged();
                ModNetworking.sendCatalystOverlayToDimension((net.minecraft.server.level.ServerLevel) level, pos, newVisible);
            }
            return InteractionResult.SUCCESS_SERVER; // server-side success (1.21)
        } else {
            return InteractionResult.SUCCESS; // client-side success (hand animation)
        }
    }

    /** Empty-hand right click path. Same behavior: only on sneak. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FactionCatalystBlockEntity fbe) {
                boolean newVisible = !fbe.isOverlayVisible();
                fbe.setOverlayVisible(newVisible);
                fbe.setChanged();
                ModNetworking.sendCatalystOverlayToDimension((net.minecraft.server.level.ServerLevel) level, pos, newVisible);
            }
            return InteractionResult.SUCCESS_SERVER;
        } else {
            return InteractionResult.SUCCESS;
        }
    }
}
