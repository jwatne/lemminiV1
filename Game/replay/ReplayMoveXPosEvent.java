package game.replay;

/**
 * Storage class for MOVE_XPOS event.
 *
 * @author Volker Oth
 */
public class ReplayMoveXPosEvent extends ReplayEvent {
    /** screen x position. */
    private int xPos;

    /**
     * Screen X position changed event.
     *
     * @param ctr Frame counter
     * @param x   release x position
     */
    ReplayMoveXPosEvent(final int ctr, final int x) {
        super(ctr, ReplayStream.MOVE_XPOS);
        xPos = x;
    }

    @Override
    public final String toString() {
        return super.toString() + ", " + xPos;
    }

    /**
     * Returns screen x position.
     *
     * @return screen x position.
     */
    public final int getxPos() {
        return xPos;
    }

    /**
     * Sets screen x position.
     *
     * @param screenXPosition screen x position.
     */
    public final void setxPos(final int screenXPosition) {
        this.xPos = screenXPosition;
    }
}
