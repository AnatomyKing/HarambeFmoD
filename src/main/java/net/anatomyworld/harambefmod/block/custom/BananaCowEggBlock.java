package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.entity.ModEntities;
import net.anatomyworld.harambefmod.entity.bananacow.BananaCow;
import net.anatomyworld.harambefmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
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

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 2); // 0,1,2
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached"); // oak above + flower below

    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 14, 14);

    public BananaCowEggBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(ATTACHED, false));
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

    /* Keep ATTACHED up-to-date */
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level,
                        @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
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
        BlockState up = level.getBlockState(eggPos.above());
        BlockState down = level.getBlockState(eggPos.below());
        return up.is(BlockTags.OAK_LOGS) && down.is(ModBlocks.MUSAVACCA_FLOWER.get());
    }

    /* Growth */
    @Override public boolean isRandomlyTicking(@NotNull BlockState s) {
        return s.getValue(ATTACHED) && s.getValue(AGE) < 2;
    }
    @Override public void randomTick(@NotNull BlockState s, @NotNull ServerLevel lvl,
                                     @NotNull BlockPos p, @NotNull RandomSource r) {
        if (!s.getValue(ATTACHED)) return;
        int age = s.getValue(AGE);
        if (age < 2 && r.nextInt(3) == 0) {
            lvl.setBlock(p, s.setValue(AGE, age + 1), Block.UPDATE_ALL);
        }
    }

    /* Bonemeal */
    @Override public boolean isValidBonemealTarget(@NotNull net.minecraft.world.level.LevelReader lvl,
                                                   @NotNull BlockPos p, @NotNull BlockState s) {
        return s.getValue(ATTACHED) && s.getValue(AGE) < 2;
    }
    @Override public boolean isBonemealSuccess(@NotNull Level l, @NotNull RandomSource r,
                                               @NotNull BlockPos p, @NotNull BlockState s) { return true; }
    @Override public void performBonemeal(@NotNull ServerLevel l, @NotNull RandomSource r,
                                          @NotNull BlockPos p, @NotNull BlockState s) {
        int age = s.getValue(AGE);
        if (age < 2) l.setBlock(p, s.setValue(AGE, Math.min(2, age + 1)), Block.UPDATE_ALL);
    }

    /* Drops / Hatch */
    @Override
    protected void spawnAfterBreak(@NotNull BlockState state, @NotNull ServerLevel level,
                                   @NotNull BlockPos pos, @NotNull ItemStack tool, boolean dropXp) {
        if (level.isClientSide || !level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) return;

        boolean attached = state.getValue(ATTACHED);
        int age = state.getValue(AGE);

        // Always give the flower back if it was attached
        if (attached && level.getBlockState(pos.below()).is(ModBlocks.MUSAVACCA_FLOWER.get())) {
            popResource(level, pos, new ItemStack(ModItems.MUSAVACCA_FLOWER.get()));
            level.removeBlock(pos.below(), false);
        }

        if (hasSilkTouch(level, tool)) {
            Item stageItem = switch (age) {
                case 0 -> ModItems.BANANA_COW_EGG_UNRIPE.get();
                case 1 -> ModItems.BANANA_COW_EGG_RIPENING.get();
                default -> ModItems.BANANA_COW_EGG_RIPE.get();
            };
            popResource(level, pos, new ItemStack(stageItem));
            return;
        }

        if (age == 2) hatch(level, pos); // no egg drop
    }

    private static boolean hasSilkTouch(ServerLevel level, ItemStack tool) {
        if (tool.isEmpty()) return false;
        Holder<Enchantment> silk = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        return EnchantmentHelper.getItemEnchantmentLevel(silk, tool) > 0;
    }

    private static void hatch(ServerLevel level, BlockPos pos) {
        BananaCow cow = ModEntities.BANANA_COW.get().create(level);
        if (cow != null) {
            cow.setAge(0);
            cow.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    level.random.nextFloat() * 360f, 0f);
            level.addFreshEntity(cow);
        }
    }
}
