package net.anatomyworld.harambefmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClearCosmeticWardrobePayload() implements CustomPacketPayload {
    public static final ClearCosmeticWardrobePayload INSTANCE = new ClearCosmeticWardrobePayload();

    public static final Type<ClearCosmeticWardrobePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("harambefmod", "clear_cosmetic_wardrobe"));

    // No fields -> encode/decode nothing
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearCosmeticWardrobePayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
