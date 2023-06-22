package game.lemmings;
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

import game.Type;

/**
 * Class for handling walking Lemmings. Moved from Lemming by John Watne
 * 06/2023.
 */
public class Walker {
    /** at this height a walker will turn around. */
    public static final int WALKER_OBSTACLE_HEIGHT = 14;
    /** a walker walks one pixel per frame. */
    private static final int WALKER_STEP = 1;
    /** if a walker jumps up 6 pixels, it becomes a jumper. */
    private static final int JUMPER_JUMP = 4;
    /**
     * The Lemming owning this Walker instance.
     */
    private Lemming lemming;

    /**
     * Creates a Walker instance for the owning Lemming.
     *
     * @param owner the Lemming owning the constructed instance.
     */
    public Walker(final Lemming owner) {
        this.lemming = owner;
    }

    /**
     * Animates walker.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @param oldX            the old X value of the foot in pixels.
     * @param explode         <code>true</code> if the Lemming is to explode.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type animateWalker(final Type startingNewType, final int oldX,
            final boolean explode) {
        Type newType = startingNewType;
        int free;

        if (explode) {
            newType = Type.BOMBER;
            lemming.playOhNoIfNotToBeNuked();
        } else if (!lemming.turnedByStopper()) {
            if (lemming.getDirection() == Direction.RIGHT) {
                lemming.setX(lemming.getX() + WALKER_STEP);
            } else if (lemming.getDirection() == Direction.LEFT) {
                lemming.setX(lemming.getX() - WALKER_STEP);
            }

            boolean doBreak = false;

            // check
            free = lemming.freeBelow(Faller.FALL_DISTANCE_FALL);

            if (free >= Faller.FALL_DISTANCE_FALL) {
                lemming.setY(lemming.getY() + Faller.FALLER_STEP);
            } else {
                lemming.setY(lemming.getY() + free);
                lemming.setCounter(free);
            }

            final int levitation = lemming.aboveGround();

            // check for flip direction
            if (levitation < WALKER_OBSTACLE_HEIGHT && (lemming.getY()
                    + lemming.getLemRes().getHeight() / 2) > 0) {
                if (levitation >= JUMPER_JUMP) {
                    lemming.setY(lemming.getY() - Lemming.JUMPER_STEP);
                    newType = Type.JUMPER;
                    doBreak = true; // Stop processing after enclosing if/else.
                } else {
                    lemming.setY(lemming.getY() - levitation);
                }
            } else {
                lemming.setX(oldX);

                if (lemming.canClimb()) {
                    newType = Type.CLIMBER;
                    doBreak = true; // Stop processing after enclosing if/else.
                } else {
                    lemming.setDirection(
                            (lemming.getDirection() == Direction.RIGHT)
                                    ? Direction.LEFT
                                    : Direction.RIGHT);
                }
            }

            if (!doBreak && (free > 0)) {
                // check for conversion to faller
                lemming.setCounter(lemming.getCounter() + Faller.FALLER_STEP);
                // @check: is this ok? increasing
                // counter, but using free???

                if (free >= Faller.FALL_DISTANCE_FALL) {
                    newType = Type.FALLER;
                }
            }
        }

        return newType;
    }
}
