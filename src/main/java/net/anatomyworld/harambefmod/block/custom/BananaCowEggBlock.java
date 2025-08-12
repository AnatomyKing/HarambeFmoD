package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.entity.ModEntities;
import net.anatomyworld.harambefmod.entity.bananacow.BananaCow;
import net.anatomyworld.harambefmod.item.ModItems;
import net.anatomyworld.harambefmod.data.ModTags;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BananaCowEggBlock extends Block implements BonemealableBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 2); // 0..2
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached"); // ceiling in tag + flower below
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

    /* keep ATTACHED synced */
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(ATTACHED, isAttached(level, pos)), Block.UPDATE_ALL);
        }
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state,
                                           @NotNull net.minecraft.core.Direction face,
                                           @NotNull BlockState fromState,
                                           @NotNull LevelAccessor level,
                                           @NotNull BlockPos pos,
                                           @NotNull BlockPos fromPos) {
        if (!level.isClientSide()) {
            return state.setValue(ATTACHED, isAttached((Level) level, pos));
        }
        return state;
    }

    private boolean isAttached(Level level, BlockPos eggPos) {
        BlockState up   = level.getBlockState(eggPos.above());
        BlockState down = level.getBlockState(eggPos.below());
        return up.is(ModTags.Blocks.BANANA_COW_GROWTH) && down.is(ModBlocks.MUSAVACCA_FLOWER.get());
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

    /* drops / hatch — handled here (table should be EMPTY) */
    @Override
    protected void spawnAfterBreak(@NotNull BlockState state, @NotNull ServerLevel level,
                                   @NotNull BlockPos pos, @NotNull ItemStack tool, boolean dropXp) {
        if (level.isClientSide || !level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) return;

        boolean attached = state.getValue(ATTACHED);
        int age = state.getValue(AGE);

        // If the egg was attached, remove the flower and return its item.
        if (attached && level.getBlockState(pos.below()).is(ModBlocks.MUSAVACCA_FLOWER.get())) {
            level.removeBlock(pos.below(), false);
            // Block implements ItemLike, so this resolves the BlockItem you auto-registered.
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

        // No Silk Touch: hatch when ripe (age == 2) — attached or not.
        if (age == 2) {
            hatch(level, pos, attached);
        }

        super.spawnAfterBreak(state, level, pos, tool, dropXp);
    }

    /* 1.21+ enchantment check */
    private static boolean hasSilkTouch(ServerLevel level, ItemStack tool) {
        if (tool.isEmpty()) return false;
        Holder<Enchantment> silk = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        return EnchantmentHelper.getItemEnchantmentLevel(silk, tool) > 0;
    }

    /** General hatch with collision-friendly placement. */
    public static void hatch(ServerLevel level, BlockPos pos, boolean wasAttached) {
        BananaCow cow = ModEntities.BANANA_COW.get().create(level);
        if (cow == null) return;

        final double eps = 0.01D;
        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;
        float yaw = level.random.nextFloat() * 360F;

        if (wasAttached) {
            double y = pos.getY() - 0.40D - eps; // use space freed by flower
            cow.moveTo(x, y, z, yaw, 0.0F);
            if (!level.noCollision(cow)) {
                y = pos.getY() + eps; // fallback
                cow.moveTo(x, y, z, yaw, 0.0F);
            }
            if (level.noCollision(cow)) level.addFreshEntity(cow);
            return;
        }

        double y = pos.getY() + eps; // detached: try in-place
        cow.moveTo(x, y, z, yaw, 0.0F);

        if (!level.noCollision(cow)) {
            BlockPos candidate = findTwoHighAirNearby(level, pos);
            if (candidate != null) {
                x = candidate.getX() + 0.5D;
                y = candidate.getY() + eps;
                z = candidate.getZ() + 0.5D;
                cow.moveTo(x, y, z, yaw, 0.0F);
            }
        }

        if (!level.noCollision(cow) && level.getBlockState(pos.below()).isAir()) {
            y = pos.getY() - 0.30D;
            cow.moveTo(x, y, z, yaw, 0.0F);
        }

        if (level.noCollision(cow)) level.addFreshEntity(cow);
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

