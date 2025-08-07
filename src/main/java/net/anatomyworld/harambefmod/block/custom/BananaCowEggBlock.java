package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.entity.ModEntities;
import net.anatomyworld.harambefmod.entity.bananacow.BananaCow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * Banana Cow egg: drops itself only with Silk Touch (handled by
 * the generated loot table) and otherwise hatches an adult Banana Cow.
 */
public class BananaCowEggBlock extends Block {

    /** Matches the Blockbench model ([1 0 1] → [15 15 15]). */
    private static final VoxelShape EGG_SHAPE =
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public BananaCowEggBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull net.minecraft.world.level.BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext ctx) {
        return EGG_SHAPE;
    }

    /** Custom break-behaviour: hatch or (via loot table) drop self. */
    @Override
    protected void spawnAfterBreak(@NotNull BlockState state,
                                   @NotNull ServerLevel level,
                                   @NotNull BlockPos pos,
                                   @NotNull ItemStack tool,
                                   boolean dropXP) {

        if (level.isClientSide ||
                !level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            return;                                         // nothing to do
        }

        if (hasSilkTouch(level, tool)) {
            // Silk Touch → vanilla loot table drops the egg item
            super.spawnAfterBreak(state, level, pos, tool, dropXP);
        } else {
            // No Silk Touch → hatch an ADULT Banana Cow, no item drop
            hatch(level, pos);
        }
    }

    /* ---------- helpers ---------- */

    /** Modern (Holder-based) Silk Touch check – 1.20.5+/1.21 signature. */
    private static boolean hasSilkTouch(ServerLevel level, ItemStack tool) {
        if (tool.isEmpty()) return false;

        Holder<Enchantment> silk = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        return EnchantmentHelper.getItemEnchantmentLevel(silk, tool) > 0;
    }

    /** Spawn an adult Banana Cow exactly where the egg was. */
    private static void hatch(ServerLevel level, BlockPos pos) {
        BananaCow cow = ModEntities.BANANA_COW.get().create(level);
        if (cow != null) {
            cow.setAge(0);                                  // adult
            cow.moveTo(pos.getX() + 0.5D,
                    pos.getY(),
                    pos.getZ() + 0.5D,
                    level.random.nextFloat() * 360F,
                    0.0F);
            level.addFreshEntity(cow);
        }
    }
}
