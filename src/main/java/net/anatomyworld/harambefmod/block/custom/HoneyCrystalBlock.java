package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * A single block that behaves like BOTH "block of amethyst" and "budding amethyst":
 * - Random ticks (20%): grow/upgrade buds ONLY on TOP or BOTTOM.
 * - If there's a CLUSTER facing this block, small chance to promote it into another Honey Crystal Block,
 *   enabling infinite vertical propagation.
 */
public final class HoneyCrystalBlock extends Block {
    // Vanilla budding amethyst: ~20% chance per random tick (documented).
    private static final float GROW_CHANCE    = 0.20f;
    // Cluster -> block promotion chance (tweak to taste).
    private static final float PROMOTE_CHANCE = 0.05f;

    public HoneyCrystalBlock(BlockBehaviour.Properties props) {
        super(props.randomTicks());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        // Pick UP or DOWN
        Direction dir = rng.nextBoolean() ? Direction.UP : Direction.DOWN;
        BlockPos targetPos = pos.relative(dir);
        BlockState target = level.getBlockState(targetPos);

        // 1) Cluster promotion → new honey_crystal_block
        if (target.is(ModBlocks.HONEY_CRYSTAL_CLUSTER.get())
                && target.getValue(BlockStateProperties.FACING) == dir) {
            if (rng.nextFloat() < PROMOTE_CHANCE) {
                level.setBlock(targetPos, ModBlocks.HONEY_CRYSTAL_BLOCK.get().defaultBlockState(), 3);
                return;
            }
        }

        // 2) Bud growth/upgrade (air or source water can be replaced)
        if (rng.nextFloat() >= GROW_CHANCE) return;

        boolean isWater = target.getBlock() == Blocks.WATER && target.getFluidState().isSource();
        boolean replaceable = target.isAir() || isWater;

        if (!replaceable
                && !target.is(ModBlocks.SMALL_HONEY_CRYSTAL_BUD.get())
                && !target.is(ModBlocks.MEDIUM_HONEY_CRYSTAL_BUD.get())
                && !target.is(ModBlocks.LARGE_HONEY_CRYSTAL_BUD.get())) {
            return;
        }

        BlockState small = ModBlocks.SMALL_HONEY_CRYSTAL_BUD.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, dir)
                .setValue(BlockStateProperties.WATERLOGGED, isWater);
        BlockState medium = ModBlocks.MEDIUM_HONEY_CRYSTAL_BUD.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, dir)
                .setValue(BlockStateProperties.WATERLOGGED, isWater);
        BlockState large = ModBlocks.LARGE_HONEY_CRYSTAL_BUD.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, dir)
                .setValue(BlockStateProperties.WATERLOGGED, isWater);
        BlockState cluster = ModBlocks.HONEY_CRYSTAL_CLUSTER.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, dir)
                .setValue(BlockStateProperties.WATERLOGGED, isWater);

        if (target.is(ModBlocks.SMALL_HONEY_CRYSTAL_BUD.get())
                && target.getValue(BlockStateProperties.FACING) == dir) {
            level.setBlock(targetPos, medium, 3);
        } else if (target.is(ModBlocks.MEDIUM_HONEY_CRYSTAL_BUD.get())
                && target.getValue(BlockStateProperties.FACING) == dir) {
            level.setBlock(targetPos, large, 3);
        } else if (target.is(ModBlocks.LARGE_HONEY_CRYSTAL_BUD.get())
                && target.getValue(BlockStateProperties.FACING) == dir) {
            level.setBlock(targetPos, cluster, 3);
        } else if (replaceable) {
            level.setBlock(targetPos, small, 3);
        }
    }
}
