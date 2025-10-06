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
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import java.util.*;

/**
 * Forces specific blocks onto chosen ChunkSectionLayers AND/OR disables Ambient Occlusion (AO)
 * by wrapping BlockStateModels during ModelEvent.ModifyBakingResult (NeoForge 1.21.8).
 *
 * Notes:
 * - Layer forcing relies on NeoForge's model extension: parts can declare a specific ChunkSectionLayer.
 * - AO control uses the BlockModelPart extension method that returns a TriState for ambient occlusion.
 *   (TriState.FALSE disables AO for that part.)
 */
public final class HarambeRenderLayers {

    // 1) CUTOUT blocks (foliage, plants, clusters, etc.)
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
            ModBlocks.HONEY_CRYSTAL_CLUSTER.get(),
            ModBlocks.CAROTENE_SHORT_GRASS.get(),
            ModBlocks.BELMONT_SHORT_GRASS.get(),
            ModBlocks.DYNASTY_SHORT_GRASS.get(),
            ModBlocks.IMPERIUM_SHORT_GRASS.get(),
            ModBlocks.MISCHIEF_SHORT_GRASS.get(),
            ModBlocks.BELMONT_LEAVES.get(),
            ModBlocks.DYNASTY_LEAVES.get(),
            ModBlocks.IMPERIUM_LEAVES.get(),
            ModBlocks.MISCHIEF_LEAVES.get(),
            ModBlocks.BELMONT_SAPLING.get(),
            ModBlocks.DYNASTY_SAPLING.get(),
            ModBlocks.IMPERIUM_SAPLING.get(),
            ModBlocks.MISCHIEF_SAPLING.get(),
            ModBlocks.BIG_BELMONT_BANNER.get(),
            ModBlocks.BIG_DYNASTY_BANNER.get(),
            ModBlocks.BIG_IMPERIUM_BANNER.get(),
            ModBlocks.BIG_MISCHIEF_BANNER.get(),
            ModBlocks.BELMONT_VEIN.get(),
            ModBlocks.DYNASTY_VEIN.get(),
            ModBlocks.IMPERIUM_VEIN.get(),
            ModBlocks.MISCHIEF_VEIN.get()

    );

    // 2) TRANSLUCENT blocks (portals, tinted glass-like, etc.)
    private static final Set<Block> TRANSLUCENT_BLOCKS = Sets.newHashSet(
            ModBlocks.BANANA_PORTAL.get()
    );


    private static final Set<Block> NO_AO_BLOCKS = Sets.newHashSet(
            ModBlocks.CAROTENE_SHORT_GRASS.get(),
            ModBlocks.BELMONT_SHORT_GRASS.get(),
            ModBlocks.DYNASTY_SHORT_GRASS.get(),
            ModBlocks.IMPERIUM_SHORT_GRASS.get(),
            ModBlocks.MISCHIEF_SHORT_GRASS.get(),
            ModBlocks.BELMONT_SAPLING.get(),
            ModBlocks.DYNASTY_SAPLING.get(),
            ModBlocks.IMPERIUM_SAPLING.get(),
            ModBlocks.MISCHIEF_SAPLING.get()

    );

    private HarambeRenderLayers() {}

    /** Register this on the MOD bus (client-only): modEventBus.addListener(HarambeRenderLayers::onModifyBakingResult) */
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
        if (models.isEmpty()) return;

        for (Map.Entry<BlockState, BlockStateModel> e : models.entrySet()) {
            BlockState state = e.getKey();
            BlockStateModel original = e.getValue();
            Block b = state.getBlock();

            // Decide the forced layer (or keep default if null)
            ChunkSectionLayer forcedLayer = null;
            if (CUTOUT_BLOCKS.contains(b)) {
                forcedLayer = ChunkSectionLayer.CUTOUT;
            } else if (TRANSLUCENT_BLOCKS.contains(b)) {
                forcedLayer = ChunkSectionLayer.TRANSLUCENT;
            }

            // Decide AO override (TriState.FALSE disables AO; DEFAULT keeps vanilla/model behavior)
            TriState forcedAO = NO_AO_BLOCKS.contains(b) ? TriState.FALSE : TriState.DEFAULT;

            // Only wrap if we need to force something; otherwise keep the original model
            if (forcedLayer != null || forcedAO != TriState.DEFAULT) {
                e.setValue(new ForcePropsStateModel(original, forcedLayer, forcedAO));
            }
        }
    }

    /** Wraps a BlockStateModel and replaces its parts with proxies that can force layer and/or AO. */
    private static final class ForcePropsStateModel extends DelegateBlockStateModel {
        private final ChunkSectionLayer forcedLayerOrNull; // null = preserve base getRenderType
        private final TriState forcedAO; // TriState.DEFAULT = preserve, FALSE/TRUE = override

        ForcePropsStateModel(BlockStateModel delegate, ChunkSectionLayer forcedLayerOrNull, TriState forcedAO) {
            super(delegate);
            this.forcedLayerOrNull = forcedLayerOrNull;
            this.forcedAO = forcedAO;
        }

        @Override
        public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                 RandomSource random, List<BlockModelPart> out) {
            List<BlockModelPart> original = new ArrayList<>();
            this.delegate.collectParts(level, pos, state, random, original);

            TextureAtlasSprite particle = this.particleIcon(level, pos, state);

            for (BlockModelPart part : original) {
                out.add(new ForcePropsPart(part, particle, forcedLayerOrNull, forcedAO));
            }
        }
    }

    /**
     * Minimal proxy that:
     * - Delegates quads & particle icon
     * - Forces a chosen ChunkSectionLayer if provided
     * - Forces ambient occlusion via TriState if requested
     */
    private record ForcePropsPart(
            BlockModelPart base,
            TextureAtlasSprite particle,
            ChunkSectionLayer forcedLayerOrNull,
            TriState forcedAO
    ) implements BlockModelPart {

        // --- Vanilla methods ---
        @Override
        public List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(Direction face) {
            return base.getQuads(face);
        }

        /** Deprecated but required; keep vanilla behavior. */
        @Override
        public boolean useAmbientOcclusion() {
            return base.useAmbientOcclusion();
        }

        @Override
        public TextureAtlasSprite particleIcon() {
            return (particle != null) ? particle : base.particleIcon();
        }

        // --- NeoForge extension methods on BlockModelPart (present at runtime) ---

        /** AO override: FALSE disables, TRUE forces on, DEFAULT preserves base value. */
        @Override
        public TriState ambientOcclusion() {
            if (forcedAO != null && forcedAO != TriState.DEFAULT) {
                return forcedAO;
            }
            // Preserve the base model part's AO decision if available
            return base.ambientOcclusion();
        }

        /** Render layer override: when null, preserve base; otherwise force the chosen layer. */
        @Override
        public ChunkSectionLayer getRenderType(BlockState state) {
            if (forcedLayerOrNull != null) {
                return forcedLayerOrNull;
            }
            return base.getRenderType(state);
        }
    }
}
