package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = HarambeCore.MOD_ID, value = Dist.CLIENT)
public final class ModDataGenerators {

    /** Register *all* data providers (runs with --client datagen). */
    @SubscribeEvent
    public static void gatherData(final GatherDataEvent.Client event) {
        // Recipes (Runner pattern in 1.21.x)
        event.createProvider(ModRecipeProvider.Runner::new);

        // Block tags (constructor: (PackOutput, CompletableFuture<HolderLookup.Provider>))
        event.createProvider(ModBlockTagsProvider::new);

        // Loot tables (constructor: (PackOutput, CompletableFuture<HolderLookup.Provider>))
        event.createProvider(ModLootTableProvider::new);

        // If you add item tags later and they depend on block tags:
        // event.createBlockAndItemTags(ModBlockTagsProvider::new, ModItemTagsProvider::new);
    }

    private ModDataGenerators() {}
}
