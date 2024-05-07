package invtweaks.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import invtweaks.InvTweaksMod;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ContOverride {
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
        } else if (!sortRangeSpec.equalsIgnoreCase(InvTweaksConfig.NO_SPEC_OVERRIDE)) {
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
        if (x != InvTweaksConfig.NO_POS_OVERRIDE) result.set("x", x);
        if (y != InvTweaksConfig.NO_POS_OVERRIDE) result.set("y", y);
        if (!sortRangeSpec.equalsIgnoreCase(InvTweaksConfig.NO_SPEC_OVERRIDE)) result.set("sortRange", sortRangeSpec);
        return result;
    }
}