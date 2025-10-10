package net.anatomyworld.harambefmod.faction;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.effect.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.ExplosionKnockbackEvent;
import org.jetbrains.annotations.Nullable;

public final class FactionProtectionEvents {
    private FactionProtectionEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.register(FactionProtectionEvents.class);
        HarambeCore.LOGGER.info("[PROTECT] FactionProtectionEvents registered.");
    }

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

    /** Which aura a player currently has (client and server helpers). */
    private static @Nullable Faction zoneFactionViaEffects(Player p) {
        if (p.hasEffect(ModMobEffects.BELMONT_PROTECTION)) return Faction.BELMONT;
        if (p.hasEffect(ModMobEffects.DYNASTY_PROTECTION)) return Faction.DYNASTY;
        if (p.hasEffect(ModMobEffects.IMPERIUM_PROTECTION)) return Faction.IMPERIUM;
        if (p.hasEffect(ModMobEffects.MISCHIEF_PROTECTION)) return Faction.MISCHIEF;
        return null;
    }

    /* ------------------------------------------------------------
     * FEED THE ZONE TIMER ON ANY XP-CAPABLE DEATH NEAR A CATALYST
     * (don’t use LivingExperienceDropEvent; sculk eats the XP orbs)
     * ------------------------------------------------------------ */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent e) {
        if (!(e.getEntity().level() instanceof ServerLevel sl)) return;

        LivingEntity victim = e.getEntity();
        if (!wouldDropXp(victim)) return; // match “entity that drops XP”

        // Find nearest catalyst (active or inactive) within bloom radius
        BlockPos deathPos = victim.blockPosition();
        var entry = CatalystRegistry.nearestWithinInclusive(
                sl.dimension(), deathPos, CatalystRegistry.BLOOM_RADIUS_BLOCKS);
        if (entry == null) return;

        boolean hostile = isHostileLike(victim);
        long delta = hostile ? CatalystRegistry.BONUS_HOSTILE_MS : CatalystRegistry.BONUS_PASSIVE_MS;

        // Extend and get the UPDATED entry (old entry object is stale)
        var updated = CatalystRegistry.extendExpiryAllowInactiveAndGet(sl.dimension(), entry.pos(), delta);
        if (updated == null) return;

        HarambeCore.LOGGER.debug("[CATALYST] bloom feed +{}ms at {} (faction={}, hostile={})",
                delta, updated.pos(), updated.faction(), hostile);

        // Immediately push/extend aura to all players currently inside the zone
        refreshAuraForPlayersInZone(sl, updated);
    }

    private static boolean wouldDropXp(LivingEntity v) {
        // Players: drop XP if they have any XP/levels
        if (v instanceof ServerPlayer sp) {
            return sp.experienceLevel > 0 || sp.experienceProgress > 0f;
        }
        // Animals: only adults drop XP
        if (v instanceof Animal an) return !an.isBaby();
        // Other mobs (villagers, golems, monsters, etc.): usually drop XP
        return v instanceof Mob;
    }

    private static boolean isHostileLike(LivingEntity v) {
        // Hostile mobs OR players count as "hostile" for 12s, animals as "passive" for 6s
        if (v instanceof Enemy) return true;
        if (v instanceof ServerPlayer) return true;
        if (v instanceof Animal) return false;
        // other xp-dropping non-animals (villagers/golems) – treat as hostile for consistency
        return true;
    }

    /** Push the current remaining time as an effect to all players standing in the given entry's zone. */
    private static void refreshAuraForPlayersInZone(ServerLevel sl, CatalystRegistry.Entry entry) {
        long now = System.currentTimeMillis();
        long remainingMs = Math.max(0L, entry.expiresAtMs() - now);
        int ticks = (int) Math.min(Integer.MAX_VALUE, remainingMs / 50L);
        Holder<MobEffect> eff = switch (entry.faction()) {
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

            if (ticks <= 0) {
                sp.removeEffect(eff);
            } else {
                MobEffectInstance cur = sp.getEffect(eff);
                if (cur == null || Math.abs(cur.getDuration() - ticks) > 2) {
                    sp.addEffect(new MobEffectInstance(eff, ticks, 0, false, true, true));
                }
            }

            // keep exactly one aura
            if (!eff.equals(ModMobEffects.BELMONT_PROTECTION))  sp.removeEffect(ModMobEffects.BELMONT_PROTECTION);
            if (!eff.equals(ModMobEffects.DYNASTY_PROTECTION))  sp.removeEffect(ModMobEffects.DYNASTY_PROTECTION);
            if (!eff.equals(ModMobEffects.IMPERIUM_PROTECTION)) sp.removeEffect(ModMobEffects.IMPERIUM_PROTECTION);
            if (!eff.equals(ModMobEffects.MISCHIEF_PROTECTION)) sp.removeEffect(ModMobEffects.MISCHIEF_PROTECTION);
        }
    }

    /* ------------------------------------------------------------
     * Breaking / left-clicking
     * ------------------------------------------------------------ */
    @SubscribeEvent
    public static void onLeftClickBlock(LeftClickBlock e) {
        if (e.getLevel().isClientSide()) {
            var f = zoneFactionViaEffects(e.getEntity());
            if (f != null && !isOwner(e.getEntity(), f)) {
                e.setCanceled(true);
                return;
            }
        } else if (e.getLevel() instanceof ServerLevel sl) {
            var entry = zoneAt(sl, e.getPos());
            if (entry == null) return;
            if (e.getEntity() instanceof ServerPlayer sp && isOwner(sp, entry.faction())) return;
            e.setCanceled(true);
            if (e.getEntity() instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundBlockUpdatePacket(sl, e.getPos()));
            }
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed e) {
        if (!(e.getEntity().level() instanceof ServerLevel sl)) return;
        BlockPos pos = e.getPosition().orElse(e.getEntity().blockPosition());
        var entry = zoneAt(sl, pos);
        if (entry == null) return;
        if (e.getEntity() instanceof ServerPlayer sp && isOwner(sp, entry.faction())) return;
        e.setNewSpeed(0.0F);
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        var entry = zoneAt(sl, e.getPos());
        if (entry == null) return;
        var sp = (e.getPlayer() instanceof ServerPlayer p) ? p : null;
        if (isOwner(sp, entry.faction())) return;
        e.setCanceled(true);
    }

    /* ------------------------------------------------------------
     * Placing / right-clicking (reach-in protected)
     * ------------------------------------------------------------ */
    @SubscribeEvent
    public static void onRightClickBlock(RightClickBlock e) {
        if (e.getLevel().isClientSide()) {
            var f = zoneFactionViaEffects(e.getEntity());
            if (f != null && !isOwner(e.getEntity(), f)) {
                e.setCanceled(true);
                e.setCancellationResult(InteractionResult.FAIL);
            }
            return;
        }

        if (!(e.getLevel() instanceof ServerLevel sl)) return;

        BlockPos clicked = e.getPos();
        var face = e.getFace();
        BlockPos placePos = (face != null) ? clicked.relative(face) : clicked;

        var entryClicked = zoneAt(sl, clicked);
        var entryPlace  = zoneAt(sl, placePos);
        var entry = (entryPlace != null) ? entryPlace : entryClicked;
        if (entry == null) return;

        var sp = (e.getEntity() instanceof ServerPlayer p) ? p : null;
        if (isOwner(sp, entry.faction())) return;

        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.FAIL);

        if (sp != null) {
            sp.connection.send(new ClientboundBlockUpdatePacket(sl, clicked));
            if (!placePos.equals(clicked)) {
                sp.connection.send(new ClientboundBlockUpdatePacket(sl, placePos));
            }
            sp.inventoryMenu.broadcastChanges();
        }
    }

    /** Authoritative guard for any placement that bypasses RightClickBlock. */
    @SubscribeEvent
    public static void onEntityPlace(BlockEvent.EntityPlaceEvent e) {
        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        var entry = zoneAt(sl, e.getPos());
        if (entry == null) return;

        ServerPlayer sp = (e.getEntity() instanceof ServerPlayer p) ? p : null;
        if (isOwner(sp, entry.faction())) return;

        e.setCanceled(true);
        if (sp != null) {
            sp.connection.send(new ClientboundBlockUpdatePacket(sl, e.getPos()));
            sp.inventoryMenu.broadcastChanges();
        }
    }

    /** Beds/doors/etc that create multiple blocks at once. */
    @SubscribeEvent
    public static void onEntityMultiPlace(BlockEvent.EntityMultiPlaceEvent e) {
        if (!(e.getLevel() instanceof ServerLevel sl)) return;

        boolean touchesZone = e.getReplacedBlockSnapshots().stream()
                .anyMatch(snap -> zoneAt(sl, snap.getPos()) != null);
        if (!touchesZone) return;

        ServerPlayer sp = (e.getEntity() instanceof ServerPlayer p) ? p : null;
        if (sp != null) {
            var anyForbidden = e.getReplacedBlockSnapshots().stream().anyMatch(snap -> {
                var entry = zoneAt(sl, snap.getPos());
                return entry != null && !isOwner(sp, entry.faction());
            });
            if (!anyForbidden) return; // allow if entirely their own zone
        }

        e.setCanceled(true);
        if (sp != null) sp.inventoryMenu.broadcastChanges();
    }

    /** Buckets/fluids that would create blocks (obsidian/cobble, flowing water/lava). */
    @SubscribeEvent
    public static void onFluidPlace(BlockEvent.FluidPlaceBlockEvent e) {
        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        var entry = zoneAt(sl, e.getPos());
        if (entry == null) return;
        e.setCanceled(true);
    }

    /* ------------------------------------------------------------
     * Items used in air (inside-zone only)
     * ------------------------------------------------------------ */
    @SubscribeEvent
    public static void onRightClickItem(RightClickItem e) {
        if (e.getLevel().isClientSide()) {
            var f = zoneFactionViaEffects(e.getEntity());
            if (f != null && !isOwner(e.getEntity(), f)) {
                e.setCanceled(true);
                e.setCancellationResult(InteractionResult.FAIL);
            }
            return;
        }

        if (e.getLevel() instanceof ServerLevel sl) {
            var entry = zoneAt(sl, e.getEntity().blockPosition());
            if (entry == null) return;
            if (e.getEntity() instanceof ServerPlayer sp && isOwner(sp, entry.faction())) return;

            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.FAIL);
            if (e.getEntity() instanceof ServerPlayer sp) {
                sp.inventoryMenu.broadcastChanges();
            }
        }
    }

    /* ------------------------------------------------------------
     * Entity interactions & combat inside zones
     * ------------------------------------------------------------ */
    @SubscribeEvent
    public static void onEntityInteract(EntityInteract e) {
        if (e.getLevel().isClientSide()) {
            var f = zoneFactionViaEffects(e.getEntity());
            if (f != null && !isOwner(e.getEntity(), f)) {
                e.setCanceled(true);
                e.setCancellationResult(InteractionResult.FAIL);
            }
            return;
        }

        if (e.getLevel() instanceof ServerLevel sl) {
            var entry = zoneAt(sl, e.getTarget().blockPosition());
            if (entry == null) return;
            var sp = (e.getEntity() instanceof ServerPlayer p) ? p : null;
            if (isOwner(sp, entry.faction())) return;

            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent e) {
        if (!(e.getEntity().level() instanceof ServerLevel sl)) return;
        var entry = zoneAt(sl, e.getTarget().blockPosition());
        if (entry == null) return;
        var sp = (e.getEntity() instanceof ServerPlayer p) ? p : null;
        if (sp != null && isOwner(sp, entry.faction())) return;
        e.setCanceled(true);
    }

    /* ------------------------------------------------------------
     * Explosions: keep blocks safe + protect entities in zones
     * ------------------------------------------------------------ */
    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate e) {
        if (!(e.getLevel() instanceof ServerLevel sl)) return;

        var blocks = e.getAffectedBlocks();
        if (blocks != null && !blocks.isEmpty()) {
            blocks.removeIf(pos -> CatalystRegistry.activeEntryAt(sl.dimension(), pos) != null);
        }

        var ents = e.getAffectedEntities();
        if (ents != null && !ents.isEmpty()) {
            ents.removeIf(ent -> CatalystRegistry.activeEntryAt(sl.dimension(), ent.blockPosition()) != null);
        }
    }

    @SubscribeEvent
    public static void onExplosionKnockback(ExplosionKnockbackEvent e) {
        if (!(e.getLevel() instanceof ServerLevel sl)) return;
        var entry = CatalystRegistry.activeEntryAt(sl.dimension(), e.getAffectedEntity().blockPosition());
        if (entry == null) return;
        e.setKnockbackVelocity(Vec3.ZERO);
    }
}
