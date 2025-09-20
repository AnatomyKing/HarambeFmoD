package net.anatomyworld.harambefmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class Config {
    public static final ModConfigSpec COMMON_SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> OVERWORLD_LIKE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        OVERWORLD_LIKE = b
                .comment("""
                Dimensions treated as 'overworld-like' for Nether return.
                The last one visited will be used when leaving the Nether.
                Include minecraft:overworld and any custom overworld-mimic dims.
                """)
                .defineList(
                        "overworldLikeDimensions",
                        List.of(
                                "minecraft:overworld",
                                "harambefmod:dynastirium",
                                "harambefmod:belandes",
                                "harambefmod:impero",
                                "harambefmod:marchelus",
                                "harambefmod:dynastirium_default",
                                "harambefmod:belnades_default",
                                "harambefmod:impero_default",
                                "harambefmod:marchelus_default"
                        ),
                        () -> "minecraft:overworld",
                        o -> (o instanceof String s) && ResourceLocation.tryParse(s) != null
                );

        COMMON_SPEC = b.build();
    }

    public static boolean isOverworldLike(ResourceKey<Level> dimKey) {
        return OVERWORLD_LIKE.get().contains(dimKey.location().toString());
    }

    public static ResourceKey<Level> levelKey(String id) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(id));
    }
}
