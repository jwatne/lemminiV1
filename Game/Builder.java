package game;

/**
 * Class for handling builder Lemmings. Logic moved from Lemming class by John
 * Watne 06/2023.
 */
public class Builder {
    /** number of steps before the warning sound is played. */
    private static final int STEPS_WARNING = 9;
    /**
     * 9 constant.
     */
    private static final int NINE = 9;
    /**
     * Minimum amount that must be free above lemming.
     */
    private static final int MIN_FREE_ABOVE_STEP = 8;
    /** number of steps a builder can build. */
    private static final int STEPS_MAX = 12;
    /**
     * Lemming owning an instance of this class.
     */
    private Lemming lemming;

    /**
     * Constructs a Builder for the specified Lemming.
     *
     * @param owner the Lemming owning this instance of the Builder class.
     */
    public Builder(final Lemming owner) {
        this.lemming = owner;
    }

    /**
     * Animates builder.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @param oldX            the old X value of the foot in pixels.
     * @param explode         <code>true</code> if the Lemming is to explode.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type animateBuilder(final Type startingNewType, final int oldX,
            final boolean explode) {
        Type newType = startingNewType;

        if (explode) {
            newType = Type.BOMBER;
            lemming.playOhNoIfNotToBeNuked();
        } else if (!lemming.turnedByStopper()) {
            int idx = lemming.getFrameIdx() + 1;

            if (idx >= lemming.getLemRes().getFrames() * Lemming.TIME_SCALE) {
                // step created -> move up
                idx = 0;
                lemming.setCounter(1 + lemming.getCounter()); // step counter;

                if (lemming.getDirection() == Direction.RIGHT) {
                    lemming.setX(lemming.getX() + Lemming.STEP_PIXELS);
                    // step forward
                } else {
                    lemming.setX(lemming.getX() - Lemming.STEP_PIXELS);
                }

                lemming.setY(lemming.getY() - 2); // step up
                final int levitation = lemming.aboveGround();
                // should be 0, if not, we built into a wall -> stop
                // check for conversion to walker
                final int fa = lemming.freeAbove(MIN_FREE_ABOVE_STEP);
                // check if builder is too close to
                // ceiling

                if (fa < MIN_FREE_ABOVE_STEP || levitation > 0) {
                    newType = Type.WALKER;

                    // a lemming can jump through the ceiling like in
                    // Mayhem2-Boiler Room
                    if (levitation >= Lemming.WALKER_OBSTACLE_HEIGHT) {
                        // avoid getting stuck
                        lemming.setX(oldX);
                        lemming.setY(lemming.getY() + 2);
                    }

                    lemming.setDirection(
                            (lemming.getDirection() == Direction.RIGHT)
                                    ? Direction.LEFT
                                    : Direction.RIGHT);
                } else
                // check for last step used
                if (lemming.getCounter() >= STEPS_MAX) {
                    newType = Type.BUILDER_END;
                }
            } else if (idx == NINE * Lemming.TIME_SCALE) {
                // stair mask is the same height as a lemming
                Mask m;
                m = lemming.getLemRes().getMask(lemming.getDirection());
                final int sx = lemming.screenX();
                final int sy = lemming.screenY();
                m.paintStep(sx, sy, 0,
                        GameController.getLevel().getDebrisColor());

                if (lemming.getCounter() >= STEPS_WARNING) {
                    SoundController.getSound().play(SoundController.SND_TING);
                }
            }
        }

        return newType;
    }

}
