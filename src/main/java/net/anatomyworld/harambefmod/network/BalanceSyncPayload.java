package net.anatomyworld.harambefmod.network;

import io.netty.buffer.ByteBuf;
import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** S->C: send the player's latest balance for the HUD cache. */
public record BalanceSyncPayload(long balance) implements CustomPacketPayload {
    public static final Type<BalanceSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "balance_sync"));

    /** 1.21.x: use ByteBufCodecs.VAR_LONG for compact varlong encoding. */
    public static final StreamCodec<ByteBuf, BalanceSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG, BalanceSyncPayload::balance,
                    BalanceSyncPayload::new
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
