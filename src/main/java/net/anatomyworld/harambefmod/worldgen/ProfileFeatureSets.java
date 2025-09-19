package net.anatomyworld.harambefmod.worldgen;

import java.util.List;
import java.util.Map;

public final class ProfileFeatureSets {
    private ProfileFeatureSets() {}

    /** Which feature “bases” to inject per profile suffix. */
    public static final Map<String, List<String>> BY_SUFFIX = Map.of(
            PatchProfiles.HARAMBE_DEFAULT.suffix(), List.of(
                    "swap_short_grass",
                    "patch_tree"
            ),
            PatchProfiles.BELNADES_DEFAULT.suffix(), List.of(
                    "swap_short_grass",
                    "patch_tree"
            ),
            PatchProfiles.DYNASTIRIUM_DEFAULT.suffix(), List.of(
                    "swap_short_grass",
                    "patch_tree"
            ),
            PatchProfiles.IMPERO_DEFAULT.suffix(), List.of(
                    "swap_short_grass",
                    "patch_tree"
            ),
            PatchProfiles.MARCHELUS_DEFAULT.suffix(), List.of(
                    "swap_short_grass",
                    "patch_tree"
            )
    );
}
