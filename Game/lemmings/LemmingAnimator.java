package game.lemmings;

import game.GameController;
import game.SoundController;
import game.Type;
import game.level.Level;
import game.level.Mask;
import game.level.SpriteObjectHandler;
import game.level.Stencil;
import lemmini.Constants;
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
 * Class for animating Lemmings. Code moved from Lemming by John Watne 07/2023.
 */
public final class LemmingAnimator {
    /**
     * Amount to deduct from y value for climber to walker transition.
     */
    private static final int CLIMBER_TO_WALKER_Y_OFFSET = 10;
    /**
     * Number of frames to rewind for floater.
     */
    private static final int FLOATER_REWIND = 5;
    /**
     * -6 constant.
     */
    private static final int NEGATIVE_SIX = -6;

    /**
     * Private default constructor for utility class.
     */
    private LemmingAnimator() {

    }

    /**
     * Update animation, move Lemming, check state transitions.
     *
     * @param lemming the Lemming to be animated.
     */
    public static void animate(final Lemming lemming) {
        final Type type = lemming.getSkill();
        final Type oldType = type;
        Type newType = type;
        final int oldX = lemming.getX();
        final boolean explode = lemming.getExploder().checkExplodeState();
        int selectCtr = lemming.getSelectCtr();

        if (selectCtr > 0) {
            lemming.setSelectCtr(--selectCtr);
        }

        flipDirBorder(lemming);
        // lemming state machine
        final LemmingStateMachine machine = new LemmingStateMachine(lemming);
        newType = machine.executeLemmingStateMachine(newType, oldX, explode);

        // check collision with exit and traps
        newType = processTrapMasks(newType, lemming);

        // animate
        if (oldType == newType) {
            final boolean trigger = animateLoopOrOnce(lemming);
            newType = animateIfTriggerConditionReached(newType, trigger,
                    lemming);
        }

        lemming.changeType(oldType, newType);
    }

    /**
     * Perform Type-specific animation for the Lemming.
     *
     * @param type    the Type of the Lemming.
     * @param lemming the Lemming to animate.
     * @return the updated Type of the Lemming.
     */
    public static Type animateLemming(final Type type, final Lemming lemming) {
        Type newType = type;

        switch (type) {
        case BOMBER_STOPPER:
            final LemmingResource[] lemmings = Lemming.getLemmings();
            final int maskX = lemming.getMaskX();
            final int maskY = lemming.getMaskY();
            final Direction dir = lemming.getDirection();
            final Mask m = lemmings[Type.getOrdinal(Type.STOPPER)].getMask(dir);
            m.clearType(maskX, maskY, 0, Stencil.MSK_STOPPER);
            //$FALL-THROUGH$
        case BOMBER:
            lemming.getBomber().explode(type);
            break;
        case SPLAT:
        case DROWNING:
        case TRAPPED:
            lemming.setHasDied(true);
            break;
        case EXITING:
            lemming.setHasLeft(true);
            GameController.increaseLeft();
            break;
        case FLOATER_START:
            lemming.setType(Type.FLOATER); // should never happen
            //$FALL-THROUGH$
        case FLOATER:
            final int frameIdx = lemming.getFrameIdx();
            lemming.setFrameIdx(frameIdx - FLOATER_REWIND * Lemming.TIME_SCALE);
            // rewind 5 frames
            break;
        case CLIMBER_TO_WALKER:
            newType = Type.WALKER;
            final int y = lemming.getY();
            lemming.setY(y - CLIMBER_TO_WALKER_Y_OFFSET);
            // why is this needed? could be done via foot
            // coordinates?
            break;
        case DIGGER:
            // the dig mask must be applied to the bottom of the lemming
            newType = applyDigMaskToBottomOfLemming(newType, lemming);
            break;
        case BUILDER_END:
            newType = Type.WALKER;
            break;
        default:
            break;
        }

        return newType;
    }

    /**
     * Applies dig mask to bottom of Lemming.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @param lemming
     * @return the updated new Type to be assigned to the Lemming.
     */
    private static Type applyDigMaskToBottomOfLemming(
            final Type startingNewType, final Lemming lemming) {
        Type newType = startingNewType;
        int free;
        final LemmingResource lemRes = lemming.getLemRes();
        final Direction dir = lemming.getDirection();
        final Mask m = lemRes.getMask(dir);
        final int sx = lemming.screenX();
        final int sy = lemming.screenY();
        m.eraseMask(sx, sy, 0, Stencil.MSK_STEEL);
        int y = lemming.getY();

        // check for conversion to walker when hitting steel
        if (lemRes.getImask(dir).checkType(sx, sy, 0, Stencil.MSK_STEEL)) {
            SoundController.playLastFewStepsSound();
            newType = Type.WALKER;
        } else {
            lemming.setY(y + 2); // move down
        }

        // check for conversion to faller
        int freeMin = Integer.MAX_VALUE;
        free = 0;
        final int xOld = lemming.getX();
        int x = xOld;

        for (int i = NEGATIVE_SIX; i < Constants.SIX; i++) {
            // should be 14 pixels, here it's more like 12
            x = xOld + i;

            if (x < 0) {
                x = 0;
            } else if (x >= Level.WIDTH) {
                x = Level.WIDTH;
            }

            free = lemming.freeBelow(Floater.FLOATER_STEP);

            if (free < freeMin) {
                freeMin = free;
            }
        }

        x = xOld;
        lemming.setX(x);
        free = freeMin;

        if (free > 0) {
            // convert to faller or walker
            newType = Type.FALLER;

            if (free >= Faller.FALLER_STEP) {
                y += Faller.FALLER_STEP;
            } else {
                y += free;
            }

            lemming.setY(y);
        }

        return newType;
    }

    /**
     * Animates the Lemming's current type if the trigger condition is reached.
     *
     * @param initialNewType the original new Type to be assigned to the Lemming
     *                       before the call to this method.
     * @param trigger        <code>true</code> if trigger condition reached.
     * @param lemming        the Lemming to animate.
     * @return the updated new Type to be assigned to the Lemming.
     */
    private static Type animateIfTriggerConditionReached(
            final Type initialNewType, final boolean trigger,
            final Lemming lemming) {
        Type newType = initialNewType;

        if (trigger) {
            // Trigger condition reached?
            newType = LemmingAnimator.animateLemming(initialNewType, lemming);
        }

        return newType;
    }

    /**
     * Performs the proper animation for a LOOP or ONCE animation mode.
     *
     * @param lemming the Lemming to be animated.
     *
     * @return <code>true</code> if the trigger condition is reached by
     *         executing the animation.
     */
    private static boolean animateLoopOrOnce(final Lemming lemming) {
        boolean trigger = false;

        switch (lemming.getLemRes().getAnimMode()) {
        case LOOP:
            trigger = animateLoop(trigger, lemming);
            break;
        case ONCE:
            trigger = animateOnce(trigger, lemming);
            break;
        default:
            break;
        }

        return trigger;
    }

    /**
     * Perform special animations if the {@link Stencil} for the pixel in the
     * middle of the Lemming indicates a trap or level exit.
     *
     * @param initialNewType the original new Type to be assigned to the Lemming
     *                       before the call to this method.
     * @param lemming        the Lemming to be animated.
     * @return the updated new Type to be assigned to the Lemming.
     */
    private static Type processTrapMasks(final Type initialNewType,
            final Lemming lemming) {
        Type newType = initialNewType;
        final int s = lemming.stencilMid();
        final SpriteObjectHandler spriteObjectHandler = new SpriteObjectHandler(
                lemming);

        switch (s & (Stencil.MSK_TRAP | Stencil.MSK_EXIT)) {
        case Stencil.MSK_TRAP_DROWN:
            newType = spriteObjectHandler.animateDrowning(newType, s);
            break;
        case Stencil.MSK_TRAP_DIE:
            newType = spriteObjectHandler.animateNormalDeath(newType, s);
            break;
        case Stencil.MSK_TRAP_REPLACE:
            final boolean hasDied = lemming.hasDied();
            lemming.setHasDied(spriteObjectHandler
                    .replaceLemmingWithSpecialDeathAnimation(s, hasDied));
            break;
        case Stencil.MSK_EXIT:
            newType = spriteObjectHandler.animateExitLevel(newType, s,
                    lemming.getSkill());
            break;
        default:
            break;
        }

        return newType;
    }

    /**
     * Animates once.
     *
     * @param startingTrigger the initial value of the trigger before calling
     *                        this method.
     * @param lemming         the Lemming to be animated.
     * @return the updated value of the trigger.
     */
    private static boolean animateOnce(final boolean startingTrigger,
            final Lemming lemming) {
        boolean trigger = startingTrigger;
        int frameIdx = lemming.getFrameIdx();

        if (frameIdx < lemming.getLemRes().getFrames() * Lemming.TIME_SCALE
                - 1) {
            lemming.setFrameIdx(++frameIdx);
        } else {
            trigger = true;
        }

        return trigger;
    }

    /**
     * Animates loop.
     *
     * @param startingTrigger the initial value of the trigger before calling
     *                        this method.
     * @param lemming         the Lemming to be animated.
     * @return the updated value of the trigger.
     */
    private static boolean animateLoop(final boolean startingTrigger,
            final Lemming lemming) {
        boolean trigger = startingTrigger;
        int frameIdx = lemming.getFrameIdx();
        final LemmingResource lemRes = lemming.getLemRes();

        if (++frameIdx >= lemRes.getFrames() * Lemming.TIME_SCALE) {
            frameIdx = 0;
        }

        lemming.setFrameIdx(frameIdx);
        final int maskStep = lemRes.getMaskStep();

        if (maskStep > 0 && frameIdx % (maskStep * Lemming.TIME_SCALE) == 0) {
            trigger = true;
        }

        return trigger;
    }

    /**
     * Check if Lemming reached the left or right border of the level and was
     * turned.
     *
     * @param lemming
     *
     * @return true if lemming was turned, false otherwise.
     */
    private static boolean flipDirBorder(final Lemming lemming) {
        boolean flip = false;

        if (lemming.getLemRes().getDirs() > 1) {
            int x = lemming.getX();

            if (x < 0) {
                x = 0;
                flip = true;
            } else if (x >= Level.WIDTH) {
                x = Level.WIDTH - 1;
                flip = true;
            }

            lemming.setX(x);
        }

        if (flip) {
            lemming.setDirection(
                    (lemming.getDirection() == Direction.RIGHT) ? Direction.LEFT
                            : Direction.RIGHT);
        }

        return flip;
    }

}
