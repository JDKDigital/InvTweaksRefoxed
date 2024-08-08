package invtweaks.events;

import com.google.common.base.Throwables;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import invtweaks.InvTweaksMod;
import invtweaks.config.ContOverride;
import invtweaks.config.InvTweaksConfig;
import invtweaks.config.Ruleset;
import invtweaks.gui.InvTweaksButtonSort;
import invtweaks.network.PacketSortInv;
import invtweaks.util.ClientUtils;
import invtweaks.util.Sorting;
import invtweaks.util.Utils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@EventBusSubscriber(modid = InvTweaksMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private ClientEvents() {
        // nothing to do
    }

    public static final int MIN_SLOTS = 9;

    private static void requestSort(boolean isPlayer, String screenClass) {
        if (ClientUtils.serverConnectionExists()) {
            PacketDistributor.sendToServer(new PacketSortInv(isPlayer, screenClass));
        } else {
            Sorting.executeSort(ClientUtils.safeGetPlayer(), isPlayer, screenClass);
        }
    }

    //region onScreenEventInit

    public static @Nullable Slot getDefaultButtonPlacement(Collection<Slot> slots, Predicate<Slot> filter) {
        if (slots.stream().filter(filter).count() < MIN_SLOTS) {
            return null;
        }
        // pick the rightmost slot first, then the topmost in case of a tie
        // TODO change button position algorithm?
        return slots.stream()
                .filter(filter)
                .max(Comparator.<Slot>comparingInt(s -> s.x).thenComparingInt(s -> -s.y))
                .orElse(null);
    }

    private static final Set<Screen> screensWithExtSort = Collections.newSetFromMap(new WeakHashMap<>());
    @SubscribeEvent
    public static void onScreenEventInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen && !(screen instanceof CreativeModeInventoryScreen)) {
            // first, work with player inventory
            Slot placement = getDefaultButtonPlacement(screen.getMenu().slots, slot -> slot.container instanceof Inventory);
            if (placement != null
                    && InvTweaksConfig.isSortEnabled(true)
                    && InvTweaksConfig.isButtonEnabled(true)) {
                try {
                    event.addListener(
                            new InvTweaksButtonSort(
                                    screen.getGuiLeft() + placement.x + 17,
                                    screen.getGuiTop() + placement.y,
                                    btn -> requestSort(true, screen.getClass().getName())));
                } catch (Exception e) {
                    Throwables.throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            }

            // then, work with external inventory
            ContOverride override = InvTweaksConfig.getPlayerContOverride(Minecraft.getInstance().player, screen.getClass().getName(), screen.getMenu().getClass().getName());
            var isSortDisabled = Optional.ofNullable(override).filter(ContOverride::isSortDisabled).isPresent();

            if (!(screen instanceof EffectRenderingInventoryScreen) && !isSortDisabled) {
                int x = InvTweaksConfig.NO_POS_OVERRIDE, y = InvTweaksConfig.NO_POS_OVERRIDE;
                if (override != null) {
                    x = override.getX();
                    y = override.getY();
                }
                placement = getDefaultButtonPlacement(screen.getMenu().slots, slot -> !(slot.container instanceof Inventory || slot.container instanceof CraftingContainer));
                if (placement != null) {
                    if (x == InvTweaksConfig.NO_POS_OVERRIDE) {
                        x = placement.x + 17;
                    }
                    if (y == InvTweaksConfig.NO_POS_OVERRIDE) {
                        y = placement.y;
                    }
                }

                if (InvTweaksConfig.isSortEnabled(false)) {
                    try {
                        if (InvTweaksConfig.isButtonEnabled(false)) {
                            event.addListener(new InvTweaksButtonSort(screen.getGuiLeft() + x, screen.getGuiTop() + y, btn -> requestSort(false, screen.getClass().getName())));
                        }
                        screensWithExtSort.add(screen);
                    } catch (Exception e) {
                        Throwables.throwIfUnchecked(e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    //endregion

    //region onKeyPressed
    private static BooleanSupplier isJEIKeyboardActive = () -> false;

    public static void setJEIKeyboardActiveFn(BooleanSupplier query) {
        isJEIKeyboardActive = query;
    }

    public static boolean isJEIKeyboardActive() {
        return isJEIKeyboardActive.getAsBoolean();
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen && !(screen instanceof CreativeModeInventoryScreen) && !(screen.getFocused() instanceof EditBox) && !isJEIKeyboardActive()) {
            if (
                    InvTweaksConfig.isSortEnabled(true) &&
                    KeyMappings.SORT_PLAYER.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))
            ) {
                requestSort(true, screen.getClass().getName());
            }
            if (
                    InvTweaksConfig.isSortEnabled(false) &&
                    screensWithExtSort.contains(event.getScreen()) &&
                    KeyMappings.SORT_INVENTORY.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))
            ) {
                requestSort(false, screen.getClass().getName());
            }

            Slot slot = screen.getSlotUnderMouse();
            if (slot != null) {
                boolean isPlayerSort = Utils.isPlayerContainer(slot.container, screen, Minecraft.getInstance().player);;
                if (InvTweaksConfig.isSortEnabled(isPlayerSort)
                        && (isPlayerSort || screensWithExtSort.contains(event.getScreen()))
                        && KeyMappings.SORT_EITHER.isActiveAndMatches(
                                InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) {
                    requestSort(isPlayerSort, screen.getClass().getName());
                }
            }
        }
    }



    //endregion

    //region onMouseButtonPressed

    @SubscribeEvent
    public static void onMouseButtonPressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen && !(event.getScreen() instanceof CreativeModeInventoryScreen)) {
            boolean isMouseActive = KeyMappings.SORT_EITHER.getKeyConflictContext().isActive() && KeyMappings.SORT_EITHER.matchesMouse(event.getButton());
            if (!isMouseActive) return;

            Slot slot = screen.getSlotUnderMouse();
            if (slot != null) {
                // Disable sort if in creative and has clicked a non-empty slot
                if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasInfiniteMaterials() && !slot.getItem().isEmpty()) {
                    return;
                }
                boolean isPlayerSort = Utils.isPlayerContainer(slot.container, screen, Minecraft.getInstance().player);
                if (InvTweaksConfig.isSortEnabled(isPlayerSort) && (isPlayerSort || screensWithExtSort.contains(event.getScreen()))) {
                    requestSort(isPlayerSort, screen.getClass().getName());
                    event.setCanceled(true); // stop pick block event
                }
            }
        }
    }

    //endregion

    //region renderOverlay
    @SubscribeEvent
    public static void renderOverlay(RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            Player ent = Minecraft.getInstance().player;
            if (!InvTweaksConfig.isQuickViewEnabled()) {
                return;
            }

            Ruleset rules = InvTweaksConfig.getSelfCompiledRules();
            IntList frozen =
                    Optional.ofNullable(rules.catToInventorySlots("/FROZEN"))
                            .map(IntArrayList::new) // prevent modification
                            .orElseGet(IntArrayList::new);
            frozen.sort(null);

            assert ent != null;
            if (Collections.binarySearch(frozen, ent.getInventory().selected) >= 0) {
                return;
            }

            HumanoidArm dominantHand = ent.getMainArm();
            int i = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
            int i2 = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 16 - 3;
            int prime = dominantHand == HumanoidArm.RIGHT ? i + 91 + 10 : i - 91 - 26;
            int itemCount = IntStream.range(0, ent.getInventory().items.size()).filter(idx -> Collections.binarySearch(frozen, idx) < 0).mapToObj(ent.getInventory().items::get).filter(st -> ItemStack.isSameItemSameComponents(st, ent.getMainHandItem())).mapToInt(ItemStack::getCount).sum();

            if (itemCount > ent.getMainHandItem().getCount()) {
                ItemStack toRender = ent.getMainHandItem().copy();
                toRender.setCount(itemCount);

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                Minecraft.getInstance().gui.renderSlot(event.getGuiGraphics(), prime, i2, DeltaTracker.ZERO, ent, toRender, 0);

                RenderSystem.disableBlend();
            }
        }
    }
}
