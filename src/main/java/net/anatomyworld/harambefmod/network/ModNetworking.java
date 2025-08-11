// src/main/java/net/anatomyworld/harambefd/network/ModNetworking.java
package net.anatomyworld.harambefmod.network;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.entity.PearlFireBlockEntity;
import net.anatomyworld.harambefmod.item.custom.FlintAndPearlItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;


public class ModNetworking {
    public static void register(IEventBus modBus) {
        modBus.addListener(ModNetworking::registerPayloads);
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent evt) {
        PayloadRegistrar reg = evt.registrar("1");

        // 1) PlaceFirePayload → places colored fire & damages item
        reg.playToServer(
                PlaceFirePayload.TYPE,
                PlaceFirePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> handlePlaceFire(payload, ctx))
        );


        reg.playToServer(
                SyncColorPayload.TYPE,
                SyncColorPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (!(ctx instanceof ServerPayloadContext serverCtx)) return;
                    ServerPlayer player = serverCtx.player();
                    if (player == null) return;

                    // figure out which hand holds our custom item
                    InteractionHand hand = player.getMainHandItem().getItem() instanceof FlintAndPearlItem
                            ? InteractionHand.MAIN_HAND
                            : InteractionHand.OFF_HAND;

                    ItemStack stack = player.getItemInHand(hand);

                    stack.set(
                            net.anatomyworld.harambefmod.component.ModDataComponents.FLAME_COLOR.get(),
                            payload.hex()
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

        // place block
        level.setBlock(pos, ModBlocks.PEARL_FIRE.get().defaultBlockState(), 11);


        if (level.getBlockEntity(pos) instanceof PearlFireBlockEntity be) {
            be.setColor(payload.color());
        }

        // sound
        level.playSound(null, pos,
                SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.4F + 0.8F
        );

        // damage
        ItemStack stack = player.getItemInHand(
                payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND
        );
        if (!stack.isEmpty() && stack.getItem() instanceof FlintAndPearlItem) {
            stack.hurtAndBreak(1, player, /* onBrokenCallback= */ null);
        }
    }
}
