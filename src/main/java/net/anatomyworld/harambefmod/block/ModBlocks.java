package net.anatomyworld.harambefmod.block;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.custom.*;
import net.anatomyworld.harambefmod.block.custom.CaroteneGrassBlock;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
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


    public static final DeferredBlock<AnyChestBlock> ANYTOMITHIUM_CHEST =
            BLOCKS.registerBlock("anytomoithium_chest",
                    props -> new AnyChestBlock(
                            props.mapColor(MapColor.COLOR_PURPLE)
                                    .strength(2.5F)
                                    .sound(SoundType.NETHER_ORE)
                                    .noOcclusion()
                                    .pushReaction(PushReaction.NORMAL)
                    ));


    /* -------------------- Blocks (use registerBlock!) -------------------- */

    public static final DeferredBlock<Block> BANANA_PEARL_BLOCK =
            BLOCKS.registerBlock("banana_pearl_block",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(1.5F, 6.0F)
                            .sound(SoundType.AMETHYST)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.NORMAL)));

    public static final DeferredBlock<Block> PEARLIDIAN =
            BLOCKS.registerBlock("pearlidian",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(50.0F, 1200.0F)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.IGNORE)
                    ));

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

    public static final DeferredBlock<Block> BANANA_CREAM_STONE =
            BLOCKS.registerBlock("banana_cream_stone",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.6F)
                            .sound(SoundType.HONEY_BLOCK)
                            .pushReaction(PushReaction.NORMAL)));

    public static final DeferredBlock<Block> CHOCO_CREAM_STONE =
            BLOCKS.registerBlock("choco_cream_stone",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.6F)
                            .sound(SoundType.HONEY_BLOCK)
                            .pushReaction(PushReaction.NORMAL)));

    public static final DeferredBlock<Block> VANILLA_CREAM_STONE =
            BLOCKS.registerBlock("vanilla_cream_stone",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.6F)
                            .sound(SoundType.HONEY_BLOCK)
                            .pushReaction(PushReaction.NORMAL)));

    public static final DeferredBlock<Block> STRAWBERRY_CREAM_STONE =
            BLOCKS.registerBlock("strawberry_cream_stone",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.6F)
                            .sound(SoundType.HONEY_BLOCK)
                            .pushReaction(PushReaction.NORMAL)));

    public static final DeferredBlock<Block> BANANA_BLOCK =
            BLOCKS.registerBlock("banana_block",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(1.0F)
                            .sound(SoundType.SHROOMLIGHT)
                            .pushReaction(PushReaction.NORMAL)));

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

    public static final DeferredBlock<CaroteneGrassBlock> CAROTENE_GRASS_BLOCK =
            BLOCKS.registerBlock("carotene_grass_block",
                    props -> new CaroteneGrassBlock(
                            props.mapColor(MapColor.GRASS)
                                    .strength(0.6F)
                                    .sound(SoundType.GRASS)
                                    .randomTicks() // optional: matches many ground blocks
                                    .pushReaction(PushReaction.NORMAL)
                    ));




    public static final net.neoforged.neoforge.registries.DeferredBlock<HoneyCrystalBlock> HONEY_CRYSTAL_BLOCK =
            BLOCKS.registerBlock("honey_crystal_block",
                    p -> new HoneyCrystalBlock(p
                            .strength(1.5F, 1.5F)
                            .sound(net.minecraft.world.level.block.SoundType.AMETHYST)));

    public static final net.neoforged.neoforge.registries.DeferredBlock<HoneyCrystalClusterLikeBlock> SMALL_HONEY_CRYSTAL_BUD =
            BLOCKS.registerBlock("small_honey_crystal_bud",
                    p -> new HoneyCrystalClusterLikeBlock(3, 4, p
                            .strength(1.0F)
                            .sound(net.minecraft.world.level.block.SoundType.AMETHYST)
                            .noOcclusion()));

    public static final net.neoforged.neoforge.registries.DeferredBlock<HoneyCrystalClusterLikeBlock> MEDIUM_HONEY_CRYSTAL_BUD =
            BLOCKS.registerBlock("medium_honey_crystal_bud",
                    p -> new HoneyCrystalClusterLikeBlock(4, 4, p
                            .strength(1.0F)
                            .sound(net.minecraft.world.level.block.SoundType.AMETHYST)
                            .noOcclusion()));

    public static final net.neoforged.neoforge.registries.DeferredBlock<HoneyCrystalClusterLikeBlock> LARGE_HONEY_CRYSTAL_BUD =
            BLOCKS.registerBlock("large_honey_crystal_bud",
                    p -> new HoneyCrystalClusterLikeBlock(5, 4, p
                            .strength(1.0F)
                            .sound(net.minecraft.world.level.block.SoundType.AMETHYST)
                            .noOcclusion()));

    public static final net.neoforged.neoforge.registries.DeferredBlock<HoneyCrystalClusterLikeBlock> HONEY_CRYSTAL_CLUSTER =
            BLOCKS.registerBlock("honey_crystal_cluster",
                    p -> new HoneyCrystalClusterLikeBlock(7, 3, p
                            .strength(1.0F)
                            .sound(net.minecraft.world.level.block.SoundType.AMETHYST)
                            .noOcclusion()));
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
