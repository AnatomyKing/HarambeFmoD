package net.anatomyworld.harambefmod.event;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;

@EventBusSubscriber(modid = HarambeCore.MOD_ID)
public final class BelmontGrassBonemealHandler {
    private BelmontGrassBonemealHandler() {}

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
            if (!level.getBlockState(pos.relative(d)).is(ModBlocks.BELMONT_GRASS_BLOCK.get())) continue;

            if (level instanceof ServerLevel sl) {
                // 1) convert the block
                sl.setBlock(pos, ModBlocks.BELMONT_GRASS_BLOCK.get().defaultBlockState(), 3);

                // 2) VANILLA bone-meal effect (client expects it one block ABOVE)
                BlockPos posUp = pos.above();
                sl.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, posUp, 0);


                double cx = pos.getX() + 0.5;
                double cy = pos.getY() + 1.0;
                double cz = pos.getZ() + 0.5;
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, cx, cy, cz,
                        12,   // count
                        0.5,  // dx
                        0.5,  // dy
                        0.5,  // dz
                        0.0); // speed

                // Optional extra sparkle burst (same helper vanilla crops use)
                BoneMealItem.addGrowthParticles(sl, posUp, 15);

                // (Also nice to have the bone-meal use sound)
                sl.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
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
