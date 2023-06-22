package game;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import gameutil.Fader;
import gameutil.FaderState;
import gameutil.Sprite;
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
    /**
     * Standard value of 10.
     */
    private static final int DECIMAL_10 = 10;
    /**
     * Self explanatory....
     */
    private static final int SECONDS_PER_MINUTE = 60;
    /**
     * Hexidecimal value 0x20.
     */
    private static final int REPLAY_IMAGE_CUTOFF = 0x20;
    /**
     * Replay frame mask value = Hexidecimal value 0x3f.
     */
    private static final int REPLAY_FRAME_MASK = 0x3f;
    /**
     * Scale factor.
     */
    private static final int SCALE_FACTOR = 4;
    /**
     * The maximum number of levels for a difficulty level.
     */
    private static final int MAX_LEVELS = 30;
    /**
     * Initial capacity of ArrayList for lemmsUnderCursor.
     */
    private static final int INITIAL_CAPACITY = 10;
    /**
     * Maximum hexidecimal value for an RGBA color component.
     */
    private static final int MAX_COLOR_COMPONENT_VALUE = 0xff;

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
    /** graphics object for the background image. */
    private static Graphics2D bgGfx;
    /** color used to erase the background (black). */
    private static Color blankColor = new Color(MAX_COLOR_COMPONENT_VALUE, 0, 0,
            0);
    /** flag: fast forward mode is active. */
    private static boolean fastForward;
    /** flag: Superlemming mode is active. */
    private static boolean superLemming;
    /** game state. */
    private static GameState gameState;
    /** transition (fading) state. */
    private static TransitionState transitionState;
    /** flag: entry is openend. */
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
    /** frame counter used to handle release of new Lemmings. */
    private static int releaseCtr;
    /** frame counter used to update animated sprite objects. */
    private static int animCtr;
    /** level object. */
    private static Level level;
    /** index of current difficulty level. */
    private static int curDiffLevel;
    /** index of current level pack. */
    private static int curLevelPack;
    /** index of current level. */
    private static int curLevelNumber;
    /** index of next difficulty level. */
    private static int nextDiffLevel;
    /** index of next level pack. */
    private static int nextLevelPack;
    /** index of next level. */
    private static int nextLevelNumber;
    /** list of all active Lemmings in the Level. */
    private static LinkedList<Lemming> lemmings;
    /** list of all active explosions. */
    private static LinkedList<Explosion> explosions;
    /** list of all Lemmings under the mouse cursor. */
    private static List<Lemming> lemmsUnderCursor;
    /** array of available level packs. */
    private static LevelPack[] levelPack;
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

    /** Lemming for which skill change is requested. */
    private static Lemming lemmSkillRequest;
    /** horizontal scrolling offset for level. */
    private static int xPos;
    /** replay stream used for handling replays. */
    private static ReplayStream replay;
    /** frame counter used for handling replays. */
    private static int replayFrame;
    /** old value of release rate. */
    private static int releaseRateOld;
    /** old value of nuke flag. */
    private static boolean nukeOld;
    /** old value of horizontal scrolling position. */
    private static int xPosOld;
    /** old value of selected skill. */
    private static Type lemmSkillOld;
    /** flag: replay mode is active. */
    private static boolean replayMode;
    /** flag: replay mode should be stopped. */
    private static boolean stopReplayMode;
    /** listener to inform GUI of player's progress. */
    private static UpdateListener levelMenuUpdateListener;
    /** number of Lemmings which left the level. */
    private static int numLeft;

    /** number of Lemmings available. */
    private static int numLemmingsMax;
    /** number of Lemmings who entered the level. */
    private static int numLemmingsOut;
    /** number of Lemmings which have to be rescued to finish the level. */
    private static int numToRecue;
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
     * Initialization.
     *
     * @param frame the parent component (main frame of the application).
     *
     * @throws ResourceException
     */
    public static void init(final Component frame) throws ResourceException {
        bgImage = ToolBox.createImage(Level.WIDTH, Level.HEIGHT,
                Transparency.BITMASK);
        bgGfx = bgImage.createGraphics();
        gameState = GameState.INIT;
        SoundController.initSound();
        Icons.init(frame);
        Explosion.init(frame);
        Lemming.loadLemmings(frame);
        lemmings = new LinkedList<Lemming>();
        explosions = new LinkedList<Explosion>();
        lemmsUnderCursor = new ArrayList<Lemming>(INITIAL_CAPACITY);
        lemmSkillRequest = null;
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
        levelPack = new LevelPack[dirs.size() + 1];
        levelPack[0] = new LevelPack(); // dummy

        for (int i = 0; i < dirs.size(); i++) { // read levels
            final String lvlName = dirs.get(i);
            levelPack[i + 1] = new LevelPack(Core.findResource("levels/"
                    + ToolBox.addSeparator(lvlName) + "levelpack.ini"));
        }

        curDiffLevel = 0;
        curLevelPack = 1; // since 0 is dummy
        curLevelNumber = 0;
        replayFrame = 0;
        replay = new ReplayStream();
        replayMode = false;
        stopReplayMode = false;
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
     * Calculate absolute level number from diff level and relative level
     * number.
     *
     * @param lvlPack             level pack
     * @param diffLevel           difficulty level
     * @param relativeLevelNumber relative level number
     * @return absolute level number (0..127)
     */
    static int absLevelNum(final int lvlPack, final int diffLevel,
            final int relativeLevelNumber) {
        final LevelPack lpack = levelPack[lvlPack];
        // calculate absolute level number
        int absLvl = relativeLevelNumber;

        for (int i = 0; i < diffLevel; i++) {
            absLvl += lpack.getLevels(i).length;
        }

        return absLvl;
    }

    /**
     * Calculate diffLevel and relative level number from absolute level number.
     *
     * @param lvlPack level pack
     * @param lvlAbs  absolute level number
     * @return { difficulty level, relative level number }
     */
    public static int[] relLevelNum(final int lvlPack, final int lvlAbs) {
        final int[] retval = new int[2];
        final LevelPack lpack = levelPack[lvlPack];
        final int diffLevels = lpack.getDiffLevels().length;
        int lvl = 0;
        int diffLvl = 0;
        int maxLevels = MAX_LEVELS;

        for (int i = 0, ls = 0; i < diffLevels; i++) {
            final int oldLs = ls;
            // add number of levels existing in this diff level
            maxLevels = lpack.getLevels(i).length;
            ls += maxLevels;

            if (lvlAbs < ls) {
                diffLvl = i;
                lvl = lvlAbs - oldLs; // relative level mumber
                break;
            }
        }

        retval[0] = diffLvl;
        retval[1] = lvl;
        return retval;
    }

    /**
     * Proceed to next level.
     *
     * @return true: ok, false: no more level in this difficulty level
     */
    public static synchronized boolean nextLevel() {
        final int num = curLevelNumber + 1;

        if (num < levelPack[curLevelPack].getLevels(curDiffLevel).length) {
            curLevelNumber = num;
            return true;
        } else {
            return false; // congrats - difficulty level done
        }
    }

    /**
     * Fade out at end of level.
     */
    public static synchronized void endLevel() {
        transitionState = TransitionState.END_LEVEL;
        gameState = GameState.LEVEL_END;
        Fader.setState(FaderState.OUT);
    }

    /**
     * Level successfully finished, enter debriefing and tell GUI to enable next
     * level.
     */
    static synchronized void finishLevel() {
        Music.stop();
        setFastForward(false);
        setSuperLemming(false);
        replayMode = false;

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
     * Restart level.
     *
     * @param doReplay true: replay, false: play
     * @param frame    the parent component (main frame of the application).
     */
    private static synchronized void restartLevel(final boolean doReplay,
            final Component frame) {
        initLevel(frame);

        if (doReplay) {
            replayMode = true;
            replay.save(Core.getResourcePath() + "/replay.rpl");
            replay.rewind();
        } else {
            replayMode = false;
            replay.clear();
        }
    }

    /**
     * Initialize a level after it was loaded.
     *
     * @param frame the parent component (main frame of the application).
     */
    private static void initLevel(final Component frame) {
        Music.stop();
        setFastForward(false);
        setPaused(false);
        nuke = false;
        lemmSkillRequest = null;
        TextScreen.setMode(TextScreen.Mode.INIT);
        bgGfx.setBackground(blankColor);
        bgGfx.clearRect(0, 0, bgImage.getWidth(), bgImage.getHeight());
        stencil = getLevel().paintLevel(bgImage, frame, stencil);
        lemmings.clear();
        explosions.clear();
        Icons.reset();
        TrapDoor.reset(getLevel().getEntryNum());
        entryOpened = false;
        entryOpenCtr = 0;
        secondCtr = 0;
        releaseCtr = 0;
        SkillHandler.setLemmSkill(Type.UNDEFINED);
        ReleaseRateHandler.initLevel();
        numLeft = 0;
        final int releaseRate = getLevel().getReleaseRate();
        ReleaseRateHandler.setReleaseRate(releaseRate);
        numLemmingsMax = getLevel().getNumLemmings();
        numLemmingsOut = 0;
        numToRecue = getLevel().getNumToRescue();
        time = getLevel().getTimeLimitSeconds();
        SkillHandler.initLevel(getLevel());
        setxPos(getLevel().getXpos());
        ReleaseRateHandler.calcReleaseBase();
        mapPreview = getLevel().createMiniMap(mapPreview, bgImage, SCALE_FACTOR,
                SCALE_FACTOR, false);
        setSuperLemming(getLevel().isSuperLemming());
        replayFrame = 0;
        stopReplayMode = false;
        releaseRateOld = releaseRate;
        lemmSkillOld = SkillHandler.getLemmSkill();
        nukeOld = false;
        xPosOld = getLevel().getXpos();
        gameState = GameState.BRIEFING;
    }

    /**
     * Request the restart of this level.
     *
     * @param doReplay
     */
    public static synchronized void requestRestartLevel(
            final boolean doReplay) {
        if (doReplay) {
            transitionState = TransitionState.REPLAY_LEVEL;
        } else {
            transitionState = TransitionState.RESTART_LEVEL;
        }

        Fader.setState(FaderState.OUT);
    }

    /**
     * Request a new level.
     *
     * @param lPack    index of level pack
     * @param dLevel   index of difficulty level
     * @param lNum     level number
     * @param doReplay true: replay, false: play
     */
    public static synchronized void requestChangeLevel(final int lPack,
            final int dLevel, final int lNum, final boolean doReplay) {
        nextLevelPack = lPack;
        nextDiffLevel = dLevel;
        nextLevelNumber = lNum;

        if (doReplay) {
            transitionState = TransitionState.LOAD_REPLAY;
        } else {
            transitionState = TransitionState.LOAD_LEVEL;
        }

        Fader.setState(FaderState.OUT);
    }

    /**
     * Start a new level.
     *
     * @param lPack    index of level pack
     * @param dLevel   index of difficulty level
     * @param lNum     level number
     * @param doReplay true: replay, false: play
     * @param frame    the parent component (main frame of the application).
     * @return the new level.
     */
    private static synchronized Level changeLevel(final int lPack,
            final int dLevel, final int lNum, final boolean doReplay,
            final Component frame) throws ResourceException, LemmException {
        // gameState = GAME_ST_INIT;
        curLevelPack = lPack;
        curDiffLevel = dLevel;
        curLevelNumber = lNum;
        final String lvlPath = levelPack[curLevelPack]
                .getInfo(curDiffLevel, curLevelNumber).getFileName();
        // lemmings need to be reloaded to contain pink color
        Lemming.loadLemmings(frame);
        // loading the level will patch pink lemmings pixels to correct color
        getLevel().loadLevel(lvlPath, frame);

        // if with and height would be stored inside the level, the bgImage etc.
        // would
        // have to
        // be recreated here
        // bgImage = gc.createCompatibleImage(Level.width, Level.height,
        // Transparency.BITMASK);
        // bgGfx = bgImage.createGraphics();

        initLevel(frame);

        if (doReplay) {
            replayMode = true;
            replay.rewind();
        } else {
            replayMode = false;
            replay.clear();
        }

        return getLevel();
    }

    /**
     * Get level lost state.
     *
     * @return true if level was lost, false otherwise
     */
    static synchronized boolean wasLost() {
        if (gameState != GameState.LEVEL && numLeft >= numToRecue) {
            return false;
        }

        return true;
    }

    /**
     * Get current replay image.
     *
     * @return current replay image
     */
    public static synchronized BufferedImage getReplayImage() {
        if (!replayMode) {
            return null;
        }

        if ((replayFrame & REPLAY_FRAME_MASK) > REPLAY_IMAGE_CUTOFF) {
            return MiscGfx.getImage(MiscGfx.Index.REPLAY_1);
        } else {
            return MiscGfx.getImage(MiscGfx.Index.REPLAY_2);
        }
    }

    /**
     * Lemming has left the Level.
     */
    static synchronized void increaseLeft() {
        numLeft += 1;
    }

    /**
     * Stop replay.
     */
    public static void stopReplayMode() {
        if (replayMode) {
            stopReplayMode = true;
        }
    }

    /**
     * Return time as String "minutes-seconds".
     *
     * @return time as String "minutes-seconds"
     */
    public static synchronized String getTimeString() {
        final String t1 = Integer.toString(time / 60);
        String t2 = Integer.toString(time % SECONDS_PER_MINUTE);

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

        if (!replayMode) {
            assignSkill(false); // first try to assign skill
        }

        // check +/- buttons also if paused
        ReleaseRateHandler.checkPlusMinusButtons();

        if (isPaused()) {
            return;
        }

        testForEndOfReplayMode();

        if (!replayMode) {
            handleNonReplayModeUpdate();
        } else {
            handleReplayModeUpdate();
        }

        // replay: xpos changed
        // store locally to avoid it's overwritten amidst function
        final boolean nukeTemp = nuke;
        checkForTimeExpired();
        releaseLemmings(nukeTemp);
        nuke(nukeTemp);
        openTrapDoors();
        // end of game conditions
        if ((nukeTemp || numLemmingsOut == getNumLemmingsMax())
                && explosions.size() == 0 && lemmings.size() == 0) {
            endLevel();
        }

        animateLemmings();
        handleExplosions();
        animateLevelObjects();

        if (!replayMode) {
            assignSkill(true); // 2nd try to assign skill
        }

        replayFrame++;
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
     * Loop through ad animate any explosions.
     */
    private static void handleExplosions() {
        synchronized (explosions) {
            final Iterator<Explosion> it = explosions.iterator();

            while (it.hasNext()) {
                final Explosion e = it.next();

                if (e.isFinished()) {
                    it.remove();
                } else {
                    e.update();
                }
            }
        }
    }

    /**
     * Animate or remove lemmings from frame.
     */
    private static void animateLemmings() {
        synchronized (lemmings) {
            final Iterator<Lemming> it = lemmings.iterator();

            while (it.hasNext()) {
                final Lemming l = it.next();

                if (l.hasDied() || l.hasLeft()) {
                    it.remove();
                    continue;
                }

                l.animate();
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
                    + DECIMAL_10 * MAX_ANIM_CTR) {
                entryOpened = true;
                releaseCtr = ReleaseRateHandler.getReleaseBase();
                // first lemming to enter at once
                SoundController.playMusicIfMusicOn();
            }
        }
    }

    /**
     * Handle nuking if appropriate.
     *
     * @param nukeTemp Stored initial value of {@link #nuke}.
     */
    private static void nuke(final boolean nukeTemp) {
        if (nukeTemp && ((updateCtr & 1) == 1)) {
            synchronized (lemmings) {
                for (final Lemming l : lemmings) {
                    if (!l.nuke() && !l.hasDied() && !l.hasLeft()) {
                        l.setSkill(Type.NUKE);
                        // System.out.println("nuked!");
                        break;
                    }
                }
            }
        }
    }

    /**
     * Releases lemmings if appropriate to current game conditions.
     *
     * @param nukeTemp Stored initial value of {@link #nuke}.
     */
    private static void releaseLemmings(final boolean nukeTemp) {
        if (entryOpened && !nukeTemp && !isPaused()
                && numLemmingsOut < getNumLemmingsMax()
                && ++releaseCtr >= ReleaseRateHandler.getReleaseBase()) {
            releaseCtr = 0;

            try {
                if (getLevel().getEntryNum() != 0) {
                    final Entry e = getLevel().getEntry(TrapDoor.getNext());
                    final Lemming l = new Lemming(e.getxPos() + 2,
                            e.getyPos() + 20);

                    synchronized (lemmings) {
                        lemmings.add(l);
                    }

                    numLemmingsOut++;
                }
            } catch (final ArrayIndexOutOfBoundsException ex) {
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
     * Handle replay mode-specific portions of frame update.
     */
    private static void handleReplayModeUpdate() {
        // replay mode
        ReplayEvent r;

        while ((r = replay.getNext(replayFrame)) != null) {
            switch (r.getType()) {
            case ReplayStream.ASSIGN_SKILL:
                SkillHandler.assignSkillAndDecrementAvailable(
                        (ReplayAssignSkillEvent) r, lemmings);
                SoundController.playSettingNewSKillSound();
                break;
            case ReplayStream.SET_RELEASE_RATE:
                final ReplayReleaseRateEvent rr = (ReplayReleaseRateEvent) r;
                ReleaseRateHandler.setReleaseRate(rr.getReleaseRate());
                ReleaseRateHandler.calcReleaseBase();
                SoundController
                        .playPitched(ReleaseRateHandler.getReleaseRate());
                break;
            case ReplayStream.NUKE:
                nuke = true;
                break;
            case ReplayStream.MOVE_XPOS:
                final ReplayMoveXPosEvent rx = (ReplayMoveXPosEvent) r;
                setxPos(rx.getxPos());
                break;
            case ReplayStream.SELECT_SKILL:
                SkillHandler.selectSkill((ReplaySelectSkillEvent) r);
                break;
            default:
                break;
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
                replay.addReleaseRateEvent(replayFrame, releaseRate);
                releaseRateOld = releaseRate;
            }

            // replay: nuked?
            if (nuke != nukeOld) {
                replay.addNukeEvent(replayFrame);
                nukeOld = nuke;
            }

            // replay: xPos changed?
            if (getxPos() != xPosOld) {
                replay.addXPosEvent(replayFrame, getxPos());
                xPosOld = getxPos();
            }

            final Type lemmSkill = SkillHandler.getLemmSkill();

            // skill changed
            if (lemmSkill != lemmSkillOld) {
                replay.addSelectSkillEvent(replayFrame, lemmSkill);
                lemmSkillOld = lemmSkill;
            }
        } else {
            replay.clear();
        }
    }

    /**
     * Checks wheter in replay mode and should stop replay mode. If so, stops
     * replay mode and clears both flag attributes.
     */
    private static void testForEndOfReplayMode() {
        if (replayMode && stopReplayMode) {
            replay.clearFrom(replayFrame);
            replayMode = false;
            stopReplayMode = false;
        }
    }

    /**
     * Request a skill change for a Lemming (currently selected skill).
     *
     * @param lemm Lemming
     */
    public static synchronized void requestSkill(final Lemming lemm) {
        if (SkillHandler.getLemmSkill() != Type.UNDEFINED) {
            lemmSkillRequest = lemm;
        }

        stopReplayMode();
    }

    /**
     * Assign the selected skill to the selected Lemming.
     *
     * @param delete flag: reset the current skill request
     */
    private static synchronized void assignSkill(final boolean delete) {
        final Type lemmSkill = SkillHandler.getLemmSkill();

        if (lemmSkillRequest == null || Type.UNDEFINED == lemmSkill) {
            return;
        }

        final Lemming lemm = lemmSkillRequest;

        if (delete) {
            lemmSkillRequest = null;
        }

        stopReplayMode();
        final boolean canSet = canSetSkill(lemm);

        if (canSet) {
            lemmSkillRequest = null; // erase request
            SoundController.playMouseClickedSound();

            if (isPaused()) {
                setPaused(false);
                Icons.press(Icons.Type.PAUSE);
            }

            // add to replay stream
            if (!wasCheated) {
                synchronized (lemmings) {
                    for (int i = 0; i < lemmings.size(); i++) {
                        if (lemmings.get(i) == lemm) { // if 2nd try
                                                       // (delete==true) assign
                                                       // to next frame
                            replay.addAssignSkillEvent(
                                    replayFrame + ((delete) ? 1 : 0), lemmSkill,
                                    i);
                        }
                    }
                }
            }
        } else if (delete) {
            SoundController.playTingSound();
        }
    }

    /**
     * Indicates whether the lemming's skill can be set to the selected one.
     *
     * @param lemm the lemming whose skill is to be set, if possible.
     * @return <code>true</code> if the lemming's skill can be set to the
     *         selected one.
     */
    private static boolean canSetSkill(final Lemming lemm) {
        boolean canSet = false;

        if (isCheat()) {
            canSet = lemm.setSkill(SkillHandler.getLemmSkill());
        } else {
            canSet = SkillHandler.canSetSkillOfLemmingIfNotCheatMode(lemm);
        }

        return canSet;
    }

    /**
     * Fade in/out.
     *
     * @param g     graphics object
     * @param frame the parent JFrame (main frame of the application).
     */
    public static void fade(final Graphics g, final JFrame frame) {
        if (Fader.getState() == FaderState.OFF
                && transitionState != TransitionState.NONE) {
            switch (transitionState) {
            case END_LEVEL:
                finishLevel();
                break;
            case TO_BRIEFING:
                gameState = GameState.BRIEFING;
                break;
            case TO_DEBRIEFING:
                gameState = GameState.DEBRIEFING;
                break;
            case TO_INTRO:
                gameState = GameState.INTRO;
                break;
            case TO_LEVEL:
                fadeToLevel();
                break;
            case RESTART_LEVEL:
            case REPLAY_LEVEL:
                restartLevel(transitionState == TransitionState.REPLAY_LEVEL,
                        frame);
                break;
            case LOAD_LEVEL:
            case LOAD_REPLAY:
                try {
                    changeLevel(nextLevelPack, nextDiffLevel, nextLevelNumber,
                            transitionState == TransitionState.LOAD_REPLAY,
                            frame);
                    frame.setTitle("Lemmini - " + getLevel().getLevelName());
                } catch (final ResourceException ex) {
                    Core.resourceError(ex.getMessage());
                } catch (final LemmException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }

                break;
            default:
                break;
            }

            Fader.setState(FaderState.IN);
            transitionState = TransitionState.NONE;
        }

        Fader.fade(g);
    }

    /**
     * Fade to level.
     */
    private static void fadeToLevel() {
        SoundController.playStartOfLevelSound();

        try {
            Music.load("music/"
                    + GameController.levelPack[GameController.curLevelPack]
                            .getInfo(GameController.curDiffLevel,
                                    GameController.curLevelNumber)
                            .getMusic());
        } catch (final ResourceException ex) {
            Core.resourceError(ex.getMessage());
        } catch (final LemmException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        gameState = GameState.LEVEL;
    }

    /**
     * Draw the explosions.
     *
     * @param g      graphics object
     * @param width  width of screen in pixels
     * @param height height of screen in pixels
     * @param xOfs   horizontal level offset in pixels
     */
    public static void drawExplosions(final Graphics2D g, final int width,
            final int height, final int xOfs) {
        synchronized (explosions) {
            for (final Explosion e : explosions) {
                e.draw(g, width, height, xOfs);
            }
        }
    }

    /**
     * Add a new explosion.
     *
     * @param x x coordinate in pixels.
     * @param y y coordinate in pixels.
     */
    public static void addExplosion(final int x, final int y) {
        // create particle explosion
        synchronized (GameController.explosions) {
            GameController.explosions.add(new Explosion(x, y));
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
     * Get index of current level pack.
     *
     * @return index of current level pack
     */
    public static int getCurLevelPackIdx() {
        return curLevelPack;
    }

    /**
     * Get current level pack.
     *
     * @return current level pack
     */
    public static LevelPack getCurLevelPack() {
        return levelPack[curLevelPack];
    }

    /**
     * get number of level packs.
     *
     * @return number of level packs
     */
    public static int getLevelPackNum() {
        return levelPack.length;
    }

    /**
     * Get level pack via index.
     *
     * @param i index of level pack
     * @return LevelPack
     */
    public static LevelPack getLevelPack(final int i) {
        return levelPack[i];
    }

    /**
     * Get index of current difficulty level.
     *
     * @return index of current difficulty level
     */
    public static int getCurDiffLevel() {
        return curDiffLevel;
    }

    /**
     * Get number of current level.
     *
     * @return number of current leve
     */
    public static int getCurLevelNumber() {
        return curLevelNumber;
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
     * Set transition state.
     *
     * @param ts TransitionState
     */
    public static void setTransition(final TransitionState ts) {
        transitionState = ts;
    }

    /**
     * Load a replay.
     *
     * @param fn file name
     * @return replay level info object
     */
    public static ReplayLevelInfo loadReplay(final String fn) {
        return replay.load(fn);
    }

    /**
     * Save a replay.
     *
     * @param fn file name
     * @return true if saved successfully, false otherwise
     */
    public static boolean saveReplay(final String fn) {
        return replay.save(fn);
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
     * Get list of all Lemmings under the mouse cursor.
     *
     * @return list of all Lemmings under the mouse cursor
     */
    public static List<Lemming> getLemmsUnderCursor() {
        return lemmsUnderCursor;
    }

    /**
     * Get list of all Lemmings in this level.
     *
     * @return list of all Lemmings in this level
     */
    public static LinkedList<Lemming> getLemmings() {
        return lemmings;
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
     * Get small preview image of level.
     *
     * @return small preview image of level
     */
    public static BufferedImage getMapPreview() {
        return mapPreview;
    }

    /**
     * Get number of Lemmings to rescue.
     *
     * @return number of Lemmings to rescue
     */
    public static int getNumToRecue() {
        return numToRecue;
    }

    /**
     * Get time left in seconds.
     *
     * @return time left in seconds
     */
    public static int getTime() {
        return time;
    }
}
