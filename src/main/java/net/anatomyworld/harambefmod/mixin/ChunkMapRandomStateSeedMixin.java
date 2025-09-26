// src/main/java/net/anatomyworld/harambefmod/mixin/ChunkMapRandomStateSeedMixin.java
package net.anatomyworld.harambefmod.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.anatomyworld.harambefmod.worldgen.api.SeedOverrideSupport;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.OptionalLong;

/**
 * Make the *stored* RandomState in ChunkMap use the per-dimension seed.
 * This one controls the terrain noise, so changing it changes terrain.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapRandomStateSeedMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/RandomState;create(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)Lnet/minecraft/world/level/levelgen/RandomState;"
            )
    )
    private RandomState harambefmod$randomStateWithPerDimSeed(
            NoiseGeneratorSettings settings,
            HolderGetter<NormalNoise.NoiseParameters> noises,
            long worldSeed,
            @Local ChunkGenerator generator // captured from the ctor via Mixin Extras
    ) {
        long seedToUse = worldSeed;
        if (generator instanceof net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator nb) {
            OptionalLong o = ((SeedOverrideSupport)(Object) nb).harambefmod$getSeedOverride();
            if (o.isPresent()) seedToUse = o.getAsLong();
        }
        return RandomState.create(settings, noises, seedToUse);
    }
}
