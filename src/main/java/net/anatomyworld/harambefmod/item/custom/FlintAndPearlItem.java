package net.anatomyworld.harambefmod.item.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.entity.PearlFireBlockEntity;
import net.anatomyworld.harambefmod.component.ModDataComponents;
import net.anatomyworld.harambefmod.network.PlaceFirePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public final class FlintAndPearlItem extends FlintAndSteelItem {
    private static final String DEFAULT_COLOR = "#D5CD49";

    public FlintAndPearlItem(Properties props) { super(props); }

    /* ------------------------------------------------------------------
     *  Right-click AIR  → colour picker (client only)
     * ---------------------------------------------------------------- */
    @Override
    public @NotNull InteractionResultHolder<ItemStack>
    use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            ScreenOpener.openIfLookingAtAir(held);
            return InteractionResultHolder.success(held);
        }
        return super.use(level, player, hand);
    }

    /* ------------------------------------------------------------------
     *  Right-click ENTITY  → ignite creepers
     * ---------------------------------------------------------------- */
    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack stack,
                                                           Player player,
                                                           @NotNull LivingEntity target,
                                                           InteractionHand hand) {
        if (!player.level().isClientSide && target instanceof Creeper creeper) {
            creeper.ignite();
            stack.hurtAndBreak(1, player,
                    hand == InteractionHand.MAIN_HAND
                            ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                            : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /* ------------------------------------------------------------------
     *  Right-click BLOCK
     * ---------------------------------------------------------------- */
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext ctx) {

        Level       level    = ctx.getLevel();
        BlockPos    clicked  = ctx.getClickedPos();
        Direction   face     = ctx.getClickedFace();
        BlockPos    firePos  = clicked.relative(face);
        BlockState  state    = level.getBlockState(clicked);
        ItemStack   stack    = ctx.getItemInHand();

        /* ------------------------------------------------ client side -- */
        if (level.isClientSide) {
            // Only send the pearl-fire payload when *none* of the vanilla
            // interactions apply.
            if (shouldPlacePearlFireClient(state, level, firePos, face)) {
                String hex = stack.getOrDefault(
                        ModDataComponents.FLAME_COLOR.get(), DEFAULT_COLOR);
                int rgb = Integer.parseInt(hex.replace("#", ""), 16);

                PacketDistributor.sendToServer(new PlaceFirePayload(
                        firePos, rgb,
                        ctx.getHand() == InteractionHand.MAIN_HAND,
                        (byte) face.ordinal()));

                ClientPredictor.place(level, firePos, rgb);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        /* ------------------------------------------------ server side -- */

        // TNT
        if (state.getBlock() instanceof TntBlock) {
            state.onCaughtFire(level, clicked, face, ctx.getPlayer());
            level.setBlock(clicked, Blocks.AIR.defaultBlockState(), 11);
            finishUse(level, clicked, stack, ctx);
            return InteractionResult.sidedSuccess(false);
        }

        // Candles & campfires
        if (CampfireBlock.canLight(state)
                || CandleBlock.canLight(state)
                || CandleCakeBlock.canLight(state)) {
            level.setBlock(clicked, state.setValue(BlockStateProperties.LIT, true), 11);
            finishUse(level, clicked, stack, ctx);
            return InteractionResult.sidedSuccess(false);
        }

        // Nether portal (checks both axes like vanilla)
        if (trySpawnNetherPortal(level, firePos)) {
            finishUse(level, firePos, stack, ctx);
            return InteractionResult.sidedSuccess(false);
        }

        /* Pearl fire will be placed later via PlaceFirePayload */
        return InteractionResult.PASS;
    }

    /* ------------------------------------------------------------------
     *  Helpers
     * ---------------------------------------------------------------- */
    private static void finishUse(Level lvl, BlockPos pos,
                                  ItemStack stack, UseOnContext ctx) {
        playUseSound(lvl, pos);
        damage(stack, ctx);
    }

    private static boolean shouldPlacePearlFireClient(BlockState clickedState,
                                                      Level level,
                                                      BlockPos firePos,
                                                      Direction face) {
        // If vanilla would do something, *skip* pearl fire
        if (clickedState.getBlock() instanceof TntBlock)                          return false;
        if (CampfireBlock.canLight(clickedState)
                || CandleBlock.canLight(clickedState)
                || CandleCakeBlock.canLight(clickedState))                        return false;
        if (PortalShape.findEmptyPortalShape(level, firePos, Direction.Axis.X)
                .isPresent() ||
                PortalShape.findEmptyPortalShape(level, firePos, Direction.Axis.Z)
                        .isPresent())                                                     return false;

        // Otherwise, place our fire if allowed
        return BaseFireBlock.canBePlacedAt(level, firePos, face);
    }

    private static boolean trySpawnNetherPortal(Level lvl, BlockPos pos) {
        return PortalShape.findEmptyPortalShape(lvl, pos, Direction.Axis.X)
                .map(shape -> { shape.createPortalBlocks(); return true; })
                .orElseGet(() ->
                        PortalShape.findEmptyPortalShape(lvl, pos, Direction.Axis.Z)
                                .map(shape -> { shape.createPortalBlocks(); return true; })
                                .orElse(false));
    }

    private static void playUseSound(Level lvl, BlockPos pos) {
        lvl.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE,
                net.minecraft.sounds.SoundSource.BLOCKS,
                1.0F, lvl.getRandom().nextFloat() * 0.4F + 0.8F);
    }

    private static void damage(ItemStack stack, UseOnContext ctx) {
        Player user = ctx.getPlayer();
        if (user != null) {
            var slot = ctx.getHand() == InteractionHand.MAIN_HAND
                    ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, user, slot);
        } else stack.shrink(1);
    }

    /* ------------------------------------------------------------------
     *  Client-only helpers
     * ---------------------------------------------------------------- */
    @OnlyIn(Dist.CLIENT)
    private static final class ScreenOpener {
        private ScreenOpener() {}
        static void openIfLookingAtAir(ItemStack stack) {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.hitResult != null &&
                    mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
                mc.setScreen(
                        new net.anatomyworld.harambefmod.client.gui.FireColorSelectionScreen(stack));
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
