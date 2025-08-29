// src/main/java/net/anatomyworld/harambefmod/item/boat/MusavaccaBoatItem.java
package net.anatomyworld.harambefmod.item.boat;

import net.anatomyworld.harambefmod.entity.ModEntities;
import net.anatomyworld.harambefmod.entity.boat.musavacca.MusavaccaBoat;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class MusavaccaBoatItem extends Item {
    public MusavaccaBoatItem(Properties props) { super(props); }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level,
                                          @NotNull Player player,
                                          @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        BlockHitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() == HitResult.Type.MISS) return InteractionResult.PASS;

        if (!level.isClientSide) {
            // Construct with THIS item as the drop supplier (correct for 1.21.8)
            MusavaccaBoat boat = new MusavaccaBoat(ModEntities.MUSAVACCA_BOAT.get(), level, () -> this);

            boat.setPos(hit.getLocation().x, hit.getLocation().y + 0.10, hit.getLocation().z);
            boat.setYRot(player.getYRot());
            boat.setXRot(0.0F);

            level.addFreshEntity(boat);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOAT_PADDLE_WATER, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        // SUCCESS client / CONSUME server
        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }
}
