package cy.jdkdigital.invtweaks.events;

import cy.jdkdigital.invtweaks.InvTweaksMod;
import cy.jdkdigital.invtweaks.config.InvTweaksConfig;
import cy.jdkdigital.invtweaks.util.ClientUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = InvTweaksMod.MODID)
public class ServerEvents {
    private ServerEvents() {
        // nothing to do
    }

    private static final Map<Player, EnumMap<InteractionHand, Item>> itemsCache = new WeakHashMap<>();
    private static final Map<Player, Object2IntMap<Item>> usedCache = new WeakHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !InvTweaksConfig.getPlayerAutoRefill(event.player)) {
            return;
        }
        if (event.side == LogicalSide.SERVER) {
            EnumMap<InteractionHand, Item> cached = itemsCache.computeIfAbsent(event.player, k -> new EnumMap<>(InteractionHand.class));
            Object2IntMap<Item> ucached = usedCache.computeIfAbsent(event.player, k -> new Object2IntOpenHashMap<>());
            for (InteractionHand hand : InteractionHand.values()) {
                if (cached.get(hand) != null
                        && event.player.getItemInHand(hand).isEmpty()
                        && ((ServerPlayer) event.player)
                        .getStats()
                        .getValue(Stats.ITEM_USED.get(cached.get(hand)))
                        > ucached.getOrDefault(cached.get(hand), Integer.MAX_VALUE)) {
                    searchForSubstitute(event.player, hand, cached.get(hand));
                }
                ItemStack held = event.player.getItemInHand(hand);
                cached.put(hand, held.isEmpty() ? null : held.getItem());
                if (!held.isEmpty()) {
                    ucached.put(
                            held.getItem(),
                            ((ServerPlayer) event.player)
                                    .getStats()
                                    .getValue(Stats.ITEM_USED.get(held.getItem())));
                }
            }
        } else {
            if (InvTweaksConfig.isDirty()) {
                if (ClientUtils.serverConnectionExists()) PacketDistributor.SERVER.noArg().send(InvTweaksConfig.getSyncPacket());
                InvTweaksConfig.setDirty(false);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            if (event.getEntity() == ClientUtils.safeGetPlayer()) {
                InvTweaksConfig.setDirty(true);
            }
        }
    }

    private static void searchForSubstitute(Player ent, InteractionHand hand, Item item) {
        IntList frozen =
                Optional.ofNullable(InvTweaksConfig.getPlayerRules(ent).catToInventorySlots("/FROZEN"))
                        .map(IntArrayList::new) // prevent modification
                        .orElseGet(IntArrayList::new);
        frozen.sort(null);

        if (Collections.binarySearch(frozen, ent.getInventory().selected) >= 0) {
            return; // ignore frozen slot
        }

        // thank Simon for the flattening
        IItemHandler cap = ent.getCapability(Capabilities.ItemHandler.ENTITY);
        if (cap != null) {
            for (int i = 0; i < cap.getSlots(); ++i) {
                if (Collections.binarySearch(frozen, i) >= 0) {
                    continue; // ignore frozen slot
                }
                ItemStack cand = cap.extractItem(i, Integer.MAX_VALUE, true).copy();
                if (cand.getItem() == item) {
                    cap.extractItem(i, Integer.MAX_VALUE, false);
                    ent.setItemInHand(hand, cand);
                    break;
                }
            }
        }
    }
}
