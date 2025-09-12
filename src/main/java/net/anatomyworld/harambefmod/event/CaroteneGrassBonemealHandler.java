package net.anatomyworld.harambefmod.event;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;

public final class CaroteneGrassBonemealHandler {
    private CaroteneGrassBonemealHandler() {}

    /** Bonemeal Rooted Dirt next to Carotene Grass -> convert it (nylium-like). */
    public static void onBonemeal(BonemealEvent event) {
        BlockState state = event.getState();
        if (!state.is(Blocks.ROOTED_DIRT)) return;

        var level = event.getLevel();
        BlockPos pos = event.getPos();

        // need air above (like nylium conversion space check)
        if (!level.getBlockState(pos.above()).isAir()) return;

        // look for adjacent carotene grass
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(d)).is(ModBlocks.CAROTENE_GRASS_BLOCK.get())) {
                if (level instanceof ServerLevel sl) {
                    // do the conversion
                    sl.setBlock(pos, ModBlocks.CAROTENE_GRASS_BLOCK.get().defaultBlockState(), 3);
                    // bonemeal particles/sound (vanilla uses 2005 for the “green sparkle”)
                    sl.levelEvent(2005, pos, 0);
                }

                // consume one bonemeal if not creative
                var player = event.getPlayer(); // 1.21+: getPlayer(), not getEntity()
                if (player == null || !player.getAbilities().instabuild) {
                    event.getStack().shrink(1);
                }

                // tell the system that bonemeal was used and stop vanilla
                event.setSuccessful(true); // signals “handled/used”
                event.setCanceled(true);   // prevents vanilla handling
                return;
            }
        }
    }
}
