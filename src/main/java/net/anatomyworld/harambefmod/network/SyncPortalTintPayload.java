// src/main/java/net/anatomyworld/harambefmod/network/SyncPortalTintPayload.java
package net.anatomyworld.harambefmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** S->C: announce a portal interior (anchor/axis/size) + RGB tint so the client
 *  can tint all panes immediately (even before BE sync).
 */
public record SyncPortalTintPayload(BlockPos anchor, Direction.Axis axis, int width, int height, int rgb)
        implements CustomPacketPayload {

    public static final Type<SyncPortalTintPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("harambefmod", "sync_portal_tint"));

    // IMPORTANT: Use RegistryFriendlyByteBuf in 1.21.x and StreamCodec.of(...)
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPortalTintPayload> STREAM_CODEC =
            StreamCodec.of(
                    // encoder: (buf, value)
                    (RegistryFriendlyByteBuf buf, SyncPortalTintPayload p) -> {
                        buf.writeBlockPos(p.anchor());
                        buf.writeEnum(p.axis());
                        buf.writeVarInt(p.width());
                        buf.writeVarInt(p.height());
                        buf.writeInt(p.rgb());
                    },
                    // decoder: (buf) -> value
                    (RegistryFriendlyByteBuf buf) -> new SyncPortalTintPayload(
                            buf.readBlockPos(),
                            buf.readEnum(Direction.Axis.class),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readInt()
                    )
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
