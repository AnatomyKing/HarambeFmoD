// StrippableNatrualPillarBlock.java (NeoForge 1.21.8)
package net.anatomyworld.harambefmod.block.custom;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Log-like pillar that:
 * - adds a 'natural' boolean blockstate (true for worldgen / false for player-placed)
 * - preserves AXIS + NATURAL when stripping
 */
public class StrippableNatrualPillarBlock extends RotatedPillarBlock {
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

    private final Supplier<? extends RotatedPillarBlock> stripped;

    public StrippableNatrualPillarBlock(Properties props, Supplier<? extends RotatedPillarBlock> stripped) {
        super(props);
        this.stripped = stripped;
        // Default for worldgen: natural=true, axis=Y
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(NATURAL, Boolean.TRUE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NATURAL);
    }

    /** Player placement => mark as NOT natural. Worldgen keeps the default (natural=true). */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState base = super.getStateForPlacement(ctx);
        if (base == null) return null;
        return base.setValue(NATURAL, Boolean.FALSE);
    }

    /** Preserve AXIS + NATURAL when stripping. */
    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state,
                                                     UseOnContext ctx,
                                                     ItemAbility ability,
                                                     boolean simulate) {
        if (ability == ItemAbilities.AXE_STRIP) {
            return stripped.get().defaultBlockState()
                    .setValue(AXIS, state.getValue(AXIS))
                    .setValue(NATURAL, state.getValue(NATURAL));
        }
        return super.getToolModifiedState(state, ctx, ability, simulate);
    }
}
