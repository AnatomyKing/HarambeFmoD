package net.anatomyworld.harambefmod.block.custom;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static net.minecraft.world.level.block.RotatedPillarBlock.AXIS;

public class StrippablePillarBlock extends RotatedPillarBlock {
    private final Supplier<? extends RotatedPillarBlock> stripped;

    public StrippablePillarBlock(Properties props, Supplier<? extends RotatedPillarBlock> stripped) {
        super(props);
        this.stripped = stripped;
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state,
                                                     UseOnContext ctx,
                                                     ItemAbility ability,
                                                     boolean simulate) {
        if (ability == ItemAbilities.AXE_STRIP) {
            return stripped.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
        }
        return super.getToolModifiedState(state, ctx, ability, simulate);
    }
}
