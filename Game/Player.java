package game;

import java.util.HashMap;
import java.util.Map;

import tools.FileUtils;
import tools.Props;

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
 * Stores player progress.
 *
 * @author Volker Oth
 */
public class Player {

    /** property class to store player settings persistently. */
    private final Props props;
    /** name of the INI file used for persistence. */
    private final String iniFileStr;
    /** used to store level progress. */
    private final Map<String, GroupBitfield> lvlGroup;
    /** cheat mode enabled?. */
    private boolean cheat;
    /** player's name. */
    private final String name;

    /**
     * Constructor.
     *
     * @param n player's name
     */
    public Player(final String n) {
        name = n;
        lvlGroup = new HashMap<String, GroupBitfield>();
        // read main ini file
        props = new Props();
        // create players directory if it doesn't exist
        final String pathname = Core.getResourcePath() + "players";
        FileUtils.makeDirIfItDoesNotExist(pathname);

        iniFileStr = pathname + "/" + name + ".ini";

        if (props.load(iniFileStr)) {
            // might exist or not - if not, it's created
            // file existed, now extract entries
            final String[] sdef = {null, null};

            for (int idx = 0; true; idx++) {
                final String[] s = props.get("group" + Integer.toString(idx),
                        sdef);
                if (s == null || s.length != 2 || s[0] == null) {
                    break;
                }

                // first string is the level group key identifier
                // second string is a GroupBitfield used as bitfield to store
                // won levels
                lvlGroup.put(s[0], new GroupBitfield(s[1]));
            }
        }

        // cheat mode
        cheat = false;
    }

    /**
     * Enable cheat mode for this player.
     */
    public void enableCheatMode() {
        cheat = true;
    }

    /**
     * Store player's progress.
     */
    public void store() {
        int idx = 0;

        for (final var entry : lvlGroup.entrySet()) {
            final String s = entry.getKey();
            final GroupBitfield bf = entry.getValue();
            final String sout = s + ", " + bf.toString();
            props.set("group" + Integer.toString(idx++), sout);
        }

        props.save(iniFileStr);
    }

    /**
     * Allow a level to be played.
     *
     * @param pack level pack
     * @param diff difficulty level
     * @param num  level number
     * @return updated bitfield
     */
    public GroupBitfield setAvailable(final String pack, final String diff,
            final int num) {
        // get current bitfield
        final String id = LevelPack.getID(pack, diff);
        GroupBitfield bf = lvlGroup.get(id);

        if (bf == null) {
            bf = GroupBitfield.ONE; // first level is always available
        }

        bf = new GroupBitfield(bf.setBit(num)); // set bit in bitfield (just
                                                // overwrite existing bit)
        // store new value
        lvlGroup.put(id, bf);
        return bf;
    }

    /**
     * Check if player is allowed to play a level.
     *
     * @param pack level pack
     * @param diff difficulty level
     * @param num  level number
     * @return true if allowed, false if not
     */
    public boolean isAvailable(final String pack, final String diff,
            final int num) {
        if (isCheat()) {
            return true;
        }

        // get current bitfield
        final String id = LevelPack.getID(pack, diff);
        GroupBitfield bf = lvlGroup.get(id);

        if (bf == null) {
            bf = GroupBitfield.ONE; // first level is always available
        }

        return (bf.testBit(num));
    }

    /**
     * Check if player is allowed to play a level.
     *
     * @param bf  bitfield containing the approval information for all levels of
     *            this pack/difficulty
     * @param num number of level
     * @return true if allowed, false if not
     */
    public boolean isAvailable(final GroupBitfield bf, final int num) {
        if (isCheat()) {
            return true;
        }

        return (bf.testBit(num));
    }

    /**
     * Get bitfield containing the approval information for all levels of this
     * pack/difficulty.
     *
     * @param pack level pack
     * @param diff difficulty level
     * @return bitfield containing the approval information for all levels of
     *         this pack/difficulty
     */
    public GroupBitfield getBitField(final String pack, final String diff) {
        if (isCheat()) {
            return new GroupBitfield("18446744073709551615");
            // 0xffffffffffffffff (8 bytes with all bits set)
        }

        final String id = LevelPack.getID(pack, diff);
        final GroupBitfield bf = lvlGroup.get(id);

        if (bf == null) {
            return GroupBitfield.ONE;
        }

        return bf;
    }

    /**
     * Get player's name.
     *
     * @return player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Get cheat state.
     *
     * @return true if cheat is enabled
     */
    public boolean isCheat() {
        return cheat;
    }
}
