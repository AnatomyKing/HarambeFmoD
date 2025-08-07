package net.anatomyworld.harambefmod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Payload for placing a colored fire on the server, sent from client. */
public record PlaceFirePayload(BlockPos pos, int color, boolean mainHand, byte face) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlaceFirePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("harambefd", "place_fire"));
    // Define how to encode/decode the payload data
    public static final StreamCodec<ByteBuf, PlaceFirePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, p -> p.pos().getX(),
            ByteBufCodecs.INT, p -> p.pos().getY(),
            ByteBufCodecs.INT, p -> p.pos().getZ(),
            ByteBufCodecs.INT, PlaceFirePayload::color,
            ByteBufCodecs.BOOL, PlaceFirePayload::mainHand,
            ByteBufCodecs.BYTE, PlaceFirePayload::face,
            (x, y, z, color, mainHand, face) -> new PlaceFirePayload(new BlockPos(x, y, z), color, mainHand, face)
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
