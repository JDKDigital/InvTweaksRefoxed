package invtweaks.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.ImmutableMap;
import invtweaks.InvTweaksMod;
import invtweaks.network.PacketUpdateConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class InvTweaksConfig {
    public static final ForgeConfigSpec CLIENT_CONFIG;
    /**
     * Sentinel to indicate that the GUI position should be left alone.
     */
    public static final int NO_POS_OVERRIDE = -1418392593;

    public static final String NO_SPEC_OVERRIDE = "default";
    public static final Map<String, Category> DEFAULT_CATS =
            ImmutableMap.<String, Category>builder()
                    .put("sword", new Category("/instanceof:net.minecraft.world.item.SwordItem"))
                    .put("axe", new Category("/instanceof:net.minecraft.world.item.AxeItem"))
                    .put("pickaxe", new Category("/instanceof:net.minecraft.world.item.PickaxeItem"))
                    .put("shovel", new Category("/instanceof:net.minecraft.world.item.ShovelItem"))
                    .put("hoe", new Category("/instanceof:net.minecraft.world.item.HoeItem"))
                    .put(
                            "acceptableFood",
                            new Category(
                                    String.format(
                                            "/isFood:; !%s; !%s; !%s; !%s",
                                            ForgeRegistries.ITEMS.getKey(Items.ROTTEN_FLESH),
                                            ForgeRegistries.ITEMS.getKey(Items.SPIDER_EYE),
                                            ForgeRegistries.ITEMS.getKey(Items.POISONOUS_POTATO),
                                            ForgeRegistries.ITEMS.getKey(Items.PUFFERFISH))))
                    .put("torch", new Category(ForgeRegistries.ITEMS.getKey(Items.TORCH).toString()))
                    .put("cheapBlocks", new Category("/tag:forge:cobblestone", "/tag:minecraft:dirt"))
                    .put("blocks", new Category("/instanceof:net.minecraft.world.item.BlockItem"))
                    .build();
    public static final List<String> DEFAULT_RAW_RULES = Arrays.asList("D /LOCKED", "A1-C9 /OTHER");
    public static final Ruleset DEFAULT_RULES = new Ruleset(DEFAULT_RAW_RULES);
    public static final Map<String, ContOverride> DEFAULT_CONT_OVERRIDES =
            ImmutableMap.<String, ContOverride>builder()
                    .put("appeng.client.gui.implementations.*Screen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("appeng.client.gui.me.items.*Screen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("de.mari_023.ae2wtlib.wct.*Screen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.github.glodblock.epp.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("org.cyclops.integrateddynamics.inventory.container.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("org.cyclops.integratedterminals.inventory.container.ContainerTerminalStoragePart", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.refinedmods.refinedstorage.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("tfar.craftingstation.CraftingStationMenu", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("tfar.dankstorage.container.DankContainers", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE,""))
                    .put("mcjty.rftoolsutility.modules.crafter.blocks.CrafterContainer", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("gripe._90.megacells.menu.MEGAInterfaceMenu", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("cofh.thermal.core.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("ironfurnaces.gui.furnaces.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, "0-45"))
                    .put("com.direwolf20.justdirethings.client.screens.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.direwolf20.laserio.client.screens.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.stal111.forbidden_arcanus.client.gui.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("aztech.modern_industrialization.*.gui.*Screen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .build();

    private static final ForgeConfigSpec.ConfigValue<List<? extends UnmodifiableConfig>> CATS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> RULES;
    private static final ForgeConfigSpec.BooleanValue ENABLE_AUTOREFILL;
    private static final ForgeConfigSpec.BooleanValue ENABLE_QUICKVIEW;
    private static final ForgeConfigSpec.IntValue ENABLE_SORT;
    private static final ForgeConfigSpec.IntValue ENABLE_BUTTONS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends UnmodifiableConfig>>
            CONT_OVERRIDES;
    private static final Map<UUID, Map<String, Category>> playerToCats = new HashMap<>();
    private static final Map<UUID, Ruleset> playerToRules = new HashMap<>();
    private static final Set<UUID> playerAutoRefill = new HashSet<>();
    private static final Map<UUID, Map<String, ContOverride>> playerToContOverrides = new HashMap<>();
    private static Map<String, Category> COMPILED_CATS = DEFAULT_CATS;
    private static Ruleset COMPILED_RULES = DEFAULT_RULES;
    private static Map<String, ContOverride> COMPILED_CONT_OVERRIDES = DEFAULT_CONT_OVERRIDES;
    private static boolean isDirty = false;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        {
            builder.comment("Sorting customization").push("sorting");

            CATS =
                    builder
                            .comment(
                                    "Categor(y/ies) for sorting",
                                    "",
                                    "name: the name of the category",
                                    "",
                                    "spec:",
                                    "Each element denotes a series of semicolon-separated clauses",
                                    "Items need to match all clauses of at least one element",
                                    "Items matching earlier elements are earlier in order",
                                    "A clause of the form /tag:<tag_value> matches a tag",
                                    "Clauses /instanceof:<fully_qualified_name> or /class:<fully_qualified_name> check if item is",
                                    "instance of class or exactly of that class respectively",
                                    "Specifying an item's registry name as a clause checks for that item",
                                    "Prepending an exclamation mark at the start of a clause inverts it")
                            .defineList(
                                    "category",
                                    DEFAULT_CATS.entrySet().stream()
                                            .map(ent -> ent.getValue().toConfig(ent.getKey()))
                                            .collect(Collectors.toList()),
                                    obj -> obj instanceof UnmodifiableConfig);

            RULES =
                    builder
                            .comment(
                                    "Rules for sorting",
                                    "Each element is of the form <POS> <CATEGORY>",
                                    "A-D is the row from top to bottom",
                                    "1-9 is the column from left to right",
                                    "POS denotes the target slots",
                                    "Exs. POS = D3 means 3rd slot of hotbar",
                                    "     POS = B means 2nd row, left to right",
                                    "     POS = 9 means 9th column, bottom to top",
                                    "     POS = A1-C9 means slots A1,A2,…,A9,B1,…,B9,C1,…,C9",
                                    "     POS = A9-C1 means slots A9,A8,…,A1,B9,…,B1,C9,…,C1",
                                    "Append v to POS of the form A1-C9 to move in columns instead of rows",
                                    "Append r to POS of the form B or 9 to reverse slot order",
                                    "CATEGORY is the item category to designate the slots to",
                                    "CATEGORY = /LOCKED prevents slots from moving in sorting",
                                    "CATEGORY = /FROZEN has the effect of /LOCKED and, in addition, ignores slot in auto-refill",
                                    "CATEGORY = /OTHER covers all remaining items after other rules are exhausted")
                            .defineList("rules", DEFAULT_RAW_RULES, obj -> obj instanceof String);

            CONT_OVERRIDES =
                    builder
                            .comment(
                                    "Custom settings per GUI",
                                    "x = x-position of external sort button relative to GUI top left",
                                    "y = same as above except for the y-position",
                                    "Omit x and y to leave position unchanged",
                                    "sortRange = slots to sort",
                                    "E.g. sortRange = \"5,0-2\" sorts slots 5,0,1,2 in that order",
                                    "sortRange = \"\" disables sorting for that container",
                                    "Out-of-bound slots are ignored",
                                    "Omit sortRange to leave as default")
                            .defineList(
                                    "containerOverrides",
                                    DEFAULT_CONT_OVERRIDES.entrySet().stream()
                                            .map(ent -> ent.getValue().toConfig(ent.getKey()))
                                            .collect(Collectors.toList()),
                                    obj -> obj instanceof UnmodifiableConfig);

            builder.pop();
        }

        {
            builder.comment("Tweaks").push("tweaks");

            ENABLE_AUTOREFILL = builder.comment("Enable auto-refill").define("autoRefill", true);
            ENABLE_QUICKVIEW =
                    builder
                            .comment(
                                    "Enable a quick view of how many items that you're currently holding exists in your inventory by displaying it next your hotbar.")
                            .define("quickView", true);
            ENABLE_SORT =
                    builder
                            .comment(
                                    "0 = disable sorting",
                                    "1 = player sorting only",
                                    "2 = external sorting only",
                                    "3 = all sorting enabled (default)")
                            .defineInRange("enableSort", 3, 0, 3);
            ENABLE_BUTTONS =
                    builder
                            .comment(
                                    "0 = disable buttons (i.e. keybind only)",
                                    "1 = buttons for player sorting only",
                                    "2 = buttons for external sorting only",
                                    "3 = all buttons enabled (default)")
                            .defineInRange("enableButtons", 3, 0, 3);

            builder.pop();
        }

        CLIENT_CONFIG = builder.build();
    }

    @SuppressWarnings("unchecked")
    public static PacketUpdateConfig getSyncPacket() {
        return new PacketUpdateConfig(
                (List<UnmodifiableConfig>) CATS.get(),
                (List<String>) RULES.get(),
                (List<UnmodifiableConfig>) CONT_OVERRIDES.get(),
                ENABLE_AUTOREFILL.get());
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        BlockableEventLoop<?> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);
        executor.submitAsync(() -> setDirty(true));
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) {
        BlockableEventLoop<?> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);
        executor.submitAsync(() -> setDirty(true));
    }

    public static boolean isDirty() {
        return isDirty;
    }

    @SuppressWarnings("unchecked")
    public static void setDirty(boolean newVal) {
        isDirty = newVal;
        if (isDirty) {
            COMPILED_CATS = cfgToCompiledCats((List<UnmodifiableConfig>) CATS.get());
            COMPILED_RULES = new Ruleset((List<String>) RULES.get());
            COMPILED_CONT_OVERRIDES = cfgToCompiledContOverrides((List<UnmodifiableConfig>) CONT_OVERRIDES.get());
        }
    }

    public static Map<String, Category> getSelfCompiledCats() {
        return COMPILED_CATS;
    }

    public static Ruleset getSelfCompiledRules() {
        return COMPILED_RULES;
    }

    public static Map<String, ContOverride> getSelfCompiledContOverrides() {
        return COMPILED_CONT_OVERRIDES;
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData =
                CommentedFileConfig.builder(path)
                        .sync()
                        .autosave()
                        .writingMode(WritingMode.REPLACE)
                        .build();

        configData.load();
        spec.setConfig(configData);
    }

    public static void setPlayerCats(Player ent, Map<String, Category> cats) {
        playerToCats.put(ent.getUUID(), cats);
    }

    public static void setPlayerRules(Player ent, Ruleset ruleset) {
        playerToRules.put(ent.getUUID(), ruleset);
    }

    public static void setPlayerAutoRefill(Player ent, boolean autoRefill) {
        if (autoRefill) {
            playerAutoRefill.add(ent.getUUID());
        } else {
            playerAutoRefill.remove(ent.getUUID());
        }
    }

    public static void setPlayerContOverrides(Player ent, Map<String, ContOverride> val) {
        playerToContOverrides.put(ent.getUUID(), val);
    }

    public static Map<String, Category> getPlayerCats(Player ent) {
        if (DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> ent == Minecraft.getInstance().player) == Boolean.TRUE) {
            return getSelfCompiledCats();
        }
        return playerToCats.getOrDefault(ent.getUUID(), DEFAULT_CATS);
    }

    public static Ruleset getPlayerRules(Player ent) {
        if (DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> ent == Minecraft.getInstance().player) == Boolean.TRUE) {
            return getSelfCompiledRules();
        }
        return playerToRules.getOrDefault(ent.getUUID(), DEFAULT_RULES);
    }

    public static boolean getPlayerAutoRefill(Player ent) {
        if (DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> ent == Minecraft.getInstance().player) == Boolean.TRUE) {
            return ENABLE_AUTOREFILL.get();
        }
        return playerAutoRefill.contains(ent.getUUID());
    }

    public static ContOverride getPlayerContOverride(Player ent, String screenClass, String contClass) {
        var map = playerToContOverrides.getOrDefault(ent.getUUID(), DEFAULT_CONT_OVERRIDES);
        if (DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> ent == Minecraft.getInstance().player) == Boolean.TRUE) {
            map = getSelfCompiledContOverrides();
        }
        if (map.containsKey(screenClass)) {
            return map.get(screenClass);
        }
        if (map.containsKey(contClass)) {
            return map.get(contClass);
        }
        for (String s : map.keySet()) {
            var regex = Pattern.compile(s);
            if (regex.matcher(screenClass).matches() || regex.matcher(contClass).matches()) {
                return map.get(s);
            }
        }
        return null;
    }

    public static boolean isSortEnabled(boolean isPlayerSort) {
        return isFlagEnabled(ENABLE_SORT.get(), isPlayerSort);
    }

    public static boolean isButtonEnabled(boolean isPlayer) {
        return isFlagEnabled(ENABLE_BUTTONS.get(), isPlayer);
    }

    private static boolean isFlagEnabled(int flag, boolean isPlayer) {
        return flag == 3 || flag == (isPlayer ? 1 : 2);
    }

    public static boolean isQuickViewEnabled() {
        return ENABLE_QUICKVIEW.get();
    }

    public static Map<String, Category> cfgToCompiledCats(List<UnmodifiableConfig> lst) {
        Map<String, Category> catsMap = new LinkedHashMap<>();
        for (UnmodifiableConfig subCfg : lst) {
            String name = subCfg.getOrElse("name", "");
            if (!name.equals("") && !name.startsWith("/")) {
                catsMap.put(
                        name, new Category(subCfg.getOrElse("spec", Collections.emptyList())));
            }
        }
        return catsMap;
    }

    public static Map<String, ContOverride> cfgToCompiledContOverrides(List<UnmodifiableConfig> lst) {
        Map<String, ContOverride> res = new LinkedHashMap<>();
        for (UnmodifiableConfig subCfg : lst) {
            res.put(
                    subCfg.getOrElse("containerClass", ""),
                    new ContOverride(
                            subCfg.getOrElse("x", NO_POS_OVERRIDE),
                            subCfg.getOrElse("y", NO_POS_OVERRIDE),
                            subCfg.getOrElse("sortRange", NO_SPEC_OVERRIDE)));
        }
        return res;
    }
}
