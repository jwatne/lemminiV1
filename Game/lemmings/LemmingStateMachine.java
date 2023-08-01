package game.lemmings;

import game.Type;
import game.level.Mask;
import game.level.Stencil;

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
 * Utility class for executing Lemming state machine. Code moved from Lemming by
 * John Watne 08/2023.
 */
public class LemmingStateMachine {
    /**
     * The Lemming on which the state machine is executed.
     */
    private final Lemming lemming;
    /**
     * Class for handling builder skill, if assigned to the current Lemming.
     */
    private final Builder builder;
    /**
     * Class for handling floater skill, if assigned to the current Lemming.
     */
    private final Floater floater;
    /**
     * Class for handling climber skill, if assigned to the current Lemming.
     */
    private final Climber climber;
    /**
     * Class for handling walking for the current Lemming.
     */
    private final Walker walker;
    /**
     * Class for handling mining skill for the current Lemming, if assigned.
     */
    private final Miner miner;
    /**
     * Class for handling basher skill for the current Lemming, if assigned.
     */
    private final Basher basher;
    /**
     * Class for handling Stopper skill for the current Lemming, if assigned.
     */
    private final Stopper stopper;

    /**
     * Constructs the state machine for the owning Lemming.
     *
     * @param owner the Lemming for which the state machine is run.
     */
    public LemmingStateMachine(final Lemming owner) {
        this.lemming = owner;
        builder = new Builder(owner);
        floater = new Floater(owner);
        climber = new Climber(owner);
        walker = new Walker(owner);
        miner = new Miner(owner);
        basher = new Basher(owner);
        stopper = new Stopper(owner);
    }

    /**
     * Executes the Lemming state machine.
     *
     * @param initialNewType the original new Type to be assigned to the Lemming
     *                       before the call to this method.
     * @param oldX           the old X value of the foot in pixels.
     * @param explode        <code>true</code> if the Lemming is to explode.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type executeLemmingStateMachine(final Type initialNewType,
            final int oldX, final boolean explode) {
        Type newType = initialNewType;
        final Type type = lemming.getSkill();
        final Bomber bomber = lemming.getBomber();

        switch (type) {
        case FALLER:
            newType = handleFaller(explode, initialNewType);
            break;
        case JUMPER:
            newType = handleJumper(explode, newType);
            break;
        case WALKER:
            newType = walker.animateWalker(newType, oldX, explode);
            break;
        case FLOATER_START, FLOATER:
            newType = handleFloater(explode, newType);
            break;
        case CLIMBER:
            newType = climber.animateClimber(newType, explode);
            break;
        case SPLAT:
            bomber.animateSplat(explode);
            break;
        case BASHER:
            newType = basher.animateBasher(newType, explode);
            break;
        case MINER:
            newType = miner.animateMiner(newType, explode);
            break;
        case DIGGER:
        case BUILDER_END:
            newType = handleDiggerOrBuilderEnd(explode, newType);
            break;
        case BUILDER:
            newType = builder.animateBuilder(newType, oldX, explode);
            break;
        case STOPPER:
            newType = stopper.animateStopper(newType, explode);
            break;
        case BOMBER_STOPPER:
        case BOMBER:
            handleBomberStopperOrBomber();
            break;
        case CLIMBER_TO_WALKER:
        default:
            // Both CLIMBER_TO_WALKER and all cases not explicitly checked above
            // should at least explode
            if (explode) {
                bomber.explode(type);
            }
        }

        return newType;
    }

    /**
     * Handle BOMBER_STOPPER and BOMBER Lemmings.
     */
    private void handleBomberStopperOrBomber() {
        final Type type = lemming.getSkill();
        final Bomber bomber = lemming.getBomber();
        // don't erase stopper mask before stopper finally explodes or falls
        final int free = lemming.freeBelow(Floater.FLOATER_STEP);

        if (free > 0) {
            // stopper falls -> erase mask and convert to normal stopper.
            eraseMaskAndConvertToNormalStopper();

            if (type == Type.BOMBER) {
                bomber.animateBomber();
            }
        }
    }

    /**
     * Handles shared DIGGER / BUILDER_END code.
     *
     * @param explode        <code>true</code> if the Lemming is to explode.
     * @param initialNewType the original new Type to be assigned to the Lemming
     *                       before the call to this method.
     * @return the updated new Type to be assigned to the Lemming.
     */
    private Type handleDiggerOrBuilderEnd(final boolean explode,
            final Type initialNewType) {
        var newType = initialNewType;

        if (explode) {
            newType = Type.BOMBER;
            lemming.playOhNoIfNotToBeNuked();
        }

        return newType;
    }

    /**
     * Handles a FLOATER / FLOATER_START Lemming.
     *
     * @param explode        <code>true</code> if the Lemming is to explode.
     * @param initialNewType the original new Type to be assigned to the Lemming
     *                       before the call to this method.
     * @return the updated new Type to be assigned to the Lemming.
     */
    private Type handleFloater(final boolean explode,
            final Type initialNewType) {
        var newType = initialNewType;
        final Type type = lemming.getSkill();
        floater.animateFloaterStart(explode);

        if (Type.FLOATER == type) {
            newType = floater.animateFloater(newType, explode);
        }

        return newType;
    }

    /**
     * Handles a Jumper Lemming.
     *
     * @param explode        <code>true</code> if the Lemming is to explode.
     * @param initialNewType the original new Type to be assigned to the Lemming
     *                       before the call to this method.
     * @return the updated new Type to be assigned to the Lemming.
     */
    private Type handleJumper(final boolean explode,
            final Type initialNewType) {
        var newType = initialNewType;

        if (explode) {
            newType = Type.BOMBER;
            lemming.playOhNoIfNotToBeNuked();
        } else if (!lemming.turnedByStopper()) {
            newType = animateJumper(newType);
        }

        return newType;
    }

    /**
     * Handles a Faller Lemming.
     *
     * @param explode        <code>true</code> if the Lemming is to explode.
     * @param initialNewType the original new Type to be assigned to the Lemming
     *                       before the call to this method.
     * @return the updated new Type to be assigned to the Lemming.
     */
    private Type handleFaller(final boolean explode,
            final Type initialNewType) {
        final Type type = lemming.getSkill();
        var newType = initialNewType;
        final Bomber bomber = lemming.getBomber();

        if (explode) {
            bomber.explode(type);
        } else {
            newType = lemming.getFaller().animateFaller(newType);
        }

        return newType;
    }

    /**
     * Erases mask and converts to normal stopper.
     */
    private void eraseMaskAndConvertToNormalStopper() {
        final Mask m = Lemming.getLemmings()[Type.getOrdinal(Type.STOPPER)]
                .getMask(lemming.getDirection());
        m.clearType(lemming.getMaskX(), lemming.getMaskY(), 0,
                Stencil.MSK_STOPPER);
        lemming.setType(Type.BOMBER);
    }

    /**
     * Animates jumper.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @return the updated new Type to be assigned to the Lemming.
     */
    private Type animateJumper(final Type startingNewType) {
        Type newType = startingNewType;
        final int levitation = lemming.aboveGround();
        final int y = lemming.getY();

        if (levitation > Lemming.JUMPER_STEP) {
            lemming.setY(y - Lemming.JUMPER_STEP);
        } else {
            // conversion to walker
            lemming.setY(y - levitation);
            newType = Type.WALKER;
        }

        return newType;
    }

}
