package net.anatomyworld.harambefmod.block;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.entity.PearlFireBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

/** Registers all Block Entity types for the mod. */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, HarambeCore.MOD_ID);

    // Register the PearlFireBlock's BlockEntity type, linking it to the PearlFire block
    public static final Supplier<BlockEntityType<PearlFireBlockEntity>> PEARL_FIRE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("pearl_fire",
                    () -> BlockEntityType.Builder.of(PearlFireBlockEntity::new, ModBlocks.PEARL_FIRE.get())
                            .build(null));

    /** Called from {@link net.anatomyworld.harambefmod.HarambeCore} */
    public static void register(IEventBus modBus) {
        BLOCK_ENTITY_TYPES.register(modBus);
    }

    private ModBlockEntities() {}
}
