package game.lemmings;

import java.awt.image.BufferedImage;

import game.GameController;
import game.LemmingExplosion;
import game.MiscGfx;
import game.SoundController;
import game.Type;
import game.level.Level;
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
    /** number of resources (animations/names). */
    public static final int NUM_RESOURCES = 17;

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

    /**
     * Sets static array of resources for each Lemming skill/type.
     *
     * @param lemmingResources static array of resources for each Lemming
     *                         skill/type.
     */
    public static void setLemmings(final LemmingResource[] lemmingResources) {
        lemmings = lemmingResources;
    }

    /** font used for the explosion counter. */
    private static ExplodeFont explodeFont;

    /**
     * Class for handling explosions, if any, for the current Lemming.
     */
    private final LemmingExplosion exploder;

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
     * Class for handling bomber skill for the current Lemming, if assigned.
     */
    private Bomber bomber;

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
        faller = new Faller(this);
        bomber = new Bomber(this);
    }

    /**
     * Returns value of counter used to display the select image in replay mode.
     *
     * @return value of counter used to display the select image in replay mode.
     */
    public int getSelectCtr() {
        return selectCtr;
    }

    /**
     * Sets value of counter used to display the select image in replay mode.
     *
     * @param counterValue value for counter used to display the select image in
     *                     replay mode.
     */
    public void setSelectCtr(final int counterValue) {
        this.selectCtr = counterValue;
    }

    /**
     * Sets font used for the explosion counter.
     *
     * @param fontUsed font used for the explosion counter.
     */
    public static void setExplodeFont(final ExplodeFont fontUsed) {
        Lemming.explodeFont = fontUsed;
    }

    /**
     * Returns class for handling explosions, if any, for the current Lemming.
     *
     * @return class for handling explosions, if any, for the current Lemming.
     */
    public LemmingExplosion getExploder() {
        return exploder;
    }

    /**
     * Returns class for handling bomber skill for the current Lemming, if
     * assigned.
     *
     * @return class for handling bomber skill for the current Lemming, if
     *         assigned.
     */
    public Bomber getBomber() {
        return bomber;
    }

    /**
     * Sets class for handling bomber skill for the current Lemming, if
     * assigned.
     *
     * @param lemmingBomber class for handling bomber skill for the current
     *                      Lemming, if assigned.
     */
    public void setBomber(final Bomber lemmingBomber) {
        this.bomber = lemmingBomber;
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
    public void changeType(final Type oldType, final Type newType) {
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
     * Get stencil value from the middle of the lemming.
     *
     * @return stencil value from the middle of the lemming
     */
    public int stencilMid() {
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
    public static LemmingResource getResource(final Type type) {
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
     * Set: Lemming has left the level.
     *
     * @param leftLevel true if Lemming has left the level, false otherwise
     */
    public void setHasLeft(final boolean leftLevel) {
        this.hasLeft = leftLevel;
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
     * Set: Lemming is to be nuked.
     *
     * @param toBeNuked true if Lemming is to be nuked, false otherwise
     */
    public void setNuke(final boolean toBeNuked) {
        this.nuke = toBeNuked;
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
     * Set: Lemming can float.
     *
     * @param doFloat true if Lemming can float, false otherwise
     */
    public void setCanFloat(final boolean doFloat) {
        this.canFloat = doFloat;
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
     * Set: Lemming can climb.
     *
     * @param climb true if Lemming can climb, false otherwise
     */
    public void setCanClimb(final boolean climb) {
        this.canClimb = climb;
    }

    /**
     * Get: Lemming can get a new skill.
     *
     * @return true if Lemming can get a new skill, false otherwise
     */
    public boolean canChangeSkill() {
        return canChangeSkill;
    }

    /**
     * Check if digging is possible.
     *
     * @return true if digging is possible, false otherwise.
     */
    public boolean canDig() {
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
    public boolean canMine() {
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

}
