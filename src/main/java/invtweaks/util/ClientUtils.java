package invtweaks.util;

import invtweaks.network.NetworkDispatcher;
import invtweaks.network.PacketSortInv;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

public class ClientUtils {
    private ClientUtils() {
        // nothing to do
    }

    public static Player safeGetPlayer() {
        return Minecraft.getInstance().player;
    }

    public static boolean serverConnectionExists() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.connection.isConnected(PacketSortInv.ID);
    }
}
