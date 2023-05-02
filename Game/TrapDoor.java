package Game;

/**
 * Trapdoor/Entry class
 * Trapdoor logic: for numbers >1, just take the next door for each lemming and
 * wrap around to 1 when
 * the last one is reached.
 * Special rule for 3 trapdoors: the order is 1, 2, 3, 2 (loop), not 1, 2, 3
 * (loop)
 *
 * @author Volker Oth
 */
public class TrapDoor {
    /** pattern for three entries */
    private final static int[] PATTERN3 = { 0, 1, 2, 1 };
    /** number of entries */
    private static int entries;
    /** entry counter */
    private static int counter;

    /**
     * Reset to new number of entries.
     * 
     * @param e number of entries
     */
    static void reset(final int e) {
        entries = e;
        counter = 0;
    }

    /**
     * Get index of next entry.
     * 
     * @return index of next entry
     */
    static int getNext() {
        final int retVal = counter;
        counter++;

        if (entries != 3) {
            if (counter >= entries) {
                counter = 0;
            }

            return retVal;
        }

        // special case: 3
        if (counter >= 4) {
            counter = 0;
        }

        return PATTERN3[retVal];
    }
}