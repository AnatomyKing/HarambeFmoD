package net.anatomyworld.harambefmod.mixin;

import net.anatomyworld.harambefmod.worldgen.internal.SeedQueue;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerSeedQueueResetMixin {
    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void harambefmod$resetSeedQueue(CallbackInfo ci) {
        SeedQueue.clear();
    }
}
