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
 * Class for handling floater skill, if assigned to parent Lemming. Code moved
 * from Lemming class by John Watne 06/2023.
 */
public class Floater {
    /** a floater falls down two pixels per frame. */
    public static final int FLOATER_STEP = 2;
    /**
     * 7 constant.
     */
    private static final int SEVEN = 7;
    /**
     * 4 constant.
     */
    private static final int FOUR = 4;
    /**
     * 3 constant.
     */
    private static final int THREE = 3;
    /**
     * 5 constant.
     */
    private static final int FIVE = 5;
    /**
     * 6 constant.
     */
    private static final int SIX = 6;
    /**
     * The lemming owning the instance of this class.
     */
    private Lemming lemming;

    /**
     * Constructs a Floater for the Lemming owning this instance of the class.
     *
     * @param owner the owner of this instance of the class.
     */
    public Floater(final Lemming owner) {
        this.lemming = owner;
    }

    /**
     * Animates floater.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @param explode         <code>true</code> if the Lemming is to explode.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type animateFloater(final Type startingNewType,
            final boolean explode) {
        Type newType = startingNewType;
        int free;

        if (explode) {
            lemming.explode();
        } else {
            free = lemming.freeBelow(FLOATER_STEP);

            if (free == Faller.FALL_DISTANCE_FORCE_FALL) {
                lemming.setY(lemming.getY() + FLOATER_STEP);
            } else {
                lemming.setY(lemming.getY() + free); // max: FLOATER_STEP
            }

            if (!lemming.getFaller().crossedLowerBorder()) {
                lemming.setCounter(lemming.getCounter() + free); // fall counter

                // check ground hit
                if (free == 0) {
                    newType = Type.WALKER;
                    lemming.setCounter(0);
                }
            }
        }

        return newType;
    }

    /**
     * Starts animating floater.
     *
     * @param explode <code>true</code> if the Lemming is to explode.
     */
    public void animateFloaterStart(final boolean explode) {
        if (explode) {
            lemming.explode();
        } else {
            int counter2 = lemming.getCounter2();

            switch (counter2++) {
            case 0:
            case 1: // keep falling with faller speed
            case 2:
                lemming.setY(
                        lemming.getY() + Faller.FALLER_STEP - FLOATER_STEP);
                break;
            case THREE:
                lemming.setY(lemming.getY() - (FLOATER_STEP - 1)); // decelerate
                                                                   // a little
                break;
            case FOUR:
            case FIVE:
            case SIX:
            case SEVEN:
                lemming.setY(lemming.getY() - FLOATER_STEP); // decelerate some
                                                             // more
                break;
            default:
                lemming.setType(Type.FLOATER);
            }

            lemming.setCounter2(counter2);
        }
    }

}
