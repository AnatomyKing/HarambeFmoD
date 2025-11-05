package net.anatomyworld.harambefmod.component;

import com.mojang.serialization.Codec;
import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.DataComponents;

public final class ModDataComponents {

    public static final DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(
                    net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE,
                    HarambeCore.MOD_ID
            );

    // Example existing component: hex color string like "#D5CD49"
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> FLAME_COLOR =
            DATA_COMPONENTS.registerComponentType("flame_color", builder ->
                    builder
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .cacheEncoding()
            );

    /**
     * Universal cosmetic skin:
     * component id: harambefmod:cosmetic_skin
     * value: namespace:path of the item to visually mimic.
     *
     * If present on an armor item, that armor slot can render as the given item.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> COSMETIC_SKIN =
            DATA_COMPONENTS.registerComponentType("cosmetic_skin", builder ->
                    builder
                            .persistent(ResourceLocation.CODEC)
                            .cacheEncoding()
            );

    /**
     * Whether the cosmetic skin is active.
     *
     * component id: harambefmod:cosmetic_skin_active
     *
     * Convention:
     * - If null  -> treat as "ON" (default).
     * - If true  -> ON.
     * - If false -> OFF (do not render overlay, but keep cosmetic_skin remembered).
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> COSMETIC_SKIN_ACTIVE =
            DATA_COMPONENTS.registerComponentType("cosmetic_skin_active", builder ->
                    builder
                            .persistent(Codec.BOOL)
            );

    private ModDataComponents() {}
}
