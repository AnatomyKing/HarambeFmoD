package net.anatomyworld.harambefmod.item.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.entity.PearlFireBlockEntity;
import net.anatomyworld.harambefmod.network.PlaceFirePayload;
import net.anatomyworld.harambefmod.world.BananaPortalShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FlintAndPearlItem extends FlintAndSteelItem {
    private static final String DEFAULT_COLOR = "#D5CD49";

    public FlintAndPearlItem(Properties props) {
        super(props);
    }

    // 1.21.8: Item#use now returns InteractionResult (not InteractionResultHolder)
    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            ScreenOpener.openIfLookingAtAir(held);
            // Report success client-side; server will do nothing here
            return InteractionResult.SUCCESS;
        }
        // Delegate to base (plays swing/usage logic as appropriate)
        return super.use(level, player, hand);
    }

    // Signature unchanged in 1.21.8: returns InteractionResult
    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack stack, Player player, @NotNull LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide && target instanceof Creeper creeper) {
            creeper.ignite();
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                    ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // FlintAndSteelItem#useOn returns InteractionResult in 1.21.8
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext ctx) {
        Level     level   = ctx.getLevel();
        BlockPos  clicked = ctx.getClickedPos();
        Direction face    = ctx.getClickedFace();
        BlockPos  firePos = clicked.relative(face);
        BlockState state  = level.getBlockState(clicked);
        ItemStack stack   = ctx.getItemInHand();

        if (level.isClientSide) {
            if (shouldPlacePearlFireClient(state, level, firePos, face)) {
                String hex = stack.getOrDefault(net.anatomyworld.harambefmod.component.ModDataComponents.FLAME_COLOR.get(), DEFAULT_COLOR);
                int rgb = Integer.parseInt(hex.replace("#", ""), 16);

                // 1.21.8 NeoForge: client->server send via ClientPacketDistributor
                ClientPacketDistributor.sendToServer(new PlaceFirePayload(
                        firePos, rgb, ctx.getHand() == InteractionHand.MAIN_HAND, (byte) face.ordinal()
                ));

                ClientPredictor.place(level, firePos, rgb);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        // Try Banana portal first (server)
        {
            String hex = stack.getOrDefault(net.anatomyworld.harambefmod.component.ModDataComponents.FLAME_COLOR.get(), DEFAULT_COLOR);
            int rgb = Integer.parseInt(hex.replace("#", ""), 16);

            if (trySpawnBananaPortal((net.minecraft.server.level.ServerLevel) level, firePos, rgb, hex.toUpperCase(), ctx.getPlayer())) {
                finishUse(level, firePos, stack, ctx);
                return InteractionResult.SUCCESS; // action performed
            }
        }

        // TNT / lights
        if (state.getBlock() instanceof TntBlock) {
            state.onCaughtFire(level, clicked, face, ctx.getPlayer());
            level.setBlock(clicked, Blocks.AIR.defaultBlockState(), 11);
            finishUse(level, clicked, stack, ctx);
            return InteractionResult.SUCCESS;
        }
        if (CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
            level.setBlock(clicked, state.setValue(BlockStateProperties.LIT, true), 11);
            finishUse(level, clicked, stack, ctx);
            return InteractionResult.SUCCESS;
        }

        // Vanilla nether portal
        if (trySpawnNetherPortal(level, firePos)) {
            finishUse(level, firePos, stack, ctx);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static boolean shouldPlacePearlFireClient(BlockState clickedState, Level level, BlockPos firePos, Direction face) {
        if (clickedState.getBlock() instanceof TntBlock) return false;
        if (CampfireBlock.canLight(clickedState) || CandleBlock.canLight(clickedState) || CandleCakeBlock.canLight(clickedState)) return false;

        // If a vanilla nether portal could spawn here, do not place our custom fire client-predictively
        if (PortalShape.findEmptyPortalShape(level, firePos, Direction.Axis.X).isPresent()
                || PortalShape.findEmptyPortalShape(level, firePos, Direction.Axis.Z).isPresent()) return false;

        return BaseFireBlock.canBePlacedAt(level, firePos, face);
    }

    private static boolean trySpawnNetherPortal(Level lvl, BlockPos pos) {
        // 1.21.8: createPortalBlocks now requires a LevelAccessor parameter
        return PortalShape.findEmptyPortalShape(lvl, pos, Direction.Axis.X)
                .map(shape -> { shape.createPortalBlocks(lvl); return true; })
                .orElseGet(() -> PortalShape.findEmptyPortalShape(lvl, pos, Direction.Axis.Z)
                        .map(shape -> { shape.createPortalBlocks(lvl); return true; })
                        .orElse(false));
    }

    private static void finishUse(Level lvl, BlockPos pos, ItemStack stack, UseOnContext ctx) {
        lvl.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, lvl.getRandom().nextFloat() * 0.4F + 0.8F);
        Player user = ctx.getPlayer();
        if (user != null) {
            var slot = ctx.getHand() == InteractionHand.MAIN_HAND
                    ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, user, slot);
        } else {
            stack.shrink(1);
        }
    }

    private static boolean trySpawnBananaPortal(
            net.minecraft.server.level.ServerLevel server,
            BlockPos firePos, int rgb, String hexUpper,
            @Nullable Player player
    ) {
        var frameOpt = BananaPortalShape.find(server, firePos);
        if (frameOpt.isEmpty()) return false;
        var frame = frameOpt.get();

        // Choose a center-ish interior position for a small broadcast radius (visual pre-tint)
        Direction right = (frame.axis() == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        BlockPos center = frame.anchor()
                .relative(right, (frame.width()  - 1) / 2)
                .above((frame.height() - 1) / 2);

        net.anatomyworld.harambefmod.network.ModNetworking.sendPortalTintToNearby(
                server,
                center,
                new net.anatomyworld.harambefmod.network.SyncPortalTintPayload(
                        frame.anchor(),
                        frame.axis(),
                        frame.width(),
                        frame.height(),
                        rgb
                )
        );

        // Defer the actual fill to the next tick
        server.getServer().execute(() -> {
            // Fill interior & set color/axis/anchor on each BE
            BananaPortalShape.fill(server, frame, rgb);

            // Decide & store FRONT at light time (restricted to valid normals)
            Direction front = pickFront(player, frame);

            // Write FRONT to every BE in this interior
            for (int y = 0; y < frame.height(); y++) {
                for (int x = 0; x < frame.width(); x++) {
                    BlockPos ip = frame.anchor().relative(right, x).above(y);
                    var be = server.getBlockEntity(ip);
                    if (be instanceof net.anatomyworld.harambefmod.block.entity.BananaPortalBlockEntity pbe) {
                        pbe.setFront(front);
                    }
                }
            }

            // Register or link with the chosen code
            var data = net.anatomyworld.harambefmod.world.PortalLinkData.get(server);
            int res = data.registerOrLink(server, frame, hexUpper, rgb, front);

            if (res < 0) {
                // Undo fill if code already fully used
                for (int y = 0; y < frame.height(); y++) {
                    for (int x = 0; x < frame.width(); x++) {
                        BlockPos ip = frame.anchor().relative(right, x).above(y);
                        if (server.getBlockState(ip).is(ModBlocks.BANANA_PORTAL.get())) {
                            server.setBlock(ip, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
                server.players().forEach(p -> {
                    if (p.distanceToSqr(firePos.getX() + 0.5, firePos.getY() + 0.5, firePos.getZ() + 0.5) < 16 * 16)
                        p.displayClientMessage(net.minecraft.network.chat.Component.literal("That code is already linked."), true);
                });
                return;
            }

            if (res == 0) {
                server.players().forEach(p -> {
                    if (p.distanceToSqr(firePos.getX() + 0.5, firePos.getY() + 0.5, firePos.getZ() + 0.5) < 16 * 16)
                        p.displayClientMessage(net.minecraft.network.chat.Component.literal("Portal set to code " + hexUpper + ". Light another frame with the same code to link."), true);
                });
            } else {
                server.players().forEach(p -> {
                    if (p.distanceToSqr(firePos.getX() + 0.5, firePos.getY() + 0.5, firePos.getZ() + 0.5) < 16 * 16)
                        p.displayClientMessage(net.minecraft.network.chat.Component.literal("Linked portals for " + hexUpper + "!"), true);
                });
            }
        });

        return true;
    }

    private static Direction pickFront(@Nullable Player player, BananaPortalShape.Frame frame) {
        if (player == null) return frame.axis() == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;

        Direction look = player.getDirection(); // nearest horizontal cardinal
        if (frame.axis() == Direction.Axis.X) {
            // choose NORTH/SOUTH
            return (look == Direction.NORTH || look == Direction.SOUTH)
                    ? look
                    : (player.getZ() >= (frame.anchor().getZ() + frame.width() / 2.0) ? Direction.SOUTH : Direction.NORTH);
        } else {
            // axis Z: choose EAST/WEST
            return (look == Direction.EAST || look == Direction.WEST)
                    ? look
                    : (player.getX() >= (frame.anchor().getX() + frame.width() / 2.0) ? Direction.EAST : Direction.WEST);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static final class ScreenOpener {
        private ScreenOpener() {}
        static void openIfLookingAtAir(ItemStack stack) {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.MISS) {
                mc.setScreen(new net.anatomyworld.harambefmod.client.gui.FireColorSelectionScreen(stack));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static final class ClientPredictor {
        private ClientPredictor() {}
        static void place(Level lvl, BlockPos pos, int rgb) {
            lvl.setBlock(pos, ModBlocks.PEARL_FIRE.get().defaultBlockState(), 11);
            var be = lvl.getBlockEntity(pos);
            if (be instanceof PearlFireBlockEntity fire) fire.setColor(rgb);
        }
    }
}
