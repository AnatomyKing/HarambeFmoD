package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.worldgen.TreeStyles;
import net.anatomyworld.harambefmod.worldgen.trees.MischiefTreeBuilder;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Sapling for the Mischief mangrove-wisteria x spooky tree. */
public class MischiefSaplingBlock extends AbstractPatchTreeSaplingBlock {

    public static final MapCodec<MischiefSaplingBlock> CODEC =
            BlockBehaviour.simpleCodec(MischiefSaplingBlock::new);

    public MischiefSaplingBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override protected TreeStyles.PatchTreeBuilder builder() { return new MischiefTreeBuilder(); }
    @Override protected int estMaxHeight() { return 22; }
    @Override protected int estNearRadius() { return 2; }

    @Override
    protected MapCodec<? extends MischiefSaplingBlock> codec() {
        return CODEC;
    }
}
