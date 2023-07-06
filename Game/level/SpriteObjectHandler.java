package game.level;

import game.GameController;
import game.SoundController;
import game.Type;
import game.lemmings.Direction;
import game.lemmings.Lemming;
import game.lemmings.LemmingResource;

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
 * Handler for a Lemming's SpriteObjects. Code moved from Lemming by John Watne
 * 07/2023.
 */
public class SpriteObjectHandler {
    /**
     * The Lemming owning the instance of this class.
     */
    private Lemming lemming;

    /**
     * Constructs a {@link SpriteObjectHandler} for the specified Lemming.
     *
     * @param owner the Lemming owning the instance of this class.
     */
    public SpriteObjectHandler(final Lemming owner) {
        this.lemming = owner;
    }

    /**
     * Animates exiting the level.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @param s               {@link Stencil} value from the middle of the
     *                        Lemming.
     * @param type            Lemming's skill/type.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type animateExitLevel(final Type startingNewType, final int s,
            final Type type) {
        Type newType = startingNewType;

        switch (type) {
        case WALKER:
        case JUMPER:
        case BASHER:
        case MINER:
        case BUILDER:
        case DIGGER:
            final SpriteObject spr = GameController.getLevel()
                    .getSprObject(Stencil.getObjectID(s));
            newType = Type.EXITING;
            SoundController.getSound().play(spr.getSound());
            break;
        default:
            break;
        }

        return newType;
    }

    /**
     * Replaces the Lemming with the special death animation.
     *
     * @param s           {@link Stencil} value from the middle of the Lemming.
     * @param alreadyDead <code>true</code> if the Lemming has already died.
     * @return <code>true</code> if the Lemming has died.
     */
    public boolean replaceLemmingWithSpecialDeathAnimation(final int s,
            final boolean alreadyDead) {
        boolean hasDied = alreadyDead;
        final SpriteObject spr = GameController.getLevel()
                .getSprObject(Stencil.getObjectID(s));

        if (spr.canBeTriggered()) {
            if (spr.trigger()) {
                SoundController.getSound().play(spr.getSound());
                hasDied = true;
            }
        } else {
            SoundController.getSound().play(spr.getSound());
            hasDied = true;
        }

        eraseStopperMaskForStopperOrBomberStopper();
        return hasDied;
    }

    /**
     * Animates normal death.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @param s               {@link Stencil} value from the middle of the
     *                        Lemming.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type animateNormalDeath(final Type startingNewType, final int s) {
        Type newType = startingNewType;
        final Type type = lemming.getSkill();

        if (type != Type.TRAPPED) {
            final SpriteObject spr = GameController.getLevel()
                    .getSprObject(Stencil.getObjectID(s));

            if (spr.canBeTriggered()) {
                if (spr.trigger()) {
                    SoundController.getSound().play(spr.getSound());
                    newType = Type.TRAPPED;
                }
            } else {
                SoundController.getSound().play(spr.getSound());
                newType = Type.TRAPPED;
            }

            eraseStopperMaskForStopperOrBomberStopper();
        }

        return newType;
    }

    /**
     * Animates drowning.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @param s               {@link Stencil} value from the middle of the
     *                        Lemming.
     * @return the updated new Type to be assigned to the Lemming.
     */
    public Type animateDrowning(final Type startingNewType, final int s) {
        Type newType = startingNewType;

        if (lemming.getSkill() != Type.DROWNING) {
            newType = Type.DROWNING;
            final SpriteObject spr = GameController.getLevel()
                    .getSprObject(Stencil.getObjectID(s));
            SoundController.getSound().play(spr.getSound());
        }

        return newType;
    }

    /**
     * Erase the stopper mask if the owning Lemming is of type STOPPER or
     * BOMBER_STOPPER.
     */
    private void eraseStopperMaskForStopperOrBomberStopper() {
        final Type type = lemming.getSkill();

        if (type == Type.STOPPER || type == Type.BOMBER_STOPPER) {
            final LemmingResource[] lemmings = Lemming.getLemmings();
            final Direction dir = lemming.getDirection();
            // erase stopper mask
            final Mask m = lemmings[Lemming.getOrdinal(Type.STOPPER)]
                    .getMask(dir);
            m.clearType(lemming.getMaskX(), lemming.getMaskY(), 0,
                    Stencil.MSK_STOPPER);
        }
    }

}
