package net.anatomyworld.harambefmod.cosmetic.client;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.client.model.TransformModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = HarambeCore.MOD_ID, value = Dist.CLIENT)
public final class ClientModels {
    @SubscribeEvent
    public static void registerLoaders(ModelEvent.RegisterLoaders evt) {
        // Register our custom unbaked model loader exactly once.
        evt.register(TransformModel.LOADER_ID, new TransformModel.Loader());
    }
}
