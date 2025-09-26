// src/main/java/net/anatomyworld/harambefmod/data/ModDataGenerators.java
package net.anatomyworld.harambefmod.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ModDataGenerators {

    /** Register ALL providers for the "clientData" run here (NeoForge 1.21.6–1.21.8 style). */
    public static void gatherData(final GatherDataEvent.Client event) {
        // Recipes (Runner pattern in 1.21.x)
        event.createProvider(ModRecipeProvider.Runner::new);

        // ✅ Block + Item tags (item tags mirror block tags)
        event.createBlockAndItemTags(ModBlockTagsProvider::new, PlankyItemTagsProvider::new);

        // Loot tables
        event.createProvider(ModLootTableProvider::new);

        // Models / blockstates / auto item models
        event.createProvider(ModModelProvider::new);

        // Equipment assets (if you have these)
        event.createProvider(EquipmentAssetsProvider::new);

        // Lang
        event.createProvider(out -> new ModLanguageProvider(out, "en_us"));

        // GLMs / worldgen (your existing ones)
        event.createProvider(out -> new ModGLMProvider(out, event.getLookupProvider()));
        event.createProvider(out -> new ModWorldgenProvider(out, event.getLookupProvider()));
    }

    private ModDataGenerators() {}
}
