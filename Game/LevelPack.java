package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * Handling of a level pack.
 *
 * @author Volker Oth
 */
public class LevelPack {

    /**
     * Max fall distance.
     */
    private static final int MAX_FALL_DISTANCE = 126;
    /** name of the level pack. */
    private final String name;
    /** seed used to generate the level codes. */
    private final String codeSeed;
    /**
     * array containing names of difficulty levels (easiest first, hardest
     * last).
     */
    private String[] diffLevels;
    /** array of array of level info - [difficulty][level number]. */
    private final LevelInfo[][] lvlInfo;
    /** path of level pack - where the INI files for the level are located. */
    private final String path;
    /** maximum number of pixels a Lemming can fall before he dies. */
    private final int maxFallDistance;
    /** offset to apply in level code algorithm. */
    private final int codeOffset;

    /**
     * Constructor for dummy level pack. Needed for loading single levels.
     */
    public LevelPack() {
        name = "test";
        path = "";
        codeSeed = "AAAAAAAAAA";
        maxFallDistance = MAX_FALL_DISTANCE;
        codeOffset = 0;

        diffLevels = new String[1];
        diffLevels[0] = "test";

        lvlInfo = new LevelInfo[1][1];
        lvlInfo[0][0] = new LevelInfo();
        // lvlInfo[0][0].code = "..........";
        lvlInfo[0][0].setMusic("tim1.mod");
        lvlInfo[0][0].setName("test");
        lvlInfo[0][0].setFileName("");
    }

    /**
     * Constructor for loading a level pack.
     *
     * @param fname file name of level pack ini
     * @throws ResourceException
     */
    public LevelPack(final String fname) throws ResourceException {
        // extract path from descriptor file
        path = ToolBox.getPathName(fname);
        // load the descriptor file
        final Props props = new Props();

        if (!props.load(fname)) {
            throw new ResourceException(fname);
        }

        // read name
        name = props.get("name", "");
        // read code seed
        codeSeed = props.get("codeSeed", "").trim().toUpperCase();
        // read code level offset
        codeOffset = props.get("codeOffset", 0);
        // read max falling distance
        maxFallDistance = props.get("maxFallDistance", MAX_FALL_DISTANCE);
        // read levels of difficulty
        final List<String> difficulty = new ArrayList<String>(); // <String>
        int idx = 0;
        String diffLevel;

        do {
            diffLevel = props.get("level_" + Integer.toString(idx++), "");

            if (diffLevel.length() > 0) {
                difficulty.add(diffLevel);
            }
        } while (diffLevel.length() > 0);

        diffLevels = new String[difficulty.size()];
        diffLevels = difficulty.toArray(diffLevels);
        // read music files
        final List<String> music = new ArrayList<String>(); // <String>
        String track;
        idx = 0;

        do {
            track = props.get("music_" + Integer.toString(idx++), "");

            if (track.length() > 0) {
                music.add(track);
            }
        } while (track.length() > 0);

        // read levels
        lvlInfo = new LevelInfo[difficulty.size()][];
        String[] levelStr;
        final String[] def = {""};

        for (int diff = 0; diff < difficulty.size(); diff++) {
            idx = 0;
            final List<LevelInfo> levels = new ArrayList<LevelInfo>();
            diffLevel = difficulty.get(diff);

            do {
                levelStr = props.get(
                        diffLevel.toLowerCase() + "_" + Integer.toString(idx),
                        def);

                // filename, music number
                if (levelStr.length == 2) {
                    // get name from ini file
                    final Props lvlProps = new Props();
                    lvlProps.load(path + "/"/* +lvlPath+"/" */ + levelStr[0]);
                    // Now put everything together
                    final LevelInfo info = new LevelInfo();
                    info.setFileName(
                            path + "/"/* +lvlPath+"/" */ + levelStr[0]);
                    info.setMusic(
                            music.get(Integer.parseInt(levelStr[1/* 2 */])));
                    // info.code = levelStr[1];
                    info.setName(lvlProps.get("name", "")); // only used in menu
                    levels.add(info);
                }

                idx++;
            } while (levelStr.length == 2);

            lvlInfo[diff] = new LevelInfo[levels.size()];
            lvlInfo[diff] = levels.toArray(lvlInfo[diff]);
        }
    }

    /**
     * Assemble level pack and difficulty level to string.
     *
     * @param pack level pack
     * @param diff name of difficulty level
     * @return String formed from level pack and difficulty level
     */
    public static String getID(final String pack, final String diff) {
        return pack.toLowerCase() + "-" + diff.toLowerCase();
    }

    /**
     * Return levels of difficulty as string array.
     *
     * @return levels of difficulty as string array
     */
    public String[] getDiffLevels() {
        // Use modern Stream API to pass copy of array - avoids possible
        // exposure of internal representation by returning a reference to a
        // mutable object.
        return Arrays.stream(diffLevels).toArray(String[]::new);
    }

    /**
     * Get name of level pack.
     *
     * @return name of level pack
     */
    public String getName() {
        return name;
    }

    /**
     * Get code seed.
     *
     * @return code seed.
     */
    public String getCodeSeed() {
        return codeSeed;
    }

    /**
     * Get maximum fall distance.
     *
     * @return maximum fall distance
     */
    public int getMaxFallDistance() {
        return maxFallDistance;
    }

    /**
     * Get offset to apply in level code algorithm.
     *
     * @return offset to apply in level code algorithm
     */
    public int getCodeOffset() {
        return codeOffset;
    }

    /**
     * Get level info for a certain level.
     *
     * @param diffLvl difficulty level
     * @param level   level number
     * @return LevelInfo for the given level
     */
    public LevelInfo getInfo(final int diffLvl, final int level) {
        return lvlInfo[diffLvl][level];
    }

    /**
     * Return all levels for a given difficulty.
     *
     * @param diffLevel number of difficulty level
     * @return level names as string array
     */
    public String[] getLevels(final int diffLevel) {
        final String[] names = new String[lvlInfo[diffLevel].length];

        for (int i = 0; i < lvlInfo[diffLevel].length; i++) {
            names[i] = lvlInfo[diffLevel][i].getName();
        }

        return names;
    }
}
