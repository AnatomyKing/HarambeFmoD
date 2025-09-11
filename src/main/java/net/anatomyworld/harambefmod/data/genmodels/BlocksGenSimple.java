package net.anatomyworld.harambefmod.data.genmodels;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import static net.anatomyworld.harambefmod.data.genmodels.ModelUtil.*;

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
}
