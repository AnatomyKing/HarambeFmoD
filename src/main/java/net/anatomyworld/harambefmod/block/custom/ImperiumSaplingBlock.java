package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.worldgen.TreeStyles;
import net.anatomyworld.harambefmod.worldgen.trees.ImperiumTreeBuilder;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Sapling for the Imperium redwood/pine cone-crown tree. */
public class ImperiumSaplingBlock extends AbstractPatchTreeSaplingBlock {

    public static final MapCodec<ImperiumSaplingBlock> CODEC =
            BlockBehaviour.simpleCodec(ImperiumSaplingBlock::new);

    public ImperiumSaplingBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override protected TreeStyles.PatchTreeBuilder builder() { return new ImperiumTreeBuilder(); }
    @Override protected int estMaxHeight() { return 32; }  // tall!
    @Override protected int estNearRadius() { return 2; }

    @Override
    protected MapCodec<? extends ImperiumSaplingBlock> codec() {
        return CODEC;
    }
}
