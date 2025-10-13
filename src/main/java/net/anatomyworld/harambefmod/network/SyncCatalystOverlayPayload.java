package net.anatomyworld.harambefmod.network;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record SyncCatalystOverlayPayload(ResourceKey<Level> dim, BlockPos pos, boolean visible)
        implements CustomPacketPayload {

    public static final Type<SyncCatalystOverlayPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "sync_catalyst_overlay"));

    public static final StreamCodec<FriendlyByteBuf, SyncCatalystOverlayPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceKey.streamCodec(Registries.DIMENSION), SyncCatalystOverlayPayload::dim,
                    BlockPos.STREAM_CODEC, SyncCatalystOverlayPayload::pos,
                    ByteBufCodecs.BOOL, SyncCatalystOverlayPayload::visible,
                    SyncCatalystOverlayPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
