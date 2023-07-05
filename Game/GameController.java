package game;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import game.lemmings.Lemming;
import game.lemmings.SkillHandler;
import game.level.Explosion;
import game.level.Level;
import game.level.ReleaseRateHandler;
import game.level.SpriteObject;
import game.level.Stencil;
import game.replay.ReplayController;
import gameutil.Fader;
import gameutil.FaderHandler;
import gameutil.FaderState;
import gameutil.Sprite;
import lemmini.Constants;
import tools.MicrosecondTimer;
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
 * Game controller. Contains all the game logic.
 *
 * @author Volker Oth
 */
public final class GameController {
    /** key repeat bitmask for icons. */
    public static final int KEYREPEAT_ICON = 1;
    /** key repeat bitmask for keys. */
    public static final int KEYREPEAT_KEY = 2;
    /** updates 5 frames instead of 1 in fast forward mode. */
    public static final int FAST_FWD_MULTI = 5;
    /** updates 3 frames instead of 1 in Superlemming mode. */
    public static final int SUPERLEMM_MULTI = 3;
    /**
     * Time per frame in microseconds - this is the timing everything else is
     * based on.
     */
    public static final int MICROSEC_PER_FRAME = 30 * 1000;
    /** resync if time difference greater than that (in microseconds). */
    public static final int MICROSEC_RESYNC = 5 * 30 * 1000;
    /** redraw animated level obejcts every 3rd frame (about 100ms). */
    private static final int MAX_ANIM_CTR = 100 * 1000 / MICROSEC_PER_FRAME;
    /** open Entry after about 1.5 seconds. */
    private static final int MAX_ENTRY_OPEN_CTR = 1500 * 1000
            / MICROSEC_PER_FRAME;
    /** one second is 33.33 ticks (integer would cause error). */
    private static final double MAX_SECOND_CTR = 1000.0 * 1000
            / MICROSEC_PER_FRAME;
    /** the background stencil. */
    private static Stencil stencil;
    /** the background image. */
    private static BufferedImage bgImage;
    /** flag: use advanced mouse selection methods. */
    private static boolean advancedSelect;
    /** flag: use classical mouse cursor behavior. */
    private static boolean classicalCursor;
    /** flag: fast forward mode is active. */
    private static boolean fastForward;
    /** flag: Superlemming mode is active. */
    private static boolean superLemming;
    /** game state. */
    private static GameState gameState;
    /** flag: entry is opened. */
    private static boolean entryOpened;
    /** flag: nuke was activated. */
    private static boolean nuke;
    /** flag: game is paused. */
    private static boolean paused;
    /** flag: cheat/debug mode is activated. */
    private static boolean cheat = false;
    /** flag: cheat mode was activated during play. */
    private static boolean wasCheated = false;
    /** frame counter for handling opening of entries. */
    private static int entryOpenCtr;

    /** frame counter for handling time. */
    private static double secondCtr;
    /** frame counter used to update animated sprite objects. */
    private static int animCtr;
    /** level object. */
    private static Level level;
    /** small preview version of level used in briefing screen. */
    private static BufferedImage mapPreview;

    /**
     * Indicates whether nuke was activated.
     *
     * @return <code>true</code> if nuke was activated.
     */
    public static boolean isNuke() {
        return nuke;
    }

    /**
     * Sets whether nuke was activated.
     *
     * @param nukeActivated <code>true</code> if nuke was activated.
     */
    public static void setNuke(final boolean nukeActivated) {
        GameController.nuke = nukeActivated;
    }

    /** horizontal scrolling offset for level. */
    private static int xPos;
    /** old value of release rate. */
    private static int releaseRateOld;
    /** old value of nuke flag. */
    private static boolean nukeOld;

    /** old value of horizontal scrolling position. */
    private static int xPosOld;

    /** old value of selected skill. */
    private static Type lemmSkillOld;
    /** listener to inform GUI of player's progress. */
    private static UpdateListener levelMenuUpdateListener;
    /** number of Lemmings which left the level. */
    private static int numLeft;

    /** number of Lemmings available. */
    private static int numLemmingsMax;

    /** number of Lemmings which have to be rescued to finish the level. */
    private static int numToRescue;
    /** time left in seconds. */
    private static int time;
    /** free running update counter. */
    private static int updateCtr;

    /**
     * Private constructor for utility class.
     */
    private GameController() {

    }

    /**
     * Sets old value of horizontal scrolling position.
     *
     * @param oldPosition old value of horizontal scrolling position.
     */
    public static void setxPosOld(final int oldPosition) {
        GameController.xPosOld = oldPosition;
    }

    /**
     * Sets old value of nuke flag.
     *
     * @param oldValue old value of nuke flag.
     */
    public static void setNukeOld(final boolean oldValue) {
        GameController.nukeOld = oldValue;
    }

    /**
     * Returns old value of selected skill.
     *
     * @return old value of selected skill.
     */
    public static Type getLemmSkillOld() {
        return lemmSkillOld;
    }

    /**
     * Sets old value of selected skill.
     *
     * @param oldSkill old value of selected skill.
     */
    public static void setLemmSkillOld(final Type oldSkill) {
        GameController.lemmSkillOld = oldSkill;
    }

    /**
     * Returns old value of release rate.
     *
     * @return old value of release rate.
     */
    public static int getReleaseRateOld() {
        return releaseRateOld;
    }

    /**
     * Sets old value of release rate.
     *
     * @param oldRate old value of release rate.
     */
    public static void setReleaseRateOld(final int oldRate) {
        GameController.releaseRateOld = oldRate;
    }

    /**
     * Returns value of frame counter for handling time.
     *
     * @return value of frame counter for handling time.
     */
    public static double getSecondCtr() {
        return secondCtr;
    }

    /**
     * Sets value of frame counter for handling time.
     *
     * @param counterValue value of frame counter for handling time.
     */
    public static void setSecondCtr(final double counterValue) {
        GameController.secondCtr = counterValue;
    }

    /**
     * Returns frame counter for handling opening of entries.
     *
     * @return frame counter for handling opening of entries.
     */
    public static int getEntryOpenCtr() {
        return entryOpenCtr;
    }

    /**
     * Sets value of frame counter for handling opening of entries.
     *
     * @param counterValue value of frame counter for handling opening of
     *                     entries.
     */
    public static void setEntryOpenCtr(final int counterValue) {
        GameController.entryOpenCtr = counterValue;
    }

    /**
     * Indicates whether entry is opened.
     *
     * @return <code>true</code> if entry is opened.
     */
    public static boolean isEntryOpened() {
        return entryOpened;
    }

    /**
     * Sets whether entry is opened.
     *
     * @param opened <code>true</code> if entry is opened.
     */
    public static void setEntryOpened(final boolean opened) {
        GameController.entryOpened = opened;
    }

    /**
     * Initialization.
     *
     * @param frame the parent component (main frame of the application).
     *
     * @throws ResourceException
     */
    public static void init(final Component frame) throws ResourceException {
        bgImage = ToolBox.createImage(Level.WIDTH, Level.HEIGHT,
                Transparency.BITMASK);
        gameState = GameState.INIT;
        SoundController.initSound();
        Icons.init(frame);
        Explosion.init(frame);
        Lemming.loadLemmings(frame);
        LemmingHandler.init();
        ExplosionHandler.init();
        LemmFont.init(frame);
        NumFont.init(frame);
        LemmCursor.init(frame);
        Music.init();
        Music.setGain(SoundController.getMusicGain());
        MiscGfx.init(frame);
        ReleaseRateHandler.init();
        SkillHandler.setTimerNuke(new MicrosecondTimer());
        level = new Level();
        // read level packs
        final File dir = new File(Core.getResourcePath() + "levels");
        final File[] files = dir.listFiles();
        // now get the names of the directories
        final List<String> dirs = getNamesOfDirectories(files);
        Collections.sort(dirs);
        FaderHandler.init(bgImage, dirs);
        ReplayController.init();
        wasCheated = isCheat();
    }

    /**
     * Returns the names of the directories.
     *
     * @param files the level files.
     * @return the names of the directories.
     */
    private static List<String> getNamesOfDirectories(final File[] files) {
        final List<String> dirs = new ArrayList<String>();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    dirs.add(files[i].getName());
                }
            }
        }

        return dirs;
    }

    /**
     * Fade out at end of level.
     */
    public static synchronized void endLevel() {
        FaderHandler.setTransitionState(TransitionState.END_LEVEL);
        gameState = GameState.LEVEL_END;
        Fader.setState(FaderState.OUT);
    }

    /**
     * Level successfully finished, enter debriefing and tell GUI to enable next
     * level.
     */
    public static synchronized void finishLevel() {
        Music.stop();
        setFastForward(false);
        setSuperLemming(false);
        ReplayController.setReplayMode(false);
        final int curLevelPack = FaderHandler.getCurLevelPackIdx();

        if (!wasLost() && (curLevelPack != 0)) {
            levelMenuUpdateListener.update();
        }

        gameState = GameState.DEBRIEFING;
    }

    /**
     * Hook for GUI to get informed when a level was successfully finished.
     *
     * @param l UpdateListener
     */
    public static void setLevelMenuUpdateListener(final UpdateListener l) {
        levelMenuUpdateListener = l;
    }

    /**
     * Get level lost state.
     *
     * @return true if level was lost, false otherwise
     */
    public static synchronized boolean wasLost() {
        if (gameState != GameState.LEVEL && numLeft >= numToRescue) {
            return false;
        }

        return true;
    }

    /**
     * Lemming has left the Level.
     */
    public static synchronized void increaseLeft() {
        numLeft += 1;
    }

    /**
     * Return time as String "minutes-seconds".
     *
     * @return time as String "minutes-seconds"
     */
    public static synchronized String getTimeString() {
        final String t1 = Integer.toString(time / 60);
        String t2 = Integer.toString(time % Constants.SECONDS_PER_MINUTE);

        if (t2.length() < 2) {
            t2 = "0" + t2;
        }

        return t1 + "-" + t2;
    }

    /**
     * Update the whole game state by one frame.
     */
    public static synchronized void update() {
        if (gameState != GameState.LEVEL) {
            return;
        }

        updateCtr++;
        final boolean replayMode = ReplayController.isReplayMode();

        if (!replayMode) {
            LemmingHandler.assignSkill(false); // first try to assign skill
        }

        // check +/- buttons also if paused
        ReleaseRateHandler.checkPlusMinusButtons();

        if (isPaused()) {
            return;
        }

        ReplayController.testForEndOfReplayMode();
        final LinkedList<Lemming> lemmings = LemmingHandler.getLemmings();

        if (!replayMode) {
            handleNonReplayModeUpdate();
        } else {
            ReplayController.handleReplayModeUpdate(lemmings);
        }

        // replay: xpos changed
        // store locally to avoid it's overwritten amidst function
        final boolean nukeTemp = nuke;
        checkForTimeExpired();
        LemmingHandler.releaseLemmings(nukeTemp, entryOpened);
        LemmingHandler.nuke(nukeTemp, updateCtr);
        openTrapDoors();

        // end of game conditions
        if ((nukeTemp
                || LemmingHandler.getNumLemmingsOut() == getNumLemmingsMax())
                && ExplosionHandler.getExplosions().size() == 0
                && lemmings.size() == 0) {
            endLevel();
        }

        LemmingHandler.animateLemmings();
        ExplosionHandler.handleExplosions();
        animateLevelObjects();

        if (!replayMode) {
            LemmingHandler.assignSkill(true); // 2nd try to assign skill
        }

        ReplayController.incrementReplayFrame();
    }

    /**
     * Animates level objects.
     */
    private static void animateLevelObjects() {
        if (++animCtr > MAX_ANIM_CTR) {
            animCtr -= MAX_ANIM_CTR;

            for (int n = 0; n < getLevel().getSprObjectNum(); n++) {
                final SpriteObject spr = getLevel().getSprObject(n);
                spr.getImageAnim(); // just to animate
            }
        }
    }

    /**
     * Open trap doors if appropriate.
     */
    private static void openTrapDoors() {
        if (!entryOpened) {
            if (++entryOpenCtr == MAX_ENTRY_OPEN_CTR) {
                for (int i = 0; i < getLevel().getEntryNum(); i++) {
                    getLevel().getSprObject(getLevel().getEntry(i).getId())
                            .setAnimMode(Sprite.Animation.ONCE);
                }

                SoundController.playTrapDoorOpenSound();
            } else if (entryOpenCtr == MAX_ENTRY_OPEN_CTR
                    + Constants.DECIMAL_10 * MAX_ANIM_CTR) {
                entryOpened = true;
                LemmingHandler
                        .setReleaseCtr(ReleaseRateHandler.getReleaseBase());
                // first lemming to enter at once
                SoundController.playMusicIfMusicOn();
            }
        }
    }

    /**
     * Checks if time for the level has expired and, if so, ends the level.
     */
    private static void checkForTimeExpired() {
        secondCtr += 1.0;

        if (secondCtr > MAX_SECOND_CTR) {
            // one second passed
            secondCtr -= MAX_SECOND_CTR;
            time--;

            if (!isCheat() && time == 0) {
                // level failed
                endLevel();
            }
        }
    }

    /**
     * Handles non-replay mode-specific portions of frame update.
     */
    private static void handleNonReplayModeUpdate() {
        if (!wasCheated) {
            // replay: release rate changed?
            final int releaseRate = ReleaseRateHandler.getReleaseRate();

            if (releaseRate != releaseRateOld) {
                releaseRateOld = ReplayController
                        .addReleaseRateEvent(releaseRate);
            }

            // replay: nuked?
            if (nuke != nukeOld) {
                nukeOld = ReplayController.addNukeEvent(nuke);
            }

            // replay: xPos changed?
            int updatedXPos = getxPos();

            if (updatedXPos != xPosOld) {
                ReplayController.addXPosEvent(updatedXPos);
                xPosOld = updatedXPos;
            }

            final Type lemmSkill = SkillHandler.getLemmSkill();

            // skill changed
            if (lemmSkill != lemmSkillOld) {
                ReplayController.addSelectSkillEvent(lemmSkill);
                lemmSkillOld = lemmSkill;
            }
        } else {
            ReplayController.clear();
        }
    }

    /**
     * Draw icon bar.
     *
     * @param g graphics object
     * @param x x coordinate in pixels
     * @param y y coordinate in pixels
     */
    public static void drawIcons(final Graphics2D g, final int x, final int y) {
        g.drawImage(Icons.getImg(), x, y, null);
    }

    /**
     * Set horizontal scrolling offset.
     *
     * @param x horizontal scrolling offset in pixels
     */
    public static void setxPos(final int x) {
        xPos = x;
    }

    /**
     * Get horizontal scrolling offset.
     *
     * @return horizontal scrolling offset in pixels
     */
    public static int getxPos() {
        return xPos;
    }

    /**
     * Set game state.
     *
     * @param s new game state
     */
    public static void setGameState(final GameState s) {
        gameState = s;
    }

    /**
     * Get game state.
     *
     * @return game state
     */
    public static GameState getGameState() {
        return gameState;
    }

    /**
     * Enable/disable cheat mode.
     *
     * @param c true: enable, false: disable
     */
    public static void setCheat(final boolean c) {
        cheat = c;
    }

    /**
     * Get state of cheat mode.
     *
     * @return true if cheat mode enabled, false otherwise
     */
    public static boolean isCheat() {
        return cheat;
    }

    /**
     * Activate/deactivate Superlemming mode.
     *
     * @param sl true: activate, false: deactivate
     */
    public static void setSuperLemming(final boolean sl) {
        superLemming = sl;
    }

    /**
     * Get Superlemming state.
     *
     * @return true is Superlemming mode is active, false otherwise
     */
    public static boolean isSuperLemming() {
        return superLemming;
    }

    /**
     * Indicates whether cheat mode was activated.
     *
     * @return true: cheat mode was activated, false otherwise
     */
    public static boolean isWasCheated() {
        return wasCheated;
    }

    /**
     * Set cheated detection.
     *
     * @param c true: cheat mode was activated, false otherwise
     */
    public static void setWasCheated(final boolean c) {
        wasCheated = c;
    }

    /**
     * Enable pause mode.
     *
     * @param p true: pause is active, false otherwise
     */
    public static void setPaused(final boolean p) {
        paused = p;
    }

    /**
     * Get pause state.
     *
     * @return true if pause is active, false otherwise
     */
    public static boolean isPaused() {
        return paused;
    }

    /**
     * Enable fast forward mode.
     *
     * @param ff true: fast forward is active, false otherwise
     */
    public static void setFastForward(final boolean ff) {
        fastForward = ff;
    }

    /**
     * Get fast forward state.
     *
     * @return true if fast forward is active, false otherwise
     */
    public static boolean isFastForward() {
        return fastForward;
    }

    /**
     * get number of lemmings left in the game.
     *
     * @return number of lemmings left in the game.
     */
    public static int getNumLeft() {
        return numLeft;
    }

    /**
     * Set number of Lemmings left in the game.
     *
     * @param n number of Lemmings left in the game.
     */
    public static void setNumLeft(final int n) {
        numLeft = n;
    }

    /**
     * Get level object.
     *
     * @return level object
     */
    public static Level getLevel() {
        return level;
    }

    /**
     * Get maximum number of Lemmings for this level.
     *
     * @return maximum number of Lemmings for this level
     */
    public static int getNumLemmingsMax() {
        return numLemmingsMax;
    }

    /**
     * Sets number of Lemmings available.
     *
     * @param lemmingsAvailable number of Lemmings available.
     */
    public static void setNumLemmingsMax(final int lemmingsAvailable) {
        GameController.numLemmingsMax = lemmingsAvailable;
    }

    /**
     * Get icon type from x position.
     *
     * @param x x position in pixels
     * @return icon type
     */
    public static Icons.Type getIconType(final int x) {
        return Icons.getType(x);
    }

    /**
     * Icon was pressed.
     *
     * @param t icon type
     */
    public static void pressIcon(final Icons.Type t) {
        Icons.press(t);
    }

    /**
     * Icon was released.
     *
     * @param t icon type
     */
    public static void releaseIcon(final Icons.Type t) {
        Icons.release(t);
    }

    /**
     * Set music gain.
     *
     * @param g gain (0..1.0)
     */
    public static void setMusicGain(final double g) {
        SoundController.setMusicGain(g);

        if (Music.getType() != null) {
            Music.setGain(g);
        }
    }

    /**
     * Set advanced mouse selection mode.
     *
     * @param sel true: advanced selection mode active, false otherwise
     */
    public static void setAdvancedSelect(final boolean sel) {
        advancedSelect = sel;
    }

    /**
     * Get state of advanced mouse selection mode.
     *
     * @return true if advanced selection mode activated, false otherwise
     */
    public static boolean isAdvancedSelect() {
        return advancedSelect;
    }

    /**
     * Set classical cursor mode.
     *
     * @param sel true: classical cursor mode active, false otherwise
     */
    public static void setClassicalCursor(final boolean sel) {
        classicalCursor = sel;
    }

    /**
     * Get state of classical cursor mode.
     *
     * @return true if classical cursor mode activated, false otherwise
     */
    public static boolean isClassicalCursor() {
        return classicalCursor;
    }

    /**
     * Get background image of level.
     *
     * @return background image of level
     */
    public static BufferedImage getBgImage() {
        return bgImage;
    }

    /**
     * Get background stencil of level.
     *
     * @return background stencil of level
     */
    public static Stencil getStencil() {
        return stencil;
    }

    /**
     * Sets the background stencil.
     *
     * @param backgroundStencil the background stencil.
     */
    public static void setStencil(final Stencil backgroundStencil) {
        GameController.stencil = backgroundStencil;
    }

    /**
     * Get small preview image of level.
     *
     * @return small preview image of level
     */
    public static BufferedImage getMapPreview() {
        return mapPreview;
    }

    /**
     * Sets small preview version of level used in briefing screen.
     *
     * @param preview small preview version of level used in briefing screen.
     */
    public static void setMapPreview(final BufferedImage preview) {
        GameController.mapPreview = preview;
    }

    /**
     * Get number of Lemmings to rescue.
     *
     * @return number of Lemmings to rescue
     */
    public static int getNumToRescue() {
        return numToRescue;
    }

    /**
     * Sets number of Lemmings to rescue.
     *
     * @param numberToRescue number of Lemmings to rescue.
     */
    public static void setNumToRescue(final int numberToRescue) {
        GameController.numToRescue = numberToRescue;
    }

    /**
     * Get time left in seconds.
     *
     * @return time left in seconds
     */
    public static int getTime() {
        return time;
    }

    /**
     * Sets time left in seconds.
     *
     * @param timeLeft time left in seconds.
     */
    public static void setTime(final int timeLeft) {
        GameController.time = timeLeft;
    }
}
