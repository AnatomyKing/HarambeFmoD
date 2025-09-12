package net.anatomyworld.harambefmod.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLLoader;

public class AnyPhoneItem extends Item {
    public AnyPhoneItem(Properties props) { super(props); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // Client-only: open your cosmetic wardrobe screen
        if (level.isClientSide && FMLLoader.getDist().isClient()) {
            net.anatomyworld.harambefmod.client.ClientHooks.openCosmeticWardrobeScreen();
        }
        // End the interaction pipeline with a successful right-click (hand swing)
        return InteractionResult.SUCCESS;
    }
}
