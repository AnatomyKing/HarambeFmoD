// src/main/java/net/anatomyworld/harambefmod/entity/boat/musavacca/MusavaccaBoat.java
package net.anatomyworld.harambefmod.entity.boat.musavacca;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * Boat that rides ~7 px higher when in/touching water (true physics: hitbox & riders move).
 * Works on NeoForge 1.21.8 without relying on removed methods.
 */
public class MusavaccaBoat extends AbstractBoat {

    /** 7 px visual offset in block units (16 px = 1 block). */
    private static final double WATER_LIFT_BLOCKS = 7.0 / 16.0;

    public MusavaccaBoat(EntityType<? extends MusavaccaBoat> type, Level level, Supplier<Item> item) {
        super(type, level, item);
    }

    public MusavaccaBoat(EntityType<? extends MusavaccaBoat> type, Level level) {
        this(type, level, () -> Items.OAK_BOAT);
    }

    /** Keep riders on top of the hitbox (vanilla feel). */
    @Override
    protected double rideHeight(EntityDimensions dims) {
        return dims.height();
    }

    /** Subtle stern bias with a single rider. */
    @Override
    protected float getSinglePassengerXOffset() {
        return -0.30F;
    }

    @Override
    public void tick() {
        // Run vanilla tick (handles inputs, buoyancy, movement, etc.)
        super.tick();

        // Detect if we're in/at water (covers "under water" and resting on the surface).
        if (isInOrTouchingWater()) {
            // Vanilla "settle" height when first touching water is:
            //   getWaterLevelAbove() - getBbHeight() + 0.101
            // We raise that by WATER_LIFT_BLOCKS so the boat rides higher.
            final double targetY = (double) this.getWaterLevelAbove()
                    - this.getBbHeight()
                    + 0.101
                    + WATER_LIFT_BLOCKS;

            // Smoothly converge to the target (prevents snapping/jitter).
            final double y = this.getY();
            final double newY = y + (targetY - y) * 0.7; // approach ~70% per tick

            if (Math.abs(newY - y) > 1.0e-4) {
                // Neutralize vertical velocity so vanilla buoyancy doesn't immediately "undo" us.
                Vec3 v = this.getDeltaMovement();
                this.setDeltaMovement(v.x, 0.0, v.z);
                this.setPos(this.getX(), newY, this.getZ());
            }
        }
    }

    /** True if the boat is under water or touching water at/below its feet. */
    private boolean isInOrTouchingWater() {
        if (this.isUnderWater()) return true; // UNDER_WATER or UNDER_FLOWING_WATER

        // Check the block the boat occupies and the one just below for water.
        final BlockPos here = this.blockPosition();
        final FluidState fsHere = this.level().getFluidState(here);
        if (fsHere.is(FluidTags.WATER)) return true;

        final FluidState fsBelow = this.level().getFluidState(here.below());
        return fsBelow.is(FluidTags.WATER);
    }
}
