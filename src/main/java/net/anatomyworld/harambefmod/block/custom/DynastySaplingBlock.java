package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.worldgen.TreeStyles;
import net.anatomyworld.harambefmod.worldgen.trees.DynastyTreeBuilder;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Sapling for the Dynasty braided-trunk tree. */
public class DynastySaplingBlock extends AbstractPatchTreeSaplingBlock {

    public static final MapCodec<DynastySaplingBlock> CODEC =
            BlockBehaviour.simpleCodec(DynastySaplingBlock::new);

    public DynastySaplingBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override protected TreeStyles.PatchTreeBuilder builder() { return new DynastyTreeBuilder(); }
    @Override protected int estMaxHeight() { return 20; }
    @Override protected int estNearRadius() { return 2; }

    @Override
    protected MapCodec<? extends DynastySaplingBlock> codec() {
        return CODEC;
    }
}
