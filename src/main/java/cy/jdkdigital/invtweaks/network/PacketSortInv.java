package cy.jdkdigital.invtweaks.network;

import cy.jdkdigital.invtweaks.InvTweaksMod;
import cy.jdkdigital.invtweaks.util.Sorting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record PacketSortInv(boolean isPlayer) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(InvTweaksMod.MODID, "packet_sort_inv");

    public PacketSortInv(final FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    public static void handle(final PacketSortInv packet, final PlayPayloadContext ctx) {
        ctx.workHandler()
                .submitAsync(() -> ctx.player().ifPresent(p -> Sorting.executeSort(p, packet.isPlayer)))
                .exceptionally(e -> {
                    InvTweaksMod.LOGGER.error("Failed to sort inventory", e);
                    return null;
                });
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeBoolean(isPlayer);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
