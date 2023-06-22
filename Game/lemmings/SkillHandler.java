package game.lemmings;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import game.GameController;
import game.Icons;
import game.LemmCursor;
import game.Level;
import game.NumFont;
import game.ReleaseRateHandler;
import game.ReplayAssignSkillEvent;
import game.ReplaySelectSkillEvent;
import game.SoundController;
import game.Type;
import tools.MicrosecondTimer;
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
 * Class for handling assignable Lemmings skills. Extracted from GameController
 * by John Watne 06/2023.
 */
public final class SkillHandler {
    /**
     * Horizontal spacing between lemming skill selection icons, in pixels.
     */
    private static final int ICON_HORIZONTAL_SPACING = 8;
    /**
     * Index for the diggers selection button.
     */
    private static final int DIGGERS_INDEX = 9;
    /**
     * Index for the miners selection button.
     */
    private static final int MINERS_INDEX = 8;
    /**
     * Index for the bashers selection button.
     */
    private static final int BASHERS_INDEX = 7;
    /**
     * Index for the builders selection button.
     */
    private static final int BUILDERS_INDEX = 6;
    /**
     * Index for the blockers selection button.
     */
    private static final int BLOCKERS_INDEX = 5;
    /**
     * Index for the bombers selection button.
     */
    private static final int BOMBERS_INDEX = 4;
    /**
     * Index for the floaters selection button.
     */
    private static final int FLOATERS_INDEX = 3;
    /**
     * Standard value of 10.
     */
    private static final int DECIMAL_10 = 10;
    /**
     * Nuke icon: maximum time between two mouse clicks for double click
     * detection (in microseconds).
     */
    private static final long MICROSEC_NUKE_DOUBLE_CLICK = 240 * 1000;

    /** number of climber skills left to be assigned. */
    private static int numClimbers;
    /** number of floater skills left to be assigned. */
    private static int numFloaters;
    /** number of bomber skills left to be assigned. */
    private static int numBombers;
    /** number of blocker skills left to be assigned. */
    private static int numBlockers;
    /** number of builder skills left to be assigned. */
    private static int numBuilders;
    /** number of basher skills left to be assigned. */
    private static int numBashers;
    /** number of miner skills left to be assigned. */
    private static int numMiners;
    /** number of digger skills left to be assigned. */
    private static int numDiggers;
    /** skill to assign to lemming (skill icon). */
    private static Type lemmSkill;
    /** timer used for nuking. */
    private static MicrosecondTimer timerNuke;

    /**
     * Private default constructor for utility class.
     */
    private SkillHandler() {

    }

    /**
     * Returns timer used for nuking.
     *
     * @return timer used for nuking.
     */
    public static MicrosecondTimer getTimerNuke() {
        return timerNuke;
    }

    /**
     * Sets timer used for nuking.
     *
     * @param timer timer used for nuking.
     */
    public static void setTimerNuke(final MicrosecondTimer timer) {
        SkillHandler.timerNuke = timer;
    }

    /**
     * Returns skill to assign to lemming (skill icon).
     *
     * @return skill to assign to lemming (skill icon).
     */
    public static Type getLemmSkill() {
        return lemmSkill;
    }

    /**
     * Sets skill to assign to lemming (skill icon).
     *
     * @param skill skill to assign to lemming (skill icon).
     */
    public static void setLemmSkill(final Type skill) {
        SkillHandler.lemmSkill = skill;
    }

    /**
     * Initialize skill-handling fields for the level.
     *
     * @param level level object.
     */
    public static void initLevel(final Level level) {
        numClimbers = level.getNumClimbers();
        numFloaters = level.getNumFloaters();
        numBombers = level.getNumBombers();
        numBlockers = level.getNumBlockers();
        numBuilders = level.getNumBuilders();
        numBashers = level.getNumBashers();
        numMiners = level.getNumMiners();
        numDiggers = level.getMumDiggers();
    }

    /**
     * Assigns the selected skill to a lemming and decrements the count of
     * available number of that skill by 1.
     *
     * @param rs       the ReplayAssignSkillEvent for assigning the skill.
     * @param lemmings list of all active Lemmings in the Level.
     */
    public static void assignSkillAndDecrementAvailable(
            final ReplayAssignSkillEvent rs,
            final LinkedList<Lemming> lemmings) {
        synchronized (lemmings) {
            final Lemming l = lemmings.get(rs.getLemming());
            l.setSkill(rs.getSkill());
            l.setSelected();
        }

        switch (rs.getSkill()) {
        case FLOATER:
            numFloaters -= 1;
            break;
        case CLIMBER:
            numClimbers -= 1;
            break;
        case BOMBER:
            numBombers -= 1;
            break;
        case DIGGER:
            numDiggers -= 1;
            break;
        case BASHER:
            numBashers -= 1;
            break;
        case BUILDER:
            numBuilders -= 1;
            break;
        case MINER:
            numMiners -= 1;
            break;
        case STOPPER:
            numBlockers -= 1;
            break;
        default:
            break;
        }
    }

    /**
     * Draw the skill/release rate values.
     *
     * @param g graphics object
     * @param y y offset in pixels
     */
    public static void drawCounters(final Graphics2D g, final int y) {
        // draw counters
        int val = 0;

        for (int i = 0; i < DECIMAL_10; i++) {
            switch (i) {
            case 0:
                val = GameController.getLevel().getReleaseRate();
                break;
            case 1:
                val = ReleaseRateHandler.getReleaseRate();
                break;
            case 2:
                val = numClimbers;
                break;
            case FLOATERS_INDEX:
                val = numFloaters;
                break;
            case BOMBERS_INDEX:
                val = numBombers;
                break;
            case BLOCKERS_INDEX:
                val = numBlockers;
                break;
            case BUILDERS_INDEX:
                val = numBuilders;
                break;
            case BASHERS_INDEX:
                val = numBashers;
                break;
            case MINERS_INDEX:
                val = numMiners;
                break;
            case DIGGERS_INDEX:
                val = numDiggers;
                break;
            default:
                break;
            }

            g.drawImage(NumFont.numImage(val),
                    Icons.WIDTH * i + ICON_HORIZONTAL_SPACING, y, null);
        }

    }

    /**
     * Indicates whether the lemming's skill can be set to the selected one,
     * given that cheat mode is NOT enabled.
     *
     * @param lemm the lemming whose skill is to be set, if possible.
     * @return <code>true</code> if the lemming's skill can be set to the
     *         selected one, given that cheat mode is NOT enabled.
     */
    public static boolean canSetSkillOfLemmingIfNotCheatMode(
            final Lemming lemm) {
        boolean canSet = false;

        switch (lemmSkill) {
        case BASHER:
            if (numBashers > 0 && lemm.setSkill(lemmSkill)) {
                numBashers -= 1;
                canSet = true;
            }

            break;
        case BOMBER:
            if (numBombers > 0 && lemm.setSkill(lemmSkill)) {
                numBombers -= 1;
                canSet = true;
            }

            break;
        case BUILDER:
            if (numBuilders > 0 && lemm.setSkill(lemmSkill)) {
                numBuilders -= 1;
                canSet = true;
            }

            break;
        case CLIMBER:
            if (numClimbers > 0 && lemm.setSkill(lemmSkill)) {
                numClimbers -= 1;
                canSet = true;
            }

            break;
        case DIGGER:
            if (numDiggers > 0 && lemm.setSkill(lemmSkill)) {
                numDiggers -= 1;
                canSet = true;
            }

            break;
        case FLOATER:
            if (numFloaters > 0 && lemm.setSkill(lemmSkill)) {
                numFloaters -= 1;
                canSet = true;
            }

            break;
        case MINER:
            if (numMiners > 0 && lemm.setSkill(lemmSkill)) {
                numMiners -= 1;
                canSet = true;
            }

            break;
        case STOPPER:
            if (numBlockers > 0 && lemm.setSkill(lemmSkill)) {
                numBlockers -= 1;
                canSet = true;
            }

            break;
        default:
            break;
        }

        return canSet;
    }

    /**
     * Get a Lemming under the selection cursor.
     *
     * @param type cursor type
     * @return fitting Lemming or null if none found
     */
    public static synchronized Lemming lemmUnderCursor(
            final LemmCursor.Type type) {
        // search for level without the skill
        final List<Lemming> lemmsUnderCursor = GameController
                .getLemmsUnderCursor();

        for (int i = 0; i < lemmsUnderCursor.size(); i++) {
            final Lemming l = lemmsUnderCursor.get(i);

            // Walker only cursor: ignore non-walkers
            if (type == LemmCursor.Type.WALKER && l.getSkill() != Type.WALKER) {
                continue;
            }

            if (type == LemmCursor.Type.LEFT
                    && l.getDirection() != Direction.LEFT) {
                continue;
            }

            if (type == LemmCursor.Type.RIGHT
                    && l.getDirection() != Direction.RIGHT) {
                continue;
            }

            switch (lemmSkill) {
            case CLIMBER:
                if (!l.canClimb()) {
                    return l;
                }
                break;
            case FLOATER:
                if (!l.canFloat()) {
                    return l;
                }
                break;
            default:
                if (l.canChangeSkill() && l.getSkill() != lemmSkill
                        && l.getName().length() > 0) {
                    // System.out.println(l.getName());
                    return l;
                }
            }

            break;
        }

        if (type == LemmCursor.Type.NORMAL && lemmsUnderCursor.size() > 0) {
            final Lemming l = lemmsUnderCursor.get(0);

            if (l.getName().length() == 0) {
                return null;
            }

            return l;
        }

        return null;
    }

    /**
     * Selects the specified skill.
     *
     * @param rsse the ReplayEvent for selecting the skill.
     */
    public static void selectSkill(final ReplaySelectSkillEvent rsse) {
        lemmSkill = rsse.getSkill();

        switch (lemmSkill) {
        case FLOATER:
            Icons.press(Icons.Type.FLOAT);
            break;
        case CLIMBER:
            Icons.press(Icons.Type.CLIMB);
            break;
        case BOMBER:
            Icons.press(Icons.Type.BOMB);
            break;
        case DIGGER:
            Icons.press(Icons.Type.DIG);
            break;
        case BASHER:
            Icons.press(Icons.Type.BASH);
            break;
        case BUILDER:
            Icons.press(Icons.Type.BUILD);
            break;
        case MINER:
            Icons.press(Icons.Type.MINE);
            break;
        case STOPPER:
            Icons.press(Icons.Type.BLOCK);
            break;
        default:
            break;
        }
    }

    /**
     * Handle pressing of an icon button.
     *
     * @param type icon type
     */
    public static synchronized void handleIconButton(final Icons.Type type) {
        final Type startingSkill = lemmSkill;
        boolean ok = false;

        switch (type) {
        case FLOAT:
            if (GameController.isCheat() || numFloaters > 0) {
                lemmSkill = Type.FLOATER;
            }

            GameController.stopReplayMode();
            break;
        case CLIMB:
            if (GameController.isCheat() || numClimbers > 0) {
                lemmSkill = Type.CLIMBER;
            }

            GameController.stopReplayMode();
            break;
        case BOMB:
            if (GameController.isCheat() || numBombers > 0) {
                lemmSkill = Type.BOMBER;
            }

            GameController.stopReplayMode();
            break;
        case DIG:
            if (GameController.isCheat() || numDiggers > 0) {
                lemmSkill = Type.DIGGER;
            }

            GameController.stopReplayMode();
            break;
        case BASH:
            if (GameController.isCheat() || numBashers > 0) {
                lemmSkill = Type.BASHER;
            }

            GameController.stopReplayMode();
            break;
        case BUILD:
            if (GameController.isCheat() || numBuilders > 0) {
                lemmSkill = Type.BUILDER;
            }

            GameController.stopReplayMode();
            break;
        case MINE:
            if (GameController.isCheat() || numMiners > 0) {
                lemmSkill = Type.MINER;
            }

            GameController.stopReplayMode();
            break;
        case BLOCK:
            if (GameController.isCheat() || numBlockers > 0) {
                lemmSkill = Type.STOPPER;
            }

            GameController.stopReplayMode();
            break;
        case NUKE:
            ok = true;
            handleNukeIconButton();
            break;
        case PAUSE:
            GameController.setPaused(!GameController.isPaused());
            ok = true;
            break;
        case FFWD:
            GameController.setFastForward(!GameController.isFastForward());
            ok = true;
            break;
        case PLUS:
            ok = true; // supress sound
            ReleaseRateHandler.getPlus().pressed(GameController.KEYREPEAT_ICON);
            GameController.stopReplayMode();
            break;
        case MINUS:
            ok = true; // supress sound
            ReleaseRateHandler.getMinus()
                    .pressed(GameController.KEYREPEAT_ICON);
            GameController.stopReplayMode();
            break;
        default:
            break;
        }

        playIconButtonPressSound(type, startingSkill, ok);
    }

    /**
     * Handle pressing of nuke icon button.
     */
    private static void handleNukeIconButton() {
        GameController.stopReplayMode();

        if (timerNuke.delta() < MICROSEC_NUKE_DOUBLE_CLICK) {
            if (!GameController.isNuke()) {
                GameController.setNuke(true);
                SoundController.playNukeSound();
            }
        } else {
            timerNuke.deltaUpdate();
        }
    }

    /**
     * Plays the sound appropriate for the icon button pressed, if any.
     *
     * @param type          the icon type.
     * @param startingSkill the lemming skill before pressing the button.
     * @param ok            <code>true</code> if suppressing default icon button
     *                      press sound.
     */
    private static void playIconButtonPressSound(final Icons.Type type,
            final Type startingSkill, final boolean ok) {
        if (ok || lemmSkill != startingSkill) {
            switch (type) {
            case PLUS:
                // supress sound
            default:
                SoundController.playSettingNewSKillSound();
            }

            Icons.press(type);
        } else {
            SoundController.playTingSound();
        }
    }

}
