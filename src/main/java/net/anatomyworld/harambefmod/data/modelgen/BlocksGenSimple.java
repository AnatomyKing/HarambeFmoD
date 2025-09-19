package net.anatomyworld.harambefmod.data.modelgen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import static net.anatomyworld.harambefmod.data.modelgen.ModelUtil.*;

public final class BlocksGenSimple {
    private BlocksGenSimple() {}

    /** cube_all (ao aan, standaard) */
    public static void cubeAll(BlockModelGenerators gen, Block... blocks) {
        for (Block b : blocks) gen.createTrivialCube(b);
    }

    /** leaves (cutout_mipped, AO uit volgens template) */
    public static void leaves(BlockModelGenerators gen, Block... blocks) {
        for (Block b : blocks) {
            TextureMapping map = new TextureMapping().put(TextureSlot.ALL, texOf(b));
            ResourceLocation model = ModelTemplates.LEAVES.create(b, map, gen.modelOutput);
            gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(b, mv(model)));
        }
    }

    /** cross (planten/doorzichtige sprites) */
    public static void cross(BlockModelGenerators gen, Block... blocks) {
        for (Block b : blocks) {
            TextureMapping map = new TextureMapping().put(TextureSlot.CROSS, texOf(b));
            ResourceLocation model = ModelTemplates.CROSS.create(b, map, gen.modelOutput);
            gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(b, mv(model)));
        }
    }

    /** vertical + horizontal pillar (zoals logs/stems) */
    public static void pillarAuto(BlockModelGenerators gen, Block... blocks) {
        for (Block b : blocks) {
            gen.createRotatedPillarWithHorizontalVariant(
                    b, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT
            );
        }
    }

    public static void pillarAutoOverride(BlockModelGenerators gen,
                                          Block block,
                                          ResourceLocation end,
                                          ResourceLocation side) {
        // vertical version
        TexturedModel.Provider vertical = TexturedModel.createDefault(
                b -> new TextureMapping()
                        .put(TextureSlot.SIDE, side)
                        .put(TextureSlot.END, end),
                ModelTemplates.CUBE_COLUMN
        );

        // horizontal version
        TexturedModel.Provider horizontal = TexturedModel.createDefault(
                b -> new TextureMapping()
                        .put(TextureSlot.SIDE, side)
                        .put(TextureSlot.END, end),
                ModelTemplates.CUBE_COLUMN_HORIZONTAL
        );

        gen.createRotatedPillarWithHorizontalVariant(block, vertical, horizontal);
    }

    /** furnace-like: side/front/top */
    public static void orientable(BlockModelGenerators gen, Block block,
                                  ResourceLocation side, ResourceLocation front, ResourceLocation top) {
        var provider = TexturedModel.ORIENTABLE_ONLY_TOP.updateTexture(map -> {
            map.put(TextureSlot.SIDE, side);
            map.put(TextureSlot.FRONT, front);
            map.put(TextureSlot.TOP, top);
        });
        gen.createHorizontallyRotatedBlock(block, provider);
    }

    /** Simple one-variant blockstate: { "variants": { "": { "model": "<block model id>" } } } */
    public static void simpleState(BlockModelGenerators gen, Block block, String blockModelId) {
        ResourceLocation model = ResourceLocation.parse(blockModelId); // e.g. "harambefmod:block/musavacca_leaves_crown"
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, mv(model)));
    }

    public static void barrelAutoTrivial(BlockModelGenerators gen, Block... blocks) {
        TexturedModel.Provider provider = TexturedModel.createDefault(
                b -> new TextureMapping()
                        .put(TextureSlot.SIDE,   TextureMapping.getBlockTexture(b))
                        .put(TextureSlot.TOP,    TextureMapping.getBlockTexture(b, "_top"))
                        .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(b, "_bottom")),
                ModelTemplates.CUBE_BOTTOM_TOP
        );
        for (Block b : blocks) gen.createTrivialBlock(b, provider);
    }

    /** Full override: provide side/top/bottom explicitly. */
    public static void barrelAutoTrivialOverride(BlockModelGenerators gen,
                                                 Block block,
                                                 ResourceLocation side,
                                                 ResourceLocation top,
                                                 ResourceLocation bottom) {
        TexturedModel.Provider provider = TexturedModel.createDefault(
                b -> new TextureMapping()
                        .put(TextureSlot.SIDE,   side)
                        .put(TextureSlot.TOP,    top)
                        .put(TextureSlot.BOTTOM, bottom),
                ModelTemplates.CUBE_BOTTOM_TOP
        );
        gen.createTrivialBlock(block, provider);
    }

    /** Convenience: override side & bottom, keep top = <block>_top. */
    public static void barrelAutoTrivialOverride(BlockModelGenerators gen,
                                                 Block block,
                                                 ResourceLocation side,
                                                 ResourceLocation bottom) {
        barrelAutoTrivialOverride(gen, block, side, texOf(block, "_top"), bottom);
    }

    /** Convenience: override ONLY bottom, keep side = <block>, top = <block>_top. */
    public static void barrelAutoTrivialOverride(BlockModelGenerators gen,
                                                 Block block,
                                                 ResourceLocation bottom) {
        barrelAutoTrivialOverride(gen, block, texOf(block), texOf(block, "_top"), bottom);
    }


    public static ResourceLocation texOf(Block b) { return texOf(b, ""); }
    public static ResourceLocation texOf(Block b, String suffix) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(b);
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath() + suffix);
    }

    public static void pillarAutoMatchEnd(BlockModelGenerators gen, Block... blocks) {
        for (Block b : blocks) {
            TexturedModel.Provider provider = TexturedModel.createDefault(
                    block -> new TextureMapping()
                            .put(TextureSlot.SIDE, texOf(block))
                            .put(TextureSlot.END, texOf(block)), // ends use the same texture as side
                    ModelTemplates.CUBE_COLUMN
            );
            gen.createTrivialBlock(b, provider);
        }
    }

}
