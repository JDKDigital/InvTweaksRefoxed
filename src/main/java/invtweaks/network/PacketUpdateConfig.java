package invtweaks.network;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import invtweaks.InvTweaksMod;
import invtweaks.config.InvTweaksConfig;
import invtweaks.config.Ruleset;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacketUpdateConfig implements CustomPacketPayload, IPayloadHandler<PacketUpdateConfig>
{
    public static final Type<PacketUpdateConfig> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(InvTweaksMod.MODID, "packet_update_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateConfig> CODEC = new StreamCodec<>()
    {
        @Override
        public PacketUpdateConfig decode(RegistryFriendlyByteBuf buff) {
            return new PacketUpdateConfig(buff);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PacketUpdateConfig packetUpdateConfig) {
            buffer.writeVarInt(packetUpdateConfig.cats.size());
            for (UnmodifiableConfig subCfg : packetUpdateConfig.cats) {
                buffer.writeUtf(subCfg.getOrElse("name", ""));
                List<String> spec = subCfg.getOrElse("spec", Collections.emptyList());
                buffer.writeVarInt(spec.size());
                for (String subSpec : spec) {
                    buffer.writeUtf(subSpec);
                }
            }
            buffer.writeVarInt(packetUpdateConfig.rules.size());
            for (String subRule : packetUpdateConfig.rules) {
                buffer.writeUtf(subRule);
            }
            buffer.writeVarInt(packetUpdateConfig.contOverrides.size());
            for (UnmodifiableConfig contOverride : packetUpdateConfig.contOverrides) {
                buffer.writeUtf(contOverride.getOrElse("containerClass", ""));
                int x = contOverride.getIntOrElse("x", InvTweaksConfig.NO_POS_OVERRIDE);
                int y = contOverride.getIntOrElse("y", InvTweaksConfig.NO_POS_OVERRIDE);
                buffer.writeInt(x).writeInt(y);
                buffer.writeUtf(contOverride.getOrElse("sortRange", InvTweaksConfig.NO_SPEC_OVERRIDE));
            }
            buffer.writeBoolean(packetUpdateConfig.autoRefill);
        }
    };

    private final List<UnmodifiableConfig> cats;
    private final List<String> rules;
    private final List<UnmodifiableConfig> contOverrides;
    private final boolean autoRefill;

    @SuppressWarnings("unused")
    public PacketUpdateConfig() {
        this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false);
    }

    public PacketUpdateConfig(List<UnmodifiableConfig> cats, List<String> rules, List<UnmodifiableConfig> contOverrides, boolean autoRefill) {
        this.cats = cats;
        this.rules = rules;
        this.contOverrides = contOverrides;
        this.autoRefill = autoRefill;
    }

    public PacketUpdateConfig(RegistryFriendlyByteBuf buf) {
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

    @Override
    public void handle(PacketUpdateConfig packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            InvTweaksConfig.setPlayerCats(ctx.player(), InvTweaksConfig.cfgToCompiledCats(packet.cats));
            InvTweaksConfig.setPlayerRules(ctx.player(), new Ruleset(packet.rules));
            InvTweaksConfig.setPlayerAutoRefill(ctx.player(), packet.autoRefill);
            InvTweaksConfig.setPlayerContOverrides(ctx.player(), InvTweaksConfig.cfgToCompiledContOverrides(packet.contOverrides));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
