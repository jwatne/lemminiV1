package game;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
/*
 * Copyright 2009 Volker Oth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    /**
     * Get number of Lemming type in internal resource array.
     *
     * @param t Type
     * @return resource number for type
     */
    public static int getOrdinal(final Type t) {
        switch (t) {
        case BOMBER_STOPPER:
            return Type.BOMBER.ordinal();
        case FLOATER_START:
            return Type.FLOATER.ordinal();
        default:
            return t.ordinal();
        }
    }
}
