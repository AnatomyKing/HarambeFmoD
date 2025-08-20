package net.anatomyworld.harambefmod.world;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public final class MusavaccaTreePlacer {

    /** Looks for data/harambefmod/structures/musavacca_tree.nbt */
    public static final ResourceLocation TEMPLATE_ID =
            ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "musavacca_tree");

    private MusavaccaTreePlacer() {}

    /** Place tree with trunk base at {@code origin}. Converts ground under trunk to ROOTED_DIRT. */
    public static boolean place(ServerLevel level, BlockPos origin) {
        Optional<StructureTemplate> tpl = level.getStructureManager().get(TEMPLATE_ID);
        if (tpl.isPresent()) {
            convertGroundUnderTrunk(level, origin);
            var settings = new StructurePlaceSettings().setIgnoreEntities(true);
            return tpl.get().placeInWorld(level, origin, origin, settings, level.getRandom(), Block.UPDATE_ALL);
        }

        convertGroundUnderTrunk(level, origin);
        return placeFromSpec(level, origin);
    }

    /** Mirror azalea behavior: the block below the trunk becomes rooted dirt. */
    private static void convertGroundUnderTrunk(ServerLevel level, BlockPos origin) {
        BlockPos below = origin.below();
        level.setBlock(below, Blocks.ROOTED_DIRT.defaultBlockState(), Block.UPDATE_ALL);
    }

    /* ---------- Fallback spec placement (5×7×5 footprint) ---------- */

    private static boolean isSoftForPlacement(BlockState s) {
        return s.isAir()
                || s.canBeReplaced()
                || s.is(BlockTags.LEAVES)
                || s.is(BlockTags.SAPLINGS)
                || s.is(Blocks.SNOW)
                || s.is(Blocks.VINE)
                || s.is(Blocks.FERN)
                || s.is(Blocks.LARGE_FERN)
                || s.is(Blocks.TALL_GRASS);
    }

    private static void setIfSoft(ServerLevel level, BlockPos pos, BlockState state) {
        if (isSoftForPlacement(level.getBlockState(pos))) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }

    /** Hardcoded layout; origin aligns with structure (2,0,2). */
    private static boolean placeFromSpec(ServerLevel level, BlockPos origin) {
        BlockState stemY = ModBlocks.MUSAVACCA_STEM.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState stemX = ModBlocks.MUSAVACCA_STEM.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState stemZ = ModBlocks.MUSAVACCA_STEM.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);

        // Let leaves auto-calc: no PERSISTENT, no manual DISTANCE
        BlockState leaves = ModBlocks.MUSAVACCA_LEAVES.get().defaultBlockState();
        BlockState crown  = ModBlocks.MUSAVACCA_LEAVES_CROWN.get().defaultBlockState();

        BlockState flower = ModBlocks.MUSAVACCA_FLOWER.get().defaultBlockState();

        var place    = (java.util.function.BiConsumer<BlockPos, BlockState>) (pos, state) -> setIfSoft(level, pos, state);
        var placeRel = (java.util.function.BiConsumer<int[], BlockState>) (xyz, state) ->
                place.accept(origin.offset(xyz[0] - 2, xyz[1], xyz[2] - 2), state);

        // y=0..5 vertical trunk at (2,*,2)
        for (int y = 0; y <= 5; y++) placeRel.accept(new int[]{2, y, 2}, stemY);

        // y=2 flowers N/E/S/W
        placeRel.accept(new int[]{1, 2, 2}, flower);
        placeRel.accept(new int[]{2, 2, 1}, flower);
        placeRel.accept(new int[]{2, 2, 3}, flower);
        placeRel.accept(new int[]{3, 2, 2}, flower);

        // y=3 arms + crown corners
        placeRel.accept(new int[]{1, 3, 2}, stemX);
        placeRel.accept(new int[]{3, 3, 2}, stemX);
        placeRel.accept(new int[]{2, 3, 1}, stemZ);
        placeRel.accept(new int[]{2, 3, 3}, stemZ);
        placeRel.accept(new int[]{1, 3, 1}, crown);
        placeRel.accept(new int[]{1, 3, 3}, crown);
        placeRel.accept(new int[]{3, 3, 1}, crown);
        placeRel.accept(new int[]{3, 3, 3}, crown);

        // y=4 ring of leaves + arms + trunk
        placeRel.accept(new int[]{0, 4, 2}, leaves);  // W extreme
        placeRel.accept(new int[]{4, 4, 2}, leaves);  // E extreme
        placeRel.accept(new int[]{2, 4, 0}, leaves);  // N extreme (NEW)
        placeRel.accept(new int[]{2, 4, 4}, leaves);  // S extreme (NEW)

        placeRel.accept(new int[]{1, 4, 1}, leaves);
        placeRel.accept(new int[]{1, 4, 3}, leaves);
        placeRel.accept(new int[]{3, 4, 1}, leaves);
        placeRel.accept(new int[]{3, 4, 3}, leaves);

        placeRel.accept(new int[]{1, 4, 2}, stemX);
        placeRel.accept(new int[]{3, 4, 2}, stemX);
        placeRel.accept(new int[]{2, 4, 1}, stemZ);
        placeRel.accept(new int[]{2, 4, 3}, stemZ);
        placeRel.accept(new int[]{2, 4, 2}, stemY);

        // y=5 leaf cross + trunk
        placeRel.accept(new int[]{1, 5, 2}, leaves);
        placeRel.accept(new int[]{2, 5, 1}, leaves);
        placeRel.accept(new int[]{2, 5, 2}, stemY);
        placeRel.accept(new int[]{2, 5, 3}, leaves);
        placeRel.accept(new int[]{3, 5, 2}, leaves);

        // y=6 crown top
        placeRel.accept(new int[]{2, 6, 2}, crown);

        return true;
    }
}
