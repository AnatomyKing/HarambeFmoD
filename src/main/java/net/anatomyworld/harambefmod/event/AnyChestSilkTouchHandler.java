package net.anatomyworld.harambefmod.event;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.entity.AnyChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class AnyChestSilkTouchHandler {

    private AnyChestSilkTouchHandler() {}

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Block block = state.getBlock();

        // Only handle your AnyChest block
        if (block != ModBlocks.ANYTOMITHIUM_CHEST.get()) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        ItemStack tool = player.getMainHandItem();
        boolean hasSilkTouch = hasSilkTouch(level, tool);

        // Grab the block entity BEFORE we remove the block
        if (!(level.getBlockEntity(pos) instanceof AnyChestBlockEntity chest)) {
            return;
        }

        // Stop vanilla from doing its own drops
        event.setCanceled(true);

        // Build the chest item drop
        ItemStack chestStack = new ItemStack(ModBlocks.ANYTOMITHIUM_CHEST.get());

        if (hasSilkTouch) {
            // Save contents on the chest item (shulker-style)
            chest.saveToItem(chestStack);
            chest.clearContent();
        } else {
            // Drop contents into the world (normal chest behavior)
            dropInventory(level, pos, chest);
        }

        // Remove block + block entity
        level.removeBlock(pos, false);

        // Always drop the chest block itself
        Block.popResource(level, pos, chestStack);
    }

    private static boolean hasSilkTouch(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        HolderLookup.RegistryLookup<Enchantment> lookup =
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> silk = lookup.getOrThrow(Enchantments.SILK_TOUCH);

        return EnchantmentHelper.getTagEnchantmentLevel(silk, stack) > 0;
    }

    private static void dropInventory(ServerLevel level, BlockPos pos, AnyChestBlockEntity chest) {
        int size = chest.getContainerSize();
        for (int i = 0; i < size; ++i) {
            ItemStack slot = chest.getItem(i);
            if (!slot.isEmpty()) {
                Block.popResource(level, pos, slot.copy());
            }
        }
        chest.clearContent();
    }
}
