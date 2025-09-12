package net.anatomyworld.harambefmod.network;

import net.anatomyworld.harambefmod.cosmetic.CosmeticSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record SyncCosmeticSetsPayload(List<Entry> entries) implements CustomPacketPayload {
    public static final Type<SyncCosmeticSetsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("harambefmod", "sync_cosmetic_sets"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCosmeticSetsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncCosmeticSetsPayload::entries,
                    SyncCosmeticSetsPayload::new
            );

    public record Entry(ResourceLocation id, CosmeticSet set) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, Entry::id,
                        CosmeticSet.STREAM_CODEC, Entry::set,
                        Entry::new
                );
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
