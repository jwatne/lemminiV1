package game;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/** Lemming heading. */
public enum Direction {
    /** Right. */
    RIGHT,
    /** Left. */
    LEFT,
    /** No direction. */
    NONE;

    /**
     * Lookup Map.
     */
    private static final Map<Integer, Direction> LOOKUP = new HashMap<>();

    static {
        for (final Direction s : EnumSet.allOf(Direction.class)) {
            LOOKUP.put(s.ordinal(), s);
        }
    }

    /**
     * Reverse lookup implemented via hashtable.
     *
     * @param val Ordinal value
     * @return Parameter with ordinal value val
     */
    public static Direction get(final int val) {
        return LOOKUP.get(val);
    }

}
