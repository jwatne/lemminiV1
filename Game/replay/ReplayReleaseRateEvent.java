package game.replay;

/**
 * Storage class for SET_RELEASE_RATE event.
 *
 * @author Volker Oth
 */
public class ReplayReleaseRateEvent extends ReplayEvent {
    /**
     * Release rate.
     */
    private int releaseRate;

    /**
     * Release Rate changed event.
     *
     * @param ctr  Frame counter
     * @param rate release rate value
     */
    ReplayReleaseRateEvent(final int ctr, final int rate) {
        super(ctr, ReplayStream.SET_RELEASE_RATE);
        releaseRate = rate;
    }

    @Override
    public final String toString() {
        return super.toString() + ", " + releaseRate;
    }

    /**
     * Returns release rate.
     *
     * @return release rate.
     */
    public final int getReleaseRate() {
        return releaseRate;
    }

    /**
     * Sets release rate.
     *
     * @param rate release rate.
     */
    public final void setReleaseRate(final int rate) {
        this.releaseRate = rate;
    }
}
