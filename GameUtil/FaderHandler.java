package gameutil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import game.Core;
import game.ExplosionHandler;
import game.GameController;
import game.GameState;
import game.Icons;
import game.LemmException;
import game.LemmingHandler;
import game.LevelPack;
import game.Music;
import game.ResourceException;
import game.SoundController;
import game.TransitionState;
import game.TrapDoor;
import game.Type;
import game.lemmings.LemmingImageLoader;
import game.lemmings.SkillHandler;
import game.level.Level;
import game.level.LevelLoader;
import game.level.LevelPainter;
import game.level.ReleaseRateHandler;
import game.level.TextScreen;
import game.replay.ReplayController;
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
 * Utility class for handling Faders. Code moved from GameController by John
 * Watne 07/2023.
 */
public final class FaderHandler {
    /**
     * The maximum number of levels for a difficulty level.
     */
    private static final int MAX_LEVELS = 30;
    /**
     * Maximum hexidecimal value for an RGBA color component.
     */
    private static final int MAX_COLOR_COMPONENT_VALUE = 0xff;
    /**
     * Scale factor.
     */
    private static final int SCALE_FACTOR = 4;
    /** graphics object for the background image. */
    private static Graphics2D bgGfx;
    /** color used to erase the background (black). */
    private static Color blankColor = new Color(MAX_COLOR_COMPONENT_VALUE, 0, 0,
            0);
    /** transition (fading) state. */
    private static TransitionState transitionState;
    /** index of next difficulty level. */
    private static int nextDiffLevel;
    /** index of next level pack. */
    private static int nextLevelPack;

    /** index of next level. */
    private static int nextLevelNumber;
    /** index of current level pack. */
    private static int curLevelPack;
    /** index of current difficulty level. */
    private static int curDiffLevel;
    /** index of current level. */
    private static int curLevelNumber;
    /** array of available level packs. */
    private static LevelPack[] levelPack;

    /**
     * Private default constructor for utility class.
     */
    private FaderHandler() {

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
     * Returns index of next level.
     *
     * @return index of next level.
     */
    public static int getNextLevelNumber() {
        return nextLevelNumber;
    }

    /**
     * Sets index of next level.
     *
     * @param levelIndex index of next level.
     */
    public static void setNextLevelNumber(final int levelIndex) {
        FaderHandler.nextLevelNumber = levelIndex;
    }

    /**
     * Returns index of next difficulty level.
     *
     * @return index of next difficulty level.
     */
    public static int getNextDiffLevel() {
        return nextDiffLevel;
    }

    /**
     * Sets index of next difficulty level.
     *
     * @param levelIndex index of next difficulty level.
     */
    public static void setNextDiffLevel(final int levelIndex) {
        FaderHandler.nextDiffLevel = levelIndex;
    }

    /**
     * Returns index of next level pack.
     *
     * @return index of next level pack.
     */
    public static int getNextLevelPack() {
        return nextLevelPack;
    }

    /**
     * Sets index of next level pack.
     *
     * @param packIndex index of next level pack.
     */
    public static void setNextLevelPack(final int packIndex) {
        FaderHandler.nextLevelPack = packIndex;
    }

    /**
     * Returns transition (fading) state.
     *
     * @return transition (fading) state.
     */
    public static TransitionState getTransitionState() {
        return transitionState;
    }

    /**
     * Sets transition (fading) state.
     *
     * @param state transition (fading) state.
     */
    public static void setTransitionState(final TransitionState state) {
        FaderHandler.transitionState = state;
    }

    /**
     * Returns index of current difficulty level.
     *
     * @return index of current difficulty level.
     */
    public static int getCurDiffLevel() {
        return curDiffLevel;
    }

    /**
     * Sets index of current difficulty level.
     *
     * @param levelIndex index of current difficulty level.
     */
    public static void setCurDiffLevel(final int levelIndex) {
        FaderHandler.curDiffLevel = levelIndex;
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
     * Sets index of current level pack.
     *
     * @param levelPackIndex index of current level pack.
     */
    public static void setCurLevelPack(final int levelPackIndex) {
        FaderHandler.curLevelPack = levelPackIndex;
    }

    /**
     * Returns index of current level.
     *
     * @return index of current level.
     */
    public static int getCurLevelNumber() {
        return curLevelNumber;
    }

    /**
     * Sets index of current level.
     *
     * @param levelIndex index of current level.
     */
    public static void setCurLevelNumber(final int levelIndex) {
        FaderHandler.curLevelNumber = levelIndex;
    }

    /**
     * Returns array of available level packs.
     *
     * @return array of available level packs.
     */
    public static LevelPack[] getLevelPack() {
        return levelPack;
    }

    /**
     * Initialization.
     *
     * @param bgImage background image for the parent frame.
     * @param dirs    names of directories containing level packs.
     * @throws ResourceException if a problem occurs while extracting resources.
     */
    public static void init(final BufferedImage bgImage,
            final List<String> dirs) throws ResourceException {
        bgGfx = bgImage.createGraphics();
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
    public static int absLevelNum(final int lvlPack, final int diffLevel,
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
     * Request a new level.
     *
     * @param lPack    index of level pack
     * @param dLevel   index of difficulty level
     * @param lNum     level number
     * @param doReplay true: replay, false: play
     */
    public static synchronized void requestChangeLevel(final int lPack,
            final int dLevel, final int lNum, final boolean doReplay) {
        FaderHandler.setNextLevelPack(lPack);
        FaderHandler.setNextDiffLevel(dLevel);
        FaderHandler.setNextLevelNumber(lNum);

        if (doReplay) {
            FaderHandler.setTransitionState(TransitionState.LOAD_REPLAY);
        } else {
            FaderHandler.setTransitionState(TransitionState.LOAD_LEVEL);
        }

        Fader.setState(FaderState.OUT);
    }

    /**
     * Proceed to next level.
     *
     * @return true: ok, false: no more level in this difficulty level
     */
    public static synchronized boolean nextLevel() {
        final int num = FaderHandler.getCurLevelNumber() + 1;

        if (num < levelPack[curLevelPack].getLevels(curDiffLevel).length) {
            FaderHandler.setCurLevelNumber(num);
            return true;
        } else {
            return false; // congrats - difficulty level done
        }
    }

    /**
     * Request the restart of this level.
     *
     * @param doReplay
     */
    public static synchronized void requestRestartLevel(
            final boolean doReplay) {
        if (doReplay) {
            FaderHandler.setTransitionState(TransitionState.REPLAY_LEVEL);
        } else {
            FaderHandler.setTransitionState(TransitionState.RESTART_LEVEL);
        }

        Fader.setState(FaderState.OUT);
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
                GameController.finishLevel();
                break;
            case TO_BRIEFING:
                GameController.setGameState(GameState.BRIEFING);
                break;
            case TO_DEBRIEFING:
                GameController.setGameState(GameState.DEBRIEFING);
                break;
            case TO_INTRO:
                GameController.setGameState(GameState.INTRO);
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
                    frame.setTitle("Lemmini - "
                            + GameController.getLevel().getLevelName());
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
     * Restart level.
     *
     * @param doReplay true: replay, false: play
     * @param frame    the parent component (main frame of the application).
     */
    private static synchronized void restartLevel(final boolean doReplay,
            final Component frame) {
        initLevel(frame);
        ReplayController.doReplayIfReplayMode(doReplay);
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
        curLevelPack = lPack;
        curDiffLevel = dLevel;
        curLevelNumber = lNum;
        final String lvlPath = levelPack[curLevelPack]
                .getInfo(curDiffLevel, curLevelNumber).getFileName();
        // lemmings need to be reloaded to contain pink color
        LemmingImageLoader.loadLemmings(frame);
        // loading the level will patch pink lemmings pixels to correct color
        LevelLoader.loadLevel(lvlPath, frame, GameController.getLevel());

        // if width and height would be stored inside the level, the bgImage
        // etc. would have to be recreated here
        initLevel(frame);
        ReplayController.rewindIfReplayMode(doReplay);
        return GameController.getLevel();
    }

    /**
     * Initialize a level after it was loaded.
     *
     * @param frame the parent component (main frame of the application).
     */
    private static void initLevel(final Component frame) {
        Music.stop();
        GameController.setFastForward(false);
        GameController.setPaused(false);
        GameController.setNuke(false);
        LemmingHandler.initLevelsLemmings();
        TextScreen.setMode(TextScreen.Mode.INIT);
        bgGfx.setBackground(blankColor);
        final BufferedImage bgImage = GameController.getBgImage();
        bgGfx.clearRect(0, 0, bgImage.getWidth(), bgImage.getHeight());
        final Level level = GameController.getLevel();
        final LevelPainter levelPainter = new LevelPainter(level);
        GameController.setStencil(levelPainter.paintLevel(bgImage, frame,
                GameController.getStencil()));
        ExplosionHandler.initLevel();
        Icons.reset();
        TrapDoor.reset(level.getEntryNum());
        GameController.setEntryOpened(false);
        GameController.setEntryOpenCtr(0);
        GameController.setSecondCtr(0);
        SkillHandler.setLemmSkill(Type.UNDEFINED);
        ReleaseRateHandler.initLevel();
        GameController.setNumLeft(0);
        final int releaseRate = level.getReleaseRate();
        ReleaseRateHandler.setReleaseRate(releaseRate);
        GameController.setNumLemmingsMax(level.getNumLemmings());
        GameController.setNumToRescue(level.getNumToRescue());
        GameController.setTime(level.getTimeLimitSeconds());
        SkillHandler.initLevel(level);
        final int xpos = level.getXpos();
        GameController.setxPos(xpos);
        ReleaseRateHandler.calcReleaseBase();
        GameController.setMapPreview(
                level.createMiniMap(GameController.getMapPreview(), bgImage,
                        SCALE_FACTOR, SCALE_FACTOR, false));
        GameController.setSuperLemming(level.isSuperLemming());
        ReplayController.setReplayFrame(0);
        ReplayController.setStopReplayMode(false);
        GameController.setReleaseRateOld(releaseRate);
        GameController.setLemmSkillOld(SkillHandler.getLemmSkill());
        GameController.setNukeOld(false);
        GameController.setxPosOld(xpos);
        GameController.setGameState(GameState.BRIEFING);
    }

    /**
     * Fade to level.
     */
    private static void fadeToLevel() {
        SoundController.playStartOfLevelSound();

        try {
            Music.load("music/" + levelPack[curLevelPack]
                    .getInfo(curDiffLevel, curLevelNumber).getMusic());
        } catch (final ResourceException ex) {
            Core.resourceError(ex.getMessage());
        } catch (final LemmException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        GameController.setGameState(GameState.LEVEL);
    }

}
