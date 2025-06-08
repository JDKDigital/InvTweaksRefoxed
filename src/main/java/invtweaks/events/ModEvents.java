package invtweaks.events;

import invtweaks.InvTweaksMod;
import invtweaks.config.ContOverride;
import invtweaks.config.InvTweaksConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;

@EventBusSubscriber(modid = InvTweaksMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEvents
{
    @SubscribeEvent
    public static void interModProcess(InterModProcessEvent event) {
        event.getIMCStream().forEach(imcMessage -> {
            if (imcMessage.modId().equals(InvTweaksMod.MODID)) {
                if (imcMessage.method().equals(InvTweaksMod.IMS_METHOD_BLACKLIST)) {
                    InvTweaksMod.LOGGER.debug("adding blacklist from " + imcMessage.senderModId() + " for " + imcMessage.messageSupplier().get().toString());
                    InvTweaksConfig.IMS_CONT_OVERRIDES.put(imcMessage.messageSupplier().get().toString(), new ContOverride(InvTweaksConfig.NO_POS_OVERRIDE, InvTweaksConfig.NO_POS_OVERRIDE, ""));
                }
            }
        });
    }
}
