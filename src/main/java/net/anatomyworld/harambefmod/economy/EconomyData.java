package net.anatomyworld.harambefmod.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Global, disk-backed balances keyed by player UUID.
 * Stored on the Overworld's data storage (never fully unloads).
 */
public final class EconomyData extends SavedData {

    /** UUID codec that stores as canonical string in JSON. */
    private static final Codec<UUID> UUID_STRING_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                try { return DataResult.success(UUID.fromString(s)); }
                catch (IllegalArgumentException ex) { return DataResult.error(() -> "Invalid UUID: " + s); }
            },
            UUID::toString
    );

    /** Modern 1.21.x SavedDataType with context-aware codec. */
    public static final SavedDataType<EconomyData> TYPE = new SavedDataType<>(
            "harambefmod/economy",
            EconomyData::new, // ctor with SavedData.Context
            ctx -> RecordCodecBuilder.create(b -> b.group(
                    Codec.unboundedMap(UUID_STRING_CODEC, Codec.LONG)
                            .fieldOf("balances")
                            .forGetter(d -> d.balances)
            ).apply(b, balances -> new EconomyData(ctx, balances))),
            null // no migration
    );

    /* ------------------------------------------------------ */

    private final Map<UUID, Long> balances;

    /** Required by SavedDataType: empty instance on new worlds. */
    private EconomyData(SavedData.Context ctx) {
        this.balances = new HashMap<>();
    }

    /** Used by codec load path. */
    private EconomyData(SavedData.Context ctx, Map<UUID, Long> balances) {
        this.balances = new HashMap<>(Objects.requireNonNull(balances));
    }

    /* ------------------- static access -------------------- */

    /** Get (or create) the singleton economy store attached to the OVERWORLD. */
    public static EconomyData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /* ------------------- core operations ------------------ */

    public long getBalance(UUID id) {
        return balances.getOrDefault(id, 0L);
    }

    /** Sets absolute balance (clamped at 0). */
    public void setBalance(UUID id, long value) {
        long v = Math.max(0L, value);
        if (v == 0L) {
            if (balances.remove(id) != null) setDirty();
        } else {
            Long prev = balances.put(id, v);
            if (prev == null || prev != v) setDirty();
        }
    }

    /** Adds (saturating on overflow). Returns new balance. */
    public long add(UUID id, long delta) {
        if (delta <= 0) return getBalance(id);
        long cur = getBalance(id);
        long sum;
        try {
            sum = Math.addExact(cur, delta);
        } catch (ArithmeticException overflow) {
            sum = Long.MAX_VALUE;
        }
        setBalance(id, sum);
        return sum;
    }

    /** Removes up to amount, never below 0. Returns new balance. */
    public long remove(UUID id, long amount) {
        if (amount <= 0) return getBalance(id);
        long cur = getBalance(id);
        long next = Math.max(0L, cur - amount);
        setBalance(id, next);
        return next;
    }
}
