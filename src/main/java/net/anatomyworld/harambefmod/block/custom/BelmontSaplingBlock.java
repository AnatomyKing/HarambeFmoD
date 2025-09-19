package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.worldgen.TreeStyles;
import net.anatomyworld.harambefmod.worldgen.trees.BelmontTreeBuilder;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Sapling for the Belmont shelf-crown tree. */
public class BelmontSaplingBlock extends AbstractPatchTreeSaplingBlock {

    public static final MapCodec<BelmontSaplingBlock> CODEC =
            BlockBehaviour.simpleCodec(BelmontSaplingBlock::new);

    public BelmontSaplingBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override protected TreeStyles.PatchTreeBuilder builder() { return new BelmontTreeBuilder(); }
    @Override protected int estMaxHeight() { return 22; }   // generous headroom
    @Override protected int estNearRadius() { return 2; }   // small near-trunk clearance

    @Override
    protected MapCodec<? extends BelmontSaplingBlock> codec() {
        return CODEC;
    }
}
