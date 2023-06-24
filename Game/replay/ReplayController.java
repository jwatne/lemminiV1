package game.replay;
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

import java.awt.image.BufferedImage;
import java.util.LinkedList;

import game.Core;
import game.GameController;
import game.MiscGfx;
import game.SoundController;
import game.Type;
import game.lemmings.Lemming;
import game.lemmings.SkillHandler;
import game.level.ReleaseRateHandler;

/**
 * Replay controller. Handles replay code moved from GameController.
 */
public final class ReplayController {
    /**
     * Hexidecimal value 0x20.
     */
    private static final int REPLAY_IMAGE_CUTOFF = 0x20;
    /**
     * Replay frame mask value = Hexidecimal value 0x3f.
     */
    private static final int REPLAY_FRAME_MASK = 0x3f;
    /** replay stream used for handling replays. */
    private static ReplayStream replay;
    /** frame counter used for handling replays. */
    private static int replayFrame;
    /** flag: replay mode is active. */
    private static boolean replayMode;
    /** flag: replay mode should be stopped. */
    private static boolean stopReplayMode;

    /**
     * Indicates whether replay mode should be stopped.
     *
     * @return <code>true</code> if replay mode should be stopped.
     */
    public static boolean isStopReplayMode() {
        return stopReplayMode;
    }

    /**
     * Sets whether replay mode should be stopped.
     *
     * @param mode <code>true</code> if replay mode should be stopped.
     */
    public static void setStopReplayMode(final boolean mode) {
        ReplayController.stopReplayMode = mode;
    }

    /**
     * Indicates whether replay mode is active.
     *
     * @return <code>true</code> if replay mode is active..
     */
    public static boolean isReplayMode() {
        return replayMode;
    }

    /**
     * Sets whether replay mode is active.
     *
     * @param mode <code>true</code> if replay mode is active.
     */
    public static void setReplayMode(final boolean mode) {
        ReplayController.replayMode = mode;
    }

    /**
     * Returns frame counter used for handling replays.
     *
     * @return frame counter used for handling replays.
     */
    public static int getReplayFrame() {
        return replayFrame;
    }

    /**
     * Sets frame counter used for handling replays.
     *
     * @param frame frame counter used for handling replays.
     */
    public static void setReplayFrame(final int frame) {
        ReplayController.replayFrame = frame;
    }

    /**
     * Private default constructor for utility class.
     */
    private ReplayController() {

    }

    /**
     * Initialization.
     */
    public static void init() {
        replayFrame = 0;
        replay = new ReplayStream();
        replayMode = false;
        stopReplayMode = false;
    }

    /**
     * Save replay and rewind when restarting level.
     */
    public static void doReplay() {
        replay.save(Core.getResourcePath() + "/replay.rpl");
        replay.rewind();
    }

    /**
     * Clear the replay buffer.
     */
    public static void clear() {
        replay.clear();

    }

    /**
     * Rewind replay to start position.
     */
    public static void rewind() {
        replay.rewind();
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
     * Increment replayFrame by 1.
     */
    public static void incrementReplayFrame() {
        replayFrame++;
    }

    /**
     * Handle replay mode-specific portions of frame update.
     *
     * @param lemmings List of all active Lemmings in the Level.
     */
    public static void handleReplayModeUpdate(
            final LinkedList<Lemming> lemmings) {
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
                GameController.setNuke(true);
                break;
            case ReplayStream.MOVE_XPOS:
                final ReplayMoveXPosEvent rx = (ReplayMoveXPosEvent) r;
                GameController.setxPos(rx.getxPos());
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
     * Adds a release rate event to the replay.
     *
     * @param releaseRate the updated release rate.
     * @return the updated release rate.
     */
    public static int addReleaseRateEvent(final int releaseRate) {
        replay.addReleaseRateEvent(replayFrame, releaseRate);
        return releaseRate;
    }

    /**
     * Adds a nuke event to the replay.
     *
     * @param nuke flag: nuke was activated.
     * @return flag: nuke was activated.
     */
    public static boolean addNukeEvent(final boolean nuke) {
        replay.addNukeEvent(replayFrame);
        return nuke;
    }

    /**
     * Adds an x position event to the replay.
     *
     * @param xPos new screen position.
     */
    public static void addXPosEvent(final int xPos) {
        replay.addXPosEvent(replayFrame, xPos);
    }

    /**
     * Adds a select skill event to the replay.
     *
     * @param lemmSkill skill selected.
     */
    public static void addSelectSkillEvent(final Type lemmSkill) {
        replay.addSelectSkillEvent(replayFrame, lemmSkill);
    }

    /**
     * Does a replay mode if in replay mode.
     *
     * @param doReplay true: replay, false: play.
     */
    public static void doReplayIfReplayMode(final boolean doReplay) {
        if (doReplay) {
            replayMode = true;
            ReplayController.doReplay();
        } else {
            replayMode = false;
            ReplayController.clear();
        }
    }

    /**
     * Rewinds replay to start position if in replay mode.
     *
     * @param doReplay <code>true</code> if replay mode is to be active.
     */
    public static void rewindIfReplayMode(final boolean doReplay) {
        if (doReplay) {
            replayMode = true;
            ReplayController.rewind();
        } else {
            replayMode = false;
            ReplayController.clear();
        }
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
     * Checks whether in replay mode and should stop replay mode. If so, stops
     * replay mode and clears both flag attributes.
     */
    public static void testForEndOfReplayMode() {
        if (replayMode && stopReplayMode) {
            replay.clearFrom(replayFrame);
            replayMode = false;
            stopReplayMode = false;
        }
    }

    /**
     * Adds an assign skill event to the replay.
     *
     * @param delete    flag: reset the current skill request
     * @param lemmSkill skill to assign to lemming (skill icon).
     * @param lemming   Number of the lemming to assign the skill.
     */
    public static void addAssignSkillEvent(final boolean delete,
            final Type lemmSkill, final int lemming) {
        replay.addAssignSkillEvent(replayFrame + ((delete) ? 1 : 0), lemmSkill,
                lemming);
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
}
