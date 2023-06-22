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
import game.Level;
import game.SoundController;
import game.Type;

/**
 * Class for handling faller (default) Lemmings. Code moved from Lemming class
 * by John Watne 06/2023.
 */
public class Faller {
    /** pixels a floater falls before the parachute begins to open. */
    public static final int FALL_DISTANCE_FLOAT = 32;
    /** number of free pixels below needed to convert a lemming to a faller. */
    public static final int FALL_DISTANCE_FALL = 8;
    /** used as "free below" value to convert most skills into a faller. */
    public static final int FALL_DISTANCE_FORCE_FALL = 2 * FALL_DISTANCE_FALL;
    /** a faller falls down three pixels per frame. */
    public static final int FALLER_STEP = 3;
    /**
     * The Lemming owning the implementation of the class.
     */
    private Lemming lemming;

    /**
     * Initializes the Faller for the Lemming owning the instance.
     *
     * @param owner the Lemming owning the instance.
     */
    public Faller(final Lemming owner) {
        this.lemming = owner;
    }

    /**
     * Animates faller.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type animateFaller(final Type startingNewType) {
        Type newType = startingNewType;
        int free;
        free = lemming.freeBelow(FALLER_STEP);

        if (free == FALL_DISTANCE_FORCE_FALL) {
            lemming.setY(lemming.getY() + FALLER_STEP);
        } else {
            lemming.setY(lemming.getY() + free); // max: FALLER_STEP
        }

        if (!crossedLowerBorder()) {
            lemming.setCounter(lemming.getCounter() + free); // fall counter

            // check conversion to floater
            if (lemming.canFloat()
                    && lemming.getCounter() >= FALL_DISTANCE_FLOAT) {
                newType = Type.FLOATER_START;
                lemming.setCounter2(0); // used for parachute opening "jump" up
            } else if (free == 0) { // check ground hit
                // System.out.println(counter);
                if (lemming.getCounter() > GameController.getLevel()
                        .getMaxFallDistance()) {
                    newType = Type.SPLAT;
                } else {
                    newType = Type.WALKER;
                    lemming.setCounter(0);
                }
            }
        }

        return newType;
    }

    /**
     * Check if Lemming has fallen to/through the bottom of the level.
     *
     * @return true if Lemming has fallen to/through the bottom of the level,
     *         false otherwise
     */
    public boolean crossedLowerBorder() {
        if (lemming.getY() >= Level.HEIGHT) {
            lemming.setHasDied(true);
            SoundController.getSound().play(SoundController.SND_DIE);
            return true;
        }

        return false;
    }

}
