package net.anatomyworld.harambefmod.block;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.custom.BananaCowEggBlock;
import net.anatomyworld.harambefmod.block.custom.MusavaccaFlowerBlock;
import net.anatomyworld.harambefmod.block.custom.PearlFireBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

/** All custom blocks for the mod + auto BlockItems. */
public final class ModBlocks {

    /** Global block registry for this mod. */
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(HarambeCore.MOD_ID);

    /** Items registry here too (so BlockItems can live with their blocks). */
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HarambeCore.MOD_ID);

    /* -------------------- Blocks -------------------- */

    /** Banana Pearl Block – behaves like an amethyst block. */
    public static final DeferredBlock<Block> BANANA_PEARL_BLOCK =
            BLOCKS.register("banana_pearl_block",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(1.5F, 6.0F)
                            .sound(SoundType.AMETHYST)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.NORMAL)));

    /** Custom fire block that can have a random tint when ignited. */
    public static final DeferredBlock<PearlFireBlock> PEARL_FIRE =
            BLOCKS.register("pearl_fire", PearlFireBlock::new);

    /** Banana Cow Egg (age + attached). */
    public static final DeferredBlock<BananaCowEggBlock> BANANA_COW_EGG =
            BLOCKS.register("banana_cow_egg",
                    () -> new BananaCowEggBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.3F)
                            .sound(SoundType.HONEY_BLOCK)
                            .noOcclusion()
                            .noCollission()
                            .randomTicks()));

    /** Musavacca Flower (ceiling plant). */
    public static final DeferredBlock<MusavaccaFlowerBlock> MUSAVACCA_FLOWER =
            BLOCKS.register("musavacca_flower",
                    () -> new MusavaccaFlowerBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.1F)
                            .sound(SoundType.CROP)
                            .noOcclusion()
                            .noCollission()
                            .randomTicks()));

    /* -------------------- Auto BlockItems -------------------- */

    // Blocks that should NOT get a default BlockItem (custom logic or none).
    private static final Set<DeferredHolder<Block, ? extends Block>> SKIP_BLOCK_ITEMS = Set.of(
            BANANA_COW_EGG,   // uses three stage items instead
            PEARL_FIRE        // fire-like blocks usually don't have items
    );

    // If you want specific Item.Properties for *all* BlockItems, prepare them here:
    // private static final Item.Properties BLOCK_ITEM_PROPS = new Item.Properties();

    /** Auto-create BlockItems for every block except those in SKIP_BLOCK_ITEMS. */
    static {
        BLOCKS.getEntries().forEach(entry -> {
            if (!SKIP_BLOCK_ITEMS.contains(entry)) {
                // Overload auto-uses the block's registry name for the item id.
                // Use the 2-arg overload to pass custom properties if you need to.
                DeferredItem<BlockItem> ignored = ITEMS.registerSimpleBlockItem(entry);
                // or: ITEMS.registerSimpleBlockItem(entry, BLOCK_ITEM_PROPS);
            }
        });
    }

    /** Register both BLOCKS and the BlockItems we created above. */
    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    private ModBlocks() {}
}
