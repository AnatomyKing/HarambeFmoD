package net.anatomyworld.harambefmod.attachment;

import net.anatomyworld.harambefmod.HarambeCore;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HarambeCore.MOD_ID);

    public static final Supplier<AttachmentType<CosmeticWardrobe>> COSMETIC_WARDROBE =
            ATTACHMENTS.register("cosmetic_wardrobe", () ->
                    AttachmentType.builder(CosmeticWardrobe::new)
                            .serialize(CosmeticWardrobe.CODEC)     // save
                            .sync(CosmeticWardrobe.STREAM_CODEC)   // auto-sync to clients
                            .copyOnDeath()                          // keep after death
                            .build());

    public static void register(IEventBus modBus) { ATTACHMENTS.register(modBus); }
    private ModAttachments() {}
}
