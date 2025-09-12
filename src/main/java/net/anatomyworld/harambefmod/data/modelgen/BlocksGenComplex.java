package net.anatomyworld.harambefmod.data.modelgen;

import com.mojang.math.Quadrant;
import net.anatomyworld.harambefmod.block.custom.BananaCowEggBlock;
import net.anatomyworld.harambefmod.block.custom.MusavaccaPlantCropBlock;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static net.anatomyworld.harambefmod.data.modelgen.ModelUtil.*;

/** Complex helpers + your specific states (1.21.8-safe). */
public final class BlocksGenComplex {

    /* ------------------------ Portal axis (nether-like) ------------------------ */
    public static void portalAxis(BlockModelGenerators gen, Block portalLike) {
        // e.g. harambefmod:banana_portal
        ResourceLocation id = idOf(portalLike);
        String ns = id.getNamespace();
        String name = id.getPath();

        // Your portal texture: assets/<ns>/textures/block/<name>.png
        ResourceLocation portalTex = texOf(portalLike);

        // Vanilla portal models use "portal" + "particle" texture keys.
        TextureSlot PORTAL = TextureSlot.create("portal", TextureSlot.ALL);

        // Build fresh templates (DO NOT extend CUBE, or it will require face slots).
        ModelTemplate nsTemplate = new ModelTemplate(
                java.util.Optional.of(blockModel("minecraft", "nether_portal_ns")),
                java.util.Optional.empty(),
                TextureSlot.PARTICLE, PORTAL
        );
        ModelTemplate ewTemplate = new ModelTemplate(
                java.util.Optional.of(blockModel("minecraft", "nether_portal_ew")),
                java.util.Optional.empty(),
                TextureSlot.PARTICLE, PORTAL
        );

        // Map the required slots to your texture.
        TextureMapping mapping = new TextureMapping()
                .put(PORTAL, portalTex)
                .put(TextureSlot.PARTICLE, portalTex);

        // Write YOUR models
        ResourceLocation nsModel = nsTemplate.create(rl(ns, "block/" + name + "_ns"), mapping, gen.modelOutput);
        ResourceLocation ewModel = ewTemplate.create(rl(ns, "block/" + name + "_ew"), mapping, gen.modelOutput);

        // Blockstate: default → ns, then override by AXIS (vanilla mapping: X→NS, Z→EW)
        Variant base = new Variant(nsModel);
        gen.blockStateOutput.accept(
                MultiVariantGenerator
                        .dispatch(portalLike, BlockModelGenerators.variant(base))
                        .with(PropertyDispatch.modify(
                                        net.anatomyworld.harambefmod.block.custom.BananaPortalBlock.AXIS)
                                .select(Direction.Axis.X, VariantMutator.MODEL.withValue(nsModel))
                                .select(Direction.Axis.Z, VariantMutator.MODEL.withValue(ewModel))
                        )
        );
    }

    /* ------------------------ Portal (axis) — blockstate-only ------------------------ */
    public static void portalAxisStates(BlockModelGenerators gen, Block portalLike) {
        ResourceLocation id = idOf(portalLike);
        String ns = id.getNamespace();
        String name = id.getPath();

        // Preexisting model ids you provide
        ResourceLocation nsModel = rl(ns, "block/" + name + "_ns");
        ResourceLocation ewModel = rl(ns, "block/" + name + "_ew");

        // Default to NS; override by AXIS (X→NS, Z→EW)
        Variant base = new Variant(nsModel);
        gen.blockStateOutput.accept(
                MultiVariantGenerator
                        .dispatch(portalLike, BlockModelGenerators.variant(base))
                        .with(PropertyDispatch.modify(
                                        net.anatomyworld.harambefmod.block.custom.BananaPortalBlock.AXIS)
                                .select(Direction.Axis.X, VariantMutator.MODEL.withValue(nsModel))
                                .select(Direction.Axis.Z, VariantMutator.MODEL.withValue(ewModel))
                        )
        );
    }




    /* ------------------------ Lamp (LIT boolean) ------------------------ */
    public static void lampLit(BlockModelGenerators gen, Block lamp) {
        var id = idOf(lamp);
        var unlitTex = rl(id.getNamespace(), "block/" + id.getPath());
        var litTex   = rl(id.getNamespace(), "block/" + id.getPath() + "_on");

        var unlitModel = ModelTemplates.CUBE_ALL.create(lamp,
                new TextureMapping().put(TextureSlot.ALL, unlitTex), gen.modelOutput);
        var litModel   = ModelTemplates.CUBE_ALL.create(
                rl(id.getNamespace(), "block/" + id.getPath() + "_on"),
                new TextureMapping().put(TextureSlot.ALL, litTex), gen.modelOutput);

        var mp = MultiPartGenerator.multiPart(lamp)
                .with(
                        BlockModelGenerators.or(
                                BlockModelGenerators.condition().term(BlockStateProperties.LIT, false)
                        ),
                        BlockModelGenerators.variants(new Variant(unlitModel))
                )
                .with(
                        BlockModelGenerators.or(
                                BlockModelGenerators.condition().term(BlockStateProperties.LIT, true)
                        ),
                        BlockModelGenerators.variants(new Variant(litModel))
                );
        gen.blockStateOutput.accept(mp);
    }

    /* ------------------------ Slab/Stairs/Fence/Wall/Pane ------------------------ */
    public static void slabAuto(BlockModelGenerators gen, Block slab, Block full) {
        TextureMapping map = new TextureMapping()
                .put(TextureSlot.SIDE,   texOf(full))
                .put(TextureSlot.TOP,    texOf(full, "_top"))
                .put(TextureSlot.BOTTOM, texOf(full, "_bottom"));

        var bottom = ModelTemplates.SLAB_BOTTOM.create(slab, map, gen.modelOutput);
        var top    = ModelTemplates.SLAB_TOP.create(   slab, map, gen.modelOutput);
        var fullModel = rl(idOf(full).getNamespace(), "block/" + idOf(full).getPath());

        gen.blockStateOutput.accept(BlockModelGenerators.createSlab(slab, mv(bottom), mv(top), mv(fullModel)));
    }

    public static void stairsAuto(BlockModelGenerators gen, Block stairs) {
        TextureMapping map = new TextureMapping()
                .put(TextureSlot.SIDE,   texOf(stairs))
                .put(TextureSlot.TOP,    texOf(stairs, "_top"))
                .put(TextureSlot.BOTTOM, texOf(stairs, "_bottom"));

        var straight = ModelTemplates.STAIRS_STRAIGHT.create(stairs, map, gen.modelOutput);
        var inner    = ModelTemplates.STAIRS_INNER.create(  stairs, map, gen.modelOutput);
        var outer    = ModelTemplates.STAIRS_OUTER.create(  stairs, map, gen.modelOutput);

        gen.blockStateOutput.accept(BlockModelGenerators.createStairs(stairs, mv(inner), mv(straight), mv(outer)));
    }

    public static void fenceAuto(BlockModelGenerators gen, Block fence) {
        TextureMapping map = new TextureMapping().put(TextureSlot.TEXTURE, texOf(fence));
        var post = ModelTemplates.FENCE_POST.create(fence, map, gen.modelOutput);
        var side = ModelTemplates.FENCE_SIDE.create(fence, map, gen.modelOutput);
        gen.blockStateOutput.accept(BlockModelGenerators.createFence(fence, mv(post), mv(side)));
    }

    public static void wallAuto(BlockModelGenerators gen, Block wall) {
        TextureMapping map = new TextureMapping()
                .put(TextureSlot.SIDE,   texOf(wall))
                .put(TextureSlot.TOP,    texOf(wall, "_top"))
                .put(TextureSlot.BOTTOM, texOf(wall, "_bottom"));
        var post     = ModelTemplates.WALL_POST.create(wall, map, gen.modelOutput);
        var sideLow  = ModelTemplates.WALL_LOW_SIDE.create(wall, map, gen.modelOutput);
        var sideTall = ModelTemplates.WALL_TALL_SIDE.create(wall, map, gen.modelOutput);
        gen.blockStateOutput.accept(BlockModelGenerators.createWall(wall, mv(post), mv(sideLow), mv(sideTall)));
    }

    public static void paneAuto(BlockModelGenerators gen, Block pane) {
        ResourceLocation paneTex = texOf(pane);
        ResourceLocation edgeTex = texOf(pane, "_top");
        TextureSlot PANE = TextureSlot.create("pane", TextureSlot.ALL);
        TextureSlot EDGE = TextureSlot.create("edge", TextureSlot.ALL);

        var post    = ModelTemplates.CUBE.extend().parent(blockModel("minecraft","template_glass_pane_post"))
                .suffix("_post").requiredTextureSlot(PANE).requiredTextureSlot(EDGE)
                .renderType("minecraft:translucent").build();
        var side    = ModelTemplates.CUBE.extend().parent(blockModel("minecraft","template_glass_pane_side"))
                .suffix("_side").requiredTextureSlot(PANE).requiredTextureSlot(EDGE)
                .renderType("minecraft:translucent").build();
        var sideAlt = ModelTemplates.CUBE.extend().parent(blockModel("minecraft","template_glass_pane_side_alt"))
                .suffix("_side_alt").requiredTextureSlot(PANE).requiredTextureSlot(EDGE)
                .renderType("minecraft:translucent").build();
        var noSide  = ModelTemplates.CUBE.extend().parent(blockModel("minecraft","template_glass_pane_noside"))
                .suffix("_noside").requiredTextureSlot(PANE).requiredTextureSlot(EDGE)
                .renderType("minecraft:translucent").build();
        var noSideAlt = ModelTemplates.CUBE.extend().parent(blockModel("minecraft","template_glass_pane_noside_alt"))
                .suffix("_noside_alt").requiredTextureSlot(PANE).requiredTextureSlot(EDGE)
                .renderType("minecraft:translucent").build();

        TextureMapping tex = new TextureMapping().put(PANE, paneTex).put(EDGE, edgeTex);

        var postM      = post.create(pane, tex, gen.modelOutput);
        var sideM      = side.create(pane, tex, gen.modelOutput);
        var sideAltM   = sideAlt.create(pane, tex, gen.modelOutput);
        var noSideM    = noSide.create(pane, tex, gen.modelOutput);
        var noSideAltM = noSideAlt.create(pane, tex, gen.modelOutput);

        var mp = MultiPartGenerator.multiPart(pane)
                // unconditional post
                .with(BlockModelGenerators.variants(new Variant(postM)))
                // four true connections
                .with(BlockModelGenerators.or(BlockModelGenerators.condition().term(BlockStateProperties.NORTH, true)),
                        BlockModelGenerators.variants(new Variant(sideM)))
                .with(BlockModelGenerators.or(BlockModelGenerators.condition().term(BlockStateProperties.EAST, true)),
                        BlockModelGenerators.variants(new Variant(sideM).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))
                .with(BlockModelGenerators.or(BlockModelGenerators.condition().term(BlockStateProperties.SOUTH, true)),
                        BlockModelGenerators.variants(new Variant(sideAltM)))
                .with(BlockModelGenerators.or(BlockModelGenerators.condition().term(BlockStateProperties.WEST, true)),
                        BlockModelGenerators.variants(new Variant(sideAltM).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))
                // four false (capping pieces)
                .with(BlockModelGenerators.or(BlockModelGenerators.condition().term(BlockStateProperties.NORTH, false)),
                        BlockModelGenerators.variants(new Variant(noSideM)))
                .with(BlockModelGenerators.or(BlockModelGenerators.condition().term(BlockStateProperties.EAST, false)),
                        BlockModelGenerators.variants(new Variant(noSideAltM)))
                .with(BlockModelGenerators.or(BlockModelGenerators.condition().term(BlockStateProperties.SOUTH, false)),
                        BlockModelGenerators.variants(new Variant(noSideAltM).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))
                .with(BlockModelGenerators.or(BlockModelGenerators.condition().term(BlockStateProperties.WEST, false)),
                        BlockModelGenerators.variants(new Variant(noSideM).with(VariantMutator.Y_ROT.withValue(Quadrant.R270))));
        gen.blockStateOutput.accept(mp);
    }

    /* ------------------------ Pearl Fire (vanilla FIRE templates) ------------------------ */
    // BlocksGenComplex.java
    public static void fireAuto(BlockModelGenerators gen, Block fire) {
        // Textures you provide:
        //   assets/<ns>/textures/block/<name>_0.png
        //   assets/<ns>/textures/block/<name>_1.png
        var id = idOf(fire);
        String ns   = id.getNamespace();
        String name = id.getPath();

        // Use the built-in FIRE TextureSlot (important)
        TextureMapping tex0 = new TextureMapping().put(TextureSlot.FIRE, rl(ns, "block/" + name + "_0"));
        TextureMapping tex1 = new TextureMapping().put(TextureSlot.FIRE, rl(ns, "block/" + name + "_1"));

        // Give EVERY template a unique output path to avoid "Duplicate model definition"
        ResourceLocation floor0    = ModelTemplates.FIRE_FLOOR    .create(rl(ns, "block/" + name + "_floor0"),     tex0, gen.modelOutput);
        ResourceLocation floor1    = ModelTemplates.FIRE_FLOOR    .create(rl(ns, "block/" + name + "_floor1"),     tex1, gen.modelOutput);
        ResourceLocation side0     = ModelTemplates.FIRE_SIDE     .create(rl(ns, "block/" + name + "_side0"),      tex0, gen.modelOutput);
        ResourceLocation side1     = ModelTemplates.FIRE_SIDE     .create(rl(ns, "block/" + name + "_side1"),      tex1, gen.modelOutput);
        ResourceLocation sideAlt0  = ModelTemplates.FIRE_SIDE_ALT .create(rl(ns, "block/" + name + "_side_alt0"),  tex0, gen.modelOutput);
        ResourceLocation sideAlt1  = ModelTemplates.FIRE_SIDE_ALT .create(rl(ns, "block/" + name + "_side_alt1"),  tex1, gen.modelOutput);

        // Soul-fire style: unconditional multipart (no EAST/WEST/NORTH/SOUTH/UP properties)
        var mp = MultiPartGenerator.multiPart(fire)
                // Floors
                .with(BlockModelGenerators.variant(new Variant(floor0)))
                .with(BlockModelGenerators.variant(new Variant(floor1)))

                // Sides @ 0°
                .with(BlockModelGenerators.variant(new Variant(side0)))
                .with(BlockModelGenerators.variant(new Variant(side1)))
                .with(BlockModelGenerators.variant(new Variant(sideAlt0)))
                .with(BlockModelGenerators.variant(new Variant(sideAlt1)))

                // Sides @ 90°
                .with(BlockModelGenerators.variant(new Variant(side0).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))
                .with(BlockModelGenerators.variant(new Variant(side1).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt0).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt1).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))

                // Sides @ 180°
                .with(BlockModelGenerators.variant(new Variant(side0).with(VariantMutator.Y_ROT.withValue(Quadrant.R180))))
                .with(BlockModelGenerators.variant(new Variant(side1).with(VariantMutator.Y_ROT.withValue(Quadrant.R180))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt0).with(VariantMutator.Y_ROT.withValue(Quadrant.R180))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt1).with(VariantMutator.Y_ROT.withValue(Quadrant.R180))))

                // Sides @ 270°
                .with(BlockModelGenerators.variant(new Variant(side0).with(VariantMutator.Y_ROT.withValue(Quadrant.R270))))
                .with(BlockModelGenerators.variant(new Variant(side1).with(VariantMutator.Y_ROT.withValue(Quadrant.R270))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt0).with(VariantMutator.Y_ROT.withValue(Quadrant.R270))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt1).with(VariantMutator.Y_ROT.withValue(Quadrant.R270))));

        gen.blockStateOutput.accept(mp);
    }

    public static void fireStatesAuto(BlockModelGenerators gen, Block fire) {
        var id   = idOf(fire);
        String ns   = id.getNamespace();
        String name = id.getPath();

        // Model IDs that must already exist under assets/<ns>/models/block/
        ResourceLocation floor0   = rl(ns, "block/" + name + "_floor0");
        ResourceLocation floor1   = rl(ns, "block/" + name + "_floor1");
        ResourceLocation side0    = rl(ns, "block/" + name + "_side0");
        ResourceLocation side1    = rl(ns, "block/" + name + "_side1");
        ResourceLocation sideAlt0 = rl(ns, "block/" + name + "_side_alt0");
        ResourceLocation sideAlt1 = rl(ns, "block/" + name + "_side_alt1");

        var mp = MultiPartGenerator.multiPart(fire)
                // Floors
                .with(BlockModelGenerators.variant(new Variant(floor0)))
                .with(BlockModelGenerators.variant(new Variant(floor1)))

                // Sides @ 0°
                .with(BlockModelGenerators.variant(new Variant(side0)))
                .with(BlockModelGenerators.variant(new Variant(side1)))
                .with(BlockModelGenerators.variant(new Variant(sideAlt0)))
                .with(BlockModelGenerators.variant(new Variant(sideAlt1)))

                // Sides @ 90°
                .with(BlockModelGenerators.variant(new Variant(side0   ).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))
                .with(BlockModelGenerators.variant(new Variant(side1   ).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt0).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt1).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))

                // Sides @ 180°
                .with(BlockModelGenerators.variant(new Variant(side0   ).with(VariantMutator.Y_ROT.withValue(Quadrant.R180))))
                .with(BlockModelGenerators.variant(new Variant(side1   ).with(VariantMutator.Y_ROT.withValue(Quadrant.R180))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt0).with(VariantMutator.Y_ROT.withValue(Quadrant.R180))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt1).with(VariantMutator.Y_ROT.withValue(Quadrant.R180))))

                // Sides @ 270°
                .with(BlockModelGenerators.variant(new Variant(side0   ).with(VariantMutator.Y_ROT.withValue(Quadrant.R270))))
                .with(BlockModelGenerators.variant(new Variant(side1   ).with(VariantMutator.Y_ROT.withValue(Quadrant.R270))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt0).with(VariantMutator.Y_ROT.withValue(Quadrant.R270))))
                .with(BlockModelGenerators.variant(new Variant(sideAlt1).with(VariantMutator.Y_ROT.withValue(Quadrant.R270))));

        gen.blockStateOutput.accept(mp);
    }



    /* ------------------------ Banana Cow Egg (AGE 0..2) ------------------------ */
    public static void bananaCowEggStates(BlockModelGenerators gen, Block egg) {
        var id   = idOf(egg);
        var ns   = id.getNamespace();
        var base = id.getPath();

        ResourceLocation[] free = {
                rl(ns, "block/" + base + "_stage0"),
                rl(ns, "block/" + base + "_stage1"),
                rl(ns, "block/" + base + "_stage2")
        };
        ResourceLocation[] stem = {
                rl(ns, "block/" + base + "_stem_stage0"),
                rl(ns, "block/" + base + "_stem_stage1"),
                rl(ns, "block/" + base + "_stem_stage2")
        };

        gen.blockStateOutput.accept(
                MultiVariantGenerator
                        .dispatch(egg, mv(free[0])) // a harmless default
                        .with(PropertyDispatch.modify(BananaCowEggBlock.ATTACHED, BananaCowEggBlock.AGE)
                                // attached = false
                                .select(false, 0, VariantMutator.MODEL.withValue(free[0]))
                                .select(false, 1, VariantMutator.MODEL.withValue(free[1]))
                                .select(false, 2, VariantMutator.MODEL.withValue(free[2]))
                                // attached = true
                                .select(true,  0, VariantMutator.MODEL.withValue(stem[0]))
                                .select(true,  1, VariantMutator.MODEL.withValue(stem[1]))
                                .select(true,  2, VariantMutator.MODEL.withValue(stem[2]))
                        )
        );
    }

    /* ------------------------ Musavacca Crop (AGE 0..3) ------------------------ */
    public static void musavaccaPlantStates(BlockModelGenerators gen, Block crop) {
        String ns = idOf(crop).getNamespace();

        ResourceLocation m0 = rl(ns, "block/musavacca_plant_stage0");
        ResourceLocation m1 = rl(ns, "block/musavacca_plant_stage1");
        ResourceLocation m2 = rl(ns, "block/musavacca_plant_stage2");
        ResourceLocation m3 = rl(ns, "block/musavacca_plant_stage2");

        var state = MultiVariantGenerator.dispatch(crop, mv(m0))
                .with(PropertyDispatch.modify(MusavaccaPlantCropBlock.AGE)
                        .select(0, VariantMutator.MODEL.withValue(m0))
                        .select(1, VariantMutator.MODEL.withValue(m1))
                        .select(2, VariantMutator.MODEL.withValue(m2))
                        .select(3, VariantMutator.MODEL.withValue(m3)));
        gen.blockStateOutput.accept(state);
    }


    public static void amethystLikeClusterAuto(BlockModelGenerators gen,
                                               Block small, Block medium, Block large, Block cluster) {
        // ---- Models (vanilla buds/clusters use a single CROSS texture)
        ResourceLocation mSmall   = ModelTemplates.CROSS.create(
                small,  new TextureMapping().put(TextureSlot.CROSS, texOf(small)),  gen.modelOutput);
        ResourceLocation mMedium  = ModelTemplates.CROSS.create(
                medium, new TextureMapping().put(TextureSlot.CROSS, texOf(medium)), gen.modelOutput);
        ResourceLocation mLarge   = ModelTemplates.CROSS.create(
                large,  new TextureMapping().put(TextureSlot.CROSS, texOf(large)),  gen.modelOutput);
        ResourceLocation mCluster = ModelTemplates.CROSS.create(
                cluster,new TextureMapping().put(TextureSlot.CROSS, texOf(cluster)),gen.modelOutput);

        // ---- Blockstates (FACING -> rotations), identical mapping for all four pieces
        gen.blockStateOutput.accept(facingRotationsLikeVanillaAmethyst(small,   mSmall));
        gen.blockStateOutput.accept(facingRotationsLikeVanillaAmethyst(medium,  mMedium));
        gen.blockStateOutput.accept(facingRotationsLikeVanillaAmethyst(large,   mLarge));
        gen.blockStateOutput.accept(facingRotationsLikeVanillaAmethyst(cluster, mCluster));
    }

    /** Matches vanilla amethyst buds/clusters FACING rotations (UP/DOWN/N/E/S/W). */
    private static MultiVariantGenerator facingRotationsLikeVanillaAmethyst(Block block, ResourceLocation model) {
        Variant base = new Variant(model); // base model to mutate

        return MultiVariantGenerator
                // pass the base variant wrapper here
                .dispatch(block, BlockModelGenerators.variant(base))
                // important: in 1.21.8 you must use 'modify(...)' and pass VariantMutators to select(...)
                .with(PropertyDispatch.modify(BlockStateProperties.FACING)
                        // UP: no rotation
                        .select(Direction.UP, BlockModelGenerators.NOP)

                        // DOWN: x = 180  (90 + 90)
                        .select(Direction.DOWN,
                                BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.X_ROT_90))

                        // NORTH: x = 90
                        .select(Direction.NORTH, BlockModelGenerators.X_ROT_90)

                        // SOUTH: x = 90, y = 180  (y: 90 + 90)
                        .select(Direction.SOUTH,
                                BlockModelGenerators.X_ROT_90
                                        .then(BlockModelGenerators.Y_ROT_90)
                                        .then(BlockModelGenerators.Y_ROT_90))

                        // EAST: x = 90, y = 90
                        .select(Direction.EAST,
                                BlockModelGenerators.X_ROT_90
                                        .then(BlockModelGenerators.Y_ROT_90))

                        // WEST: x = 90, y = 270  (y: 90 + 90 + 90)
                        .select(Direction.WEST,
                                BlockModelGenerators.X_ROT_90
                                        .then(BlockModelGenerators.Y_ROT_90)
                                        .then(BlockModelGenerators.Y_ROT_90)
                                        .then(BlockModelGenerators.Y_ROT_90))
                );
    }
}

