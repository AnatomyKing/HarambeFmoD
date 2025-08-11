package net.anatomyworld.harambefmod;

import com.mojang.logging.LogUtils;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.anatomyworld.harambefmod.block.entity.PearlFireBlockEntity;
import net.anatomyworld.harambefmod.component.ModDataComponents;
import net.anatomyworld.harambefmod.data.ModDataGenerators;
import net.anatomyworld.harambefmod.entity.ModEntities;
import net.anatomyworld.harambefmod.entity.bananacow.clientmodel.BananaCowModel;
import net.anatomyworld.harambefmod.entity.bananacow.clientmodel.BananaCowRenderer;
import net.anatomyworld.harambefmod.item.ModCreativeTabs;
import net.anatomyworld.harambefmod.item.ModItems;
import net.anatomyworld.harambefmod.network.ModNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slf4j.Logger;

@Mod(HarambeCore.MOD_ID)
public final class HarambeCore {
    public static final String MOD_ID = "harambefmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public HarambeCore(IEventBus modBus, ModContainer container) {
        // Register all mod content
        ModEntities.register(modBus);
        ModItems.register(modBus);
        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModCreativeTabs.register(modBus);
        ModNetworking.register(modBus);  // **Register custom network payloads**

        // Common setup event
        modBus.addListener(this::commonSetup);

        // Config registration, data generation omitted for brevity...
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modBus.addListener(ModDataGenerators::gatherData);
    }

    private void commonSetup(final FMLCommonSetupEvent e) {
        // Common setup tasks (if any)
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static final class ClientEvents {
        @SubscribeEvent
        public static void layerDefs(EntityRenderersEvent.RegisterLayerDefinitions e) {
            // BananaCow model registration...
            e.registerLayerDefinition(
                    BananaCowModel.LAYER_LOCATION,
                    BananaCowModel::createBodyLayer

            );

        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent e) {
            e.enqueueWork(() -> {
                // Register entity renderers
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                        ModEntities.BANANA_COW.get(),
                        BananaCowRenderer::new
                );


                // Set cutout render layer for pearl fire (allows transparency)
                ItemBlockRenderTypes.setRenderLayer(
                        ModBlocks.PEARL_FIRE.get(),
                        ChunkRenderTypeSet.of(RenderType.cutout())
                );

                ItemBlockRenderTypes.setRenderLayer(
                        ModBlocks.MUSAVACCA_FLOWER.get(),
                        ChunkRenderTypeSet.of(RenderType.cutout())
                );

                ItemBlockRenderTypes.setRenderLayer(
                        ModBlocks.BANANA_COW_EGG.get(),
                        ChunkRenderTypeSet.of(RenderType.cutout())
                );

                // Register block color handler to tint each Pearl Fire with its unique color
                BlockColors blockColors = Minecraft.getInstance().getBlockColors();
                blockColors.register(
                        (BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) -> {
                            if (tintIndex == 0 && level != null && pos != null) {
                                BlockEntity be = level.getBlockEntity(pos);
                                if (be instanceof PearlFireBlockEntity pearlFire) {
                                    return pearlFire.getColor();
                                }
                            }
                            return 0xFFFFFF;
                        },
                        ModBlocks.PEARL_FIRE.get()
                );
            });
        }
    }
}
