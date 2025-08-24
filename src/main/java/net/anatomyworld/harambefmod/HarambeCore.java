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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.slf4j.Logger;

@Mod(HarambeCore.MOD_ID)
public final class HarambeCore {
    public static final String MOD_ID = "harambefmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public HarambeCore(IEventBus modBus, ModContainer container) {
        ModEntities.register(modBus);
        ModItems.register(modBus);
        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModCreativeTabs.register(modBus);
        ModNetworking.register(modBus);

        modBus.addListener(this::commonSetup);

        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modBus.addListener(ModDataGenerators::gatherData);
    }

    private void commonSetup(final FMLCommonSetupEvent e) {
        // common setup as needed
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static final class ClientEvents {

        @SubscribeEvent
        public static void registerLayerDefs(EntityRenderersEvent.RegisterLayerDefinitions e) {
            e.registerLayerDefinition(
                    BananaCowModel.LAYER_LOCATION,
                    BananaCowModel::createBodyLayer
            );
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers e) {
            e.registerEntityRenderer(ModEntities.BANANA_COW.get(), BananaCowRenderer::new);
        }

        @SubscribeEvent
        public static void registerBlockColors(RegisterColorHandlersEvent.Block e) {
            // Pearl Fire tint from its BE (expects faces with tintindex 0)
            e.register((state, level, pos, tintIndex) -> {
                if (tintIndex == 0 && level != null && pos != null) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof PearlFireBlockEntity pearlFire) {
                        return pearlFire.getColor(); // ARGB/RGB int
                    }
                }
                return 0xFFFFFFFF;
            }, ModBlocks.PEARL_FIRE.get());

            // Banana Portal tint (cache → BE → default), faces use tintindex 0
            e.register((state, level, pos, tintIndex) -> {
                if (tintIndex != 0 || level == null || pos == null) return 0xFFFFFFFF;

                int cached = net.anatomyworld.harambefmod.client.portal.BananaPortalTintCache.get(pos);
                if (cached != -1) return cached;

                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof net.anatomyworld.harambefmod.block.entity.BananaPortalBlockEntity p) {
                    return p.getColor();
                }
                return 0xFFFFFFFF;
            }, ModBlocks.BANANA_PORTAL.get());
        }
    }
}
