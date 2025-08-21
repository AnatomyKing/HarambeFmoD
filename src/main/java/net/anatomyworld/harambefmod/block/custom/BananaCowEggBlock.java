package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.data.ModTags;
import net.anatomyworld.harambefmod.entity.ModEntities;
import net.anatomyworld.harambefmod.entity.bananacow.BananaCow;
import net.anatomyworld.harambefmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BananaCowEggBlock extends Block implements BonemealableBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 2);
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 14, 14);

    public BananaCowEggBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(ATTACHED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(AGE, ATTACHED);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull net.minecraft.world.level.BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    /* Keep ATTACHED synced on placement. */
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                        @NotNull BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(ATTACHED, isAttached(level, pos)), Block.UPDATE_ALL);
        }
    }

    /* IMPORTANT: no side-effects here; just keep ATTACHED in sync. */
    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state,
                                           @NotNull net.minecraft.core.Direction face,
                                           @NotNull BlockState fromState,
                                           @NotNull LevelAccessor level,
                                           @NotNull BlockPos pos,
                                           @NotNull BlockPos fromPos) {
        if (level instanceof Level L) {
            return state.setValue(ATTACHED, isAttached(L, pos));
        }
        return state;
    }

    private boolean isAttached(Level level, BlockPos eggPos) {
        BlockState up   = level.getBlockState(eggPos.above());
        BlockState down = level.getBlockState(eggPos.below());
        return up.is(ModTags.Blocks.BANANA_COW_GROWTH) && down.is(ModBlocks.MUSAVACCA_FLOWER.get());
    }

    /* -------- NEW: act like a break when the TOP growth block is removed -------- */
    @Override
    public void neighborChanged(@NotNull BlockState state,
                                @NotNull Level level,
                                @NotNull BlockPos pos,
                                @NotNull Block neighborBlock,
                                @NotNull BlockPos fromPos,
                                boolean isMoving) {
        if (level.isClientSide) return;
        if (!state.is(this)) return;

        // ONLY handle the top neighbor change here (flower path is handled by MusavaccaFlowerBlock#onRemove)
        if (fromPos.equals(pos.above())) {
            boolean wasAttached = state.getValue(ATTACHED);
            BlockState upNow = level.getBlockState(pos.above());
            boolean lostTop = !upNow.is(ModTags.Blocks.BANANA_COW_GROWTH);

            if (wasAttached && lostTop) {
                // 0) play vanilla destroy FX for the egg
                level.levelEvent(2001, pos, Block.getId(state)); // particles + sound

                // 1) free the space first, like a real break
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

                // 2) if attached and the flower is still there, remove it and RETURN one flower item
                BlockPos flowerPos = pos.below();
                BlockState flowerState = level.getBlockState(flowerPos);
                if (flowerState.is(ModBlocks.MUSAVACCA_FLOWER.get())) {
                    level.removeBlock(flowerPos, false);
                    if (level instanceof ServerLevel sl) {
                        popResource(sl, pos, new ItemStack(ModBlocks.MUSAVACCA_FLOWER.get()));
                    }
                }

                // 3) hatch if ripe (no silk touch context here)
                int age = state.getValue(AGE);
                if (age == 2 && level instanceof ServerLevel sl) {
                    hatch(sl, pos, true); // was attached
                }

                // 4) notify listeners/sculk
                level.gameEvent(null, GameEvent.BLOCK_DESTROY, pos);
                return;
            }
        }

        // keep ATTACHED flag in sync for any other neighbor change
        boolean nowAttached = isAttached(level, pos);
        if (nowAttached != state.getValue(ATTACHED)) {
            level.setBlock(pos, state.setValue(ATTACHED, nowAttached), Block.UPDATE_CLIENTS);
        }
    }

    /* random growth while attached */
    @Override public boolean isRandomlyTicking(@NotNull BlockState s) { return s.getValue(ATTACHED) && s.getValue(AGE) < 2; }
    @Override public void randomTick(@NotNull BlockState s, @NotNull ServerLevel lvl, @NotNull BlockPos p, @NotNull RandomSource r) {
        if (!s.getValue(ATTACHED)) return;
        int age = s.getValue(AGE);
        if (age < 2 && r.nextInt(3) == 0) {
            lvl.setBlock(p, s.setValue(AGE, age + 1), Block.UPDATE_ALL);
        }
    }

    /* bonemeal */
    @Override public boolean isValidBonemealTarget(@NotNull net.minecraft.world.level.LevelReader l, @NotNull BlockPos p, @NotNull BlockState s) {
        return s.getValue(ATTACHED) && s.getValue(AGE) < 2;
    }
    @Override public boolean isBonemealSuccess(@NotNull Level l, @NotNull RandomSource r, @NotNull BlockPos p, @NotNull BlockState s) { return true; }
    @Override public void performBonemeal(@NotNull ServerLevel l, @NotNull RandomSource r, @NotNull BlockPos p, @NotNull BlockState s) {
        int age = s.getValue(AGE);
        if (age < 2) l.setBlock(p, s.setValue(AGE, Math.min(2, age + 1)), Block.UPDATE_ALL);
    }

    /* Egg broken directly: handle drops / hatch. */
    @Override
    protected void spawnAfterBreak(@NotNull BlockState state, @NotNull ServerLevel level,
                                   @NotNull BlockPos pos, @NotNull ItemStack tool, boolean dropXp) {
        if (level.isClientSide || !level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) return;

        boolean attached = state.getValue(ATTACHED);
        int age = state.getValue(AGE);

        // If attached, remove the flower block below and return the flower item from the egg.
        if (attached && level.getBlockState(pos.below()).is(ModBlocks.MUSAVACCA_FLOWER.get())) {
            level.removeBlock(pos.below(), false);
            popResource(level, pos, new ItemStack(ModBlocks.MUSAVACCA_FLOWER.get()));
        }

        if (hasSilkTouch(level, tool)) {
            ItemStack drop = switch (age) {
                case 0 -> new ItemStack(ModItems.BANANA_COW_EGG_UNRIPE.get());
                case 1 -> new ItemStack(ModItems.BANANA_COW_EGG_RIPENING.get());
                default -> new ItemStack(ModItems.BANANA_COW_EGG_RIPE.get());
            };
            popResource(level, pos, drop);
            super.spawnAfterBreak(state, level, pos, tool, dropXp);
            return;
        }

        if (age == 2) {
            hatch(level, pos, attached);
        }

        super.spawnAfterBreak(state, level, pos, tool, dropXp);
    }

    /* 1.21+ Silk Touch check */
    private static boolean hasSilkTouch(ServerLevel level, ItemStack tool) {
        if (tool.isEmpty()) return false;
        Holder<Enchantment> silk = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        return EnchantmentHelper.getItemEnchantmentLevel(silk, tool) > 0;
    }

    /** General hatch with collision-friendly placement (attached-first policy). */
    public static void hatch(ServerLevel level, BlockPos pos, boolean wasAttached) {
        BananaCow cow = ModEntities.BANANA_COW.get().create(level);
        if (cow == null) return;

        final double eps = 0.01D;
        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;
        float yaw = level.random.nextFloat() * 360F;

        boolean belowAir = level.getBlockState(pos.below()).isAir();

        // 1) Primary: "space below" (attached eggs free the flower space). For non-attached, use it too if air below.
        if (wasAttached || belowAir) {
            double yBelow = pos.getY() - 0.40D - eps;
            cow.moveTo(x, yBelow, z, yaw, 0.0F);
            if (level.noCollision(cow)) {
                level.addFreshEntity(cow);
                return;
            }
        }

        // 2) Fallback: in-place (slightly above the egg)
        double yHere = pos.getY() + eps;
        cow.moveTo(x, yHere, z, yaw, 0.0F);
        if (level.noCollision(cow)) {
            level.addFreshEntity(cow);
            return;
        }

        // 3) Fallback: nearby two-high air with support
        BlockPos candidate = findTwoHighAirNearby(level, pos);
        if (candidate != null) {
            double xN = candidate.getX() + 0.5D;
            double yN = candidate.getY() + eps;
            double zN = candidate.getZ() + 0.5D;
            cow.moveTo(xN, yN, zN, yaw, 0.0F);
            if (level.noCollision(cow)) {
                level.addFreshEntity(cow);
                return;
            }
        }

        // 4) Last resort: one block down if air
        if (belowAir) {
            double yDown = pos.getY() - 0.30D;
            cow.moveTo(x, yDown, z, yaw, 0.0F);
            if (level.noCollision(cow)) {
                level.addFreshEntity(cow);
            }
        }
    }

    private static BlockPos findTwoHighAirNearby(ServerLevel level, BlockPos origin) {
        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos p = origin.relative(d);
            if (level.getBlockState(p).isAir()
                    && level.getBlockState(p.above()).isAir()
                    && !level.getBlockState(p.below()).isAir()) {
                return p;
            }
        }
        return null;
    }
}
