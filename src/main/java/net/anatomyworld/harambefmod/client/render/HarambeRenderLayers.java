package net.anatomyworld.harambefmod.client.render;

import com.google.common.collect.Sets;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import java.util.*;

/**
 * Forces specific blocks to render on a chosen ChunkSectionLayer (CUTOUT/TRANSLUCENT)
 * for NeoForge 1.21.8, purely in code by wrapping BlockStateModels during
 * ModelEvent.ModifyBakingResult. This does not create transparency by itself —
 * your quads must have alpha in their textures or be custom-rendered.
 */
public final class HarambeRenderLayers {

    // Choose which blocks should be on which layer:
    private static final Set<Block> CUTOUT_BLOCKS = Sets.newHashSet(
            ModBlocks.MUSAVACCA_FLOWER.get(),
            ModBlocks.MUSAVACCA_LEAVES.get(),
            ModBlocks.MUSAVACCA_LEAVES_CROWN.get(),
            ModBlocks.MUSAVACCA_PLANT.get(),
            ModBlocks.MUSAVACCA_SAPLING.get(),
            ModBlocks.BANANA_COW_EGG.get(),
            ModBlocks.PEARL_FIRE.get(),
            ModBlocks.SMALL_HONEY_CRYSTAL_BUD.get(),
            ModBlocks.MEDIUM_HONEY_CRYSTAL_BUD.get(),
            ModBlocks.LARGE_HONEY_CRYSTAL_BUD.get(),
            ModBlocks.HONEY_CRYSTAL_CLUSTER.get()

    );

    private static final Set<Block> TRANSLUCENT_BLOCKS = Sets.newHashSet(
            ModBlocks.BANANA_PORTAL.get()
    );

    private HarambeRenderLayers() {}

    /** Register this on the MOD bus (client-only). */
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
        if (models.isEmpty()) return;

        for (Map.Entry<BlockState, BlockStateModel> e : models.entrySet()) {
            BlockState state = e.getKey();
            BlockStateModel original = e.getValue();
            Block b = state.getBlock();

            if (CUTOUT_BLOCKS.contains(b)) {
                e.setValue(new ForceLayerStateModel(original, ChunkSectionLayer.CUTOUT));
            } else if (TRANSLUCENT_BLOCKS.contains(b)) {
                e.setValue(new ForceLayerStateModel(original, ChunkSectionLayer.TRANSLUCENT));
            }
        }
    }

    /** Wrap a BlockStateModel and swap its parts with layer-forcing proxies. */
    private static final class ForceLayerStateModel extends DelegateBlockStateModel {
        private final ChunkSectionLayer layer;

        ForceLayerStateModel(BlockStateModel delegate, ChunkSectionLayer layer) {
            super(delegate);
            this.layer = layer;
        }

        @Override
        public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                 RandomSource random, List<BlockModelPart> out) {
            // Get the original parts
            List<BlockModelPart> original = new ArrayList<>();
            this.delegate.collectParts(level, pos, state, random, original);

            // Preserve the particle icon behavior of the base model
            TextureAtlasSprite particle = this.particleIcon(level, pos, state);

            for (BlockModelPart part : original) {
                out.add(new ForceLayerPart(part, particle, layer));
            }
        }
    }

    /**
     * Minimal proxy for BlockModelPart that forwards everything to the base part,
     * but forces the chosen ChunkSectionLayer via the NeoForge extension hook.
     * Also implements the TriState AO method to avoid deprecation warnings.
     */
    private record ForceLayerPart(BlockModelPart base,
                                  TextureAtlasSprite particle,
                                  ChunkSectionLayer forcedLayer) implements BlockModelPart {

        // --- BlockModelPart (vanilla) ---

        @Override
        public List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(Direction face) {
            return base.getQuads(face);
        }

        /** Deprecated but still required by the interface (delegated). */
        @Override
        public boolean useAmbientOcclusion() {
            return base.useAmbientOcclusion();
        }

        @Override
        public TextureAtlasSprite particleIcon() {
            return (particle != null) ? particle : base.particleIcon();
        }

        // --- NeoForge extension methods on BlockModelPartExtension ---

        /** Future-proof AO: return DEFAULT (use the model’s normal AO behavior). */
        @Override
        public TriState ambientOcclusion() {
            return TriState.DEFAULT;
        }

        /** Force which chunk layer this part should be buffered into. */
        @Override
        public ChunkSectionLayer getRenderType(BlockState state) {
            return forcedLayer;
        }
    }
}
