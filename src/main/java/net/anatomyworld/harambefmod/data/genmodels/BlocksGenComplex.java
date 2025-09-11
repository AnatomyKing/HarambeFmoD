package net.anatomyworld.harambefmod.data.genmodels;

import com.mojang.math.Quadrant;
import net.anatomyworld.harambefmod.block.custom.BananaCowEggBlock;
import net.anatomyworld.harambefmod.block.custom.BananaPortalBlock;
import net.anatomyworld.harambefmod.block.custom.MusavaccaPlantCropBlock;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static net.anatomyworld.harambefmod.data.genmodels.ModelUtil.*;

/** Complex helpers + your specific states (1.21.8-safe). */
public final class BlocksGenComplex {

    /* ------------------------ Portal axis (nether-like) ------------------------ */
    // BlocksGenComplex.java
    public static void portalAxis(BlockModelGenerators gen, Block portalLike) {
        var ds = portalLike.defaultBlockState();

        ResourceLocation xModel = blockModel("minecraft", "nether_portal_ns");
        ResourceLocation zModel = blockModel("minecraft", "nether_portal_ew");

        // Use the exact property the block defines (BananaPortalBlock.AXIS == HORIZONTAL_AXIS)
        if (ds.hasProperty(BananaPortalBlock.AXIS)) {
            gen.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(portalLike, mv(xModel))
                            .with(PropertyDispatch.modify(BananaPortalBlock.AXIS)
                                    .select(Direction.Axis.X, VariantMutator.MODEL.withValue(xModel))
                                    .select(Direction.Axis.Z, VariantMutator.MODEL.withValue(zModel))
                            )
            );
        } else {
            // Fallback: no axis on the block, single model
            gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(portalLike, mv(xModel)));
        }
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
    public static void pearlFire(BlockModelGenerators gen, Block fire) {
        String ns = idOf(fire).getNamespace();

        // Textures you provide in assets/<ns>/textures/block/:
        //   <id>_0.png  and  <id>_1.png
        ResourceLocation tex0 = rl(ns, "block/" + idOf(fire).getPath() + "_0");
        ResourceLocation tex1 = rl(ns, "block/" + idOf(fire).getPath() + "_1");

        // IMPORTANT: use the built-in slot instance required by FIRE_* templates.
        TextureMapping m0 = new TextureMapping().put(TextureSlot.FIRE, tex0);
        TextureMapping m1 = new TextureMapping().put(TextureSlot.FIRE, tex1);

        ResourceLocation floor    = ModelTemplates.FIRE_FLOOR.create(fire, m0, gen.modelOutput);
        ResourceLocation floorAlt = ModelTemplates.FIRE_FLOOR.create(
                rl(ns, "block/" + idOf(fire).getPath() + "_floor_alt"), m1, gen.modelOutput);
        ResourceLocation side     = ModelTemplates.FIRE_SIDE.create(fire, m0, gen.modelOutput);
        ResourceLocation sideAlt  = ModelTemplates.FIRE_SIDE_ALT.create(fire, m1, gen.modelOutput);
        ResourceLocation up       = ModelTemplates.FIRE_UP.create(fire, m0, gen.modelOutput);
        ResourceLocation upAlt    = ModelTemplates.FIRE_UP_ALT.create(fire, m1, gen.modelOutput);

        var base = BlockModelGenerators.condition()
                .term(BlockStateProperties.EAST,  false)
                .term(BlockStateProperties.WEST,  false)
                .term(BlockStateProperties.NORTH, false)
                .term(BlockStateProperties.SOUTH, false)
                .term(BlockStateProperties.UP,    false);

        var mp = MultiPartGenerator.multiPart(fire)
                // floor: two alternatives
                .with(base, BlockModelGenerators.variants(new Variant(floor), new Variant(floorAlt)))
                // sides
                .with(BlockModelGenerators.condition().term(BlockStateProperties.NORTH, true),
                        BlockModelGenerators.variant(new Variant(side)))
                .with(BlockModelGenerators.condition().term(BlockStateProperties.EAST, true),
                        BlockModelGenerators.variant(new Variant(side).with(VariantMutator.Y_ROT.withValue(Quadrant.R90))))
                .with(BlockModelGenerators.condition().term(BlockStateProperties.SOUTH, true),
                        BlockModelGenerators.variant(new Variant(sideAlt).with(VariantMutator.Y_ROT.withValue(Quadrant.R180))))
                .with(BlockModelGenerators.condition().term(BlockStateProperties.WEST, true),
                        BlockModelGenerators.variant(new Variant(sideAlt).with(VariantMutator.Y_ROT.withValue(Quadrant.R270))))
                // top: two alternatives
                .with(BlockModelGenerators.condition().term(BlockStateProperties.UP, true),
                        BlockModelGenerators.variants(new Variant(up), new Variant(upAlt)));

        gen.blockStateOutput.accept(mp);
    }

    /* ------------------------ Banana Cow Egg (AGE 0..2) ------------------------ */
    public static void bananaCowEggStates(BlockModelGenerators gen, Block egg) {
        var id = idOf(egg);
        var model0 = rl(id.getNamespace(), "block/" + id.getPath() + "_0");
        var model1 = rl(id.getNamespace(), "block/" + id.getPath() + "_1");
        var model2 = rl(id.getNamespace(), "block/" + id.getPath() + "_2");

        var state = MultiVariantGenerator.dispatch(egg, mv(model0))
                .with(PropertyDispatch.modify(BananaCowEggBlock.AGE)
                        .select(0, VariantMutator.MODEL.withValue(model0))
                        .select(1, VariantMutator.MODEL.withValue(model1))
                        .select(2, VariantMutator.MODEL.withValue(model2)));
        gen.blockStateOutput.accept(state);
    }

    /* ------------------------ Musavacca Crop (AGE 0..3) ------------------------ */
    public static void musavaccaCrop(BlockModelGenerators gen, Block crop, Block sapling) {
        String ns = idOf(crop).getNamespace();
        var t0 = rl(ns, "block/musavacca_plant_stage0");
        var t1 = rl(ns, "block/musavacca_plant_stage1");
        var t2 = rl(ns, "block/musavacca_plant_stage2");

        var m0 = ModelTemplates.CROSS.create(rl(ns,"block/musavacca_plant_stage0"),
                new TextureMapping().put(TextureSlot.CROSS, t0), gen.modelOutput);
        var m1 = ModelTemplates.CROSS.create(rl(ns,"block/musavacca_plant_stage1"),
                new TextureMapping().put(TextureSlot.CROSS, t1), gen.modelOutput);
        var m2 = ModelTemplates.CROSS.create(rl(ns,"block/musavacca_plant_stage2"),
                new TextureMapping().put(TextureSlot.CROSS, t2), gen.modelOutput);

        // AGE=3 -> sapling block model ("block/<sapling_id>")
        var saplingId = idOf(sapling);
        var saplingModel = rl(saplingId.getNamespace(), "block/" + saplingId.getPath());

        var state = MultiVariantGenerator.dispatch(crop, mv(m0))
                .with(PropertyDispatch.modify(MusavaccaPlantCropBlock.AGE)
                        .select(0, VariantMutator.MODEL.withValue(m0))
                        .select(1, VariantMutator.MODEL.withValue(m1))
                        .select(2, VariantMutator.MODEL.withValue(m2))
                        .select(3, VariantMutator.MODEL.withValue(saplingModel)));
        gen.blockStateOutput.accept(state);
    }
}
