package net.anatomyworld.harambefmod.block;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.custom.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.BlockItem;

import java.util.Set;

/** All custom blocks for the mod + auto BlockItems (1.21.8-safe). */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HarambeCore.MOD_ID);
    public static final DeferredRegister.Items  ITEMS  = DeferredRegister.createItems(HarambeCore.MOD_ID);

    /* -------------------- Blocks (use registerBlock!) -------------------- */

    public static final DeferredBlock<Block> BANANA_PEARL_BLOCK =
            BLOCKS.registerBlock("banana_pearl_block",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(1.5F, 6.0F)
                            .sound(SoundType.AMETHYST)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.NORMAL)));

    public static final DeferredBlock<Block> MUSAVACCA_PLANKS =
            BLOCKS.registerBlock("musavacca_planks",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_BROWN)
                            .strength(1.5F, 6.0F)
                            .sound(SoundType.BAMBOO_WOOD)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.NORMAL)));

    public static final DeferredBlock<UntintedParticleLeavesBlock> MUSAVACCA_LEAVES =
            BLOCKS.registerBlock("musavacca_leaves",
                    props -> new UntintedParticleLeavesBlock(
                            0.0F, ParticleTypes.ASH,
                            props.mapColor(MapColor.PLANT)
                                    .strength(0.2F)
                                    .randomTicks()
                                    .sound(SoundType.GRASS)
                                    .noOcclusion()
                                    .isSuffocating((s, l, p) -> false)
                                    .isViewBlocking((s, l, p) -> false)
                                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<UntintedParticleLeavesBlock> MUSAVACCA_LEAVES_CROWN =
            BLOCKS.registerBlock("musavacca_leaves_crown",
                    props -> new UntintedParticleLeavesBlock(
                            0.0F, ParticleTypes.ASH,
                            props.mapColor(MapColor.PLANT)
                                    .strength(0.2F)
                                    .randomTicks()
                                    .sound(SoundType.GRASS)
                                    .noOcclusion()
                                    .isSuffocating((s, l, p) -> false)
                                    .isViewBlocking((s, l, p) -> false)
                                    .pushReaction(PushReaction.DESTROY)));

    /** Custom fire block with BE tint. */
    public static final DeferredBlock<PearlFireBlock> PEARL_FIRE =
            BLOCKS.registerBlock("pearl_fire",
                    props -> new PearlFireBlock(props
                            .noCollission()
                            .noOcclusion()
                            .replaceable()
                            .instabreak()
                            .dynamicShape()
                            .randomTicks()
                            .pushReaction(PushReaction.DESTROY)
                            .noLootTable()
                            .sound(SoundType.WOOL)
                            .lightLevel(s -> 15)));

    public static final DeferredBlock<BananaCowEggBlock> BANANA_COW_EGG =
            BLOCKS.registerBlock("banana_cow_egg",
                    props -> new BananaCowEggBlock(props
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.3F)
                            .sound(SoundType.HONEY_BLOCK)
                            .noOcclusion()
                            .randomTicks()));

    public static final DeferredBlock<MusavaccaFlowerBlock> MUSAVACCA_FLOWER =
            BLOCKS.registerBlock("musavacca_flower",
                    props -> new MusavaccaFlowerBlock(props
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.1F)
                            .sound(SoundType.CROP)
                            .noOcclusion()
                            .noCollission()
                            .randomTicks()));

    public static final DeferredBlock<BananaPortalBlock> BANANA_PORTAL =
            BLOCKS.registerBlock("banana_portal",
                    props -> new BananaPortalBlock(props
                            .noOcclusion()
                            .noCollission()
                            .strength(-1.0F, 3_600_000.0F)
                            .lightLevel(s -> 11)
                            .sound(SoundType.GLASS)
                            .noLootTable()));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_MUSAVACCA_STEM =
            BLOCKS.registerBlock("stripped_musavacca_stem",
                    props -> new RotatedPillarBlock(props
                            .strength(2.0F)
                            .sound(SoundType.STEM)));

    public static final DeferredBlock<RotatedPillarBlock> MUSAVACCA_STEM =
            BLOCKS.registerBlock("musavacca_stem",
                    props -> new StrippablePillarBlock(
                            props.strength(2.0F).sound(SoundType.STEM),
                            STRIPPED_MUSAVACCA_STEM));

    public static final DeferredBlock<MusavaccaPlantCropBlock> MUSAVACCA_PLANT =
            BLOCKS.registerBlock("musavacca_plant",
                    props -> new MusavaccaPlantCropBlock(props
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .instabreak()
                            .randomTicks()
                            .sound(SoundType.CROP)
                            .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<MusavaccaPlantSaplingBlock> MUSAVACCA_SAPLING =
            BLOCKS.registerBlock("musavacca_sapling",
                    props -> new MusavaccaPlantSaplingBlock(props
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .instabreak()
                            .randomTicks()
                            .sound(SoundType.GRASS)));

    /* -------------------- Auto BlockItems -------------------- */

    private static final Set<DeferredHolder<Block, ? extends Block>> SKIP_BLOCK_ITEMS = Set.of(
            BANANA_COW_EGG,   // stage items instead
            PEARL_FIRE,       // fire-like blocks usually don't have items
            BANANA_PORTAL,
            MUSAVACCA_PLANT
    );

    static {
        BLOCKS.getEntries().forEach(entry -> {
            if (!SKIP_BLOCK_ITEMS.contains(entry)) {
                // uses helper so Item.Properties has its id set
                DeferredItem<BlockItem> ignored = ITEMS.registerSimpleBlockItem(entry);
            }
        });
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    private ModBlocks() {}
}
