package invtweaks.network;

import invtweaks.InvTweaksMod;
import invtweaks.util.Sorting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public class PacketSortInv implements CustomPacketPayload, IPayloadHandler<PacketSortInv>
{
    private final boolean isPlayer;
    private final String screenName;

    public static final Type<PacketSortInv> TYPE = new Type<>(new ResourceLocation(InvTweaksMod.MODID, "packet_sort_inv"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSortInv> CODEC = new StreamCodec<>()
    {
        @Override
        public PacketSortInv decode(RegistryFriendlyByteBuf buff) {
            return new PacketSortInv(buff.readBoolean(), buff.readUtf());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PacketSortInv packetSortInv) {
            buffer.writeBoolean(packetSortInv.isPlayer);
            buffer.writeUtf(packetSortInv.screenName);
        }
    };

    public PacketSortInv(boolean isPlayer, String screenClass) {
        this.isPlayer = isPlayer;
        this.screenName = screenClass;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(PacketSortInv payload, IPayloadContext context) {
        context.enqueueWork(() -> Sorting.executeSort(context.player(), payload.isPlayer, payload.screenName))
            .exceptionally(e -> {
                InvTweaksMod.LOGGER.error("Failed to sort inventory", e);
                return null;
            });
    }
}
