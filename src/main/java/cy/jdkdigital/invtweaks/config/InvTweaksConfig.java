package cy.jdkdigital.invtweaks.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.ImmutableMap;
import cy.jdkdigital.invtweaks.InvTweaksMod;
import cy.jdkdigital.invtweaks.util.Utils;
import cy.jdkdigital.invtweaks.network.PacketUpdateConfig;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class InvTweaksConfig {
    public static final ModConfigSpec CLIENT_CONFIG;
    /**
     * Sentinel to indicate that the GUI position should be left alone.
     */
    public static final int NO_POS_OVERRIDE = -1418392593;

    public static final String NO_SPEC_OVERRIDE = "default";
    public static final Map<String, Category> DEFAULT_CATS =
            ImmutableMap.<String, Category>builder()
                    .put("sword", new Category("/tag:forge:tools/swords"))
                    .put("axe", new Category("/tag:forge:tools/axes"))
                    .put("pickaxe", new Category("/tag:forge:tools/pickaxes"))
                    .put("shovel", new Category("/tag:forge:tools/shovels"))
                    .put("shield", new Category("/tag:forge:tools/shields"))
                    .put(
                            "acceptableFood",
                            new Category(
                                    String.format(
                                            "/instanceof:net.minecraft.item.Food; !%s; !%s; !%s; !%s",
                                            BuiltInRegistries.ITEM.getKey(Items.ROTTEN_FLESH),
                                            BuiltInRegistries.ITEM.getKey(Items.SPIDER_EYE),
                                            BuiltInRegistries.ITEM.getKey(Items.POISONOUS_POTATO),
                                            BuiltInRegistries.ITEM.getKey(Items.PUFFERFISH))))
                    .put(
                            "torch",
                            new Category(BuiltInRegistries.ITEM.getKey(Items.TORCH).toString()))
                    .put("cheapBlocks", new Category("/tag:minecraft:cobblestone", "/tag:minecraft:dirt"))
                    .put("blocks", new Category("/instanceof:net.minecraft.item.BlockItem"))
                    .build();
    public static final List<String> DEFAULT_RAW_RULES = Arrays.asList("D /LOCKED", "A1-C9 /OTHER");
    public static final Ruleset DEFAULT_RULES = new Ruleset(DEFAULT_RAW_RULES);
    public static final Map<String, ContOverride> DEFAULT_CONT_OVERRIDES =
            ImmutableMap.<String, ContOverride>builder()
                    .put("ad_astra_giselle_addon.client.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("agency.highlysuspect.packages.client.PackageMakerScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("appeng.client.gui.implementations.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("appeng.client.gui.me.common.MEStorageScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("appeng.client.gui.me.fluids.FluidTerminalScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("appeng.client.gui.me.interfaceterminal.InterfaceTerminalScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("appeng.client.gui.me.items.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("blusunrize.immersiveengineering.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("blusunrize.lib.manual.gui.ManualScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("ca.teamdman.sfm.client.gui.screen.ManagerScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("chiefarug.mods.systeams.client.screens.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("chiefarug.mods.systeams.compat.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("cofh.thermal.core.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("cofh.thermal.dynamics.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("cofh.thermal.expansion.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.Da_Technomancer.essentials.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.YTrollman.CableTiers.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.aetherteam.aether.client.gui.screen.inventory.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.almostreliable.merequester.client.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.ashindigo.storagecabinet.client.screen.CabinetManagerScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.blakebr0.mysticalagriculture.client.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.buuz135.refinedstoragerequestify.proxy.client.GuiRequester", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.chaosthedude.endermail.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.dannyandson.tinyredstone.gui.ChopperScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.darkere.crashutils.Screens.PlayerInvScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.davenonymous.bonsaitrees3.client.BonsaiPotScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.direwolf20.buildinggadgets.client.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.direwolf20.charginggadgets.blocks.chargingstation.ChargingStationScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.direwolf20.laserio.client.screens.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.direwolf20.mininggadgets.client.screens.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.enderio.conduits.client.gui.ConduitScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.enderio.machines.client.gui.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.finallion.graveyard.client.gui.OssuaryScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.github.alexmodguy.alexscaves.client.gui.SpelunkeryTableScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.github.alexthe666.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.github.klikli_dev.occultism.client.gui.DimensionalMineshaftScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.hidoni.transmog.gui.TransmogScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.hollingsworth.arsnouveau.client.container.CraftingTerminalScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.hrznstudio.titanium.client.screen.container.BasicAddonScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.illusivesoulworks.culinaryconstruct.client.CulinaryStationScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.klikli_dev.occultism.client.gui.storage.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.legacy.blue_skies.client.gui.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.leobeliik.convenientcurioscontainer.gui.ConvenientScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.lion.graveyard.gui.OssuaryScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.lothrazar.cyclic.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.lothrazar.plaingrinder.grind.ScreenGrinder", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.maciej916.indreb.common.block.impl.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.misha.blocks.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.mrcrayfish.furniture.client.gui.screen.inventory.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.mrcrayfish.goldenhopper.client.gui.screens.inventory.GoldenHopperScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.nyfaria.nyfsquiver.core.interfaces.QuiverContainerScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.pocky.solarpanels.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.refinedmods.refinedstorage.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.rekindled.embers.gui.SlateScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.sammy.minersdelight.content.block.copper_pot.CopperPotScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.simibubi.create.content.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.supermartijn642.core.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.supermartijn642.itemcollectors.screen.AdvancedCollectorScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.supermartijn642.simplemagnets.gui.MagnetContainerScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.supermartijn642.trashcans.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.tcn.dimensionalpocketsii.pocket.client.screen.ScreenPocket", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.teammetallurgy.aquaculture.client.gui.screen.TackleBoxScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.teammoeg.caupona.client.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.telepathicgrunt.the_bumblezone.client.screens.CrystallineFlowerScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.the9grounds.aeadditions.client.gui.MEWirelessTransceiverScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.the_millman.christmasfestivity.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.tom.storagemod.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.tynoxs.buildersdelight.content.gui.screens.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.ultramega.cabletiers.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.ultramega.creativecrafter.gui.CreativeCrafterScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.ultramega.rsinsertexportupgrade.screen.UpgradeScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("com.yogpc.qp.machines.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("commoble.jumbofurnace.client.JumboFurnaceScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("cy.jdkdigital.generatorgalore.common.container.GeneratorScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("cy.jdkdigital.productivebees.container.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("dan200.computercraft.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("de.castcrafter.travelanchors.block.ScreenTravelAnchor", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("de.ellpeck.prettypipes.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("de.mari_023.ae2wtlib.wat.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("de.maxhenkel.easyvillagers.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("de.maxhenkel.pipez.gui.ExtractScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("de.melanx.extradisks.blocks.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("dev.ftb.mods.ftbic.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("dev.gigaherz.toolbelt.common.BeltScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("dev.shadowsoffire.apotheosis.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("dev.shadowsoffire.clickmachine.block.gui.AutoClickScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("dev.shadowsoffire.hostilenetworks.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("dev.smolinacadena.refinedcooking.screen.KitchenAccessPointScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("doggytalents.client.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("earth.terrarium.ad_astra.client.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("earth.terrarium.chipped.common.menu.ChippedScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("edivad.extrastorage.client.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("elucent.eidolon.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("fuzs.barteringstation.client.gui.screens.inventory.BarteringStationScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("gg.moonflower.etched.client.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("info.u_team.useful_railroads.screen.TeleportRailScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("io.ejekta.bountiful.client.BoardScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("io.github.hw9636.autosmithingtable.client.AutoSmithingTableScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("io.github.mortuusars.chalk.client.gui.ChalkBoxScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("ironfurnaces.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("irongenerators.client.gui.GeneratorGUIScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("lain.mods.cos.impl.client.gui.GuiCosArmorInventory", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("li.cil.scannable.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("mcjty.rftoolsbase.modules.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("mcjty.rftoolsbuilder.modules.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("mcjty.rftoolspower.modules.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("mcjty.rftoolsstorage.modules.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("me.desht.modularrouters.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("me.tepis.integratednbt.NBTExtractorScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("mekanism.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("mekanism.generators.client.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("mob_grinding_utils.inventory.client.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("mod.chiselsandbits.client.screens.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("mods.waterstrainer.gui.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("mrthomas20121.thermal_extra.client.screens.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.Pandarix.betterarcheology.screen.IdentifyingScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.bdew.generators.controllers.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.bdew.generators.gui.GuiOutputConfig", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.blay09.mods.farmingforblockheads.client.gui.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.blay09.mods.waystones.client.gui.screen.WarpPlateScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.darkhax.botanypots.block.inv.BotanyPotScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.geforcemods.securitycraft.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.gigabit101.shrink.client.screen.ShrinkScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.joefoxe.hexerei.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.mehvahdjukaar.sawmill.SawmillScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.mehvahdjukaar.supplementaries.client.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.minecraft.client.gui.screens.inventory.SmithingScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.nicguzzo.wands.client.screens.MagicBagScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.orcinus.galosphere.client.gui.CombustionTableScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.permutated.pylons.client.gui.InterdictionPylonScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.permutated.pylons.machines.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.roguelogix.biggerreactors.machine.client.CyaniteReprocessorScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.satisfyu.meadow.client.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.silentchaos512.gear.block.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("net.silentchaos512.gear.item.blueprint.book.BlueprintBookContainerScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("ninjaphenix.expandedstorage.client.MiniChestScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("org.cyclops.colossalchests.client.gui.container.ContainerScreenColossalChest", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("org.cyclops.everlastingabilities.client.gui.ContainerScreenAbilityContainer", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("org.cyclops.integratedcrafting.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("org.cyclops.integratedterminals.client.gui.container.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("org.cyclops.integratedtunnels.core.part.ContainerScreenInterfaceSettings", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("org.zeith.solarflux.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("ovh.corail.woodcutter.client.gui.WoodcutterScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("owmii.powah.client.screen.container.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("se.gory_moon.chargers.client.ChargerScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("shetiphian.enderchests.client.gui.GuiEnderChest", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("shetiphian.platforms.client.gui.GuiPlatFormer", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("sirttas.elementalcraft.item.source.analysis.SourceAnalysisGlassScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("tfar.craftingstation.client.CraftingStationScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("tfar.dankstorage.client.screens.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("top.theillusivec4.culinaryconstruct.client.CulinaryScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("tv.mongotheelder.pitg.screens.GlassPaneTableScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("vapourdrive.furnacemk2.furnace.FurnaceMk2Screen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("vazkii.botania.client.gui.bag.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("vazkii.quark.addons.oddities.client.screen.CrateScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("wile.engineersdecor.blocks.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("xfacthd.framedblocks.client.screen.*", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))
                    .put("xyz.apex.forge.fantasyfurniture.client.screen.FurnitureStationMenuScreen", new ContOverride(NO_POS_OVERRIDE, NO_POS_OVERRIDE, ""))

                    .build();

    private static final ModConfigSpec.ConfigValue<List<? extends UnmodifiableConfig>> CATS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> RULES;
    private static final ModConfigSpec.BooleanValue ENABLE_AUTOREFILL;
    private static final ModConfigSpec.BooleanValue ENABLE_QUICKVIEW;
    private static final ModConfigSpec.IntValue ENABLE_SORT;
    private static final ModConfigSpec.IntValue ENABLE_BUTTONS;
    private static final ModConfigSpec.ConfigValue<List<? extends UnmodifiableConfig>>
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
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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
            COMPILED_CONT_OVERRIDES =
                    cfgToCompiledContOverrides((List<UnmodifiableConfig>) CONT_OVERRIDES.get());
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

    public static void loadConfig(ModConfigSpec spec, Path path) {
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
        if (FMLEnvironment.dist.isClient()) {
            return getSelfCompiledCats();
        }
        return playerToCats.getOrDefault(ent.getUUID(), DEFAULT_CATS);
    }

    public static Ruleset getPlayerRules(Player ent) {
        if (FMLEnvironment.dist.isClient()) {
            return getSelfCompiledRules();
        }
        return playerToRules.getOrDefault(ent.getUUID(), DEFAULT_RULES);
    }

    public static boolean getPlayerAutoRefill(Player ent) {
        if (FMLEnvironment.dist.isClient()) {
            return ENABLE_AUTOREFILL.get();
        }
        return playerAutoRefill.contains(ent.getUUID());
    }

    public static Map<String, ContOverride> getPlayerContOverrides(Player ent) {
        if (FMLEnvironment.dist.isClient()) {
            return getSelfCompiledContOverrides();
        }
        return playerToContOverrides.getOrDefault(ent.getUUID(), DEFAULT_CONT_OVERRIDES);
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
                        name, new InvTweaksConfig.Category(subCfg.getOrElse("spec", Collections.emptyList())));
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

    public static class Category {
        private final List<String> spec;
        private final List<List<Predicate<ItemStack>>> compiledSpec = new ArrayList<>();

        public Category(List<String> spec) {
            this.spec = spec;
            for (String subspec : spec) {
                List<Predicate<ItemStack>> compiledSubspec = new ArrayList<>();
                for (String clause : subspec.split("\\s*;\\s*")) {
                    compileClause(clause).ifPresent(compiledSubspec::add);
                }
                compiledSpec.add(compiledSubspec);
            }
        }

        public Category(String... spec) {
            this(Arrays.asList(spec));
        }

        private static Optional<Predicate<ItemStack>> compileClause(String clause) {
            if (clause.startsWith("!")) {
                return compileClause(clause.substring(1)).map(Predicate::negate);
            }

            String[] parts = clause.split(":", 2);
            if (parts[0].equals("/tag")) {
                TagKey<Item> itemKey = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(parts[1]));
                TagKey<Block> blockKey = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(parts[1]));

                return Optional.of(stack -> stack.is(itemKey) || (
                            stack.getItem() instanceof BlockItem blockItem
                            && blockItem.getBlock().defaultBlockState().is(blockKey))
                );
            } else if (parts[0].equals("/instanceof")
                    || parts[0].equals("/class")) { // use this for e.g. pickaxes
                try {
                    Class<?> clazz = Class.forName(parts[1]);
                    if (parts[0].equals("/instanceof")) {
                        return Optional.of(st -> clazz.isInstance(st.getItem()));
                    } else {
                        return Optional.of(st -> st.getItem().getClass().equals(clazz));
                    }
                } catch (ClassNotFoundException e) {
                    InvTweaksMod.LOGGER.warn("Class not found! Ignoring clause");
                    return Optional.empty();
                }
            } else { // default to standard item checking
                try {
                    return Optional.of(
                            st -> Objects.equals(BuiltInRegistries.ITEM.getKey(st.getItem()), new ResourceLocation(clause)));
                } catch (ResourceLocationException e) {
                    InvTweaksMod.LOGGER.warn("Invalid item resource location found.");
                    return Optional.empty();
                }
            }
        }

        // returns an index for sorting within a category
        public int checkStack(ItemStack stack) {
            return IntStream.range(0, compiledSpec.size())
                    .filter(idx -> compiledSpec.get(idx).stream().allMatch(pr -> pr.test(stack)))
                    .findFirst()
                    .orElse(-1);
        }

        public CommentedConfig toConfig(String catName) {
            CommentedConfig result = CommentedConfig.inMemory();
            result.set("name", catName);
            result.set("spec", spec);
            return result;
        }
    }

    public static class Ruleset {
        private final List<String> rules;
        private final Map<String, IntList> compiledRules = new LinkedHashMap<>();
        private final IntList compiledFallbackRules =
                new IntArrayList(Utils.gridSpecToSlots("A1-D9", false));

        public Ruleset(List<String> rules) {
            this.rules = rules;
            for (String rule : rules) {
                String[] parts = rule.split("\\s+", 2);
                if (parts.length == 2) {
                    try {
                        compiledRules
                                .computeIfAbsent(parts[1], k -> new IntArrayList())
                                .addAll(IntArrayList.wrap(Utils.gridSpecToSlots(parts[0], false)));
                        if (parts[1].equals("/OTHER")) {
                            compiledFallbackRules.clear();
                            compiledFallbackRules.addAll(
                                    IntArrayList.wrap(Utils.gridSpecToSlots(parts[0], true)));
                        }
                    } catch (IllegalArgumentException e) {
                        InvTweaksMod.LOGGER.warn("Bad slot target: " + parts[0]);
                        // throw e;
                    }
                } else {
                    InvTweaksMod.LOGGER.warn("Syntax error in rule: " + rule);
                }
            }
        }

        @SuppressWarnings("unused")
        public Ruleset(String... rules) {
            this(Arrays.asList(rules));
        }

        @SuppressWarnings("unused")
        public Ruleset(Ruleset rules) {
            this.rules = rules.rules;
            this.compiledRules.putAll(rules.compiledRules);
            this.compiledFallbackRules.clear();
            this.compiledFallbackRules.addAll(rules.compiledFallbackRules);
        }

        public IntList catToInventorySlots(String cat) {
            return compiledRules.get(cat);
        }

        public IntList fallbackInventoryRules() {
            return compiledFallbackRules;
        }
    }

    public static class ContOverride {
        private final int x, y;
        @Nullable
        private final IntList sortRange;
        private final String sortRangeSpec;

        public ContOverride(int x, int y, String sortRangeSpec) {
            this.x = x;
            this.y = y;
            this.sortRangeSpec = sortRangeSpec;
            IntList tmp = null;
            if (sortRangeSpec.isEmpty()) {
                tmp = IntLists.EMPTY_LIST;
            } else if (!sortRangeSpec.equalsIgnoreCase(NO_SPEC_OVERRIDE)) {
                try {
                    tmp =
                            Arrays.stream(sortRangeSpec.split("\\s*,\\s*"))
                                    .flatMapToInt(
                                            str -> {
                                                String[] rangeSpec = str.split("\\s*-\\s*");
                                                return IntStream.rangeClosed(
                                                        Integer.parseInt(rangeSpec[0]), Integer.parseInt(rangeSpec[1]));
                                            })
                                    .collect(IntArrayList::new, IntList::add, IntList::addAll);
                } catch (NumberFormatException e) {
                    InvTweaksMod.LOGGER.warn("Invalid slot spec: " + sortRangeSpec);
                    tmp = IntLists.EMPTY_LIST;
                }
            }
            sortRange = tmp;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public @Nullable
        IntList getSortRange() {
            return sortRange;
        }

        public boolean isSortDisabled() {
            return sortRange != null && sortRange.isEmpty();
        }

        public CommentedConfig toConfig(String contClass) {
            CommentedConfig result = CommentedConfig.inMemory();
            result.set("containerClass", contClass);
            if (x != NO_POS_OVERRIDE) result.set("x", x);
            if (y != NO_POS_OVERRIDE) result.set("y", y);
            if (!sortRangeSpec.equalsIgnoreCase(NO_SPEC_OVERRIDE)) result.set("sortRange", sortRangeSpec);
            return result;
        }
    }
}
