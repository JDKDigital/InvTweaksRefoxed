package invtweaks.network;

import invtweaks.InvTweaksMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

@Mod.EventBusSubscriber(modid = InvTweaksMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkDispatcher {
    private NetworkDispatcher() {
        // nothing to do
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(InvTweaksMod.MODID).optional();

        registrar.play(PacketSortInv.ID, PacketSortInv::new, handler -> handler.server(PacketSortInv::handle));
        registrar.play(PacketUpdateConfig.ID, PacketUpdateConfig::new, handler -> handler.server(PacketUpdateConfig::handle));
    }
}
