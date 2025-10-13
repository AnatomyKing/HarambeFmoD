package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.entity.FactionCatalystBlockEntity;
import net.anatomyworld.harambefmod.faction.CatalystRegistry;
import net.anatomyworld.harambefmod.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Locale;

/**
 * Sneak-right-click on the catalyst toggles the zone overlay for everyone.
 * Normal empty-hand right-click consumes exactly one level and uploads its XP -> time.
 *
 * XP math matches vanilla:
 *   XP to next level: 0–15: 2L+7, 16–30: 5L−38, 31+: 9L−158
 *   Total XP at level L: 0–16: L^2+6L; 17–31: (5L^2−81L+720)/2; 32+: (9L^2−325L+4440)/2
 * Conversion: 1 XP point = 1.2 seconds of protection time.
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

    /** Empty-hand right click: sneak => toggle overlay, else => upload 1 level worth of XP. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            // ---- overlay toggle (unchanged) ----
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof FactionCatalystBlockEntity fbe) {
                    boolean newVisible = !fbe.isOverlayVisible();
                    fbe.setOverlayVisible(newVisible);
                    fbe.setChanged();
                    ModNetworking.sendCatalystOverlayToDimension((ServerLevel) level, pos, newVisible);
                }
                return InteractionResult.SUCCESS_SERVER;
            } else {
                return InteractionResult.SUCCESS;
            }
        }

        // ---- normal empty-hand right-click: upload 1 level worth of XP ----
        if (!level.isClientSide) {
            ServerPlayer sp = (player instanceof ServerPlayer) ? (ServerPlayer) player : null;
            if (sp == null) return InteractionResult.PASS;

            if (sp.experienceLevel < 1) {
                sp.displayClientMessage(Component.literal("You need at least 1 level to upload XP."), true);
                return InteractionResult.SUCCESS_SERVER; // handled
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof FactionCatalystBlockEntity fbe)) return InteractionResult.PASS;

            // Measure total XP before, remove exactly one level using vanilla semantics,
            // then compute actual XP points removed (fair & exact), and convert to time.
            int before = totalXp(sp);
            sp.giveExperienceLevels(-1); // guarantees level drops by 1, preserves progress fraction
            int after = totalXp(sp);
            int removed = Math.max(0, before - after);

            if (removed <= 0) {
                // Extremely unlikely, but protect against oddities.
                sp.displayClientMessage(Component.literal("XP upload failed."), true);
                return InteractionResult.SUCCESS_SERVER;
            }

            long deltaMs = (long) removed * 1200L; // 1 XP = 1.2s
            ServerLevel sl = (ServerLevel) level;

            var updated = CatalystRegistry.extendExpiryAllowInactiveAndGet(sl.dimension(), pos, deltaMs);
            if (updated != null) {
                fbe.setExpiresAtMs(updated.expiresAtMs());
                fbe.setChanged();

                double secsAdded = removed * 1.2;
                long remainMs = Math.max(0L, updated.expiresAtMs() - System.currentTimeMillis());
                String msg = String.format(Locale.ROOT,
                        "Uploaded %,d XP (+%.1fs). New remaining: %s",
                        removed, secsAdded, formatShortDuration(remainMs));
                sp.displayClientMessage(Component.literal(msg), true);

                HarambeCore.LOGGER.debug("[CATALYST] XP upload +{}ms at {} (removedXP={})",
                        deltaMs, pos, removed);
            } else {
                sp.displayClientMessage(Component.literal("This catalyst could not accept more time."), true);
            }
            return InteractionResult.SUCCESS_SERVER;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    /* ----------------------- XP math helpers ----------------------- */

    /** XP needed to go from level L to L+1. */
    private static int xpToNext(int level) {
        if (level >= 0 && level <= 15) return 2 * level + 7;
        if (level <= 30)               return 5 * level - 38;
        return 9 * level - 158;
    }

    /** Total XP needed to reach level L (exact, integer). */
    private static int totalXpForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            // (5L^2 − 81L + 720) / 2
            return (5 * level * level - 81 * level + 720) / 2;
        } else {
            // (9L^2 − 325L + 4440) / 2
            return (9 * level * level - 325 * level + 4440) / 2;
        }
    }

    /** Player's current total XP points (level base + progress within the level). */
    private static int totalXp(Player p) {
        int lvl = p.experienceLevel;
        int base = totalXpForLevel(lvl);
        int toNext = xpToNext(lvl);
        // progress is [0,1); multiply then floor to points
        int inLevel = (int) Math.floor(p.experienceProgress * toNext + 1e-6);
        return base + inLevel;
    }

    /* ----------------------- time formatting ----------------------- */

    private static String formatShortDuration(long ms) {
        if (ms <= 0) return "00:00";
        long totalSec = ms / 1000L;
        long h = totalSec / 3600L;
        long m = (totalSec % 3600L) / 60L;
        long s = totalSec % 60L;
        if (h > 0) return String.format(Locale.ROOT, "%dh %02dm", h, m);
        return String.format(Locale.ROOT, "%02d:%02d", m, s);
    }
}
