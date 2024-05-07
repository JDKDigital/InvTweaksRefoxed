package invtweaks.network;

import invtweaks.InvTweaksMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = InvTweaksMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkDispatcher {
    private NetworkDispatcher() {
        // nothing to do
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(InvTweaksMod.MODID);

        registrar.playToServer(PacketSortInv.TYPE, PacketSortInv.CODEC, (payload, context) -> payload.handle(payload, context));
        registrar.playToServer(PacketUpdateConfig.TYPE, PacketUpdateConfig.CODEC, (payload, context) -> payload.handle(payload, context));
    }
}
