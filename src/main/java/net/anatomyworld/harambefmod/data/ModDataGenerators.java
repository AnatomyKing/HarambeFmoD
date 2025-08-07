package net.anatomyworld.harambefmod.data;

import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Registers every server-side data provider (runData). */
public final class ModDataGenerators {

    public static void gatherData(final GatherDataEvent e) {

        var gen     = e.getGenerator();
        var lookup  = e.getLookupProvider();
        ExistingFileHelper helper = e.getExistingFileHelper();
        var out     = gen.getPackOutput();

        /* ---- recipes ---- */
        gen.addProvider(e.includeServer(),
                new ModRecipeProvider(out, lookup));

        /* ---- block-tags ---- */
        gen.addProvider(e.includeServer(),
                new ModBlockTagsProvider(out, lookup, helper));


        gen.addProvider(e.includeServer(),
                new ModLootTableProvider(out, lookup));   //  <<– new
    }

    private ModDataGenerators() {}
}
