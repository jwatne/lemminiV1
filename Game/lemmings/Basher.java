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

import game.GameController;
import game.SoundController;
import game.Type;
import game.level.Level;
import game.level.Mask;
import game.level.Stencil;
import lemmini.Constants;

/**
 * Class for handling Basher role, if assigned to owning Lemming. Code moved
 * from Lemming by John Watne 06/2023.
 */
public class Basher {
    /** check N pixels above the lemming's feet. */
    private static final int BASHER_CHECK_STEP = 12;
    /**
     * 25 constant.
     */
    private static final int TWENTY_FIVE = 25;
    /**
     * 16 constant.
     */
    private static final int SIXTEEN = 16;
    /**
     * 30 constant.
     */
    private static final int THIRTY = 30;
    /**
     * 29 constant.
     */
    private static final int TWENTY_NINE = 29;
    /**
     * 28 constant.
     */
    private static final int TWENTY_EIGHT = 28;
    /**
     * 27 constant.
     */
    private static final int TWENTY_SEVEN = 27;
    /**
     * 26 constant.
     */
    private static final int TWENTY_SIX = 26;
    /**
     * Constant 14.
     */
    private static final int FOURTEEN = 14;
    /**
     * 13 constant.
     */
    private static final int THIRTEEN = 13;
    /**
     * 12 constant.
     */
    private static final int TWELVE = 12;
    /**
     * 11 constant.
     */
    private static final int ELEVEN = 11;
    /**
     * 10 constant.
     */
    private static final int TEN = 10;
    /**
     * 21 constant.
     */
    private static final int TWENTY_ONE = 21;
    /**
     * 19 constant.
     */
    private static final int NINETEEN = 19;
    /**
     * 18 constant.
     */
    private static final int EIGHTEEN = 18;
    /**
     * Lemming owning the instance of this class.
     */
    private final Lemming lemming;

    /**
     * Constructs a Basher instance owned by the specified Lemming.
     *
     * @param owner the Lemming owning the instance of this class.
     */
    public Basher(final Lemming owner) {
        this.lemming = owner;
    }

    /**
     * Animates basher.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @param explode         <code>true</code> if the Lemming is to explode.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type animateBasher(final Type startingNewType,
            final boolean explode) {
        Type newType = startingNewType;
        int free;

        if (explode) {
            newType = Type.BOMBER;
            lemming.playOhNoIfNotToBeNuked();
        } else {
            // check for conversion to faller
            // check collision with stopper
            if (lemming.turnedByStopper()) {
                newType = Type.WALKER;
            } else {
                free = lemming.freeBelow(Floater.FLOATER_STEP);

                if (free == Faller.FALL_DISTANCE_FORCE_FALL) {
                    lemming.setY(lemming.getY() + Faller.FALLER_STEP);
                } else {
                    lemming.setY(lemming.getY() + free);
                }

                if (free != 0) {
                    final int counter = lemming.getCounter() + free;
                    lemming.setCounter(counter);

                    if (counter >= Lemming.BASHER_FALL_DISTANCE) {
                        newType = Type.FALLER;
                    }
                } else {
                    lemming.setCounter(0);
                }

                Mask m;
                int checkMask;
                int idx = lemming.getFrameIdx() + 1;
                final LemmingResource lemRes = lemming.getLemRes();

                if (idx >= lemRes.getFrames() * Lemming.TIME_SCALE) {
                    idx = 0;
                }

                int sx = 0;
                int sy = 0;
                final Direction dir = lemming.getDirection();

                switch (idx) {
                case 2 * Lemming.TIME_SCALE:
                case Faller.FALLER_STEP * Lemming.TIME_SCALE:
                case Lemming.STEP_PIXELS * Lemming.TIME_SCALE:
                case Constants.FIVE * Lemming.TIME_SCALE:
                    // bash mask should have the same height as the lemming
                    m = lemRes.getMask(dir);
                    sx = lemming.screenX();
                    sy = lemming.screenY();
                    checkMask = Stencil.MSK_STEEL
                            | ((dir == Direction.LEFT) ? Stencil.MSK_NO_DIG_LEFT
                                    : Stencil.MSK_NO_DIG_RIGHT);
                    m.eraseMask(sx, sy, idx / Lemming.TIME_SCALE - 2,
                            checkMask);

                    // check for conversion to walker because there are
                    // indestructible pixels
                    if (lemRes.getImask(dir).checkType(sx, sy, 0, checkMask)) {
                        SoundController.playLastFewStepsSound();
                        newType = Type.WALKER;
                    }

                    if (idx == Constants.FIVE * Lemming.TIME_SCALE) {
                        // check for conversion to walker because there are no
                        // bricks left
                        if (!canBash()) {
                            // no bricks any more
                            newType = Type.WALKER;
                        }
                    }

                    break;
                case EIGHTEEN * Lemming.TIME_SCALE:
                case NINETEEN * Lemming.TIME_SCALE:
                case Lemming.TWENTY * Lemming.TIME_SCALE:
                case TWENTY_ONE * Lemming.TIME_SCALE:
                    // bash mask should have the same height as the lemming
                    m = lemRes.getMask(dir);
                    sx = lemming.screenX();
                    sy = lemming.screenY();
                    checkMask = Stencil.MSK_STEEL
                            | ((dir == Direction.LEFT) ? Stencil.MSK_NO_DIG_LEFT
                                    : Stencil.MSK_NO_DIG_RIGHT);
                    m.eraseMask(sx, sy, idx / Lemming.TIME_SCALE - EIGHTEEN,
                            checkMask);

                    // check for conversion to walker because there are
                    // indestructible pixels
                    if (lemRes.getImask(dir).checkType(sx, sy, 0, checkMask)) {
                        SoundController.playLastFewStepsSound();
                        newType = Type.WALKER;
                    }

                    break;
                case TEN * Lemming.TIME_SCALE:
                case ELEVEN * Lemming.TIME_SCALE:
                case TWELVE * Lemming.TIME_SCALE:
                case THIRTEEN * Lemming.TIME_SCALE:
                case FOURTEEN * Lemming.TIME_SCALE:
                case TWENTY_SIX * Lemming.TIME_SCALE:
                case TWENTY_SEVEN * Lemming.TIME_SCALE:
                case TWENTY_EIGHT * Lemming.TIME_SCALE:
                case TWENTY_NINE * Lemming.TIME_SCALE:
                case THIRTY * Lemming.TIME_SCALE:
                    if (dir == Direction.RIGHT) {
                        lemming.setX(lemming.getX() + 2);
                    } else {
                        lemming.setX(lemming.getX() - 2);
                    }

                    break;
                default:
                    break;
                }
            }
        }

        return newType;
    }

    /**
     * Check if bashing is possible.
     *
     * @return true if bashing is possible, false otherwise.
     */
    private boolean canBash() {
        final int xm = lemming.midX();
        final int ypos = Level.WIDTH * (lemming.getY() - BASHER_CHECK_STEP);
        int xb;
        int bricks = 0;

        for (int i = SIXTEEN; i < TWENTY_FIVE; i++) {
            final Direction dir = lemming.getDirection();

            if (dir == Direction.RIGHT) {
                xb = xm + i;
            } else {
                xb = xm - i;
            }

            final int sval = GameController.getStencil().get(xb + ypos);

            if ((sval & Stencil.MSK_NO_DIG_LEFT) != 0
                    && dir == Direction.LEFT) {
                return false;
            }

            if ((sval & Stencil.MSK_NO_DIG_RIGHT) != 0
                    && dir == Direction.RIGHT) {
                return false;
            }

            if ((sval & Stencil.MSK_STEEL) != 0) {
                return false;
            }

            if ((sval & Stencil.MSK_WALK_ON) == Stencil.MSK_BRICK) {
                bricks++;
            }
        }

        return bricks > 0;
    }

}
