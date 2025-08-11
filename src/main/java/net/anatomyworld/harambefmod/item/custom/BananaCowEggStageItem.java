package net.anatomyworld.harambefmod.item.custom;

import net.anatomyworld.harambefmod.block.custom.BananaCowEggBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Each item places the egg with a fixed AGE and ATTACHED=false (display egg). */
public class BananaCowEggStageItem extends BlockItem {
    private final int fixedAge; // 0,1,2

    public BananaCowEggStageItem(net.minecraft.world.level.block.Block block, Item.Properties props, int fixedAge) {
        super(block, props);
        this.fixedAge = Math.max(0, Math.min(2, fixedAge));
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext ctx) {
        BlockState base = super.getPlacementState(ctx);
        if (base == null) return null;
        return base
                .setValue(BananaCowEggBlock.AGE, this.fixedAge)
                .setValue(BananaCowEggBlock.ATTACHED, false);
    }
}
