// StrippableNatrualPillarBlock.java (NeoForge 1.21.8)
// Drop-in replacement: safe property copy when stripping.
package net.anatomyworld.harambefmod.block.custom;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

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

    /** Strip safely: copy AXIS (and only copy NATURAL if the target actually defines it). */
    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state,
                                                     UseOnContext ctx,
                                                     ItemAbility ability,
                                                     boolean simulate) {
        if (ability == ItemAbilities.AXE_STRIP) {
            BlockState out = stripped.get().defaultBlockState();

            // Always preserve axis if present on the stripped block (vanilla stripped logs do).
            if (out.hasProperty(AXIS)) {
                out = out.setValue(AXIS, state.getValue(AXIS));
            }

            // Only copy NATURAL if the stripped block ALSO declares it (your case: it does not).
            if (out.hasProperty(NATURAL)) {
                out = out.setValue(NATURAL, state.getValue(NATURAL));
            }

            return out;
        }
        return super.getToolModifiedState(state, ctx, ability, simulate);
    }
}
