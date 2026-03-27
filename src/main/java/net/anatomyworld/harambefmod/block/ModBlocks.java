package net.anatomyworld.harambefmod.block;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.custom.*;
import net.anatomyworld.harambefmod.block.custom.CaroteneGrassBlock;
import net.anatomyworld.harambefmod.block.custom.NyliumGrassBlock;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.*;
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




    public static final DeferredBlock<net.anatomyworld.harambefmod.block.custom.WallBannerBlock> BIG_BELMONT_BANNER =
            BLOCKS.registerBlock("big_belmont_banner",
                    p -> new net.anatomyworld.harambefmod.block.custom.WallBannerBlock(
                            p.noOcclusion().strength(0.5F).sound(SoundType.WOOD)));

    public static final DeferredBlock<net.anatomyworld.harambefmod.block.custom.WallBannerBlock> BIG_DYNASTY_BANNER =
            BLOCKS.registerBlock("big_dynasty_banner",
                    p -> new net.anatomyworld.harambefmod.block.custom.WallBannerBlock(
                            p.noOcclusion().strength(0.5F).sound(SoundType.WOOD)));

    public static final DeferredBlock<net.anatomyworld.harambefmod.block.custom.WallBannerBlock> BIG_IMPERIUM_BANNER =
            BLOCKS.registerBlock("big_imperium_banner",
                    p -> new net.anatomyworld.harambefmod.block.custom.WallBannerBlock(
                            p.noOcclusion().strength(0.5F).sound(SoundType.WOOD)));

    public static final DeferredBlock<net.anatomyworld.harambefmod.block.custom.WallBannerBlock> BIG_MISCHIEF_BANNER =
            BLOCKS.registerBlock("big_mischief_banner",
                    p -> new net.anatomyworld.harambefmod.block.custom.WallBannerBlock(
                            p.noOcclusion().strength(0.5F).sound(SoundType.WOOD)));

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

    public static final DeferredBlock<Block> BELMONT_PLANKS =
            BLOCKS.registerBlock("belmont_planks",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_LIGHT_GREEN)
                            .strength(1.5F, 6.0F)
                            .sound(SoundType.WOOD)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.NORMAL)));

    public static final DeferredBlock<Block> DYNASTY_PLANKS =
            BLOCKS.registerBlock("dynasty_planks",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_RED)
                            .strength(1.5F, 6.0F)
                            .sound(SoundType.WOOD)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.NORMAL)));

    public static final DeferredBlock<Block> IMPERIUM_PLANKS =
            BLOCKS.registerBlock("imperium_planks",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(1.5F, 6.0F)
                            .sound(SoundType.WOOD)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.NORMAL)));

    public static final DeferredBlock<Block> MISCHIEF_PLANKS =
            BLOCKS.registerBlock("mischief_planks",
                    props -> new Block(props
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(1.5F, 6.0F)
                            .sound(SoundType.WOOD)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.NORMAL)));


    public static final DeferredBlock<TintedParticleLeavesBlock> MUSAVACCA_LEAVES =
            BLOCKS.registerBlock("musavacca_leaves",
                    props -> new TintedParticleLeavesBlock(
                            0.0F,
                            props.mapColor(MapColor.PLANT)
                                    .strength(0.2F)
                                    .randomTicks()
                                    .sound(SoundType.GRASS)
                                    .noOcclusion()
                                    .isSuffocating((s, l, p) -> false)
                                    .isViewBlocking((s, l, p) -> false)
                                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<UntintedParticleLeavesBlock> BELMONT_LEAVES =
            BLOCKS.registerBlock("belmont_leaves",
                    props -> new UntintedParticleLeavesBlock(
                            0.08F,
                            ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0x4DEE20),
                            props.mapColor(MapColor.PLANT)
                                    .strength(0.2F)
                                    .randomTicks()
                                    .sound(SoundType.GRASS)
                                    .noOcclusion()
                                    .isSuffocating((s,l,p) -> false)
                                    .isViewBlocking((s,l,p) -> false)
                                    .pushReaction(PushReaction.DESTROY)
                    )
            );

    public static final DeferredBlock<UntintedParticleLeavesBlock> DYNASTY_LEAVES =
            BLOCKS.registerBlock("dynasty_leaves",
                    props -> new UntintedParticleLeavesBlock(
                            0.08F,
                            ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0x871413),
                            props.mapColor(MapColor.PLANT)
                                    .strength(0.2F)
                                    .randomTicks()
                                    .sound(SoundType.GRASS)
                                    .noOcclusion()
                                    .isSuffocating((s,l,p) -> false)
                                    .isViewBlocking((s,l,p) -> false)
                                    .pushReaction(PushReaction.DESTROY)
                    )
            );

    public static final DeferredBlock<UntintedParticleLeavesBlock> IMPERIUM_LEAVES =
            BLOCKS.registerBlock("imperium_leaves",
                    props -> new UntintedParticleLeavesBlock(
                            0.08F,
                            ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0x2C55A7),
                            props.mapColor(MapColor.PLANT)
                                    .strength(0.2F)
                                    .randomTicks()
                                    .sound(SoundType.GRASS)
                                    .noOcclusion()
                                    .isSuffocating((s,l,p) -> false)
                                    .isViewBlocking((s,l,p) -> false)
                                    .pushReaction(PushReaction.DESTROY)
                    )
            );


    public static final DeferredBlock<UntintedParticleLeavesBlock> MISCHIEF_LEAVES =
            BLOCKS.registerBlock("mischief_leaves",
                    props -> new UntintedParticleLeavesBlock(
                            0.08F,
                            ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0x86347E),
                            props.mapColor(MapColor.PLANT)
                                    .strength(0.2F)
                                    .randomTicks()
                                    .sound(SoundType.GRASS)
                                    .noOcclusion()
                                    .isSuffocating((s,l,p) -> false)
                                    .isViewBlocking((s,l,p) -> false)
                                    .pushReaction(PushReaction.DESTROY)
                    )
            );


    public static final DeferredBlock<TintedParticleLeavesBlock> MUSAVACCA_LEAVES_CROWN =
            BLOCKS.registerBlock("musavacca_leaves_crown",
                    props -> new TintedParticleLeavesBlock(
                            0.0F,
                            props.mapColor(MapColor.PLANT)
                                    .strength(0.2F)
                                    .randomTicks()
                                    .sound(SoundType.GRASS)
                                    .noOcclusion()
                                    .isSuffocating((s, l, p) -> false)
                                    .isViewBlocking((s, l, p) -> false)
                                    .pushReaction(PushReaction.DESTROY)));

//

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
                            .mapColor(MapColor.TERRACOTTA_BROWN)
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

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_BELMONT_LOG =
            BLOCKS.registerBlock("stripped_belmont_log",
                    props -> new RotatedPillarBlock(props
                            .strength(2.0F)
                            .sound(SoundType.STEM)));

    public static final DeferredBlock<RotatedPillarBlock> BELMONT_LOG =
            BLOCKS.registerBlock("belmont_log",
                    props -> new StrippableNatrualPillarBlock(
                            props.strength(2.0F).sound(SoundType.WOOD),
                            STRIPPED_BELMONT_LOG));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_DYNASTY_LOG =
            BLOCKS.registerBlock("stripped_dynasty_log",
                    props -> new RotatedPillarBlock(props
                            .strength(2.0F)
                            .sound(SoundType.STEM)));

    public static final DeferredBlock<RotatedPillarBlock> DYNASTY_LOG =
            BLOCKS.registerBlock("dynasty_log",
                    props -> new StrippableNatrualPillarBlock(
                            props.strength(2.0F).sound(SoundType.WOOD),
                            STRIPPED_DYNASTY_LOG));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_IMPERIUM_LOG =
            BLOCKS.registerBlock("stripped_imperium_log",
                    props -> new RotatedPillarBlock(props
                            .strength(2.0F)
                            .sound(SoundType.STEM)));

    public static final DeferredBlock<RotatedPillarBlock> IMPERIUM_LOG =
            BLOCKS.registerBlock("imperium_log",
                    props -> new StrippableNatrualPillarBlock(
                            props.strength(2.0F).sound(SoundType.WOOD),
                            STRIPPED_IMPERIUM_LOG));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_MISCHIEF_LOG =
            BLOCKS.registerBlock("stripped_mischief_log",
                    props -> new RotatedPillarBlock(props
                            .strength(2.0F)
                            .sound(SoundType.STEM)));

    public static final DeferredBlock<RotatedPillarBlock> MISCHIEF_LOG =
            BLOCKS.registerBlock("mischief_log",
                    props -> new StrippableNatrualPillarBlock(
                            props.strength(2.0F).sound(SoundType.WOOD),
                            STRIPPED_MISCHIEF_LOG));


    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_MUSAVACCA_PSEUDOSTEM =
            BLOCKS.registerBlock("stripped_musavacca_pseudostem",
                    props -> new RotatedPillarBlock(props
                            .strength(2.0F)
                            .sound(SoundType.STEM)));

    public static final DeferredBlock<RotatedPillarBlock> MUSAVACCA_PSEUDOSTEM =
            BLOCKS.registerBlock("musavacca_pseudostem",
                    props -> new StrippablePillarBlock(
                            props.strength(2.0F).sound(SoundType.STEM),
                            STRIPPED_MUSAVACCA_PSEUDOSTEM));


    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_BELMONT_WOOD =
            BLOCKS.registerBlock("stripped_belmont_wood",
                    props -> new RotatedPillarBlock(props
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<RotatedPillarBlock> BELMONT_WOOD =
            BLOCKS.registerBlock("belmont_wood",
                    props -> new StrippablePillarBlock(
                            props.strength(2.0F).sound(SoundType.WOOD),
                            STRIPPED_BELMONT_WOOD));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_DYNASTY_WOOD =
            BLOCKS.registerBlock("stripped_dynasty_wood",
                    props -> new RotatedPillarBlock(props
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<RotatedPillarBlock> DYNASTY_WOOD =
            BLOCKS.registerBlock("dynasty_wood",
                    props -> new StrippablePillarBlock(
                            props.strength(2.0F).sound(SoundType.WOOD),
                            STRIPPED_DYNASTY_WOOD));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_IMPERIUM_WOOD =
            BLOCKS.registerBlock("stripped_imperium_wood",
                    props -> new RotatedPillarBlock(props
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<RotatedPillarBlock> IMPERIUM_WOOD =
            BLOCKS.registerBlock("imperium_wood",
                    props -> new StrippablePillarBlock(
                            props.strength(2.0F).sound(SoundType.WOOD),
                            STRIPPED_IMPERIUM_WOOD));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_MISCHIEF_WOOD =
            BLOCKS.registerBlock("stripped_mischief_wood",
                    props -> new RotatedPillarBlock(props
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<RotatedPillarBlock> MISCHIEF_WOOD =
            BLOCKS.registerBlock("mischief_wood",
                    props -> new StrippablePillarBlock(
                            props.strength(2.0F).sound(SoundType.WOOD),
                            STRIPPED_MISCHIEF_WOOD));





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

    public static final DeferredBlock<BelmontSaplingBlock> BELMONT_SAPLING =
            BLOCKS.registerBlock("belmont_sapling",
                    props -> new BelmontSaplingBlock(props
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .instabreak()
                            .randomTicks()
                            .sound(SoundType.GRASS)));

    public static final DeferredBlock<DynastySaplingBlock> DYNASTY_SAPLING =
            BLOCKS.registerBlock("dynasty_sapling",
                    props -> new DynastySaplingBlock(props
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .instabreak()
                            .randomTicks()
                            .sound(SoundType.GRASS)));

    public static final DeferredBlock<ImperiumSaplingBlock> IMPERIUM_SAPLING =
            BLOCKS.registerBlock("imperium_sapling",
                    props -> new ImperiumSaplingBlock(props
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .instabreak()
                            .randomTicks()
                            .sound(SoundType.GRASS)));

    public static final DeferredBlock<MischiefSaplingBlock> MISCHIEF_SAPLING =
            BLOCKS.registerBlock("mischief_sapling",
                    props -> new MischiefSaplingBlock(props
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
                                    .randomTicks()
                                    .pushReaction(PushReaction.NORMAL)
                    ));


    public static final DeferredBlock<NyliumGrassBlock>  BELMONT_GRASS_BLOCK =
            BLOCKS.registerBlock("belmont_grass_block",
                    props -> new NyliumGrassBlock(
                            props.mapColor(MapColor.TERRACOTTA_LIGHT_GREEN)
                                    .strength(0.6F)
                                    .sound(SoundType.GRASS)
                                    .randomTicks()
                                    .pushReaction(PushReaction.NORMAL)
                    ));

    public static final DeferredBlock<NyliumGrassBlock>  DYNASTY_GRASS_BLOCK =
            BLOCKS.registerBlock("dynasty_grass_block",
                    props -> new NyliumGrassBlock(
                            props.mapColor(MapColor.TERRACOTTA_RED)
                                    .strength(0.6F)
                                    .sound(SoundType.GRASS)
                                    .randomTicks()
                                    .pushReaction(PushReaction.NORMAL)
                    ));


    public static final DeferredBlock<NyliumGrassBlock>  IMPERIUM_GRASS_BLOCK =
            BLOCKS.registerBlock("imperium_grass_block",
                    props -> new NyliumGrassBlock(
                            props.mapColor(MapColor.TERRACOTTA_BLUE)
                                    .strength(0.6F)
                                    .sound(SoundType.GRASS)
                                    .randomTicks()
                                    .pushReaction(PushReaction.NORMAL)
                    ));

    public static final DeferredBlock<NyliumGrassBlock>  MISCHIEF_GRASS_BLOCK =
            BLOCKS.registerBlock("mischief_grass_block",
                    props -> new NyliumGrassBlock(
                            props.mapColor(MapColor.TERRACOTTA_PURPLE)
                                    .strength(0.6F)
                                    .sound(SoundType.GRASS)
                                    .randomTicks()
                                    .pushReaction(PushReaction.NORMAL)
                    ));

    public static final DeferredBlock<TallGrassBlock> CAROTENE_SHORT_GRASS =
            BLOCKS.registerBlock("carotene_short_grass",
                    props -> new TallGrassBlock(
                            props.noCollission()
                                    .instabreak()
                                    .offsetType(BlockBehaviour.OffsetType.XZ)
                                    .mapColor(MapColor.GRASS)
                                    .sound(SoundType.GRASS)
                                    .pushReaction(PushReaction.DESTROY)
                    )
            );

    public static final DeferredBlock<TallGrassBlock> BELMONT_SHORT_GRASS =
            BLOCKS.registerBlock("belmont_short_grass",
                    props -> new TallGrassBlock(
                            props.noCollission()
                                    .instabreak()
                                    .offsetType(BlockBehaviour.OffsetType.XZ)
                                    .mapColor(MapColor.GRASS)
                                    .sound(SoundType.GRASS)
                                    .pushReaction(PushReaction.DESTROY)
                    )
            );

    public static final DeferredBlock<TallGrassBlock> DYNASTY_SHORT_GRASS =
            BLOCKS.registerBlock("dynasty_short_grass",
                    props -> new TallGrassBlock(
                            props.noCollission()
                                    .instabreak()
                                    .offsetType(BlockBehaviour.OffsetType.XZ)
                                    .mapColor(MapColor.GRASS)
                                    .sound(SoundType.GRASS)
                                    .pushReaction(PushReaction.DESTROY)
                    )
            );

    public static final DeferredBlock<TallGrassBlock> IMPERIUM_SHORT_GRASS =
            BLOCKS.registerBlock("imperium_short_grass",
                    props -> new TallGrassBlock(
                            props.noCollission()
                                    .instabreak()
                                    .offsetType(BlockBehaviour.OffsetType.XZ)
                                    .mapColor(MapColor.GRASS)
                                    .sound(SoundType.GRASS)
                                    .pushReaction(PushReaction.DESTROY)
                    )
            );

    public static final DeferredBlock<TallGrassBlock> MISCHIEF_SHORT_GRASS =
            BLOCKS.registerBlock("mischief_short_grass",
                    props -> new TallGrassBlock(
                            props.noCollission()
                                    .instabreak()
                                    .offsetType(BlockBehaviour.OffsetType.XZ)
                                    .mapColor(MapColor.GRASS)
                                    .sound(SoundType.GRASS)
                                    .pushReaction(PushReaction.DESTROY)
                    )
            );





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



    public static final DeferredBlock<MischiefCatalystBlock> MISCHIEF_CATALYST =
            BLOCKS.registerBlock("mischief_catalyst",
                    p -> new MischiefCatalystBlock(
                            p.mapColor(MapColor.COLOR_PURPLE)
                                    .strength(1.5F, 6.0F)
                                    .sound(SoundType.SCULK)
                                    .randomTicks()
                    ));

    public static final DeferredBlock<FactionVeinBlock> MISCHIEF_VEIN =
            BLOCKS.registerBlock("mischief_vein",
                    p -> new FactionVeinBlock(
                            p.mapColor(MapColor.COLOR_PURPLE)
                                    .noOcclusion()
                                    .strength(0.2F)
                                    .sound(SoundType.SCULK)
                                    .dynamicShape()
                    ));

    public static final DeferredBlock<ImperiumCatalystBlock> IMPERIUM_CATALYST =
            BLOCKS.registerBlock("imperium_catalyst",
                    p -> new ImperiumCatalystBlock(
                            p.mapColor(net.minecraft.world.level.material.MapColor.COLOR_LIGHT_BLUE)
                                    .strength(1.5F, 6.0F)
                                    .sound(net.minecraft.world.level.block.SoundType.SCULK)
                                    .randomTicks()
                    ));
    public static final DeferredBlock<FactionVeinBlock> IMPERIUM_VEIN =
            BLOCKS.registerBlock("imperium_vein",
                    p -> new FactionVeinBlock(
                            p.mapColor(net.minecraft.world.level.material.MapColor.COLOR_LIGHT_BLUE)
                                    .noOcclusion()
                                    .strength(0.2F)
                                    .sound(net.minecraft.world.level.block.SoundType.SCULK)
                                    .dynamicShape()
                    ));

    // Dynasty
    public static final DeferredBlock<DynastyCatalystBlock> DYNASTY_CATALYST =
            BLOCKS.registerBlock("dynasty_catalyst",
                    p -> new DynastyCatalystBlock(
                            p.mapColor(net.minecraft.world.level.material.MapColor.COLOR_GREEN)
                                    .strength(1.5F, 6.0F)
                                    .sound(net.minecraft.world.level.block.SoundType.SCULK)
                                    .randomTicks()
                    ));
    public static final DeferredBlock<FactionVeinBlock> DYNASTY_VEIN =
            BLOCKS.registerBlock("dynasty_vein",
                    p -> new FactionVeinBlock(
                            p.mapColor(net.minecraft.world.level.material.MapColor.COLOR_GREEN)
                                    .noOcclusion()
                                    .strength(0.2F)
                                    .sound(net.minecraft.world.level.block.SoundType.SCULK)
                                    .dynamicShape()
                    ));

    // Belmont
    public static final DeferredBlock<BelmontCatalystBlock> BELMONT_CATALYST =
            BLOCKS.registerBlock("belmont_catalyst",
                    p -> new BelmontCatalystBlock(
                            p.mapColor(net.minecraft.world.level.material.MapColor.COLOR_RED)
                                    .strength(1.5F, 6.0F)
                                    .sound(net.minecraft.world.level.block.SoundType.SCULK)
                                    .randomTicks()
                    ));
    public static final DeferredBlock<FactionVeinBlock> BELMONT_VEIN =
            BLOCKS.registerBlock("belmont_vein",
                    p -> new FactionVeinBlock(
                            p.mapColor(net.minecraft.world.level.material.MapColor.COLOR_RED)
                                    .noOcclusion()
                                    .strength(0.2F)
                                    .sound(net.minecraft.world.level.block.SoundType.SCULK)
                                    .dynamicShape()
                    ));


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
