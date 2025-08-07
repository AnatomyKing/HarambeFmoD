// src/main/java/net/anatomyworld/harambefd/network/SyncColorPayload.java
package net.anatomyworld.harambefmod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Carries a hex‐string (e.g. "#D5CD49") from client → server
 * so that server updates the held item stack’s component immediately.
 */
public record SyncColorPayload(String hex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncColorPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("harambefd", "sync_color"));

    public static final StreamCodec<ByteBuf, SyncColorPayload> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8
                    .map(
                            SyncColorPayload::new,
                            SyncColorPayload::hex
                    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
