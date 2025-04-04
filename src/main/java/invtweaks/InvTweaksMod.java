package invtweaks;

import invtweaks.config.InvTweaksConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(InvTweaksMod.MODID)
public class InvTweaksMod {
    public static final String MODID = "invtweaks";
    public static final Logger LOGGER = LogManager.getLogger(InvTweaksMod.MODID);

    public static final String IMS_METHOD_BLACKLIST = "blacklist-screen";

    public InvTweaksMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, InvTweaksConfig.CLIENT_CONFIG);

        InvTweaksConfig.loadConfig(InvTweaksConfig.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("invtweaks-client.toml"));

//        InterModComms.sendTo("invtweaks", IMS_METHOD_BLACKLIST, () -> "net.minecraft.client.gui.screens.*");
    }
}
