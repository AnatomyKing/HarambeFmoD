package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MusavaccaPlantCropBlock extends CropBlock {
    public static final int MAX_AGE = 3; // logical 3 turns into sapling
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[] {
            Block.box(4.5, 0.0, 5.5, 11.5, 7.0, 10.5),  // age 0
            Block.box(3.0, 0.0, 3.0, 13.0, 11.0, 13.0), // age 1
            Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0), // age 2
            Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0)  // age 3 (visual only; we convert to sapling)
    };

    public MusavaccaPlantCropBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(getAgeProperty(), 0));
    }

    @Override protected @NotNull IntegerProperty getAgeProperty() { return AGE; }
    @Override public int getMaxAge() { return MAX_AGE; }

    @Override protected @NotNull ItemLike getBaseSeedId() { return ModItems.MUSAVACCA_SPROUT.get(); }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                        net.minecraft.world.phys.shapes.CollisionContext ctx) {
        return SHAPE_BY_AGE[this.getAge(state)];
    }

    /** Random growth: 0->1->2, then convert to sapling (keep farmland intact). */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        if (!level.isAreaLoaded(pos, 1)) return;
        if (level.getRawBrightness(pos, 0) >= 9) {
            int age = getAge(state);
            if (age < 2 && rng.nextInt(7) == 0) {
                level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            } else if (age >= 2 && rng.nextInt(7) == 0) {
                convertToSapling(level, pos); // do NOT touch farmland here
            }
        }
    }

    /** Bonemeal: same behavior as above. */
    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        int age = getAge(state);
        if (age < 2) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
        } else if (level instanceof ServerLevel server) {
            convertToSapling(server, pos); // keep farmland
        }
    }

    private void convertToSapling(ServerLevel level, BlockPos pos) {
        // Keep farmland underneath as-is; sapling can survive on it.
        level.setBlock(pos, ModBlocks.MUSAVACCA_SAPLING.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(AGE);
    }
}
