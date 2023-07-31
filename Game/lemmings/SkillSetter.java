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
import game.level.Mask;
import game.level.Stencil;

/**
 * Utility for setting the skill of an individual Lemming. Code moved from
 * Lemming by John Watne 07/2023.
 */
public final class SkillSetter {
    /**
     * Private default constructor for utility class.
     */
    private SkillSetter() {

    }

    /**
     * Set new skill/type of specified Lemming.
     *
     * @param skill   new skill/type
     * @param lemming the Lemming whose skill is to be set.
     * @return true if a change was possible, false otherwise
     */
    public static boolean setSkill(final Type skill, final Lemming lemming) {
        final Type type = lemming.getSkill();

        if (skill == type || lemming.hasDied()) {
            return false;
        }

        final Bomber bomber = lemming.getBomber();

        // check types which can't even get an additional skill anymore
        switch (type) {
        case DROWNING:
        case EXITING:
        case SPLAT:
        case TRAPPED:
        case BOMBER:
            if (skill == Type.NUKE) {
                if (lemming.nuke()) {
                    return false;
                }

                lemming.setNuke(true);
                return bomber.canChangeToBomber();
            }

            return false;
        default:
            break;
        }

        // check additional skills
        switch (skill) {
        case CLIMBER:
            if (lemming.canClimb()) {
                return false;
            }

            lemming.setCanClimb(true);
            return true;
        case FLOATER:
            if (lemming.canFloat()) {
                return false;
            }

            lemming.setCanFloat(true);
            return true;
        case NUKE: // special case: nuke request
            if (lemming.nuke()) {
                return false;
            }

            lemming.setNuke(true);
            //$FALL-THROUGH$
        case BOMBER:
            return bomber.canChangeToBomber();
        default:
            break;
        }

        // check main skills
        if (lemming.canChangeSkill()) {
            switch (skill) {
            case DIGGER:
                if (lemming.canDig()) {
                    // y += DIGGER_GND_OFFSET;
                    lemming.changeType(type, skill);
                    lemming.setCounter(0);
                    return true;
                } else {
                    return false;
                }
            case MINER:
                if (lemming.canMine()) {
                    lemming.changeType(type, skill);
                    lemming.setCounter(0);
                    return true;
                } else {
                    return false;
                }
            case BASHER:
                lemming.changeType(type, skill);
                lemming.setCounter(0);
                return true;
            case BUILDER:
                final int fb = lemming.freeBelow(Faller.FALLER_STEP);

                if (fb != 0) {
                    return false;
                }

                lemming.changeType(type, skill);
                lemming.setCounter(0);
                return true;
            case STOPPER:
                final Mask m = Lemming.getResource(Type.STOPPER)
                        .getMask(Direction.LEFT);
                final int maskX = lemming.screenX();
                lemming.setMaskX(maskX);
                final int maskY = lemming.screenY();
                lemming.setMaskY(maskY);

                if (m.checkType(maskX, maskY, 0, Stencil.MSK_STOPPER)) {
                    return false; // overlaps existing stopper
                }

                lemming.changeType(type, skill);
                lemming.setCounter(0);
                // set stopper mask
                m.setStopperMask(maskX, maskY, lemming.getX());
                return true;
            default:
                break;
            }
        }

        return false;
    }

}
