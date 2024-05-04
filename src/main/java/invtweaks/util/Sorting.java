package invtweaks.util;

import com.google.common.base.Equivalence;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Streams;
import invtweaks.config.Category;
import invtweaks.config.ContOverride;
import invtweaks.config.InvTweaksConfig;
import invtweaks.config.Ruleset;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Sorting {
    private Sorting() {
        // nothing to do
    }

    public static void executeSort(Player player, boolean isPlayerSort, String screenClass) {
        if (isPlayerSort) {
            Map<String, Category> cats = InvTweaksConfig.getPlayerCats(player);
            Ruleset rules = InvTweaksConfig.getPlayerRules(player);
            IntList lockedSlots =
                    Optional.ofNullable(rules.catToInventorySlots("/LOCKED"))
                            .<IntList>map(IntArrayList::new) // copy list to prevent modification
                            .orElseGet(IntArrayList::new);
            lockedSlots.addAll(Optional.ofNullable(rules.catToInventorySlots("/FROZEN")).orElse(IntLists.EMPTY_LIST));
            lockedSlots.sort(null);

            if (player instanceof ServerPlayer serverPlayer) {
                playerSortServer(serverPlayer, cats, rules, lockedSlots);
            } else {
                playerSortClient(player, cats, rules, lockedSlots);
            }
        } else {
            AbstractContainerMenu cont = player.containerMenu;

            // check if an inventory is open
            if (cont != player.inventoryMenu) {
                ContOverride override = InvTweaksConfig.getPlayerContOverride(player, screenClass, cont.getClass().getName());
                var isSortDisabled = Optional.ofNullable(override).filter(ContOverride::isSortDisabled).isPresent();

                if (!isSortDisabled) {
                    List<Slot> validSlots =
                            (override != null && override.getSortRange() != null
                                    ? override.getSortRange().intStream()
                                    .filter(Objects::nonNull)
                                    .filter(idx -> 0 <= idx && idx < cont.slots.size())
                                    .mapToObj(cont.slots::get)
                                    : cont.slots.stream())
                                    .filter(slot -> !(slot.container instanceof Inventory))
                                    .filter(
                                            slot ->
                                                    (slot.mayPickup(player) && slot.mayPlace(slot.getItem()))
                                                            || !slot.hasItem())
                                    .collect(Collectors.toCollection(ArrayList::new));

                    if (player instanceof ServerPlayer serverPlayer) {
                        inventorySortServer(serverPlayer, validSlots);
                    } else {
                        inventorySortClient(player, validSlots);
                    }
                }
            }
        }
    }

    public static void playerSortClient(Player player, Map<String, Category> cats, Ruleset rules, IntList lockedSlots) {
        Inventory inv = player.getInventory();
        MultiPlayerGameMode pc = Minecraft.getInstance().gameMode;
        Int2ObjectMap<Slot> indexToSlot =
                player.containerMenu.slots.stream()
                        .filter(slot -> slot.container instanceof Inventory)
                        .filter(slot -> 0 <= slot.getSlotIndex() && slot.getSlotIndex() < 36)
                        .collect(
                                Collectors.toMap(
                                        Slot::getSlotIndex,
                                        Function.identity(),
                                        (u, v) -> u,
                                        Int2ObjectOpenHashMap::new));

        IntList stackIdxs =
                IntStream.range(0, inv.items.size())
                        .filter(idx -> Collections.binarySearch(lockedSlots, idx) < 0)
                        .filter(idx -> !inv.items.get(idx).isEmpty())
                        .collect(IntArrayList::new, IntList::add, IntList::addAll);
        Map<Equivalence.Wrapper<ItemStack>, Set<Slot>> gatheredSlots =
                Utils.gatheredSlots(
                        () ->
                                stackIdxs.stream()
                                        .mapToInt(v -> v)
                                        .mapToObj(indexToSlot::get)
                                        .filter(Slot::hasItem)
                                        .iterator());
        List<Equivalence.Wrapper<ItemStack>> stackWs =
                new ArrayList<>(gatheredSlots.keySet());
        stackWs.sort(
                Comparator.comparing(Equivalence.Wrapper::get, Utils.FALLBACK_COMPARATOR));

        for (Map.Entry<String, Category> ent : cats.entrySet()) {
            IntList specificRules = rules.catToInventorySlots(ent.getKey());
            if (specificRules == null) specificRules = IntLists.EMPTY_LIST;
            specificRules =
                    specificRules.stream()
                            .filter(idx -> Collections.binarySearch(lockedSlots, idx) < 0)
                            .mapToInt(v -> v)
                            .collect(IntArrayList::new, IntList::add, IntList::addAll);

            List<Slot> specificRulesSlots =
                    specificRules.stream()
                            .map(
                                    idx -> indexToSlot.get((int) idx))
                            .collect(Collectors.toCollection(ArrayList::new));
            ListIterator<Slot> toIt = specificRulesSlots.listIterator();

            Client.processCategoryClient(
                    player, pc, gatheredSlots, stackWs, ent.getValue(), toIt);
        }

        List<Slot> fallbackList =
                Stream.concat(
                                Streams.stream(
                                                Optional.ofNullable(rules.catToInventorySlots("/OTHER")))
                                        .flatMap(List::stream),
                                rules.fallbackInventoryRules().stream())
                        .mapToInt(v -> v)
                        .filter(idx -> Collections.binarySearch(lockedSlots, idx) < 0)
                        .distinct()
                        .mapToObj(indexToSlot::get)
                        .collect(Collectors.toCollection(ArrayList::new));

        Client.processCategoryClient(
                player, pc, gatheredSlots, stackWs, null, fallbackList.listIterator());

    }

    public static void playerSortServer(ServerPlayer player, Map<String, Category> cats, Ruleset rules, IntList lockedSlots) {
        Inventory inv = player.getInventory();

        List<ItemStack> stacks =
                Utils.condensed(
                        () ->
                                IntStream.range(0, inv.items.size())
                                        .filter(idx -> Collections.binarySearch(lockedSlots, idx) < 0)
                                        .mapToObj(inv.items::get)
                                        .filter(st -> !st.isEmpty())
                                        .iterator());
        stacks.sort(Utils.FALLBACK_COMPARATOR);
        stacks = new LinkedList<>(stacks);

        for (int i = 0; i < inv.items.size(); ++i) {
            if (Collections.binarySearch(lockedSlots, i) < 0) {
                inv.items.set(i, ItemStack.EMPTY);
            }
        }

        for (Map.Entry<String, Category> ent : cats.entrySet()) {
            IntList specificRules = rules.catToInventorySlots(ent.getKey());
            if (specificRules == null) specificRules = IntLists.EMPTY_LIST;
            specificRules =
                    specificRules.stream()
                            .filter(idx -> Collections.binarySearch(lockedSlots, idx) < 0)
                            .mapToInt(v -> v)
                            .collect(IntArrayList::new, IntList::add, IntList::addAll);
            List<ItemStack> curStacks = new ArrayList<>();
            Iterator<ItemStack> it = stacks.iterator();
            while (it.hasNext() && curStacks.size() < specificRules.size()) {
                ItemStack st = it.next();
                if (ent.getValue().checkStack(st) >= 0) {
                    curStacks.add(st);
                    it.remove();
                }
            }
            curStacks.sort(Comparator.comparingInt(s -> cats.get(ent.getKey()).checkStack(s)));
            //noinspection UnstableApiUsage
            Streams.zip(specificRules.stream(), curStacks.stream(), Pair::of)
                    .forEach(
                            pr -> inv.items.set(pr.getKey(), pr.getValue()));
        }

        PrimitiveIterator.OfInt fallbackIt =
                Stream.concat(
                                Optional.ofNullable(rules.catToInventorySlots("/OTHER")).stream()
                                        .flatMap(List::stream),
                                rules.fallbackInventoryRules().stream())
                        .mapToInt(v -> v)
                        .iterator();
        while (fallbackIt.hasNext()) {
            int idx = fallbackIt.nextInt();
            if (Collections.binarySearch(lockedSlots, idx) >= 0) {
                continue;
            }
            if (stacks.isEmpty()) {
                break;
            }
            if (inv.items.get(idx).isEmpty()) {
                inv.items.set(idx, stacks.remove(0));
            }
        }
    }

    public static void inventorySortClient(Player player, List<Slot> validSlots) {

        MultiPlayerGameMode pc = Minecraft.getInstance().gameMode;
        Map<Equivalence.Wrapper<ItemStack>, Set<Slot>> gatheredSlots =
                Utils.gatheredSlots(
                        () -> validSlots.stream()
                                .filter(Slot::hasItem)
                                .iterator());
        List<Equivalence.Wrapper<ItemStack>> stackWs =
                new ArrayList<>(gatheredSlots.keySet());
        stackWs.sort(
                Comparator.comparing(Equivalence.Wrapper::get, Utils.FALLBACK_COMPARATOR));

        ListIterator<Slot> toIt = validSlots.listIterator();
        for (Equivalence.Wrapper<ItemStack> stackW : stackWs) {
            BiMap<Slot, Slot> displaced = HashBiMap.create();
            Client.clientPushToSlots(
                    player, pc, gatheredSlots.get(stackW).iterator(), toIt, displaced);
            for (Map.Entry<Slot, Slot> displacedPair : displaced.entrySet()) {
                Set<Slot> toModify =
                        gatheredSlots.get(
                                Utils.STACKABLE.wrap(displacedPair.getValue().getItem()));
                toModify.remove(displacedPair.getKey());
                toModify.add(displacedPair.getValue());
            }
        }
    }

    public static void inventorySortServer(ServerPlayer serverPlayer, List<Slot> validSlots) {
        if (!validSlots.iterator().hasNext()) return;
        List<ItemStack> stacks = Utils.condensed(() -> validSlots.stream()
                .map(Slot::getItem)
                .filter(st -> !st.isEmpty())
                .iterator());
        stacks.sort(Utils.FALLBACK_COMPARATOR);

        Iterator<Slot> slotIt = validSlots.iterator();
        for (ItemStack stack : stacks) {
            Slot cur = null;
            while (slotIt.hasNext() && !(cur = slotIt.next()).mayPlace(stack)) {
                assert true;
            }
            if (cur == null || !cur.mayPlace(stack)) {
                return; // nope right out of the sort
            }
        }

        // execute sort
        validSlots.forEach(slot -> slot.set(ItemStack.EMPTY));
        slotIt = validSlots.iterator();
        for (ItemStack stack : stacks) {
            // System.out.println(i);
            Slot cur = null;
            while (slotIt.hasNext() && !(cur = slotIt.next()).mayPlace(stack)) {
                assert true;
            }
            assert cur != null;
            cur.set(stack);
        }
    }

    /**
     * This prevents the functions below from accidentally being loaded on the server.
     */
    static class Client {
        private Client() {
            // nothing to do
        }

        static void processCategoryClient(
                Player player,
                MultiPlayerGameMode pc,
                Map<Equivalence.Wrapper<ItemStack>, Set<Slot>> gatheredSlots,
                List<Equivalence.Wrapper<ItemStack>> stackWs,
                Category cat,
                ListIterator<Slot> toIt) {
            List<Equivalence.Wrapper<ItemStack>> subStackWs =
                    cat == null
                            ? new ArrayList<>(stackWs)
                            : stackWs.stream()
                            .filter(stackW -> cat.checkStack(stackW.get()) >= 0)
                            .sorted(Comparator.comparingInt(stackW -> cat.checkStack(stackW.get())))
                            .collect(Collectors.toCollection(ArrayList::new));

            for (Equivalence.Wrapper<ItemStack> stackW : subStackWs) {
                if (cat == null || cat.checkStack(stackW.get()) >= 0) {
                    BiMap<Slot, Slot> displaced = HashBiMap.create();
                    ListIterator<Slot> fromIt = (ListIterator<Slot>) gatheredSlots.get(stackW).iterator();
                    @SuppressWarnings("unused") boolean fullInserted = Client.clientPushToSlots(player, pc, fromIt, toIt, displaced);
                    for (Map.Entry<Slot, Slot> displacedPair : displaced.entrySet()) {
                        Equivalence.Wrapper<ItemStack> displacedW =
                                Utils.STACKABLE.wrap(displacedPair.getValue().getItem());
                        Set<Slot> toModify = gatheredSlots.get(displacedW);
                        toModify.remove(displacedPair.getKey());
                        toModify.add(displacedPair.getValue());
                    }
                }
            }
            stackWs.removeIf(sw -> gatheredSlots.get(sw).isEmpty());
            gatheredSlots.values().removeIf(Set::isEmpty);
        }

        /**
         * Transfers the items from a specified sequence of slots to a specified
         * sequence of slots, possibly displacing existing items.
         *
         * @param player           The player that is interacting with the sort
         * @param playerController Controller so clicks can be sent to move items
         * @param OriginIter       The Slots from which the ItemStacks will be moved
         * @param destinationIter  The Slots to which the ItemStacks will be moved
         * @param displaced        BiMap to keep track of what Slots had their items
         *                         swapped to make space for the items that needed to
         *                         be moved.
         * @return whether all items in OriginIter have been fully pushed
         */
        static boolean clientPushToSlots(Player player, MultiPlayerGameMode playerController, Iterator<Slot> OriginIter, ListIterator<Slot> destinationIter, BiMap<Slot, Slot> displaced) {
            // There are no more spaces in the destination container to put items
            if (!destinationIter.hasNext())
                return true;

            boolean completedCurrentItemSwap = true;

            // Grab more items from the to-move list.
            while (OriginIter.hasNext()) {
                // Starting new iteration -> not done with this item
                completedCurrentItemSwap = false;

                // Where is the item coming from
                Slot originSlot = OriginIter.next();
                // Pick up the origin item
                playerController.handleInventoryMouseClick(player.containerMenu.containerId, originSlot.index, 0, ClickType.PICKUP, player);

                // Find next open slot in the container
                Slot destinationSlot = null;
                while (destinationIter.hasNext()) {
                    // Check previous stack; If can put this item there, then do
                    if (destinationIter.hasPrevious()) {
                        destinationSlot = destinationIter.previous();

                        // If the stack is not at max capacity AND can stack with the one that is held right now
                        if (destinationSlot.getItem().getCount() != Math.min(destinationSlot.getMaxStackSize(), destinationSlot.getItem().getMaxStackSize())
                                && Utils.STACKABLE.equivalent(destinationSlot.getItem(), player.containerMenu.getCarried())) {
                            // Stay on this current 'previous' slot (by doing nothing).
                            assert true;
                        }

                        // Other wise advance back to where we should be.
                        else
                            destinationIter.next();
                    }

                    // Where the held item will be going
                    destinationSlot = destinationIter.next();

                    // Place held item (from origin) in destination slot,
                    // picking up whatever was at destination, if it had anything.
                    // or adding to that stack, if we backed up because it was the same item.
                    // (possibly filling the stack and getting leftover ItemStack)
                    playerController.handleInventoryMouseClick(player.containerMenu.containerId, destinationSlot.index, 0, ClickType.PICKUP, player);

                    // Didnt pick anything up -> done
                    if (player.containerMenu.getCarried().isEmpty()) {
                        completedCurrentItemSwap = true;
                        break;
                    }

                    // Did pick something else up / have leftover item from topping off the stack
                    else {
                        // If its overflow from the current item, no need to swap it back to the starting position,
                        // just try the next slot.
                        if (Utils.STACKABLE.equivalent(destinationSlot.getItem(), player.containerMenu.getCarried()))
                            continue;

                        // Else, this stack was picked up, and is being displaced...
                        // Click to put this item into the origin slot, which is guaranteed to be free
                        playerController.handleInventoryMouseClick(player.containerMenu.containerId, originSlot.index, 0, ClickType.PICKUP, player);

                        if (originSlot.hasItem() && !ItemHandlerHelper.canItemStacksStack(originSlot.getItem(), destinationSlot.getItem())) {
                            // This iteration is now complete.
                            completedCurrentItemSwap = true;
                            // Remember that the item that was in destination is now moved to origin...
                            displaced.put(destinationSlot, originSlot);
                            break;
                        }
                    }
                }
                if (!destinationIter.hasNext() && Optional.ofNullable(destinationSlot).filter(s -> s.getItem().getCount() >= Math.min(s.getMaxStackSize(), s.getItem().getMaxStackSize())).isPresent()) {
                    break;
                }
            }
            return completedCurrentItemSwap;
        }
    }
}
