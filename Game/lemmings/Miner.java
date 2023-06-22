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

import game.SoundController;
import game.Type;
import game.level.Mask;
import game.level.Stencil;

/**
 * Class for handling miner skill, if assigned to the owning Lemming.
 */
public class Miner {
    /**
     * 15 constant.
     */
    private static final int FIFTEEN = 15;
    /** from this on a miner will become a faller. */
    private static final int MINER_FALL_DISTANCE = 4;

    /**
     * The Lemming owning the instance of the class.
     */
    private final Lemming lemming;

    /**
     * Creates a Miner instance for the owning Lemming.
     *
     * @param owner the Lemming owning this instance of the class.
     */
    public Miner(final Lemming owner) {
        this.lemming = owner;
    }

    /**
     * Animates miner.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @param explode         <code>true</code> if the Lemming is to explode.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type animateMiner(final Type startingNewType,
            final boolean explode) {
        Type newType = startingNewType;
        int free;

        if (explode) {
            newType = Type.BOMBER;
            lemming.playOhNoIfNotToBeNuked();
        } else if (!lemming.turnedByStopper()) {
            Mask m;
            int sx;
            int sy;
            int idx = lemming.getFrameIdx() + 1;
            final LemmingResource lemRes = lemming.getLemRes();

            if (idx >= lemRes.getFrames() * Lemming.TIME_SCALE) {
                idx = 0;
            }

            final Direction dir = lemming.getDirection();

            switch (idx) {
            case 1 * Lemming.TIME_SCALE:
            case 2 * Lemming.TIME_SCALE:
                // check for steel in mask
                m = lemRes.getMask(dir);
                sx = lemming.screenX();
                sy = lemming.screenY();
                final int checkMask = Stencil.MSK_STEEL
                        | ((dir == Direction.LEFT) ? Stencil.MSK_NO_DIG_LEFT
                                : Stencil.MSK_NO_DIG_RIGHT);
                m.eraseMask(sx, sy, idx / Lemming.TIME_SCALE - 1, checkMask);

                if (lemRes.getImask(dir).checkType(sx, sy, 0, checkMask)) {
                    SoundController.playLastFewStepsSound();
                    newType = Type.WALKER;
                }

                break;
            case Faller.FALLER_STEP * Lemming.TIME_SCALE:
            case FIFTEEN * Lemming.TIME_SCALE:
                if (dir == Direction.RIGHT) {
                    lemming.setX(lemming.getX() + Lemming.STEP_PIXELS);
                } else {
                    lemming.setX(lemming.getX() - Lemming.STEP_PIXELS);
                }

                // check for conversion to faller
                free = lemming.freeBelow(MINER_FALL_DISTANCE);

                if (free >= MINER_FALL_DISTANCE) {
                    if (free == Faller.FALL_DISTANCE_FORCE_FALL) {
                        lemming.setY(lemming.getY() + Faller.FALLER_STEP);
                    } else {
                        lemming.setY(lemming.getY() + free);
                    }

                    newType = Type.FALLER;
                } else if (idx == FIFTEEN * Lemming.TIME_SCALE) {
                    lemming.setY(lemming.getY() + Lemming.STEP_PIXELS);
                }

                break;
            default:
                break;
            }
        }

        return newType;
    }

}
