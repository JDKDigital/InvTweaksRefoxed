package invtweaks.events;

import com.mojang.blaze3d.platform.InputConstants;
import invtweaks.InvTweaksMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = InvTweaksMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyMappings {
    private KeyMappings() {
        // nothing to do
    }

    public static final String KEY_CATEGORY = "key.categories.invtweaks";

    public static final KeyMapping SORT_PLAYER = new KeyMapping("key.invtweaks_sort_player.desc",
            KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, KEY_CATEGORY);

    public static final KeyMapping SORT_INVENTORY = new KeyMapping("key.invtweaks_sort_inventory.desc",
            KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, KEY_CATEGORY);

    public static final KeyMapping SORT_EITHER = new KeyMapping("key.invtweaks_sort_either.desc",
            KeyConflictContext.GUI, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, KEY_CATEGORY);

    @SubscribeEvent
    public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(SORT_PLAYER);
        event.register(SORT_INVENTORY);
        event.register(SORT_EITHER);
    }
}
