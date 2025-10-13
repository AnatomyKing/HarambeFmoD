package net.anatomyworld.harambefmod.faction;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.custom.FactionCatalystBlock;
import net.anatomyworld.harambefmod.effect.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityMultiPlaceEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.ExplosionKnockbackEvent;
import org.jetbrains.annotations.Nullable;

public final class FactionProtectionEvents {
    private FactionProtectionEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.register(FactionProtectionEvents.class);
        HarambeCore.LOGGER.info("[PROTECT] FactionProtectionEvents registered.");
    }

    /* ---------------- helpers ---------------- */

    private static @Nullable CatalystRegistry.Entry zoneAt(ServerLevel level, BlockPos pos) {
        return CatalystRegistry.activeEntryAt(level.dimension(), pos);
    }

    private static boolean isOwner(@Nullable Player player, Faction zoneFaction) {
        if (player == null) return false;
        var team = player.getTeam();
        if (team == null) return false;
        var pf = Faction.fromTeamName(team.getName());
        return pf != null && pf == zoneFaction;
    }

    private static @Nullable Faction zoneFactionViaEffects(Player p) {
        if (p.hasEffect(ModMobEffects.BELMONT_PROTECTION)) return Faction.BELMONT;
        if (p.hasEffect(ModMobEffects.DYNASTY_PROTECTION)) return Faction.DYNASTY;
        if (p.hasEffect(ModMobEffects.IMPERIUM_PROTECTION)) return Faction.IMPERIUM;
        if (p.hasEffect(ModMobEffects.MISCHIEF_PROTECTION)) return Faction.MISCHIEF;
        return null;
    }

    /** Full, authoritative inventory push to kill any client-side prediction/ghosts. */
    private static void hardResyncInventory(ServerPlayer sp) {
        sp.getInventory().setChanged();
        sp.inventoryMenu.broadcastChanges();
        int stateId = sp.inventoryMenu.incrementStateId();
        sp.connection.send(new ClientboundContainerSetContentPacket(
                sp.inventoryMenu.containerId,
                stateId,
                sp.inventoryMenu.getItems(),
                sp.inventoryMenu.getCarried()
        ));
    }

    /** Single place to perform the full “adventure-mode” denial + *hard* resync. */
    private static void denyLikeAdventure(@Nullable RightClickBlock e, ServerPlayer sp, ServerLevel sl, BlockPos clicked, BlockPos placePos) {
        if (e != null) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.FAIL); // ends pipeline on client, stops prediction
        }
        // Refresh both blocks (what you clicked + where the client predicted placement)
        sp.connection.send(new ClientboundBlockUpdatePacket(sl, clicked));
        if (!placePos.equals(clicked)) sp.connection.send(new ClientboundBlockUpdatePacket(sl, placePos));

        // Force an authoritative inventory reset (kills hotbar ghost even when reaching-in)
        hardResyncInventory(sp);
    }

    /* ------------------------------------------------------------
     * CATALYST PLACEMENT RULES (routed through denyLikeAdventure)
     * ------------------------------------------------------------ */

    @SubscribeEvent
    public static void onRightClickBlock(RightClickBlock e) {
        // CLIENT: gate like adventure mode for foreign active zones (stops packets early).
        if (e.getLevel().isClientSide()) {
            Player lp = e.getEntity();
            if (lp != null) {
                var aura = zoneFactionViaEffects(lp);
                if (aura != null && !isOwner(lp, aura)) {
                    e.setCanceled(true);
                    e.setCancellationResult(InteractionResult.FAIL);
                }
            }
            return;
        }

        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        ServerPlayer sp = (e.getEntity() instanceof ServerPlayer p) ? p : null;
        if (sp == null) return;

        BlockPos clicked = e.getPos();
        BlockPos placePos = e.getFace() != null ? clicked.relative(e.getFace()) : clicked;

        // Catalyst in hand? Use special validation, but DENY via the same path.
        if (isCatalystStack(e.getItemStack())) {
            Faction fac = catalystFactionFromStack(e.getItemStack());
            if (fac == null) return;

            if (!validateCatalystPlacementRivalFriendly(sl, placePos, fac, sp)) {
                denyLikeAdventure(e, sp, sl, clicked, placePos);
            }
            return;
        }

        // Non-catalyst: if either target lies in an active foreign zone, deny.
        var entryAtClick = zoneAt(sl, clicked);
        var entryAtPlace = zoneAt(sl, placePos);
        var denyEntry = entryAtPlace != null ? entryAtPlace : entryAtClick;
        if (denyEntry != null && !isOwner(sp, denyEntry.faction())) {
            denyLikeAdventure(e, sp, sl, clicked, placePos);
        }
    }

    /** Server safety: catalyst placement via other routes. */
    @SubscribeEvent
    public static void onEntityPlaceCatalyst(EntityPlaceEvent e) {
        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        Block placed = e.getPlacedBlock().getBlock();
        if (!(placed instanceof FactionCatalystBlock)) return;

        ServerPlayer sp = (e.getEntity() instanceof ServerPlayer p) ? p : null;
        Faction fac = catalystFactionFromBlock(placed);
        if (fac == null) { e.setCanceled(true); return; }

        if (!validateCatalystPlacementRivalFriendly(sl, e.getPos(), fac, sp)) {
            e.setCanceled(true);
            if (sp != null) {
                // We don't have the original clicked pos here, so use pos for both
                denyLikeAdventure(null, sp, sl, e.getPos(), e.getPos());
            }
        }
    }

    /* ------------------------------------------------------------
     * GENERAL PLACEMENT GUARDS (all blocks/fluids)
     * ------------------------------------------------------------ */

    @SubscribeEvent
    public static void onEntityPlace(EntityPlaceEvent e) {
        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        if (e.getPlacedBlock().getBlock() instanceof FactionCatalystBlock) return; // handled above

        ServerPlayer sp = (e.getEntity() instanceof ServerPlayer p) ? p : null;
        var entry = zoneAt(sl, e.getPos());
        if (entry != null && !isOwner(sp, entry.faction())) {
            e.setCanceled(true);
            if (sp != null) denyLikeAdventure(null, sp, sl, e.getPos(), e.getPos());
        }
    }

    @SubscribeEvent
    public static void onEntityMultiPlace(EntityMultiPlaceEvent e) {
        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        ServerPlayer sp = (e.getEntity() instanceof ServerPlayer p) ? p : null;

        for (net.neoforged.neoforge.common.util.BlockSnapshot snap : e.getReplacedBlockSnapshots()) {
            BlockPos pos = snap.getPos();
            var entry = zoneAt(sl, pos);
            if (entry != null && !isOwner(sp, entry.faction())) {
                e.setCanceled(true);
                if (sp != null) denyLikeAdventure(null, sp, sl, pos, pos);
                return;
            }
        }
    }

    /** Block fluid placements (flow/bucket outcome) inside protected zones. */
    @SubscribeEvent
    public static void onFluidPlace(BlockEvent.FluidPlaceBlockEvent e) {
        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        var entry = zoneAt(sl, e.getPos());
        if (entry != null) e.setCanceled(true);
    }

    /* ------------------------------------------------------------
     * FEED ZONE TIMER ON XP-CAPABLE DEATHS NEAR A CATALYST
     * ------------------------------------------------------------ */

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent e) {
        if (!(e.getEntity().level() instanceof ServerLevel sl)) return;

        LivingEntity victim = e.getEntity();
        if (!wouldDropXp(victim)) return;

        BlockPos deathPos = victim.blockPosition();
        var activeHere = CatalystRegistry.activeEntryAt(sl.dimension(), deathPos);
        var nearest = CatalystRegistry.nearestWithinInclusive(sl.dimension(), deathPos, CatalystRegistry.BLOOM_RADIUS_BLOCKS);
        if (nearest == null) return;

        if (activeHere != null && !activeHere.pos().equals(nearest.pos())) {
            var killer = e.getSource() != null ? e.getSource().getEntity() : null;
            if (killer instanceof ServerPlayer sp) {
                long remain = Math.max(0L, activeHere.expiresAtMs() - System.currentTimeMillis());
                sp.displayClientMessage(Component.literal("Zone is active (" + formatShortDuration(remain) + " left). Feed denied."), true);
            }
            return;
        }

        boolean hostile = isHostileLike(victim);
        long delta = hostile ? CatalystRegistry.BONUS_HOSTILE_MS : CatalystRegistry.BONUS_PASSIVE_MS;

        var updated = CatalystRegistry.extendExpiryAllowInactiveAndGet(sl.dimension(), nearest.pos(), delta);
        if (updated == null) return;

        var be = sl.getBlockEntity(updated.pos());
        if (be instanceof net.anatomyworld.harambefmod.block.entity.FactionCatalystBlockEntity fbe) {
            fbe.setExpiresAtMs(updated.expiresAtMs());
        }

        HarambeCore.LOGGER.debug("[CATALYST] bloom feed +{}ms at {} (faction={}, hostile={})",
                delta, updated.pos(), updated.faction(), hostile);

        refreshAuraForPlayersInZone(sl, updated);
    }

    private static boolean wouldDropXp(LivingEntity v) {
        if (v instanceof ServerPlayer sp) return sp.experienceLevel > 0 || sp.experienceProgress > 0f;
        if (v instanceof Animal an) return !an.isBaby();
        return v instanceof Mob;
    }

    private static boolean isHostileLike(LivingEntity v) {
        if (v instanceof Enemy) return true;
        if (v instanceof ServerPlayer) return true;
        if (v instanceof Animal) return false;
        return true;
    }

    private static void refreshAuraForPlayersInZone(ServerLevel sl, CatalystRegistry.Entry entry) {
        long now = System.currentTimeMillis();
        long remainingMs = Math.max(0L, entry.expiresAtMs() - now);
        int ticks = (int) Math.min(Integer.MAX_VALUE, remainingMs / 50L);
        if (ticks <= 0) return;

        var eff = switch (entry.faction()) {
            case BELMONT -> ModMobEffects.BELMONT_PROTECTION;
            case DYNASTY -> ModMobEffects.DYNASTY_PROTECTION;
            case IMPERIUM -> ModMobEffects.IMPERIUM_PROTECTION;
            case MISCHIEF -> ModMobEffects.MISCHIEF_PROTECTION;
        };

        BlockPos c = entry.pos();
        int r = CatalystRegistry.RADIUS;
        AABB box = new AABB(
                c.getX() - r, sl.getMinY(), c.getZ() - r,
                c.getX() + r + 1, sl.getMaxY(), c.getZ() + r + 1
        );

        for (ServerPlayer sp : sl.getEntitiesOfClass(ServerPlayer.class, box)) {
            int dx = Math.abs(sp.blockPosition().getX() - c.getX());
            int dz = Math.abs(sp.blockPosition().getZ() - c.getZ());
            if (dx > r || dz > r) continue;

            MobEffectInstance cur = sp.getEffect(eff);
            if (cur == null || Math.abs(cur.getDuration() - ticks) > 2) {
                sp.addEffect(new MobEffectInstance(eff, ticks, 0, false, true, true));
            }
            if (!eff.equals(ModMobEffects.BELMONT_PROTECTION))  sp.removeEffect(ModMobEffects.BELMONT_PROTECTION);
            if (!eff.equals(ModMobEffects.DYNASTY_PROTECTION))  sp.removeEffect(ModMobEffects.DYNASTY_PROTECTION);
            if (!eff.equals(ModMobEffects.IMPERIUM_PROTECTION)) sp.removeEffect(ModMobEffects.IMPERIUM_PROTECTION);
            if (!eff.equals(ModMobEffects.MISCHIEF_PROTECTION)) sp.removeEffect(ModMobEffects.MISCHIEF_PROTECTION);
        }
    }

    private static String formatShortDuration(long ms) {
        if (ms <= 0) return "00:00";
        long totalSec = ms / 1000L;
        long h = totalSec / 3600L;
        long m = (totalSec % 3600L) / 60L;
        long s = totalSec % 60L;
        if (h > 0) return String.format("%dh %02dm", h, m);
        return String.format("%02d:%02d", m, s);
    }

    /* ------------------------------------------------------------
     * ADVENTURE-MODE FEEL: deny break/attack/use if not owner
     * ------------------------------------------------------------ */

    @SubscribeEvent
    public static void onLeftClickBlock(LeftClickBlock e) {
        if (e.getLevel().isClientSide()) {
            Player lp = e.getEntity();
            if (lp != null) {
                var aura = zoneFactionViaEffects(lp);
                if (aura != null && !isOwner(lp, aura)) e.setCanceled(true);
            }
            return;
        }

        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        var entry = zoneAt(sl, e.getPos());
        if (entry == null) return;
        ServerPlayer sp = (e.getEntity() instanceof ServerPlayer p) ? p : null;
        if (!isOwner(sp, entry.faction())) {
            e.setCanceled(true);
            if (sp != null) sp.connection.send(new ClientboundBlockUpdatePacket(sl, e.getPos()));
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (!(sp.level() instanceof ServerLevel sl)) return;
        BlockPos pos = e.getPosition().orElse(sp.blockPosition());
        var entry = zoneAt(sl, pos);
        if (entry != null && !isOwner(sp, entry.faction())) e.setNewSpeed(0.0F);
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        var entry = zoneAt(sl, e.getPos());
        if (entry == null) return;
        ServerPlayer sp = (e.getPlayer() instanceof ServerPlayer p) ? p : null;
        if (!isOwner(sp, entry.faction())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(RightClickItem e) {
        if (e.getLevel().isClientSide()) {
            Player lp = e.getEntity();
            if (lp != null) {
                var aura = zoneFactionViaEffects(lp);
                if (aura != null && !isOwner(lp, aura)) {
                    e.setCanceled(true);
                    e.setCancellationResult(InteractionResult.FAIL);
                }
            }
            return;
        }

        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        var entry = zoneAt(sl, sp.blockPosition());
        if (entry != null && !isOwner(sp, entry.faction())) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.FAIL);
            hardResyncInventory(sp);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(EntityInteract e) {
        if (e.getLevel().isClientSide()) {
            Player lp = e.getEntity();
            if (lp != null) {
                var aura = zoneFactionViaEffects(lp);
                if (aura != null && !isOwner(lp, aura)) {
                    e.setCanceled(true);
                    e.setCancellationResult(InteractionResult.FAIL);
                }
            }
            return;
        }

        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        var entry = zoneAt(sl, e.getTarget().blockPosition());
        if (entry != null && !isOwner(sp, entry.faction())) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (!(sp.level() instanceof ServerLevel sl)) return;
        var entry = zoneAt(sl, e.getTarget().blockPosition());
        if (entry != null && !isOwner(sp, entry.faction())) e.setCanceled(true);
    }

    /* ---------------- catalyst helpers ---------------- */

    private static boolean isCatalystStack(net.minecraft.world.item.ItemStack stack) {
        if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem bi)) return false;
        Block b = bi.getBlock();
        return b instanceof FactionCatalystBlock
                || b == ModBlocks.BELMONT_CATALYST.get()
                || b == ModBlocks.DYNASTY_CATALYST.get()
                || b == ModBlocks.IMPERIUM_CATALYST.get()
                || b == ModBlocks.MISCHIEF_CATALYST.get();
    }

    @Nullable
    private static Faction catalystFactionFromStack(net.minecraft.world.item.ItemStack stack) {
        if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem bi)) return null;
        return catalystFactionFromBlock(bi.getBlock());
    }

    @Nullable
    private static Faction catalystFactionFromBlock(Block b) {
        if (b == ModBlocks.BELMONT_CATALYST.get())  return Faction.BELMONT;
        if (b == ModBlocks.DYNASTY_CATALYST.get())  return Faction.DYNASTY;
        if (b == ModBlocks.IMPERIUM_CATALYST.get()) return Faction.IMPERIUM;
        if (b == ModBlocks.MISCHIEF_CATALYST.get()) return Faction.MISCHIEF;
        return null;
    }

    private static boolean validateCatalystPlacementRivalFriendly(ServerLevel sl, BlockPos placePos, Faction placingFaction, @Nullable ServerPlayer actor) {
        var activeOverlap = CatalystRegistry.anyActiveOverlapping(sl.dimension(), placePos);
        if (activeOverlap != null) {
            if (actor != null) {
                long remain = Math.max(0L, activeOverlap.expiresAtMs() - System.currentTimeMillis());
                actor.displayClientMessage(Component.literal("Zone is active (" + formatShortDuration(remain) + " left). Placement locked."), true);
            }
            return false;
        }

        var anyOverlap = CatalystRegistry.anyOverlapping(sl.dimension(), placePos);
        if (anyOverlap != null && anyOverlap.faction() == placingFaction) {
            if (actor != null) actor.displayClientMessage(Component.literal("You can't place a catalyst inside your own Area."), true);
            return false;
        }

        var sameFactionInside = CatalystRegistry.sameFactionContaining(sl.dimension(), placePos, placingFaction);
        if (sameFactionInside != null) {
            if (actor != null) actor.displayClientMessage(Component.literal("You can't place a catalyst inside an existing Area of the same faction."), true);
            return false;
        }
        return true;
    }
}
