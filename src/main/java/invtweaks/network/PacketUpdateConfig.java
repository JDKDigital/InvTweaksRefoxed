package invtweaks.network;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import invtweaks.InvTweaksMod;
import invtweaks.config.InvTweaksConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacketUpdateConfig implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(InvTweaksMod.MODID, "packet_update_config");
    private final List<UnmodifiableConfig> cats;
    private final List<String> rules;
    private final List<UnmodifiableConfig> contOverrides;
    private final boolean autoRefill;

    @SuppressWarnings("unused")
    public PacketUpdateConfig() {
        this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false);
    }

    public PacketUpdateConfig(
            List<UnmodifiableConfig> cats,
            List<String> rules,
            List<UnmodifiableConfig> contOverrides,
            boolean autoRefill) {
        this.cats = cats;
        this.rules = rules;
        this.autoRefill = autoRefill;
        this.contOverrides = contOverrides;
    }

    public PacketUpdateConfig(FriendlyByteBuf buf) {
        this.cats = new ArrayList<>();
        int catsSize = buf.readVarInt();
        for (int i = 0; i < catsSize; ++i) {
            CommentedConfig subCfg = CommentedConfig.inMemory();
            subCfg.set("name", buf.readUtf(32767));
            List<String> spec = new ArrayList<>();
            int specSize = buf.readVarInt();
            for (int j = 0; j < specSize; ++j) {
                spec.add(buf.readUtf(32767));
            }
            subCfg.set("spec", spec);
            cats.add(subCfg);
        }
        this.rules = new ArrayList<>();
        int rulesSize = buf.readVarInt();
        for (int i = 0; i < rulesSize; ++i) {
            rules.add(buf.readUtf(32767));
        }
        this.contOverrides = new ArrayList<>();
        int contOverridesSize = buf.readVarInt();
        for (int i = 0; i < contOverridesSize; ++i) {
            CommentedConfig contOverride = CommentedConfig.inMemory();
            contOverride.set("containerClass", buf.readUtf(32767));
            contOverride.set("x", buf.readInt());
            contOverride.set("y", buf.readInt());
            contOverride.set("sortRange", buf.readUtf(32767));
            contOverrides.add(contOverride);
        }
        this.autoRefill = buf.readBoolean();
    }

    public static void handle(PacketUpdateConfig packet, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> ctx.player().ifPresent(p -> {
            InvTweaksConfig.setPlayerCats(p, InvTweaksConfig.cfgToCompiledCats(packet.cats));
            InvTweaksConfig.setPlayerRules(p, new InvTweaksConfig.Ruleset(packet.rules));
            InvTweaksConfig.setPlayerAutoRefill(p, packet.autoRefill);
            InvTweaksConfig.setPlayerContOverrides(p, InvTweaksConfig.cfgToCompiledContOverrides(packet.contOverrides));
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeVarInt(cats.size());
        for (UnmodifiableConfig subCfg : cats) {
            buf.writeUtf(subCfg.getOrElse("name", ""));
            List<String> spec = subCfg.getOrElse("spec", Collections.emptyList());
            buf.writeVarInt(spec.size());
            for (String subSpec : spec) {
                buf.writeUtf(subSpec);
            }
        }
        buf.writeVarInt(rules.size());
        for (String subRule : rules) {
            buf.writeUtf(subRule);
        }
        buf.writeVarInt(contOverrides.size());
        for (UnmodifiableConfig contOverride : contOverrides) {
            buf.writeUtf(contOverride.getOrElse("containerClass", ""));
            int x = contOverride.getIntOrElse("x", InvTweaksConfig.NO_POS_OVERRIDE);
            int y = contOverride.getIntOrElse("y", InvTweaksConfig.NO_POS_OVERRIDE);
            buf.writeInt(x).writeInt(y);
            buf.writeUtf(contOverride.getOrElse("sortRange", InvTweaksConfig.NO_SPEC_OVERRIDE));
        }
        buf.writeBoolean(autoRefill);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
