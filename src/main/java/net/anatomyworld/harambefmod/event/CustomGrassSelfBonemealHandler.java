package net.anatomyworld.harambefmod.event;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;

@EventBusSubscriber(modid = HarambeCore.MOD_ID) // auto-register on the game bus
public final class CustomGrassSelfBonemealHandler {
    private CustomGrassSelfBonemealHandler() {}

    @SubscribeEvent
    public static void onBonemeal(BonemealEvent event) {
        BlockState state = event.getState();
        var level = event.getLevel();
        BlockPos pos = event.getPos();

        // Which grass did we bonemeal and which short-grass should we place?
        BlockState shortGrass = null;
        if (state.is(ModBlocks.BELMONT_GRASS_BLOCK.get())) {
            shortGrass = ModBlocks.BELMONT_SHORT_GRASS.get().defaultBlockState();
        } else if (state.is(ModBlocks.DYNASTY_GRASS_BLOCK.get())) {
            shortGrass = ModBlocks.DYNASTY_SHORT_GRASS.get().defaultBlockState();
        } else if (state.is(ModBlocks.IMPERIUM_GRASS_BLOCK.get())) {
            shortGrass = ModBlocks.IMPERIUM_SHORT_GRASS.get().defaultBlockState();
        } else if (state.is(ModBlocks.CAROTENE_GRASS_BLOCK.get())) {
            shortGrass = ModBlocks.CAROTENE_SHORT_GRASS.get().defaultBlockState();
        } else {
            return; // not our grass → let vanilla handle it
        }

        // Need air above the clicked block (vanilla parity)
        if (!level.getBlockState(pos.above()).isAir()) return;

        RandomSource rand = level.getRandom();

        // Spawn 1–3 tufts; up to 16 placement attempts in a ~7×7 area, like vanilla pacing.
        int wanted = 1 + rand.nextInt(3);
        int placed = 0;
        int attempts = 16;

        for (int i = 0; i < attempts && placed < wanted; i++) {
            // Radius grows slightly over attempts (soft spread)
            int r = 1 + i / 6;              // 1..3
            int dx = Mth.nextInt(rand, -r, r);
            int dz = Mth.nextInt(rand, -r, r);

            BlockPos base = pos.offset(dx, 0, dz);
            // Only grow on more of the *same* grass type (so it feels cohesive)
            if (!level.getBlockState(base).is(state.getBlock())) continue;

            BlockPos placePos = base.above();
            if (!level.getBlockState(placePos).isAir()) continue;
            if (!shortGrass.canSurvive(level, placePos)) continue;

            // place
            if (level instanceof ServerLevel sl) {
                sl.setBlock(placePos, shortGrass, 3);
            }
            placed++;
        }

        if (placed > 0) {
            // Vanilla-style growth event: particles + sound (client expects it one block above)
            if (level instanceof ServerLevel sl) {
                BlockPos up = pos.above();
                sl.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, up, 0);
                // nice extra sparkle burst, same helper vanilla crops use
                BoneMealItem.addGrowthParticles(sl, up, 12);
                // a tiny bit more “happy villager” for feedback
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        up.getX() + 0.5, up.getY() + 0.2, up.getZ() + 0.5,
                        8, 0.4, 0.2, 0.4, 0.0);
                sl.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            // consume one bone meal if not creative
            var player = event.getPlayer();
            if (player == null || !player.getAbilities().instabuild) {
                event.getStack().shrink(1);
            }

            // mark handled so vanilla doesn't run its own grass/flower spread
            event.setSuccessful(true);
            event.setCanceled(true);
        }
    }
}
