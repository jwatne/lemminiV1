package game;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/** Lemming skill type. */
public enum Type {
    /** the typical Lemming. */
    WALKER,
    /** a falling Lemming. */
    FALLER,
    /** a climbing Lemming. */
    CLIMBER,
    /** a climbing Lemming returning to the ground. */
    CLIMBER_TO_WALKER,
    /** a Lemming on a parachute. */
    FLOATER,
    /** a Lemming dieing from a fall. */
    SPLAT,
    /** a Lemming blocking the way for the other Lemmings. */
    STOPPER,
    /** a Lemming drowning in the water. */
    DROWNING,
    /** a Lemming killed by a trap. */
    TRAPPED,
    /** a Lemming existing the level. */
    EXITING,
    /** a Lemming blowing itself up. */
    BOMBER,
    /** a Lemming building stairs. */
    BUILDER,
    /** a builder Lemmings with no more steps in his backpack. */
    BUILDER_END,
    /** a Lemming digging a hole in the ground. */
    DIGGER,
    /** a Lemming bashing the ground before it. */
    BASHER,
    /** a Lemming digging a mine with a pick. */
    MINER,
    /** a Lemming jumping over a small obstacle. */
    JUMPER,
    /* types without a separate animation */
    /** a Lemming that is nuked. */
    NUKE,
    /** a stopper that is told to explode. */
    BOMBER_STOPPER,
    /** a floater before the parachute opened completely. */
    FLOATER_START,
    /** undefined. */
    UNDEFINED;

    /**
     * Map for looking up Lemmings by type.
     */
    private static final Map<Integer, Type> LOOKUP = new HashMap<>();

    static {
        for (final Type s : EnumSet.allOf(Type.class)) {
            LOOKUP.put(s.ordinal(), s);
        }
    }

    /**
     * Reverse lookup implemented via hashtable.
     *
     * @param val Ordinal value
     * @return Parameter with ordinal value val
     */
    public static Type get(final int val) {
        return LOOKUP.get(val);
    }
}
