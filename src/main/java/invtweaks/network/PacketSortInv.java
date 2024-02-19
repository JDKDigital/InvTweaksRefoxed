package invtweaks.network;

import invtweaks.InvTweaksMod;
import invtweaks.util.Sorting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;


import java.util.function.Supplier;

public class PacketSortInv {
    private final boolean isPlayer;

    public PacketSortInv(boolean isPlayer) {
        this.isPlayer = isPlayer;
    }

    public PacketSortInv(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                Sorting.executeSort(ctx.get().getSender(), isPlayer);
            } catch (Exception e) {
                // can potentially throw exceptions which are silenced by enqueueWork
                InvTweaksMod.LOGGER.error("Failed to sort inventory", e);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isPlayer);
    }
}
