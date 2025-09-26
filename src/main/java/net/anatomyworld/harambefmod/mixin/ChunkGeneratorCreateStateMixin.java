package net.anatomyworld.harambefmod.mixin;

import net.anatomyworld.harambefmod.worldgen.api.SeedOverrideSupport;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.OptionalLong;

/** 1.21.8: createState(HolderLookup<StructureSet>, RandomState, long worldSeed) */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorCreateStateMixin {
    @ModifyVariable(method = "createState", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private long harambefmod$swapWorldSeed(long worldSeed) {
        Object self = this;
        if (self instanceof NoiseBasedChunkGenerator nb) {
            OptionalLong seed = ((SeedOverrideSupport)(Object)nb).harambefmod$getSeedOverride();
            if (seed.isPresent()) return seed.getAsLong(); // structures & carvers use this
        }
        return worldSeed;
    }
}
