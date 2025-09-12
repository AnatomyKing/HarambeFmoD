package net.anatomyworld.harambefmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectCosmeticSetPayload(ResourceLocation id) implements CustomPacketPayload {
    public static final Type<SelectCosmeticSetPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("harambefmod", "select_cosmetic_set"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectCosmeticSetPayload> STREAM_CODEC =
            StreamCodec.composite(ResourceLocation.STREAM_CODEC, SelectCosmeticSetPayload::id, SelectCosmeticSetPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
