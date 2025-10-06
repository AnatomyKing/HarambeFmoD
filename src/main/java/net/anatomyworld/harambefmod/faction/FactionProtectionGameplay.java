package net.anatomyworld.harambefmod.faction;

import net.anatomyworld.harambefmod.effect.ModMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class FactionProtectionGameplay {
    private FactionProtectionGameplay() {}

    /** 72 hours in seconds; API uses ticks. */
    public static final int PROTECTION_SECONDS = 259_200;
    private static final int PROTECTION_TICKS = PROTECTION_SECONDS * 20;

    /* Persistent-data keys */
    static final String NBT_FORCED_ADVENTURE = "harambefmodForcedAdventure";
    static final String NBT_PREV_GAMEMODE   = "harambefmodPrevGM";

    /* ---------------- steady-state (each tick) ---------------- */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || sp.level().isClientSide) return;
        reevaluateNow(sp);
    }

    /* ---------------- public helpers ---------------- */

    /** Re-applies effects and gamemode for the player's current position immediately. */
    public static void reevaluateNow(ServerPlayer sp) {
        var zone = FactionProtection.findZone(sp.level().dimension(), sp.blockPosition());

        if (zone != null) {
            // Ensure the zone's protection effect stays on (owners and visitors).
            switch (zone.faction()) {
                case BELMONT  -> ensureEffect(sp, ModMobEffects.BELMONT_PROTECTION);
                case DYNASTY  -> ensureEffect(sp, ModMobEffects.DYNASTY_PROTECTION);
                case IMPERIUM -> ensureEffect(sp, ModMobEffects.IMPERIUM_PROTECTION);
                case MISCHIEF -> ensureEffect(sp, ModMobEffects.MISCHIEF_PROTECTION);
            }

            // Owners stay free; non-owners are forced Adventure while inside.
            Faction pf = FactionProtectionEvents.playerFaction(sp);
            boolean isOwner = pf != null && pf == zone.faction();
            if (!isOwner) {
                forceAdventure(sp);
            } else {
                maybeRestoreGamemode(sp);
            }
        } else {
            // Left all zones: strip effects and restore previous mode if we forced it.
            FactionProtectionUtil.removeAllProtectionEffects(sp);
            maybeRestoreGamemode(sp);
        }
    }

    /** Hard reset used after death/cloning to avoid stuck effects or mode. */
    public static void resetProtectionState(ServerPlayer sp) {
        // Remove all four effects (uses Holder signature in 1.21).
        FactionProtectionUtil.removeAllProtectionEffects(sp);
        // If we previously forced ADVENTURE, put them back to the stored GM (or SURVIVAL).
        if (isForcedAdventure(sp)) {
            maybeRestoreGamemode(sp);
        }
        // Clear flags either way to start clean.
        CompoundTag tag = sp.getPersistentData();
        tag.remove(NBT_FORCED_ADVENTURE);
        tag.remove(NBT_PREV_GAMEMODE);
    }

    /* ---------------- internals ---------------- */

    private static void ensureEffect(ServerPlayer sp, Holder<MobEffect> effect) {
        if (effect == null) return;
        if (sp.getEffect(effect) == null) {
            // (holder, duration, amplifier, ambient, showParticles, showIcon)
            sp.addEffect(new MobEffectInstance(effect, PROTECTION_TICKS, 0, false, true, true));
        }
    }

    public static boolean isForcedAdventure(ServerPlayer sp) {
        CompoundTag tag = sp.getPersistentData();
        return readBool(tag, NBT_FORCED_ADVENTURE);
    }

    private static void forceAdventure(ServerPlayer sp) {
        CompoundTag tag = sp.getPersistentData();
        if (!readBool(tag, NBT_FORCED_ADVENTURE)) {
            writeBool(tag, NBT_FORCED_ADVENTURE, true);
            GameType cur = sp.gameMode.getGameModeForPlayer();
            writeInt(tag, NBT_PREV_GAMEMODE, cur == null ? GameType.SURVIVAL.getId() : cur.getId());
        }
        if (sp.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) {
            sp.setGameMode(GameType.ADVENTURE);
        }
    }

    private static void maybeRestoreGamemode(ServerPlayer sp) {
        if (!isForcedAdventure(sp)) return;
        CompoundTag tag = sp.getPersistentData();

        int prevId = readInt(tag, NBT_PREV_GAMEMODE, GameType.SURVIVAL.getId());
        GameType prev = GameType.byId(prevId);
        if (prev == null) prev = GameType.SURVIVAL;

        if (sp.gameMode.getGameModeForPlayer() != prev) sp.setGameMode(prev);
        tag.remove(NBT_FORCED_ADVENTURE);
        tag.remove(NBT_PREV_GAMEMODE);
    }

    /* ----- NBT utils (robust across mappings) ----- */

    private static boolean readBool(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t instanceof NumericTag num) return num.byteValue() != 0;
        return false;
    }

    private static void writeBool(CompoundTag tag, String key, boolean v) {
        tag.putByte(key, (byte) (v ? 1 : 0));
    }

    private static int readInt(CompoundTag tag, String key, int def) {
        Tag t = tag.get(key);
        if (t instanceof IntTag i) return i.intValue();
        if (t instanceof NumericTag n) return (int) n.longValue();
        return def;
    }

    private static void writeInt(CompoundTag tag, String key, int v) {
        tag.putInt(key, v);
    }
}
