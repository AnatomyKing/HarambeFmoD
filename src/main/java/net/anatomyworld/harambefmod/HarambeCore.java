package net.anatomyworld.harambefmod;

import com.mojang.logging.LogUtils;
import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.entity.PearlFireBlockEntity;
import net.anatomyworld.harambefmod.client.render.HarambeRenderLayers;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slf4j.Logger;

@Mod(HarambeCore.MOD_ID)
public final class HarambeCore {
    public static final String MOD_ID = "harambefmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public HarambeCore(IEventBus modBus, ModContainer container) {
        // Registries
        ModEntities.register(modBus);
        ModItems.register(modBus);
        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModCreativeTabs.register(modBus);
        ModNetworking.register(modBus);

        // Common + datagen
        modBus.addListener(this::commonSetup);
        container.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        modBus.addListener(ModDataGenerators::gatherData);

        // Client listeners (no @OnlyIn, no Bus enum needed)
        if (FMLLoader.getDist() == Dist.CLIENT) {
            modBus.addListener(ClientEvents::layerDefs);
            modBus.addListener(ClientEvents::clientSetup);
            // Force render layers via model bake hook
            modBus.addListener(HarambeRenderLayers::onModifyBakingResult);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent e) {
        // shared init if needed
    }

    public static final class ClientEvents {
        public static void layerDefs(EntityRenderersEvent.RegisterLayerDefinitions e) {
            e.registerLayerDefinition(BananaCowModel.LAYER_LOCATION, BananaCowModel::createBodyLayer);
        }

        public static void clientSetup(FMLClientSetupEvent e) {
            e.enqueueWork(() -> {
                // entity renderer
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                        ModEntities.BANANA_COW.get(), BananaCowRenderer::new
                );

                // colors
                BlockColors colors = Minecraft.getInstance().getBlockColors();

                // Pearl Fire per-blockentity color
                colors.register(
                        (BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) -> {
                            if (tintIndex == 0 && level != null && pos != null) {
                                BlockEntity be = level.getBlockEntity(pos);
                                if (be instanceof PearlFireBlockEntity fire) return fire.getColor();
                            }
                            return 0xFFFFFF;
                        },
                        ModBlocks.PEARL_FIRE.get()
                );

                // Banana Portal tint: cache -> BE fallback
                colors.register(
                        (state, level, pos, tintIndex) -> {
                            if (tintIndex != 0 || level == null || pos == null) return 0xFFFFFF;

                            int cached = net.anatomyworld.harambefmod.client.portal.BananaPortalTintCache.get(pos);
                            if (cached != -1) return cached;

                            var be = level.getBlockEntity(pos);
                            if (be instanceof net.anatomyworld.harambefmod.block.entity.BananaPortalBlockEntity p) {
                                return p.getColor();
                            }
                            return 0xFFFFFF;
                        },
                        ModBlocks.BANANA_PORTAL.get()
                );
            });
        }
    }
}
