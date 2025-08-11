package net.anatomyworld.harambefmod.block;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.custom.BananaCowEggBlock;
import net.anatomyworld.harambefmod.block.custom.MusavaccaFlowerBlock;
import net.anatomyworld.harambefmod.block.custom.PearlFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All custom blocks for the mod. */
public final class ModBlocks {

    /** Global block registry for this mod. */
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(HarambeCore.MOD_ID);

    /** Banana Pearl Block – behaves exactly like an amethyst block. */
    public static final DeferredBlock<Block> BANANA_PEARL_BLOCK =
            BLOCKS.register("banana_pearl_block",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)  // map/beacon color
                            .strength(1.5F, 6.0F)            // hardness & blast-resistance
                            .sound(SoundType.AMETHYST)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.NORMAL)));


    /** Custom fire block that can have a random tint when ignited. */
    public static final DeferredBlock<PearlFireBlock> PEARL_FIRE =
            BLOCKS.register("pearl_fire", PearlFireBlock::new);


    public static final DeferredBlock<BananaCowEggBlock> BANANA_COW_EGG =
            BLOCKS.register("banana_cow_egg",
                    () -> new BananaCowEggBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.3F)
                            .sound(SoundType.HONEY_BLOCK)
                            .noOcclusion()
                            .noCollission()
                            .randomTicks()));

    public static final DeferredBlock<MusavaccaFlowerBlock> MUSAVACCA_FLOWER =
            BLOCKS.register("musavacca_flower",
                    () -> new MusavaccaFlowerBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.1F)
                            .sound(SoundType.CROP)
                            .noOcclusion()
                            .noCollission()
                            .randomTicks()));


    /** Called from {@link net.anatomyworld.harambefmod.HarambeCore} to register blocks. */
    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    private ModBlocks() {}
}
