package net.anatomyworld.harambefmod.faction;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Team;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class FactionProtectionEvents {
    private FactionProtectionEvents() {}

    public static Faction playerFaction(Player p) {
        Team t = p.getTeam();
        return (t == null) ? null : Faction.fromTeamName(t.getName()); // 1.21: getName() -> String
    }

    private static void denied(Player p, String msg) {
        p.displayClientMessage(Component.literal(msg).withStyle(ChatFormatting.RED), true);
    }
    private static void denied(Player p) { denied(p, "This area is protected by another faction."); }

    private static Faction catalystFactionFromItem(Item it) {
        if (it == ModBlocks.BELMONT_CATALYST.get().asItem())  return Faction.BELMONT;
        if (it == ModBlocks.DYNASTY_CATALYST.get().asItem())  return Faction.DYNASTY;
        if (it == ModBlocks.IMPERIUM_CATALYST.get().asItem()) return Faction.IMPERIUM;
        if (it == ModBlocks.MISCHIEF_CATALYST.get().asItem()) return Faction.MISCHIEF;
        return null;
    }

    /* ------------ PRE-PLACE: cancel rivals + overlap BEFORE item use (item stays in hand) ------------ */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        Level level = e.getLevel();
        if (level.isClientSide) return;

        // 1) Pre-place checks ONLY if holding a catalyst
        Player player = e.getEntity();
        ItemStack held = player.getItemInHand(e.getHand());
        Faction placedFaction = catalystFactionFromItem(held.getItem());
        if (placedFaction != null) {
            BlockPos placeAt = e.getPos().relative(e.getFace());

            var zone = FactionProtection.findZone(level.dimension(), placeAt);
            if (zone != null && zone.faction() != placedFaction) {
                e.setCanceled(true);
                e.setCancellationResult(InteractionResult.FAIL);
                denied(player, "You cannot place a rival catalyst in a protected area.");
                return; // pre-cancel => no item consumption
            }
            if (FactionProtection.wouldOverlapOtherFaction(level.dimension(), placeAt, placedFaction)) {
                e.setCanceled(true);
                e.setCancellationResult(InteractionResult.FAIL);
                denied(player, "This catalyst would overlap a rival protected area.");
                return;
            }
        }

        // 2) Interaction gating for foreign-protected players (adventure visitors)
        BlockPos pos = e.getPos();
        BlockState state = level.getBlockState(pos);
        Block b = state.getBlock();

        boolean isStaticMenuBlock =
                b instanceof CraftingTableBlock
                        || b instanceof StonecutterBlock
                        || b instanceof LoomBlock
                        || b instanceof AnvilBlock
                        || b instanceof GrindstoneBlock
                        || b instanceof CartographyTableBlock
                        || b instanceof SmithingTableBlock
                        || b instanceof EnchantingTableBlock;

        BlockEntity be = level.getBlockEntity(pos);
        boolean hasMenuProviderBE = be instanceof MenuProvider;

        boolean isDoorLike = b instanceof DoorBlock || b instanceof TrapDoorBlock || b instanceof FenceGateBlock;
        boolean isRedstoneToggle = b instanceof LeverBlock || b instanceof ButtonBlock || b instanceof NoteBlock;

        boolean interactive = isStaticMenuBlock || hasMenuProviderBE || isDoorLike || isRedstoneToggle;
        if (!interactive) return;

        boolean forced = (player instanceof net.minecraft.server.level.ServerPlayer sp)
                && FactionProtectionGameplay.isForcedAdventure(sp);

        boolean foreignInZone = false;
        var z = FactionProtection.findZone(level.dimension(), pos);
        if (z != null) {
            Faction pf = playerFaction(player);
            foreignInZone = (pf == null || pf != z.faction());
        }

        if (forced || foreignInZone) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.FAIL);
            denied(player);
        }
    }

    /* -------------------- MELEE -------------------- */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent e) {
        Player player = e.getEntity();
        if (player.level().isClientSide) return;

        boolean forced = (player instanceof net.minecraft.server.level.ServerPlayer sp)
                && FactionProtectionGameplay.isForcedAdventure(sp);

        var zone = FactionProtection.findZone(player.level().dimension(), e.getTarget().blockPosition());
        boolean foreignInZone = false;
        if (zone != null) {
            Faction pf = playerFaction(player);
            foreignInZone = (pf == null || pf != zone.faction());
        }

        if (forced || foreignInZone) {
            e.setCanceled(true);
            denied(player);
        }
    }

    /* -------------------- RANGED / ANY DAMAGE FROM A PLAYER -------------------- */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent e) {
        Entity src = e.getSource().getEntity();
        if (!(src instanceof net.minecraft.server.level.ServerPlayer sp)) return;
        if (sp.level().isClientSide) return;

        if (FactionProtectionGameplay.isForcedAdventure(sp)) {
            e.setCanceled(true);
            e.setAmount(0.0F);
        }
    }

    /* -------------------- SAFETY NET: late cancel (still prevents block place) -------------------- */
    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent e) {
        Level level = (Level) e.getLevel();
        if (level.isClientSide) return;
        if (!(e.getEntity() instanceof Player player)) return;

        Block placed = e.getPlacedBlock().getBlock();
        Faction placedFaction =
                (placed == ModBlocks.BELMONT_CATALYST.get())  ? Faction.BELMONT
                        : (placed == ModBlocks.DYNASTY_CATALYST.get())  ? Faction.DYNASTY
                        : (placed == ModBlocks.IMPERIUM_CATALYST.get()) ? Faction.IMPERIUM
                        : (placed == ModBlocks.MISCHIEF_CATALYST.get()) ? Faction.MISCHIEF
                        : null;

        if (placedFaction == null) return;

        var zone = FactionProtection.findZone(level.dimension(), e.getPos());
        if (zone != null && zone.faction() != placedFaction) {
            e.setCanceled(true);
            denied(player, "You cannot place a rival catalyst in a protected area.");
            return;
        }
        if (FactionProtection.wouldOverlapOtherFaction(level.dimension(), e.getPos(), placedFaction)) {
            e.setCanceled(true);
            denied(player, "This catalyst would overlap a rival protected area.");
        }
        // Pre-place handler above guarantees no item loss; this is backstop only.
    }
}
