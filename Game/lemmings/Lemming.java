package game.lemmings;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import game.Core;
import game.ExplosionHandler;
import game.GameController;
import game.LemmingExplosion;
import game.MiscGfx;
import game.ResourceException;
import game.SoundController;
import game.Type;
import game.level.Level;
import game.level.Mask;
import game.level.SpriteObjectHandler;
import game.level.Stencil;
import lemmini.Constants;
import tools.Props;
import tools.ToolBox;

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
 * Implements a Lemming.
 *
 * @author Volker Oth
 */
public class Lemming {
    /**
     * 20 constant.
     */
    public static final int TWENTY = 20;
    /**
     * Number of pixels to add/subtract when stepping.
     */
    public static final int STEP_PIXELS = 4;
    /**
     * -6 constant.
     */
    private static final int NEGATIVE_SIX = -6;
    /**
     * Amount to deduct from y value for climber to walker transition.
     */
    private static final int CLIMBER_TO_WALKER_Y_OFFSET = 10;
    /**
     * Number of frames to rewind for floater.
     */
    private static final int FLOATER_REWIND = 5;
    /** name of the configuration file. */
    private static final String LEMM_INI_STR = "misc/lemming.ini";
    /** number of resources (animations/names). */
    private static final int NUM_RESOURCES = 17;

    /**
     * display string for skills/types. order must be the same as in the enum!.
     */
    private static final String[] LEMM_NAMES = {"WALKER", "FALLER", "CLIMBER",
            "CLIMBER", "FLOATER", "", "BLOCKER", "DROWNING", "", "", "BOMBER",
            "BUILDER", "BUILDER", "DIGGER", "BASHER", "MINER", "WALKER"};

    /** from this on a basher will become a faller. */
    public static final int BASHER_FALL_DISTANCE = 6;
    /** a jumper moves up two pixels per frame. */
    public static final int JUMPER_STEP = 2;
    /** Lemmini runs with 50fps instead of 25fps. */
    public static final int TIME_SCALE = 2;
    /** resource (animation etc.) for the current Lemming. */
    private LemmingResource lemRes;

    /**
     * Returns resource (animation etc.) for the current Lemming.
     *
     * @return resource (animation etc.) for the current Lemming.
     */
    public final LemmingResource getLemRes() {
        return lemRes;
    }

    /**
     * Sets resource (animation etc.) for the current Lemming.
     *
     * @param resource resource (animation etc.) for the current Lemming.
     */
    public final void setLemRes(final LemmingResource resource) {
        this.lemRes = resource;
    }

    /** animation frame. */
    private int frameIdx;

    /**
     * Returns animation frame.
     *
     * @return animation frame.
     */
    public final int getFrameIdx() {
        return frameIdx;
    }

    /**
     * Sets animation frame.
     *
     * @param frame animation frame.
     */
    public final void setFrameIdx(final int frame) {
        this.frameIdx = frame;
    }

    /** x coordinate of foot in pixels. */
    private int x;

    /**
     * Returns x coordinate of foot in pixels.
     *
     * @return x coordinate of foot in pixels.
     */
    public final int getX() {
        return x;
    }

    /**
     * Sets x coordinate of foot in pixels.
     *
     * @param xCoordinate x coordinate of foot in pixels.
     */
    public final void setX(final int xCoordinate) {
        this.x = xCoordinate;
    }

    /** y coordinate of foot in pixels. */
    private int y;

    /**
     * Returns y coordinate of foot in pixels.
     *
     * @return y coordinate of foot in pixels.
     */
    public final int getY() {
        return y;
    }

    /**
     * Sets y coordinate of foot in pixels.
     *
     * @param yCoordinate y coordinate of foot in pixels.
     */
    public final void setY(final int yCoordinate) {
        this.y = yCoordinate;
    }

    /** x coordinate for mask in pixels. */
    private int maskX;

    /**
     * Returns x coordinate for mask in pixels.
     *
     * @return x coordinate for mask in pixels.
     */
    public int getMaskX() {
        return maskX;
    }

    /**
     * Sets x coordinate for mask in pixels.
     *
     * @param maskXCoordinate x coordinate for mask in pixels.
     */
    public void setMaskX(final int maskXCoordinate) {
        this.maskX = maskXCoordinate;
    }

    /** y coordinate for mask in pixels. */
    private int maskY;

    /**
     * Returns y coordinate for mask in pixels.
     *
     * @return y coordinate for mask in pixels.
     */
    public int getMaskY() {
        return maskY;
    }

    /**
     * Sets y coordinate for mask in pixels.
     *
     * @param maskYCoordinate y coordinate for mask in pixels.
     */
    public void setMaskY(final int maskYCoordinate) {
        this.maskY = maskYCoordinate;
    }

    /** Lemming's heading. */
    private Direction dir;
    /** Lemming's skill/type. */
    private Type type;

    /**
     * Set the Lemming's skill/type.
     *
     * @param skill the new Lemming's skill/type.
     */
    public final void setType(final Type skill) {
        this.type = skill;
    }

    /** counter used for internal state changes. */
    private int counter;

    /**
     * Returns counter used for internal state changes.
     *
     * @return counter used for internal state changes.
     */
    public final int getCounter() {
        return counter;
    }

    /**
     * Sets counter used for internal state changes.
     *
     * @param stateChangeCounter counter used for internal state changes.
     */
    public final void setCounter(final int stateChangeCounter) {
        this.counter = stateChangeCounter;
    }

    /** another counter used for internal state changes. */
    private int counter2;

    /**
     * Returns another counter used for internal state changes.
     *
     * @return another counter used for internal state changes.
     */
    public final int getCounter2() {
        return counter2;
    }

    /**
     * Sets another counter used for internal state changes.
     *
     * @param anotherCounter another counter used for internal state changes.
     */
    public final void setCounter2(final int anotherCounter) {
        this.counter2 = anotherCounter;
    }

    /** Lemming can float. */
    private boolean canFloat;
    /** Lemming can climb. */
    private boolean canClimb;
    /** Lemming can change its skill. */
    private boolean canChangeSkill;
    /** Lemming is to be nuked. */
    private boolean nuke;
    /** Lemming has died. */
    private boolean hasDied;

    /**
     * Set whether Lemming has died.
     *
     * @param dead <code>true</code> if Lemming has died.
     */
    public final void setHasDied(final boolean dead) {
        this.hasDied = dead;
    }

    /** Lemming has left the level. */
    private boolean hasLeft;
    /** counter used to display the select image in replay mode. */
    private int selectCtr;

    /** static array of resources for each Lemming skill/type. */
    private static LemmingResource[] lemmings;

    /**
     * Returns static array of resources for each Lemming skill/type.
     *
     * @return static array of resources for each Lemming skill/type.
     */
    public static LemmingResource[] getLemmings() {
        return lemmings;
    }

    /** font used for the explosion counter. */
    private static ExplodeFont explodeFont;
    /**
     * Class for handling explosions, if any, for the current Lemming.
     */
    private final LemmingExplosion exploder;
    /**
     * Class for handling builder skill, if assigned to the current Lemming.
     */
    private final Builder builder;
    /**
     * Class for handling falling for current Lemming.
     */
    private final Faller faller;

    /**
     * Returns the object handling falling for the current Lemming.
     *
     * @return the object handling falling for the current Lemming.
     */
    public Faller getFaller() {
        return faller;
    }

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
    private Stopper stopper;
    /**
     * Handler for <code>this</code> Lemming's SpriteObjects.
     */
    private SpriteObjectHandler spriteObjectHandler;

    /**
     * Constructor: Create Lemming.
     *
     * @param sx x coordinate of foot
     * @param sy y coordinate of foot
     */
    public Lemming(final int sx, final int sy) {
        frameIdx = 0;
        type = Type.FALLER; // always start with a faller
        lemRes = lemmings[Type.getOrdinal(type)];
        counter = 0;
        selectCtr = 0;
        dir = Direction.RIGHT; // always start walking to the right
        x = sx;
        y = sy;
        // insideStopper = false;
        canFloat = false; // new lemming can't float
        canClimb = false; // new lemming can't climb
        canChangeSkill = false; // a faller can not change the skill to e.g.
                                // builder
        hasDied = false; // not yet
        hasLeft = false; // not yet
        nuke = false;
        exploder = new LemmingExplosion();
        builder = new Builder(this);
        faller = new Faller(this);
        floater = new Floater(this);
        climber = new Climber(this);
        walker = new Walker(this);
        miner = new Miner(this);
        basher = new Basher(this);
        stopper = new Stopper(this);
        spriteObjectHandler = new SpriteObjectHandler(this);
    }

    /**
     * Update animation, move Lemming, check state transitions.
     */
    public void animate() {
        final Type oldType = type;
        Type newType = type;
        final int oldX = x;
        final boolean explode = exploder.checkExplodeState();

        if (selectCtr > 0) {
            selectCtr--;
        }

        flipDirBorder();

        // lemming state machine
        newType = executeLemmingStateMachine(newType, oldX, explode);

        // check collision with exit and traps
        newType = processTrapMasks(newType);

        // animate
        if (oldType == newType) {
            final boolean trigger = animateLoopOrOnce();
            newType = animateIfTriggerConditionReached(newType, trigger);
        }

        changeType(oldType, newType);
    }

    /**
     * Animates the Lemming's current type if the trigger condition is reached.
     *
     * @param initialNewType the original new Type to be assigned to the Lemming
     *                       before the call to this method.
     * @param trigger        <code>true</code> if trigger condition reached.
     * @return the updated new Type to be assigned to the Lemming.
     */
    private Type animateIfTriggerConditionReached(final Type initialNewType,
            final boolean trigger) {
        Type newType = initialNewType;

        if (trigger) {
            // Trigger condition reached?
            switch (type) {
            case BOMBER_STOPPER:

                final Mask m = lemmings[Type.getOrdinal(Type.STOPPER)]
                        .getMask(dir);
                m.clearType(maskX, maskY, 0, Stencil.MSK_STOPPER);
                //$FALL-THROUGH$
            case BOMBER:
                explode();
                break;
            case SPLAT:
            case DROWNING:
            case TRAPPED:
                hasDied = true;
                break;
            case EXITING:
                hasLeft = true;
                GameController.increaseLeft();
                break;
            case FLOATER_START:
                type = Type.FLOATER; // should never happen
                //$FALL-THROUGH$
            case FLOATER:
                frameIdx -= FLOATER_REWIND * TIME_SCALE; // rewind 5 frames
                break;
            case CLIMBER_TO_WALKER:
                newType = Type.WALKER;
                y -= CLIMBER_TO_WALKER_Y_OFFSET; // why is this needed? could be
                                                 // done via foot
                // coordinates?
                break;
            case DIGGER:
                // the dig mask must be applied to the bottom of the lemming
                newType = applyDigMaskToBottomOfLemming(newType);
                break;
            case BUILDER_END:
                newType = Type.WALKER;
                break;
            default:
                break;
            }
        }

        return newType;
    }

    /**
     * Performs the proper animation for a LOOP or ONCE animation mode.
     *
     * @return <code>true</code> if the trigger condition is reached by
     *         executing the animation.
     */
    private boolean animateLoopOrOnce() {
        boolean trigger = false;

        switch (lemRes.getAnimMode()) {
        case LOOP:
            trigger = animateLoop(trigger);
            break;
        case ONCE:
            trigger = animateOnce(trigger);
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
     * @return the updated new Type to be assigned to the Lemming.
     */
    private Type processTrapMasks(final Type initialNewType) {
        Type newType = initialNewType;
        final int s = stencilMid();

        switch (s & (Stencil.MSK_TRAP | Stencil.MSK_EXIT)) {
        case Stencil.MSK_TRAP_DROWN:
            newType = spriteObjectHandler.animateDrowning(newType, s);
            break;
        case Stencil.MSK_TRAP_DIE:
            newType = spriteObjectHandler.animateNormalDeath(newType, s);
            break;
        case Stencil.MSK_TRAP_REPLACE:
            hasDied = spriteObjectHandler
                    .replaceLemmingWithSpecialDeathAnimation(s, hasDied);
            break;
        case Stencil.MSK_EXIT:
            newType = spriteObjectHandler.animateExitLevel(newType, s, type);
            break;
        default:
            break;
        }

        return newType;
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
    private Type executeLemmingStateMachine(final Type initialNewType,
            final int oldX, final boolean explode) {
        Type newType = initialNewType;
        int free;

        switch (type) {
        case FALLER:
            if (explode) {
                explode();
            } else {
                newType = faller.animateFaller(newType);
            }

            break;
        case JUMPER:
            if (explode) {
                newType = Type.BOMBER;
                playOhNoIfNotToBeNuked();
            } else if (!turnedByStopper()) {
                newType = animateJumper(newType);
            }

            break;
        case WALKER:
            newType = walker.animateWalker(newType, oldX, explode);
            break;
        case FLOATER_START:
            floater.animateFloaterStart(explode);
            //$FALL-THROUGH$
        case FLOATER:
            newType = floater.animateFloater(newType, explode);
            break;
        case CLIMBER:
            newType = climber.animateClimber(newType, explode);
            break;
        case SPLAT:
            animateSplat(explode);
            break;
        case BASHER:
            newType = basher.animateBasher(newType, explode);
            break;
        case MINER:
            newType = miner.animateMiner(newType, explode);
            break;
        case DIGGER:
        case BUILDER_END:
            // Shared DIGGER / BUILDER_END code:
            if (explode) {
                newType = Type.BOMBER;
                playOhNoIfNotToBeNuked();
            }

            break;
        case BUILDER:
            newType = builder.animateBuilder(newType, oldX, explode);
            break;
        case STOPPER:
            newType = stopper.animateStopper(newType, explode);
            break;
        case BOMBER_STOPPER:
            // don't erase stopper mask before stopper finally explodes or falls
            free = freeBelow(Floater.FLOATER_STEP);

            if (free > 0) {
                // stopper falls -> erase mask and convert to normal stopper.
                eraseMaskAndConvertToNormalStopper();
                // fall through
            } else {
                break;
            }

            //$FALL-THROUGH$
        case BOMBER:
            animateBomber();
            break;
        case CLIMBER_TO_WALKER:
        default:
            // Both CLIMBER_TO_WALKER and all cases not explicitly checked above
            // should at
            // least explode
            if (explode) {
                explode();
            }
        }

        return newType;
    }

    /**
     * Applies dig mask to bottom of Lemming.
     *
     * @param startingNewType the original new Type to be assigned to the
     *                        Lemming before the call to this method.
     * @return the updated new Type to be assigned to the Lemming.
     */
    private Type applyDigMaskToBottomOfLemming(final Type startingNewType) {
        Type newType = startingNewType;
        int free;
        final Mask m = lemRes.getMask(dir);
        final int sx = screenX();
        final int sy = screenY();
        m.eraseMask(sx, sy, 0, Stencil.MSK_STEEL);

        // check for conversion to walker when hitting steel
        if (lemRes.getImask(dir).checkType(sx, sy, 0, Stencil.MSK_STEEL)) {
            SoundController.playLastFewStepsSound();
            newType = Type.WALKER;
        } else {
            y += 2; // move down
        }

        // check for conversion to faller
        int freeMin = Integer.MAX_VALUE;
        free = 0;
        final int xOld = x;

        for (int i = NEGATIVE_SIX; i < Constants.SIX; i++) {
            // should be 14 pixels, here it's more like 12
            x = xOld + i;

            if (x < 0) {
                x = 0;
            } else if (x >= Level.WIDTH) {
                x = Level.WIDTH;
            }

            free = freeBelow(Floater.FLOATER_STEP);

            if (free < freeMin) {
                freeMin = free;
            }
        }

        x = xOld;
        free = freeMin;

        if (free > 0) {
            // convert to faller or walker
            // if (free >= FALL_DISTANCE_FALL) {
            newType = Type.FALLER;
            // } else {
            // newType = Type.FALLER;
            // }

            if (free >= Faller.FALLER_STEP) {
                y += Faller.FALLER_STEP;
            } else {
                y += free;
            }
        }

        return newType;
    }

    /**
     * Animates once.
     *
     * @param startingTrigger the initial value of the trigger before calling
     *                        this method.
     * @return the updated value of the trigger.
     */
    private boolean animateOnce(final boolean startingTrigger) {
        boolean trigger = startingTrigger;

        if (frameIdx < lemRes.getFrames() * TIME_SCALE - 1) {
            frameIdx++;
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
     * @return the updated value of the trigger.
     */
    private boolean animateLoop(final boolean startingTrigger) {
        boolean trigger = startingTrigger;

        if (++frameIdx >= lemRes.getFrames() * TIME_SCALE) {
            frameIdx = 0;
        }

        final int maskStep = lemRes.getMaskStep();

        if (maskStep > 0 && frameIdx % (maskStep * TIME_SCALE) == 0) {
            trigger = true;
        }

        return trigger;
    }

    /**
     * Animates bomber.
     */
    private void animateBomber() {
        int free;
        free = freeBelow(Floater.FLOATER_STEP);

        if (free == Faller.FALL_DISTANCE_FORCE_FALL) {
            y += Faller.FALLER_STEP;
        } else {
            y += free;
        }

        faller.crossedLowerBorder();
    }

    /**
     * Erases mask and converts to normal stopper.
     */
    private void eraseMaskAndConvertToNormalStopper() {
        final Mask m = lemmings[Type.getOrdinal(Type.STOPPER)].getMask(dir);
        m.clearType(maskX, maskY, 0, Stencil.MSK_STOPPER);
        type = Type.BOMBER;
    }

    /**
     * Animates splat.
     *
     * @param explode <code>true</code> if the Lemming is to explode.
     */
    private void animateSplat(final boolean explode) {
        if (explode) {
            explode();
        } else if (frameIdx == 0) { // looped once
            SoundController.getSound().play(SoundController.SND_SPLAT);
        }
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
        final int levitation = aboveGround();

        if (levitation > JUMPER_STEP) {
            y -= JUMPER_STEP;
        } else {
            // conversion to walker
            y -= levitation;
            newType = Type.WALKER;
        }

        return newType;
    }

    /**
     * Plays &quot;oh no&quot; sound if Lemming is not (already) to be nuked.
     */
    public void playOhNoIfNotToBeNuked() {
        if (!nuke) {
            SoundController.playNukeSound();
        }
    }

    /**
     * Check if a Lemming is to be turned by a stopper/blocker.
     *
     * @return true if Lemming is to be turned, false otherwise
     */
    public boolean turnedByStopper() {
        final int s = (stencilMid() & Stencil.MSK_STOPPER);

        if (s == Stencil.MSK_STOPPER_LEFT && dir == Direction.RIGHT) {
            dir = Direction.LEFT;
            return true;
        }
        if (s == Stencil.MSK_STOPPER_RIGHT && dir == Direction.LEFT) {
            dir = Direction.RIGHT;
            return true;
        }
        return false;
    }

    /**
     * Change skill/type.
     *
     * @param oldType old skill/type of Lemming
     * @param newType new skill/type of Lemming
     */
    private void changeType(final Type oldType, final Type newType) {
        if (oldType != newType) {
            type = newType;
            lemRes = lemmings[Type.getOrdinal(type)];

            if (newType == Type.DIGGER) {
                frameIdx = lemRes.getFrames() * TIME_SCALE - 1;
                // start digging immediately
            } else {
                frameIdx = 0;
            }

            // some types can't change the skill - check this
            switch (newType) {
            case WALKER:
                // insideStopper = (stencilMid()&Stencil.MSK_STOPPER) != 0;
            case BASHER:
            case BUILDER:
            case BUILDER_END:
            case DIGGER:
            case MINER:
                canChangeSkill = true;
                break;
            default:
                canChangeSkill = false;
            }
        }
    }

    /**
     * Let the Lemming explode.
     */
    public void explode() {
        SoundController.getSound().play(SoundController.SND_EXPLODE);
        // create particle explosion
        ExplosionHandler.addExplosion(midX(), midY());
        hasDied = true;
        changeType(type, Type.BOMBER);
        // consider height difference between lemming and mask
        final Mask m = lemRes.getMask(Direction.RIGHT);
        // check if lemming is standing on steel
        final int sy = y + 1;

        if (x > 0 && x < Level.WIDTH && sy > 0 && sy < Level.HEIGHT) {
            m.eraseMask(x - m.getWidth() / 2,
                    midY() - m.getHeight() / 2 + Constants.THREE, 0,
                    Stencil.MSK_STEEL);
        }
    }

    /**
     * Get stencil value from the middle of the lemming.
     *
     * @return stencil value from the middle of the lemming
     */
    private int stencilMid() {
        final int xm = x;
        final int ym = y - lemRes.getSize();
        int retval;

        if (xm > 0 && xm < Level.WIDTH && ym > 0 && ym < Level.HEIGHT) {
            retval = GameController.getStencil().get(xm + Level.WIDTH * ym);
        } else {
            retval = Stencil.MSK_EMPTY;
        }

        return retval;
    }

    /**
     * Check if digging is possible.
     *
     * @return true if digging is possible, false otherwise.
     */
    private boolean canDig() {
        final int ypos = Level.WIDTH * (y + 1);
        final int xm = x;
        final int sval = GameController.getStencil().get(xm + ypos);
        if ((sval & Stencil.MSK_WALK_ON) == Stencil.MSK_BRICK) {
            return true;
        }
        return false;
    }

    /**
     * Check if mining is possible.
     *
     * @return true if mining is possible, false otherwise.
     */
    private boolean canMine() {
        final int ypos = Level.WIDTH * (y + 1);
        int bricks = 0;
        int xMin;
        int xMax;

        if (dir == Direction.RIGHT) {
            xMin = x;
            xMax = x - lemRes.getFootX() + lemRes.getWidth();
        } else {
            xMin = x - lemRes.getFootX();
            xMax = x;
        }

        for (int xb = xMin; xb < xMax; xb++) {
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

    /**
     * Get number of free pixels below the lemming (max of step is checked).
     *
     * @param step
     *
     * @return number of free pixels below the lemming
     */
    public int freeBelow(final int step) {
        if (x < 0 || x >= Level.WIDTH) {
            return 0;
        }

        int free = 0;
        int pos = x;
        final Stencil stencil = GameController.getStencil();
        final int yb = y + 1;
        pos = x + yb * Level.WIDTH; // line below the lemming

        for (int i = 0; i < step; i++) {
            if (yb + i >= Level.HEIGHT) {
                return Faller.FALL_DISTANCE_FORCE_FALL; // convert most skill to
                                                        // faller
            }

            final int s = stencil.get(pos);

            if ((s & Stencil.MSK_WALK_ON) == Stencil.MSK_EMPTY) {
                free++;
            } else {
                break;
            }

            pos += Level.WIDTH;
        }

        return free;
    }

    /**
     * Check if Lemming reached the left or right border of the level and was
     * turned.
     *
     * @return true if lemming was turned, false otherwise.
     */
    private boolean flipDirBorder() {
        boolean flip = false;

        if (lemRes.getDirs() > 1) {
            if (x < 0) {
                x = 0;
                flip = true;
            } else if (x >= Level.WIDTH) {
                x = Level.WIDTH - 1;
                flip = true;
            }
        }

        if (flip) {
            dir = (dir == Direction.RIGHT) ? Direction.LEFT : Direction.RIGHT;
        }

        return flip;
    }

    /**
     * Get number of free pixels above the lemming (max of step is checked).
     *
     * @param step Step.
     *
     * @return number of free pixels above the lemming
     */
    public int freeAbove(final int step) {
        if (x < 0 || x >= Level.WIDTH) {
            return 0;
        }

        int free = 0;
        int pos;
        final int ym = midY();
        final Stencil stencil = GameController.getStencil();
        pos = x + ym * Level.WIDTH;

        for (int i = 0; i < step; i++) {
            if (ym - i <= 0) {
                return -1; // splat
            }

            if ((stencil.get(pos) & Stencil.MSK_WALK_ON) == Stencil.MSK_EMPTY) {
                free++;
            } else {
                break;
            }
            pos -= Level.WIDTH;
        }
        return free;
    }

    /**
     * Get the number of pixels of walkable ground above the Lemmings foot.
     *
     * @return number of pixels of walkable ground above the Lemmings foot.
     */
    public int aboveGround() {
        if (x < 0 || x >= Level.WIDTH) {
            return Level.HEIGHT - 1;
        }

        int ym = y;

        if (ym >= Level.HEIGHT) {
            return Level.HEIGHT - 1;
        }

        int pos = x;
        final Stencil stencil = GameController.getStencil();
        pos += ym * Level.WIDTH;
        int l; // Levitation.
        final int walkerObstacleHeight = Walker.WALKER_OBSTACLE_HEIGHT;

        for (l = 0; l < walkerObstacleHeight; l++, pos -= Level.WIDTH, ym--) {
            if (ym < 0) {
                return walkerObstacleHeight + 1; // forbid leaving
                                                 // level to
            }

            // the top
            if ((stencil.get(pos) & Stencil.MSK_WALK_ON) == Stencil.MSK_EMPTY) {
                break;
            }
        }

        return l;
    }

    /**
     * Replace a color in the animation frame with another color. Used to patch
     * the color of debris from pink color to a level specific color.
     *
     * @param findCol    color to find
     * @param replaceCol color to replace with
     */
    public static void patchColors(final int findCol, final int replaceCol) {
        for (int l = 0; l < NUM_RESOURCES; l++) { // go through all the lemmings
            final LemmingResource lr = lemmings[l];

            for (int f = 0; f < lr.getFrames(); f++) {
                for (int d = 0; d < lr.getDirs(); d++) {
                    for (int xp = 0; xp < lr.getWidth(); xp++) {
                        for (int yp = 0; yp < lr.getHeight(); yp++) {
                            final BufferedImage i = lr
                                    .getImage(Direction.get(d), f);

                            if (i.getRGB(xp, yp) == findCol) {
                                i.setRGB(xp, yp, replaceCol);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Load images used for Lemming animations.
     *
     * @param cmp parent component
     * @throws ResourceException
     */
    public static void loadLemmings(final Component cmp)
            throws ResourceException {
        explodeFont = new ExplodeFont(cmp);
        final MediaTracker tracker = new MediaTracker(cmp);
        // read lemmings definition file
        final String fn = Core.findResource(LEMM_INI_STR);
        final Props p = new Props();

        if (!p.load(fn)) {
            throw new ResourceException(LEMM_INI_STR);
        }

        lemmings = new LemmingResource[NUM_RESOURCES];
        // read lemmings
        final int[] def = {-1};

        for (int i = 0; true; i++) {
            int[] val = p.get("lemm_" + i, def);
            int type;

            if (val.length == Constants.THREE) {
                // frames, directions, animation type
                type = i;

                if (lemmings[type] == null) {
                    final BufferedImage sourceImg = ToolBox.imageToBuffered(
                            Core.loadImage(tracker, "misc/lemm_" + i + ".gif"),
                            Transparency.BITMASK);
                    try {
                        tracker.waitForAll();
                    } catch (final InterruptedException ex) {
                    }

                    lemmings[type] = new LemmingResource(sourceImg, val[0],
                            val[1]);
                    lemmings[type].setAnimMode(
                            (val[2] == 0) ? Animation.LOOP : Animation.ONCE);
                }
            } else {
                break;
            }

            // read mask
            val = p.get("mask_" + i, def);

            if (val.length == Constants.THREE) {
                // mask_Y: frames, directions, step
                type = i;
                final Image sourceImg = Core.loadImage(tracker,
                        "misc/mask_" + i + ".gif");
                final Mask mask = new Mask(ToolBox.imageToBuffered(sourceImg,
                        Transparency.BITMASK), val[0]);
                lemmings[type].setMask(Direction.RIGHT, mask);
                final int dirs = val[1];

                if (dirs > 1) {
                    final Mask maskLeft = new Mask(ToolBox.flipImageX(ToolBox
                            .imageToBuffered(sourceImg, Transparency.BITMASK)),
                            val[0]);
                    lemmings[type].setMask(Direction.LEFT, maskLeft);
                }

                lemmings[type].setMaskStep(val[2]);
            }

            // read indestructible mask
            val = p.get("imask_" + i, def);

            if (val.length == 2) {
                // mask_Y: type, frames, directions, step
                type = i;
                final Image sourceImg = Core.loadImage(tracker,
                        "misc/imask_" + i + ".gif");
                final Mask mask = new Mask(ToolBox.imageToBuffered(sourceImg,
                        Transparency.BITMASK), val[0]);
                lemmings[type].setImask(Direction.RIGHT, mask);
                final int dirs = val[1];

                if (dirs > 1) {
                    final Mask maskLeft = new Mask(ToolBox.flipImageX(ToolBox
                            .imageToBuffered(sourceImg, Transparency.BITMASK)),
                            val[0]);
                    lemmings[type].setImask(Direction.LEFT, maskLeft);
                }
            }

            // read foot position and size
            val = p.get("pos_" + i, def);

            if (val.length == Constants.THREE) {
                lemmings[type].setFootX(val[0]);
                lemmings[type].setFootY(val[1]);
                lemmings[type].setSize(val[2]);
            } else {
                break;
            }
        }
    }

    /**
     * Get display name of this Lemming.
     *
     * @return display name of this Lemming
     */
    public String getName() {
        Type t;

        switch (type) {
        case BOMBER_STOPPER:
            t = Type.BOMBER;
            break;
        case FLOATER_START:
            t = Type.FLOATER;
            break;
        default:
            t = type;
        }

        String n = LEMM_NAMES[Type.getOrdinal(t)];

        if (n.length() > 0) {
            if (canFloat) {
                if (canClimb) {
                    n += "(A)";
                } else if (t != Type.FLOATER) {
                    n += "(F)";
                }
            } else {
                if (canClimb && t != Type.CLIMBER) {
                    n += "(C)";
                }
            }
        }

        return n;
    }

    /**
     * Get current skill/type of this Lemming.
     *
     * @return current skill/type of this Lemming
     */
    public Type getSkill() {
        return type;
    }

    /**
     * Set new skill/type of this Lemming.
     *
     * @param skill new skill/type
     * @return true if a change was possible, false otherwise
     */
    public boolean setSkill(final Type skill) {
        if (skill == type || hasDied) {
            return false;
        }

        // check types which can't even get an additional skill anymore
        switch (type) {
        case DROWNING:
        case EXITING:
        case SPLAT:
        case TRAPPED:
        case BOMBER:
            if (skill == Type.NUKE) {
                if (nuke) {
                    return false;
                }

                nuke = true;

                if (exploder.getExplodeNumCtr() == 0) {
                    exploder.setExplodeNumCtr(Constants.FIVE);
                    exploder.setExplodeCtr(0);
                    return true;
                } else {
                    return false;
                }
            }

            return false;
        default:
            break;
        }

        // check additional skills
        switch (skill) {
        case CLIMBER:
            if (canClimb) {
                return false;
            }

            canClimb = true;
            return true;
        case FLOATER:
            if (canFloat) {
                return false;
            }

            canFloat = true;
            return true;
        case NUKE: // special case: nuke request
            if (nuke) {
                return false;
            }

            nuke = true;
            //$FALL-THROUGH$
        case BOMBER:
            if (exploder.getExplodeNumCtr() == 0) {
                exploder.setExplodeNumCtr(Constants.FIVE);
                exploder.setExplodeCtr(0);
                return true;
            } else {
                return false;
            }
        default:
            break;
        }

        // check main skills
        if (canChangeSkill) {
            switch (skill) {
            case DIGGER:
                if (canDig()) {
                    // y += DIGGER_GND_OFFSET;
                    changeType(type, skill);
                    counter = 0;
                    return true;
                } else {
                    return false;
                }
            case MINER:
                if (canMine()) {
                    changeType(type, skill);
                    counter = 0;
                    return true;
                } else {
                    return false;
                }
            case BASHER:
                changeType(type, skill);
                counter = 0;
                return true;
            case BUILDER:
                final int fb = freeBelow(Faller.FALLER_STEP);

                if (fb != 0) {
                    return false;
                }

                changeType(type, skill);
                counter = 0;
                return true;
            case STOPPER:
                final Mask m = Lemming.getResource(Type.STOPPER)
                        .getMask(Direction.LEFT);
                maskX = screenX();
                maskY = screenY();

                if (m.checkType(maskX, maskY, 0, Stencil.MSK_STOPPER)) {
                    return false; // overlaps existing stopper
                }

                changeType(type, skill);
                counter = 0;
                // set stopper mask
                m.setStopperMask(maskX, maskY, x);
                return true;
            default:
                break;
            }
        }

        return false;
    }

    /**
     * Get width of animation frame in pixels.
     *
     * @return width of animation frame in pixels
     */
    public int width() {
        return lemRes.getWidth();
    }

    /**
     * Get height of animation frame in pixels.
     *
     * @return height of animation frame in pixels
     */
    public int height() {
        return lemRes.getHeight();
    }

    /**
     * Get static resource for a skill/type.
     *
     * @param type skill/type
     * @return static resource for this skill/type
     */
    private static LemmingResource getResource(final Type type) {
        return lemmings[Type.getOrdinal(type)];
    }

    /**
     * Get X coordinate of upper left corner of animation frame.
     *
     * @return X coordinate of upper left corner of animation frame
     */
    public int screenX() {
        if (lemRes.getDirs() == 1 || dir == Direction.RIGHT) {
            return x - lemRes.getFootX();
        } else {
            return x - lemRes.getWidth() + lemRes.getFootX();
        }
    }

    /**
     * Get Y coordinate of upper left corner of animation frame.
     *
     * @return Y coordinate of upper left corner of animation frame
     */
    public int screenY() {
        return y - lemRes.getFootY();
    }

    /**
     * Get X coordinate of collision position in pixels.
     *
     * @return X coordinate of collision position in pixels.
     */
    public int midX() {
        return x;
    }

    /**
     * Collision position.
     *
     * @return Position inside lemming which is used for collisions
     */
    public int midY() {
        return y - lemRes.getSize();
    }

    /**
     * Get heading of Lemming.
     *
     * @return heading of Lemming
     */
    public Direction getDirection() {
        return dir;
    }

    /**
     * Sets heading of Lemming.
     *
     * @param direction heading of Lemming.
     */
    public void setDirection(final Direction direction) {
        this.dir = direction;
    }

    /**
     * Get current animation frame for this Lemming.
     *
     * @return current animation frame for this Lemming
     */
    public BufferedImage getImage() {
        return lemRes.getImage(dir, frameIdx / TIME_SCALE);
    }

    /**
     * Get image for explosion countdown.
     *
     * @return image for explosion countdown (or null if no explosion countdown)
     */
    public BufferedImage getCountdown() {
        final int explodeNumCtr = exploder.getExplodeNumCtr();

        if (explodeNumCtr == 0) {
            return null;
        } else {
            return explodeFont.getImage(explodeNumCtr - 1);
        }
    }

    /**
     * Used for replay: start to display the selection image.
     */
    public void setSelected() {
        selectCtr = TWENTY;
    }

    /**
     * Get the selection image for replay.
     *
     * @return the selection image (or null if no selection displayed)
     */
    public BufferedImage getSelectImg() {
        if (selectCtr == 0) {
            return null;
        } else {
            return MiscGfx.getImage(MiscGfx.Index.SELECT);
        }
    }

    /**
     * Get: Lemming has died.
     *
     * @return true if Lemming has died, false otherwise
     */
    public boolean hasDied() {
        return hasDied;
    }

    /**
     * Get: Lemming has left the level.
     *
     * @return true if Lemming has left the level, false otherwise
     */
    public boolean hasLeft() {
        return hasLeft;
    }

    /**
     * Get: Lemming is to be nuked.
     *
     * @return true if Lemming is to be nuked, false otherwise
     */
    public boolean nuke() {
        return nuke;
    }

    /**
     * Get: Lemming can float.
     *
     * @return true if Lemming can float, false otherwise
     */
    public boolean canFloat() {
        return canFloat;
    }

    /**
     * Get: Lemming can climb.
     *
     * @return true if Lemming can climb, false otherwise
     */
    public boolean canClimb() {
        return canClimb;
    }

    /**
     * Get: Lemming can get a new skill.
     *
     * @return true if Lemming can get a new skill, false otherwise
     */
    public boolean canChangeSkill() {
        return canChangeSkill;
    }

}