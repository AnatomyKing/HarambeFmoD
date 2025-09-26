package net.anatomyworld.harambefmod.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.anatomyworld.harambefmod.worldgen.api.SeedOverrideSupport;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalLong;
import java.util.Random;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorCodecMixin implements SeedOverrideSupport {

    @Shadow @Final @Mutable
    private static MapCodec<NoiseBasedChunkGenerator> CODEC;

    @Unique
    private OptionalLong harambefmod$seedOverride = OptionalLong.empty();

    @Override
    public void harambefmod$setSeedOverride(OptionalLong seed) {
        this.harambefmod$seedOverride = seed;
    }

    @Override
    public OptionalLong harambefmod$getSeedOverride() {
        return this.harambefmod$seedOverride;
    }

    /**
     * Extend the generator CODEC to accept:
     *  - "seed": <number>     -> fixed seed
     *  - "seed": "<string>"   -> vanilla-style string seed (String#hashCode)
     *  - "seed": true         -> pick a random long NOW and store it
     *  - (absent)             -> no override (vanilla: world seed)
     *
     * We parse with PASSTHROUGH so booleans/numbers/strings are all handled. The field is optional.
     */
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void harambefmod$extendCodec(CallbackInfo ci) {
        MapCodec<NoiseBasedChunkGenerator> orig = CODEC;

        CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        // Optional "seed" of arbitrary type (bool/num/string). We don't encode it back.
                        Codec.PASSTHROUGH.optionalFieldOf("seed").forGetter(gen -> java.util.Optional.empty()),
                        // Keep the original generator codec
                        orig.forGetter(gen -> (NoiseBasedChunkGenerator) gen)
                ).apply(instance, (rawSeedOpt, gen) -> {
                    OptionalLong seedOpt = OptionalLong.empty();

                    if (rawSeedOpt.isPresent()) {
                        Dynamic<?> dyn = rawSeedOpt.get();

                        // 1) Try boolean
                        var asBool = dyn.asBoolean().result();
                        if (asBool.isPresent()) {
                            if (asBool.get()) {
                                // "seed": true -> pick a random long now
                                long rnd = new Random().nextLong();
                                seedOpt = OptionalLong.of(rnd);
                            }
                            // "seed": false -> treat as absent (no override)
                        } else {
                            // 2) Try number
                            var asNum = dyn.asNumber().result();
                            if (asNum.isPresent()) {
                                seedOpt = OptionalLong.of(asNum.get().longValue());
                            } else {
                                // 3) Try string -> vanilla semantics
                                var asStr = dyn.asString().result();
                                if (asStr.isPresent()) {
                                    seedOpt = OptionalLong.of((long) asStr.get().hashCode());
                                }
                            }
                        }
                    }

                    ((SeedOverrideSupport)(Object) gen).harambefmod$setSeedOverride(seedOpt);
                    return gen;
                })
        );
    }
}
