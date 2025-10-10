package net.anatomyworld.harambefmod;

import com.mojang.logging.LogUtils;
import net.anatomyworld.harambefmod.attachment.ModAttachments;
import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.entity.PearlFireBlockEntity;
import net.anatomyworld.harambefmod.client.render.FactionCatalystAreaOverlay;
import net.anatomyworld.harambefmod.component.ModDataComponents;
import net.anatomyworld.harambefmod.data.ModDataGenerators;
import net.anatomyworld.harambefmod.effect.ModMobEffects;
import net.anatomyworld.harambefmod.entity.ModEntities;
import net.anatomyworld.harambefmod.entity.boat.musavacca.clientmodel.MusavaccaBoatModel;
import net.anatomyworld.harambefmod.entity.boat.musavacca.clientmodel.MusavaccaBoatRenderer;
import net.anatomyworld.harambefmod.entity.mob.bananacow.clientmodel.BananaCowModel;
import net.anatomyworld.harambefmod.entity.mob.bananacow.clientmodel.BananaCowRenderer;
import net.anatomyworld.harambefmod.event.*;
import net.anatomyworld.harambefmod.faction.FactionProtectionEvents;
import net.anatomyworld.harambefmod.item.ModCreativeTabs;
import net.anatomyworld.harambefmod.item.ModItems;
import net.anatomyworld.harambefmod.menu.ModMenus;
import net.anatomyworld.harambefmod.network.ModNetworking;
import net.anatomyworld.harambefmod.worldgen.ModFeatures;
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
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mod(HarambeCore.MOD_ID)
public final class HarambeCore {
    public static final String MOD_ID = "harambefmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HarambeCore(IEventBus modBus, ModContainer container) {
        // Registries
        ModItems.register(modBus);
        ModEntities.register(modBus);
        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModCreativeTabs.register(modBus);
        ModNetworking.register(modBus);
        ModMobEffects.register(modBus);
        ModAttachments.register(modBus);
        ModFeatures.FEATURES.register(modBus);
        ModMenus.MENUS.register(modBus); // <-- menus

        // Common + datagen
        modBus.addListener(this::commonSetup);
        container.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        modBus.addListener(ModDataGenerators::gatherData);

        // Server-side event listeners
        EVENT_BUS.addListener(net.anatomyworld.harambefmod.cosmetic.CosmeticSets::addServerReloaders);
        EVENT_BUS.addListener(net.anatomyworld.harambefmod.cosmetic.CosmeticSets::onDatapackSync);
        EVENT_BUS.addListener(CaroteneGrassBonemealHandler::onBonemeal);
        EVENT_BUS.addListener(BelmontGrassBonemealHandler::onBonemeal);
        EVENT_BUS.addListener(DynastyGrassBonemealHandler::onBonemeal);
        EVENT_BUS.addListener(ImperiumGrassBonemealHandler::onBonemeal);
        EVENT_BUS.addListener(MischiefGrassBonemealHandler::onBonemeal);


        EVENT_BUS.addListener((RegisterCommandsEvent e) ->
                net.anatomyworld.harambefmod.command.SeedHereCommand.register(e.getDispatcher())
        );
        EVENT_BUS.addListener((RegisterCommandsEvent e) ->
                net.anatomyworld.harambefmod.command.BalanceCommand.register(e.getDispatcher())
        );
        EVENT_BUS.addListener((net.neoforged.neoforge.event.RegisterCommandsEvent e) ->
                net.anatomyworld.harambefmod.command.OpenSimpleChestCommand.register(e.getDispatcher())
        );




        EVENT_BUS.addListener(net.anatomyworld.harambefmod.faction.FactionAuraEffects::onPlayerTick);

        // Client overlay (only once!)
        EVENT_BUS.addListener(FactionCatalystAreaOverlay::onRenderAfterBlockEntities);

        CrossDimPortalHandler.register();
        PortalIgnitionHandler.register();
        SleepSkipCommandFallback.register();
        FactionProtectionEvents.register();

        // Client-only listeners
        if (FMLLoader.getDist() == Dist.CLIENT) {
            modBus.addListener(ClientEvents::layerDefs);
            modBus.addListener(ClientEvents::clientSetup);
            modBus.addListener(ClientEvents::registerScreens); // <-- screen registration via event
            modBus.addListener(net.anatomyworld.harambefmod.client.hud.BalanceHud::registerLayers);
            modBus.addListener(net.anatomyworld.harambefmod.client.render.HarambeRenderLayers::onModifyBakingResult);
            modBus.addListener(net.anatomyworld.harambefmod.client.render.CosmeticWardrobeRenderData::onRegisterStateMods);
            modBus.addListener(net.anatomyworld.harambefmod.client.render.CosmeticArmorRenderHandler::onAddLayers);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent e) {
        // common setup work here if needed
    }

    public static final class ClientEvents {

        public static void layerDefs(EntityRenderersEvent.RegisterLayerDefinitions e) {
            // Banana Cow model
            e.registerLayerDefinition(BananaCowModel.LAYER_LOCATION, BananaCowModel::createBodyLayer);
            // Musavacca Boat model
            e.registerLayerDefinition(MusavaccaBoatModel.LAYER, MusavaccaBoatModel::createBodyLayer);
        }

        public static void clientSetup(FMLClientSetupEvent e) {
            e.enqueueWork(() -> {
                // Entity renderers
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                        net.anatomyworld.harambefmod.entity.ModEntities.BANANA_COW.get(), BananaCowRenderer::new
                );
                net.minecraft.client.renderer.entity.EntityRenderers.register(
                        net.anatomyworld.harambefmod.entity.ModEntities.MUSAVACCA_BOAT.get(), MusavaccaBoatRenderer::new
                );

                // Block entity renderer(s)
                net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                        net.anatomyworld.harambefmod.block.ModBlockEntities.ANY_CHEST_ENTITY.get(),
                        net.anatomyworld.harambefmod.entity.chest.any.AnyChestRenderer::new
                );

            });

            // Colors (client)
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
                    net.anatomyworld.harambefmod.block.ModBlocks.PEARL_FIRE.get()
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
                    net.anatomyworld.harambefmod.block.ModBlocks.BANANA_PORTAL.get()
            );
        }

        // Screen registration event (1.21.x)
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(
                    net.anatomyworld.harambefmod.menu.ModMenus.SIMPLE_CHEST.get(),
                    net.anatomyworld.harambefmod.client.gui.SimpleChestScreen::new
            );
        }
    }
}
