package game;

/**
 * Class for handling Stopper skill for the owning Lemming. Code moved from
 * Lemming by John Watne 06/2023.
 */
public class Stopper {
    /**
     * The Lemming owning the instance of this class.
     */
    private Lemming lemming;

    /**
     * Constructs a Stopper for the specified Lemming.
     *
     * @param owner the Lemming owning the constructed instance.
     */
    public Stopper(final Lemming owner) {
        this.lemming = owner;
    }

    /**
     * Animates stopper.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @param explode         <code>true</code> if the Lemming is to explode.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type animateStopper(final Type startingNewType,
            final boolean explode) {
        Type newType = startingNewType;
        int free;

        if (explode) {
            // don't erase stopper mask!
            newType = Type.BOMBER_STOPPER;
            lemming.playOhNoIfNotToBeNuked();
        } else {
            // check for conversion to faller
            free = lemming.freeBelow(Floater.FLOATER_STEP);

            if (free > 0) {
                if (free == Faller.FALL_DISTANCE_FORCE_FALL) {
                    lemming.setY(lemming.getY() + Faller.FALLER_STEP);
                } else {
                    lemming.setY(lemming.getY() + free);
                }

                int counter = lemming.getCounter() + free;
                lemming.setCounter(counter);

                if (counter >= Faller.FALL_DISTANCE_FALL) {
                    newType = Type.FALLER;
                } else {
                    newType = Type.WALKER;
                }

                // conversion to faller or walker -> erase stopper mask
                final Mask m = Lemming.getLemmings()[Lemming
                        .getOrdinal(Type.STOPPER)]
                                .getMask(lemming.getDirection());
                m.clearType(lemming.getMaskX(), lemming.getMaskY(), 0,
                        Stencil.MSK_STOPPER);
            } else {
                lemming.setCounter(0);
            }
        }

        return newType;
    }

}
