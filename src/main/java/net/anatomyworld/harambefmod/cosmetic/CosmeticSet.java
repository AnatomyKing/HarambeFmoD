package net.anatomyworld.harambefmod.cosmetic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record CosmeticSet(Optional<String> title,
                          Optional<ResourceLocation> head,
                          Optional<ResourceLocation> chest,
                          Optional<ResourceLocation> legs,
                          Optional<ResourceLocation> feet) {

    public static final MapCodec<CosmeticSet> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("title").forGetter(CosmeticSet::title),
            ResourceLocation.CODEC.optionalFieldOf("head").forGetter(CosmeticSet::head),
            ResourceLocation.CODEC.optionalFieldOf("chest").forGetter(CosmeticSet::chest),
            ResourceLocation.CODEC.optionalFieldOf("legs").forGetter(CosmeticSet::legs),
            ResourceLocation.CODEC.optionalFieldOf("feet").forGetter(CosmeticSet::feet)
    ).apply(i, CosmeticSet::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CosmeticSet> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), CosmeticSet::title,
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), CosmeticSet::head,
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), CosmeticSet::chest,
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), CosmeticSet::legs,
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), CosmeticSet::feet,
                    CosmeticSet::new);
}
