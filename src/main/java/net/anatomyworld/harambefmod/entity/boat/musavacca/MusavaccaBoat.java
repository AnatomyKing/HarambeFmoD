package net.anatomyworld.harambefmod.entity.boat.musavacca;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/** 3-seat boat that keeps vanilla AbstractBoat physics/controls. */
public class MusavaccaBoat extends AbstractBoat {
    public MusavaccaBoat(net.minecraft.world.entity.EntityType<? extends MusavaccaBoat> type,
                         Level level, Supplier<Item> item) {
        super(type, level, item);
    }
    public MusavaccaBoat(net.minecraft.world.entity.EntityType<? extends MusavaccaBoat> type, Level level) {
        this(type, level, () -> Items.AIR); // swap to your boat item supplier if desired
    }

    /* -------------------- Seating (inline: front/center/rear) -------------------- */

    /** 👉 Rider “butt height”. Increase to sit higher; decrease to sit lower. */
    public static final double SEAT_Y = 0.78D;

    // Forward is -Z in model space. Matches your Blockbench seat bones (-12, -1, +12 px).
    private static final double[] SEAT_Z = { -12.0 / 16.0, -1.0 / 16.0, 12.0 / 16.0 };

    @Override protected int getMaxPassengers() { return 3; }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(@NotNull Entity passenger,
                                                        @NotNull EntityDimensions dims,
                                                        float partialTick) {
        int idx = Math.max(0, Math.min(2, getPassengers().indexOf(passenger)));
        return new Vec3(0.0, SEAT_Y, SEAT_Z[idx]); // centered on X so no side-offset
    }

    /** Baseline ride height above boat origin (small). Seats control the true feel. */
    @Override
    protected double rideHeight(EntityDimensions dims) {
        return 0.12D;
    }

    /* Optional helper (kept for completeness). */
    public float rowingIntensity(float partialTick) {
        double speed = getDeltaMovement().horizontalDistance();
        return (float) Mth.clamp(speed * 20.0, 0.0, 1.0);
    }
}
