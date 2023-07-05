package game.replay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import game.LevelPack;
import game.Type;
import gameutil.FaderHandler;

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
 * Handle replays.
 *
 * @author Volker Oth
 */
public class ReplayStream {
    /**
     * Index position of lemming number in frame of replay data.
     */
    public static final int LEMMING_NUMBER_INDEX = 3;
    // event types
    /**
     * Assign skill event.
     */
    public static final int ASSIGN_SKILL = 0;
    /**
     * Screen x position change event.
     */
    public static final int MOVE_XPOS = 1;
    /**
     * Select skill event.
     */
    public static final int SELECT_SKILL = 2;
    /**
     * Set release rate event.
     */
    public static final int SET_RELEASE_RATE = 3;
    /**
     * Nuke event.
     */
    public static final int NUKE = 4;

    /**
     * List of replay events.
     */
    private List<ReplayEvent> events;
    /**
     * Position in List of replay events.
     */
    private int replayIndex;

    /**
     * Constructor.
     */
    public ReplayStream() {
        events = new ArrayList<ReplayEvent>(); // <events>
        replayIndex = 0;
    }

    /**
     * Rewind replay to start position.
     */
    public void rewind() {
        replayIndex = 0;
    }

    /**
     * Get next replay event.
     *
     * @param ctr frame counter
     * @return replay event
     */
    public ReplayEvent getNext(final int ctr) {
        if (replayIndex >= events.size()) {
            return null;
        }

        final ReplayEvent r = events.get(replayIndex);

        /*
         * Note: there can be multiple replay events for one frame. return the
         * next stored event if was stored for a frame smaller or equal to the
         * given frame counter.
         */
        if (ctr >= r.getFrameCtr()) {
            replayIndex++;
            return r;
        }

        return null; /* no more events for this frame */
    }

    /**
     * Clear the replay buffer.
     */
    public void clear() {
        events.clear();
    }

    /**
     * Clear the replay buffer from a certain frame counter.
     *
     * @param ctr frame counter
     */
    public void clearFrom(final int ctr) {
        /* Note: there can be multiple replay events for one frame. */
        for (int i = events.size() - 1; i > 0; i--) {
            final ReplayEvent r = events.get(i);

            if (r.getFrameCtr() > ctr // clearly behind ctr -> erase
                    || r.getFrameCtr() == ctr && i > replayIndex) {
                // but after
                // replayIndex ->
                // erase
                events.remove(i);
            } else {
                break;
            }
        }

        replayIndex = 0;
    }

    /**
     * Load replay buffer from file.
     *
     * @param fname file name
     * @return replay information
     */
    public ReplayLevelInfo load(final String fname) {
        final List<ReplayEvent> ev = new ArrayList<ReplayEvent>();

        try (BufferedReader f = new BufferedReader(new FileReader(fname))) {
            String line = f.readLine();

            if (!"#REPLAY".equals(line)) {
                return null;
            }

            // read level info
            line = f.readLine();

            if (line == null) {
                return null;
            }

            String[] e = line.split(",");

            for (int j = 0; j < e.length; j++) {
                e[j] = e[j].trim();
            }

            final ReplayLevelInfo rli = new ReplayLevelInfo();

            if (e[0].charAt(0) != '#') {
                return null;
            }

            rli.setLevelPack(e[0].substring(1));
            rli.setDiffLevel(Integer.parseInt(e[1]));
            rli.setLvlNumber(Integer.parseInt(e[2]));

            // read events
            while ((line = f.readLine()) != null) {
                e = line.split(",");
                final int[] i = new int[e.length];

                for (int j = 0; j < e.length; j++) {
                    i[j] = Integer.parseInt(e[j].trim());
                }

                switch (i[1] /* type */) {
                case ASSIGN_SKILL:
                    ev.add(new ReplayAssignSkillEvent(i[0], Type.get(i[2]),
                            i[LEMMING_NUMBER_INDEX]));
                    break;
                case MOVE_XPOS:
                    ev.add(new ReplayMoveXPosEvent(i[0], i[2]));
                    break;
                case SELECT_SKILL:
                    ev.add(new ReplaySelectSkillEvent(i[0], Type.get(i[2])));
                    break;
                case SET_RELEASE_RATE:
                    ev.add(new ReplayReleaseRateEvent(i[0], i[2]));
                    break;
                case NUKE:
                    ev.add(new ReplayEvent(i[0], NUKE));
                    break;
                default:
                    return null;
                }
            }

            events = ev;
            return rli;
        } catch (final FileNotFoundException e) {
            return null;
        } catch (final IOException e) {
            return null;
        } catch (final NumberFormatException e) {
            return null;
        } catch (final ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Store replay info in a file.
     *
     * @param fname file name
     * @return true if save ok, false otherwise
     */
    public boolean save(final String fname) {
        try (FileWriter f = new FileWriter(new File(fname))) {
            f.write("#REPLAY\n");
            final LevelPack lp = FaderHandler.getCurLevelPack();
            f.write("#" + lp.getName() + ", " + FaderHandler.getCurDiffLevel()
                    + ", " + FaderHandler.getCurLevelNumber() + "\n");

            for (int i = 0; i < events.size(); i++) {
                final ReplayEvent r = events.get(i);
                f.write(r.toString() + "\n"); // will use toString of the
                                              // correct child object
            }

            return true;
        } catch (final FileNotFoundException e) {
            return false;
        } catch (final IOException e) {
            return false;
        }
    }

    /**
     * Add a NUKE event (all lemmings nuked).
     *
     * @param ctr frame counter
     */
    public void addNukeEvent(final int ctr) {
        final ReplayEvent event = new ReplayEvent(ctr, NUKE);
        events.add(event);
    }

    /**
     * Add ASSIGN_SKILL event (one lemming was assigned a skill).
     *
     * @param ctr     frame counter
     * @param skill   skill assigned
     * @param lemming Lemming the skill was assigned to
     */
    public void addAssignSkillEvent(final int ctr, final Type skill,
            final int lemming) {
        final ReplayAssignSkillEvent event = new ReplayAssignSkillEvent(ctr,
                skill, lemming);
        events.add(event);
    }

    /**
     * Add SELECT_SKILL event (skill selection button was pressed).
     *
     * @param ctr   frame counter
     * @param skill skill selected
     */
    public void addSelectSkillEvent(final int ctr, final Type skill) {

        final ReplaySelectSkillEvent event = new ReplaySelectSkillEvent(ctr,
                skill);
        events.add(event);
    }

    /**
     * Add MOVE_XPOS event (screen moved left/right).
     *
     * @param ctr  frame counter
     * @param xPos new screen position
     */
    public void addXPosEvent(final int ctr, final int xPos) {
        final ReplayMoveXPosEvent event = new ReplayMoveXPosEvent(ctr, xPos);
        events.add(event);
    }

    /**
     * Add SET_RELEASE_RATE event (release rate was changed).
     *
     * @param ctr         frame counter
     * @param releaserate new release rate
     */
    public void addReleaseRateEvent(final int ctr, final int releaserate) {
        final ReplayReleaseRateEvent event = new ReplayReleaseRateEvent(ctr,
                releaserate);
        events.add(event);
    }
}
