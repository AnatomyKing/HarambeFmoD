package net.anatomyworld.harambefmod.network;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.entity.PearlFireBlockEntity;
import net.anatomyworld.harambefmod.client.portal.BananaPortalTintCache;
import net.anatomyworld.harambefmod.item.custom.FlintAndPearlItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;

public class ModNetworking {
    public static void register(IEventBus modBus) {
        modBus.addListener(ModNetworking::registerPayloads);
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent evt) {
        PayloadRegistrar reg = evt.registrar("1");

        // C -> S: place colored pearl fire (already in your code)
        reg.playToServer(
                PlaceFirePayload.TYPE,
                PlaceFirePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> handlePlaceFire(payload, ctx))
        );

        // C -> S: sync color string from UI (already in your code)
        reg.playToServer(
                SyncColorPayload.TYPE,
                SyncColorPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (!(ctx instanceof ServerPayloadContext serverCtx)) return;
                    ServerPlayer player = serverCtx.player();
                    if (player == null) return;

                    InteractionHand hand = player.getMainHandItem().getItem() instanceof FlintAndPearlItem
                            ? InteractionHand.MAIN_HAND
                            : InteractionHand.OFF_HAND;

                    ItemStack stack = player.getItemInHand(hand);
                    stack.set(net.anatomyworld.harambefmod.component.ModDataComponents.FLAME_COLOR.get(), payload.hex());
                })
        );

        // S -> C: pre-tint the whole portal interior (this is where the cache + renderer invalidation gets used)
        reg.playToClient(
                SyncPortalTintPayload.TYPE,
                SyncPortalTintPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    BananaPortalTintCache.fill(
                            payload.anchor(), payload.axis(), payload.width(), payload.height(), payload.rgb()
                    );
                })
        );
    }

    private static void handlePlaceFire(PlaceFirePayload payload, IPayloadContext ctx) {
        if (!(ctx instanceof ServerPayloadContext serverCtx)) return;
        ServerPlayer player = serverCtx.player();
        if (player == null) return;

        ServerLevel level = player.serverLevel();
        BlockPos pos      = payload.pos();
        Direction face    = Direction.values()[payload.face()];

        if (!BaseFireBlock.canBePlacedAt(level, pos, face)) return;

        level.setBlock(pos, ModBlocks.PEARL_FIRE.get().defaultBlockState(), 11);

        if (level.getBlockEntity(pos) instanceof PearlFireBlockEntity be) {
            be.setColor(payload.color());
        }

        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

        ItemStack stack = player.getItemInHand(payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (!stack.isEmpty() && stack.getItem() instanceof FlintAndPearlItem) {
            stack.hurtAndBreak(1, player, /* onBrokenCallback= */ null);
        }
    }

    /** Helper if you want to broadcast the tint packet to nearby clients from the server. */
    public static void sendPortalTintToNearby(ServerLevel server, BlockPos center, SyncPortalTintPayload payload) {
        double range = 48.0;
        double cx = center.getX() + 0.5, cy = center.getY() + 0.5, cz = center.getZ() + 0.5;
        for (ServerPlayer p : server.players()) {
            if (p.level() == server && p.distanceToSqr(cx, cy, cz) <= range * range) {
                PacketDistributor.sendToPlayer(p, payload);
            }
        }
    }
}
