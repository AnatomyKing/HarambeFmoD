package net.anatomyworld.harambefmod.mixin;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
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

import java.util.Optional;
import java.util.OptionalLong;

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

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void harambefmod$extendCodec(CallbackInfo ci) {
        MapCodec<NoiseBasedChunkGenerator> orig = CODEC;

        CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.either(Codec.LONG, Codec.STRING).optionalFieldOf("seed")
                                .xmap(
                                        opt -> opt.map(e -> e.map(l -> l, s -> (long) s.hashCode())),
                                        optL -> optL.map(Either::left)
                                )
                                .forGetter(gen -> {
                                    OptionalLong o = ((SeedOverrideSupport)(Object)gen).harambefmod$getSeedOverride();
                                    return o.isPresent() ? Optional.of(o.getAsLong()) : Optional.empty();
                                }),
                        orig.forGetter(gen -> (NoiseBasedChunkGenerator)gen)
                ).apply(instance, (seedOpt, gen) -> {
                    ((SeedOverrideSupport)(Object)gen)
                            .harambefmod$setSeedOverride(seedOpt.map(OptionalLong::of).orElseGet(OptionalLong::empty));
                    return gen;
                })
        );
    }
}
