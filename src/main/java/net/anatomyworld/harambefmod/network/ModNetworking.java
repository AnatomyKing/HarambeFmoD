package net.anatomyworld.harambefmod.network;

import net.anatomyworld.harambefmod.attachment.CosmeticWardrobe;
import net.anatomyworld.harambefmod.attachment.ModAttachments;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.entity.PearlFireBlockEntity;
import net.anatomyworld.harambefmod.client.portal.BananaPortalTintCache;
import net.anatomyworld.harambefmod.client.render.FactionCatalystAreaOverlay;
import net.anatomyworld.harambefmod.client.sync.ClientBalance;
import net.anatomyworld.harambefmod.item.custom.FlintAndPearlItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {

    public static void register(IEventBus modBus) {
        modBus.addListener(ModNetworking::registerPayloads);
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent evt) {
        // bump version string if you ever change formats
        PayloadRegistrar reg = evt.registrar("1");

        /* ---------------- C -> S ---------------- */

        // place colored pearl fire
        reg.playToServer(
                PlaceFirePayload.TYPE,
                PlaceFirePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> handlePlaceFire(payload, ctx))
        );

        // save selected flame color on held item
        reg.playToServer(
                SyncColorPayload.TYPE,
                SyncColorPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (!(ctx instanceof ServerPayloadContext s)) return;
                    ServerPlayer player = s.player(); if (player == null) return;

                    InteractionHand hand =
                            player.getMainHandItem().getItem() instanceof FlintAndPearlItem
                                    ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

                    ItemStack stack = player.getItemInHand(hand);
                    stack.set(net.anatomyworld.harambefmod.component.ModDataComponents.FLAME_COLOR.get(), payload.hex());
                })
        );

        // select a cosmetic set
        reg.playToServer(
                SelectCosmeticSetPayload.TYPE,
                SelectCosmeticSetPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (!(ctx instanceof ServerPayloadContext s)) return;
                    ServerPlayer player = s.player(); if (player == null) return;

                    var set = net.anatomyworld.harambefmod.cosmetic.CosmeticSets.get(payload.id());
                    if (set == null) return;

                    var ward = player.getData(ModAttachments.COSMETIC_WARDROBE.get());
                    set.head().ifPresentOrElse(ward::setHead, () -> ward.setHead(null));
                    set.chest().ifPresentOrElse(ward::setChest, () -> ward.setChest(null));
                    set.legs().ifPresentOrElse(ward::setLegs, () -> ward.setLegs(null));
                    set.feet().ifPresentOrElse(ward::setFeet, () -> ward.setFeet(null));
                    player.setData(ModAttachments.COSMETIC_WARDROBE.get(), ward); // triggers sync
                })
        );

        // clear cosmetic wardrobe
        reg.playToServer(
                ClearCosmeticWardrobePayload.TYPE,
                ClearCosmeticWardrobePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (!(ctx instanceof ServerPayloadContext s)) return;
                    ServerPlayer player = s.player(); if (player == null) return;

                    CosmeticWardrobe ward = player.getData(ModAttachments.COSMETIC_WARDROBE.get());
                    ward.setHead(null);
                    ward.setChest(null);
                    ward.setLegs(null);
                    ward.setFeet(null);
                    player.setData(ModAttachments.COSMETIC_WARDROBE.get(), ward);
                })
        );

        /* ---------------- S -> C ---------------- */

        reg.playToClient(
                SyncCatalystOverlayPayload.TYPE,
                SyncCatalystOverlayPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        FactionCatalystAreaOverlay.applyServerVisibility(payload.dim(), payload.pos(), payload.visible())
                )
        );

        // cosmetic set list -> client cache
        reg.playToClient(
                SyncCosmeticSetsPayload.TYPE,
                SyncCosmeticSetsPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        net.anatomyworld.harambefmod.cosmetic.client.ClientCosmeticSets.accept(payload)
                )
        );

        // pre-tint portal interiors on clients
        reg.playToClient(
                SyncPortalTintPayload.TYPE,
                SyncPortalTintPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        BananaPortalTintCache.fill(payload.anchor(), payload.axis(), payload.width(), payload.height(), payload.rgb())
                )
        );

        // balance -> client HUD cache
        reg.playToClient(
                BalanceSyncPayload.TYPE,
                BalanceSyncPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientBalance.set(payload.balance()))
        );
    }

    /* ---------------- helpers ---------------- */

    private static void handlePlaceFire(PlaceFirePayload payload, IPayloadContext ctx) {
        if (!(ctx instanceof ServerPayloadContext s)) return;
        ServerPlayer player = s.player();
        if (player == null) return;

        ServerLevel level = (ServerLevel) player.level();

        BlockPos pos = payload.pos();
        Direction face = Direction.values()[payload.face()];

        if (!BaseFireBlock.canBePlacedAt(level, pos, face)) return;

        level.setBlock(pos, ModBlocks.PEARL_FIRE.get().defaultBlockState(), 11);

        if (level.getBlockEntity(pos) instanceof PearlFireBlockEntity be) {
            be.setColor(payload.color());
        }

        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F,
                level.getRandom().nextFloat() * 0.4F + 0.8F);

        InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && stack.getItem() instanceof FlintAndPearlItem) {
            EquipmentSlot slot = payload.mainHand() ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, player, slot);
        }
    }

    /** Server helper to broadcast a payload to nearby players. */
    public static void sendPortalTintToNearby(ServerLevel server, BlockPos center, SyncPortalTintPayload payload) {
        double range = 48.0;
        double cx = center.getX() + 0.5, cy = center.getY() + 0.5, cz = center.getZ() + 0.5;
        for (ServerPlayer p : server.players()) {
            if (p.level() == server && p.distanceToSqr(cx, cy, cz) <= range * range) {
                PacketDistributor.sendToPlayer(p, payload);
            }
        }
    }

    public static void sendCatalystOverlayToDimension(ServerLevel server, BlockPos pos, boolean visible) {
        var payload = new SyncCatalystOverlayPayload(server.dimension(), pos, visible);
        for (ServerPlayer p : server.players()) {
            if (p.level() == server) {
                PacketDistributor.sendToPlayer(p, payload);
            }
        }
    }


    private ModNetworking() {}
}
