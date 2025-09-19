package net.anatomyworld.harambefmod.event;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;

public final class MischiefGrassBonemealHandler {
    private MischiefGrassBonemealHandler() {}

    /** Bonemeal Rooted Dirt next to Carotene Grass -> convert it (nylium-like) with vanilla particles. */
    @SubscribeEvent
    public static void onBonemeal(BonemealEvent event) {
        BlockState state = event.getState();
        if (!state.is(Blocks.DIRT)) return;

        var level = event.getLevel();
        BlockPos pos = event.getPos();

        // need air above (like nylium conversion)
        if (!level.getBlockState(pos.above()).isAir()) return;

        // look for adjacent carotene grass
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(d)).is(ModBlocks.CAROTENE_GRASS_BLOCK.get())) {

                if (level instanceof ServerLevel sl) {
                    // 1) convert the block
                    sl.setBlock(pos, ModBlocks.CAROTENE_GRASS_BLOCK.get().defaultBlockState(), 3);

                    // 2) VANILLA bone-meal effect (particles + sound)
                    // This is the same LevelEvent vanilla fires for bone meal usage.
                    sl.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);

                    // (Optional) also play the bone-meal use sound explicitly — harmless duplicate if client missed it.
                    sl.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

                    // (Optional) add growth particle burst like vanilla crops do (purely visual extra)
                    BoneMealItem.addGrowthParticles(sl, pos.above(), 15);
                }

                // consume one bonemeal if not creative
                var player = event.getPlayer();
                if (player == null || !player.getAbilities().instabuild) {
                    event.getStack().shrink(1);
                }

                // mark handled and stop vanilla
                event.setSuccessful(true);
                event.setCanceled(true);
                return;
            }
        }
    }
}
