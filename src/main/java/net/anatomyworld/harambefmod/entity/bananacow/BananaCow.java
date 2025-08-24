package net.anatomyworld.harambefmod.entity.bananacow;

import net.anatomyworld.harambefmod.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BananaCow extends Cow {

    public BananaCow(EntityType<? extends BananaCow> type, Level level) {
        super(type, level);
    }

    /** Attributes for Banana Cow */
    public static @NotNull AttributeSupplier.Builder createAttributes() {
        return Cow.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    /* =========================  NO BREEDING  ========================= */

    /** Never accepts breeding food → Love mode can’t start. */
    @Override public boolean isFood(ItemStack stack) { return false; }

    /** Defensive: never behave as baby (even if someone sets NBT). */
    @Override public boolean isBaby() { return false; }

    /** Ignore attempts to make this a baby. */
    @Override public void setBaby(boolean isBaby) { /* no-op */ }

    /* =========================  LOOT  ========================= */

    /** 50% banana drop (1.21.x signature). */
    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level,
                                       @NotNull DamageSource source,
                                       boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.50F) {
            Containers.dropItemStack(level, getX(), getY(), getZ(),
                    new ItemStack(ModItems.BANANA.get()));
        }
    }
}
