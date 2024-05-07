package invtweaks.config;

import invtweaks.InvTweaksMod;
import invtweaks.util.Utils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ruleset {
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