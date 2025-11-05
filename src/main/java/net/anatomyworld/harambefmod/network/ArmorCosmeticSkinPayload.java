package net.anatomyworld.harambefmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public record ArmorCosmeticSkinPayload(EquipmentSlot slot, boolean active) implements CustomPacketPayload {

    public static final Type<ArmorCosmeticSkinPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("harambefmod", "armor_cosmetic_skin"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorCosmeticSkinPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, p -> p.slot().ordinal(),
                    ByteBufCodecs.BOOL,    ArmorCosmeticSkinPayload::active,
                    (ord, act) -> new ArmorCosmeticSkinPayload(EquipmentSlot.values()[ord], act)
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
