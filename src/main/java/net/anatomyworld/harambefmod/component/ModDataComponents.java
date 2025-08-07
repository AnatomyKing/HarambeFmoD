// src/main/java/net/anatomyworld/harambefd/component/ModDataComponents.java
package net.anatomyworld.harambefmod.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.DataComponents;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Defines our per‐ItemStack “flame_color” component as a hex‐string. */
public final class ModDataComponents {
    public static final DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(
                    net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE,
                    "harambefd"
            );

    // Now stores a String like "#D5CD49"
    public static final DeferredHolder<
            DataComponentType<?>, DataComponentType<String>
            > FLAME_COLOR =
            DATA_COMPONENTS.<String>registerComponentType("flame_color", builder ->
                    builder
                            .persistent(Codec.STRING)                    // store as string
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)   // sync to client
                            .cacheEncoding()
            );

    private ModDataComponents() {}
}
