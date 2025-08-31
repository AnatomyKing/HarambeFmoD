package net.anatomyworld.harambefmod.entity.boat.musavacca;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** Vanilla-behavior boat; we don't override seats/placement/physics. */
public class MusavaccaBoat extends AbstractBoat {
    public MusavaccaBoat(EntityType<? extends MusavaccaBoat> type, Level level, Supplier<Item> item) {
        super(type, level, item);
    }

    public MusavaccaBoat(EntityType<? extends MusavaccaBoat> type, Level level) {
        // BoatItem will pass the correct supplier in normal use; OAK_BOAT is a harmless default.
        this(type, level, () -> Items.OAK_BOAT);
    }

    @Override
    protected double rideHeight(EntityDimensions dims) {
        // Matches vanilla baseline lift used by Boat
        return 0.12D;
    }
}
