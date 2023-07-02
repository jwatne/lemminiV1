package game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import game.lemmings.Lemming;
import game.lemmings.SkillHandler;
import game.level.Entry;
import game.level.Level;
import game.level.ReleaseRateHandler;
import game.replay.ReplayController;
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
 * Utility class for handling groups of Lemmings.
 */
public final class LemmingHandler {
    /**
     * Initial capacity of ArrayList for lemmsUnderCursor.
     */
    private static final int INITIAL_CAPACITY = 10;

    /** list of all active Lemmings in the Level. */
    private static LinkedList<Lemming> lemmings;
    /** list of all Lemmings under the mouse cursor. */
    private static List<Lemming> lemmsUnderCursor;
    /** Lemming for which skill change is requested. */
    private static Lemming lemmSkillRequest;
    /** number of Lemmings who entered the level. */
    private static int numLemmingsOut;

    /**
     * Private default constructor for utility class.
     */
    private LemmingHandler() {

    }

    /**
     * Returns number of Lemmings who entered the level.
     *
     * @return number of Lemmings who entered the level.
     */
    public static int getNumLemmingsOut() {
        return numLemmingsOut;
    }

    /**
     * Sets number of Lemmings who entered the level.
     *
     * @param numberOut number of Lemmings who entered the level.
     */
    public static void setNumLemmingsOut(final int numberOut) {
        LemmingHandler.numLemmingsOut = numberOut;
    }

    /** frame counter used to handle release of new Lemmings. */
    private static int releaseCtr;

    /**
     * Returns value of frame counter used to handle release of new Lemmings.
     *
     * @return value of frame counter used to handle release of new Lemmings.
     */
    public static int getReleaseCtr() {
        return releaseCtr;
    }

    /**
     * Sets value of frame counter used to handle release of new Lemmings.
     *
     * @param counter value of frame counter used to handle release of new
     *                Lemmings.
     */
    public static void setReleaseCtr(final int counter) {
        LemmingHandler.releaseCtr = counter;
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
     * Assign the selected skill to the selected Lemming.
     *
     * @param delete flag: reset the current skill request
     */
    public static synchronized void assignSkill(final boolean delete) {
        final Type lemmSkill = SkillHandler.getLemmSkill();

        if (lemmSkillRequest == null || Type.UNDEFINED == lemmSkill) {
            return;
        }

        final Lemming lemm = lemmSkillRequest;

        if (delete) {
            lemmSkillRequest = null;
        }

        ReplayController.stopReplayMode();
        final boolean canSet = canSetSkill(lemm);

        if (canSet) {
            lemmSkillRequest = null; // erase request
            SoundController.playMouseClickedSound();

            if (GameController.isPaused()) {
                GameController.setPaused(false);
                Icons.press(Icons.Type.PAUSE);
            }

            // add to replay stream
            if (!GameController.isWasCheated()) {
                synchronized (lemmings) {
                    for (int i = 0; i < lemmings.size(); i++) {
                        if (lemmings.get(i) == lemm) {
                            // if 2nd try (delete==true) assign to next frame
                            ReplayController.addAssignSkillEvent(delete,
                                    lemmSkill, i);
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
    public static boolean canSetSkill(final Lemming lemm) {
        boolean canSet = false;

        if (GameController.isCheat()) {
            canSet = lemm.setSkill(SkillHandler.getLemmSkill());
        } else {
            canSet = SkillHandler.canSetSkillOfLemmingIfNotCheatMode(lemm);
        }

        return canSet;
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

        ReplayController.stopReplayMode();
    }

    /**
     * Releases lemmings if appropriate to current game conditions.
     *
     * @param nukeTemp    Stored initial value of {@link #nuke}.
     * @param entryOpened <code>true</code> if entry is opened.
     */
    public static void releaseLemmings(final boolean nukeTemp,
            final boolean entryOpened) {
        if (entryOpened && !nukeTemp && !GameController.isPaused()
                && numLemmingsOut < GameController.getNumLemmingsMax()
                && ++releaseCtr >= ReleaseRateHandler.getReleaseBase()) {
            releaseCtr = 0;

            try {
                final Level level = GameController.getLevel();

                if (level.getEntryNum() != 0) {
                    final Entry e = level.getEntry(TrapDoor.getNext());
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
     * Handle nuking if appropriate.
     *
     * @param nukeTemp  Stored initial value of {@link #nuke}.
     * @param updateCtr free running update counter.
     */
    public static void nuke(final boolean nukeTemp, final int updateCtr) {
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
     * Animate or remove lemmings from frame.
     */
    public static void animateLemmings() {
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
     * Intializes a Level's group of Lemmings after the Level is loaded.
     */
    public static void initLevelsLemmings() {
        lemmSkillRequest = null;
        lemmings.clear();
        releaseCtr = 0;
        numLemmingsOut = 0;
    }

    /**
     * Initialization.
     */
    public static void init() {
        lemmings = new LinkedList<Lemming>();
        lemmsUnderCursor = new ArrayList<Lemming>(INITIAL_CAPACITY);
        lemmSkillRequest = null;
    }
}
