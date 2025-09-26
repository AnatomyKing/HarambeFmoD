package net.anatomyworld.harambefmod.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Expose ChunkMap#generator() (protected in Mojang mappings). */
@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {
    @Invoker("generator")
    ChunkGenerator harambefmod$generator();
}
