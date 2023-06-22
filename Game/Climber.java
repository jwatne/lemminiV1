package game;
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

/**
 * Class for handling Lemming Climber skill. Code moved from Lemming by John
 * Watne 06/2023.
 */
public class Climber {
    /** a climber climbs up 1 pixel every 2nd frame. */
    private static final int CLIMBER_STEP = 1;
    /**
     * The Lemming owning this instance of Climber.
     */
    private final Lemming lemming;

    /**
     * Constructs a Climber for the owning Lemming.
     *
     * @param owner the Lemming owning the instance constructed.
     */
    public Climber(final Lemming owner) {
        this.lemming = owner;
    }

    /**
     * Animates climber.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @param explode         <code>true</code> if the Lemming is to explode.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type animateClimber(final Type startingNewType,
            final boolean explode) {
        Type newType = startingNewType;

        if (explode) {
            lemming.explode();
        } else {
            int counter = lemming.getCounter();

            if ((++counter & 1) == 1) { // only every other step
                lemming.setY(lemming.getY() - CLIMBER_STEP);
            }

            lemming.setCounter(counter);

            if (lemming.midY() < 0 || lemming.freeAbove(2) < 2) {
                lemming.setDirection((lemming.getDirection() == Direction.RIGHT)
                        ? Direction.LEFT
                        : Direction.RIGHT);
                newType = Type.FALLER;
                counter = 0;
            } else if (reachedPlateau()) {
                counter = 0;
                newType = Type.CLIMBER_TO_WALKER;
            }
        }

        return newType;
    }

    /**
     * Check if climber reached a plateau he can walk on.
     *
     * @return true if climber reached a plateau he can walk on, false otherwise
     */
    private boolean reachedPlateau() {
        final int x = lemming.getX();

        if (x < 2 || x >= Level.WIDTH - 2) {
            return false;
        }

        final int ym = lemming.midY();

        if (ym >= Level.HEIGHT || ym < 0) {
            return false;
        }

        int pos = x;

        if (lemming.getDirection() == Direction.LEFT) {
            pos -= 2;
        } else {
            pos += 2;
        }

        pos += ym * Level.WIDTH;
        return (GameController.getStencil().get(pos)
                & Stencil.MSK_WALK_ON) == Stencil.MSK_EMPTY;
    }

}
