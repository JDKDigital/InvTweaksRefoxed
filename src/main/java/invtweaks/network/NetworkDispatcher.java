package invtweaks.network;

import invtweaks.InvTweaksMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkDispatcher {
    private NetworkDispatcher() {
        // nothing to do
    }

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(InvTweaksMod.MODID, "main"),
            () -> PROTOCOL_VERSION, NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION), NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION));

    public static void register() {
        int packetIndex = 0;
        INSTANCE.registerMessage(packetIndex++, PacketSortInv.class, PacketSortInv::encode, PacketSortInv::new, PacketSortInv::handle);
        INSTANCE.registerMessage(packetIndex++, PacketUpdateConfig.class, PacketUpdateConfig::encode, PacketUpdateConfig::new, PacketUpdateConfig::handle);
        InvTweaksMod.LOGGER.info("Registered {} network packets", packetIndex);
    }
}
