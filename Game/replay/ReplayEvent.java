package game.replay;

/**
 * Storage class for one replay event.
 *
 * @author Volker Oth
 */
public class ReplayEvent {
    /** frame counter. */
    private int frameCtr;

    /** event type. */
    private int type;

    /**
     * Constructor.
     *
     * @param ctr frame counter
     * @param t   type
     */
    ReplayEvent(final int ctr, final int t) {
        frameCtr = ctr;
        type = t;
    }

    /**
     * Returns event type.
     *
     * @return event type.
     */
    public final int getType() {
        return type;
    }

    /**
     * Sets event type.
     *
     * @param eventType event type.
     */
    public final void setType(final int eventType) {
        this.type = eventType;
    }

    /**
     * Returns frame counter.
     *
     * @return frame counter.
     */
    public final int getFrameCtr() {
        return frameCtr;
    }

    /**
     * Sets frame counter.
     *
     * @param frameCounter frame counter.
     */
    public final void setFrameCtr(final int frameCounter) {
        this.frameCtr = frameCounter;
    }

    /**
     * Standard toString method. Subclasses may safely override with desired
     * information.
     */
    @Override
    public String toString() {
        return "" + frameCtr + ", " + type;
    }
}
