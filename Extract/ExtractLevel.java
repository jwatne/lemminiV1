package extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lemmini.Constants;

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
 * Convert binary "Lemmings for Win95" level files into text format.
 */
public final class ExtractLevel {
    /**
     * Size of level name in bytes.
     */
    private static final int LEVEL_NAME_SIZE = 32;
    /**
     * Bytes per tile = 4.
     */
    private static final int BYTES_PER_TILE = 4;
    /**
     * # of tiles.
     */
    private static final int NUM_TILES = 400;
    /**
     * Number of objects.
     */
    private static final int NUM_OBJECTS = 32;
    /**
     * Buffer array size.
     */
    private static final int BUFFER_SIZE = 8;
    /**
     * Maximum Lemmings level file size, in bytes.
     */
    private static final int MAX_FILE_SIZE = 2048;
    /**
     * Scale (to conver lowres levels into hires levels).
     */
    private static final int SCALE = 2;
    /** names for defautl styles. */
    private static final String[] STYLES = {"dirt", "fire", "marble", "pillar",
            "crystal", "brick", "rock", "snow", "bubble"};

    /** release rate : 0 is slowest, 0x0FA (250) is fastest. */
    private static int releaseRate;
    /**
     * number of Lemmings in this level (maximum 0x0072 in original LVL format).
     */
    private static int numLemmings;
    /**
     * Number of Lemmings to rescue : should be less than or equal to number of
     * Lemmings.
     */
    private static int numToRescue;
    /**
     * Time Limit : max 0x00FF, 0x0001 to 0x0009 works best.
     */
    private static int timeLimit;

    /**
     * Returns time Limit : max 0x00FF, 0x0001 to 0x0009 works best.
     *
     * @return time Limit : max 0x00FF, 0x0001 to 0x0009 works best.
     */
    public static int getTimeLimit() {
        return timeLimit;
    }

    /**
     * Sets time limit.
     *
     * @param limit time limit.
     */
    public static void setTimeLimit(final int limit) {
        ExtractLevel.timeLimit = limit;
    }

    /** number of climbers in this level : max 0xfa (250). */
    private static int numClimbers;
    /** number of floaters in this level : max 0xfa (250). */
    private static int numFloaters;
    /** number of bombers in this level : max 0xfa (250). */
    private static int numBombers;
    /** number of blockers in this level : max 0xfa (250). */
    private static int numBlockers;
    /** number of builders in this level : max 0xfa (250). */
    private static int numBuilders;
    /** number of bashers in this level : max 0xfa (250). */
    private static int numBashers;
    /** number of miners in this level : max 0xfa (250). */
    private static int numMiners;
    /** number of diggers in this level : max 0xfa (250). */
    private static int numDiggers;
    /** start screen x pos : 0 - 0x04f0 (1264) rounded to modulo 8. */
    private static int xPos;
    /**
     * 0x0000 is dirt, <br>
     * 0x0001 is fire, <br>
     * 0x0002 is squasher,<br>
     * 0x0003 is pillar,<br>
     * 0x0004 is crystal,<br>
     * 0x0005 is brick, <br>
     * 0x0006 is rock, <br>
     * 0x0007 is snow, <br>
     * 0x0008 is bubble.
     */
    private static int style;
    // /** extended style: no used in windows version ?. */
    // static int extStyle;
    // /** placeholder ?. */
    // static int dummy;
    /** objects like doors - 32 objects each consists of 8 bytes. */
    private static List<LvlObject> objects;

    /**
     * Returns objects like doors - 32 objects each consists of 8 bytes.
     *
     * @return objects like doors - 32 objects each consists of 8 bytes.
     */
    public static List<LvlObject> getObjects() {
        return objects;
    }

    /**
     * Sets objects like doors - 32 objects each consists of 8 bytes.
     *
     * @param gameObjects objects like doors - 32 objects each consists of 8
     *                    bytes.
     */
    public static void setObjects(final List<LvlObject> gameObjects) {
        ExtractLevel.objects = gameObjects;
    }

    /** terrain the Lemmings walk on etc. - 400 tiles, 4 bytes each. */
    private static List<Terrain> terrain;

    /**
     * Returns terrain the Lemmings walk on etc. - 400 tiles, 4 bytes each.
     *
     * @return terrain the Lemmings walk on etc.
     */
    public static List<Terrain> getTerrain() {
        return terrain;
    }

    /**
     * Sets terrain the Lemmings walk on etc.
     *
     * @param levelTerrain terrain the Lemmings walk on etc.
     */
    public static void setTerrain(final List<Terrain> levelTerrain) {
        ExtractLevel.terrain = levelTerrain;
    }

    /** steel areas which are indestructible - 32 objects, 4 bytes each. */
    private static List<Steel> steel;

    /**
     * Returns steel areas which are indestructible - 32 objects, 4 bytes each.
     *
     * @return steel areas which are indestructible.
     */
    public static List<Steel> getSteel() {
        return steel;
    }

    /**
     * Sets steel areas which are indestructible.
     *
     * @param steelAreas steel areas which are indestructible.
     */
    public static void setSteel(final List<Steel> steelAreas) {
        ExtractLevel.steel = steelAreas;
    }

    /** 32 byte level name - filled with whitespaces. */
    private static String lvlName;

    /**
     * Returns 32 byte level name - filled with whitespaces.
     *
     * @return 32 byte level name - filled with whitespaces.
     */
    public static String getLvlName() {
        return lvlName;
    }

    /**
     * Sets 32 byte level name - filled with whitespaces.
     *
     * @param levelName 32 byte level name - filled with whitespaces.
     */
    public static void setLvlName(final String levelName) {
        ExtractLevel.lvlName = levelName;
    }

    /**
     * Private constructor for utility class.
     */
    private ExtractLevel() {

    }

    /**
     * Convert one binary LVL file into text file.
     *
     * @param fnIn  Name of binary LVL file
     * @param fnOut Name of target text file
     * @throws Exception
     */
    public static void convertLevel(final String fnIn, final String fnOut)
            throws Exception {
        // read file into buffer
        LevelBuffer b;

        try {
            b = getLevelBuffer(fnIn);
        } catch (final FileNotFoundException e) {
            throw new Exception("File " + fnIn + " not found");
        } catch (final IOException e) {
            throw new Exception("I/O error while reading " + fnIn);
        }

        // output file
        try (FileWriter fo = new FileWriter(fnOut)) {
            // add only file name without the path in the first line
            final String fn = addFileNameWithoutPathInFirstLine(fnIn);
            // analyze buffer
            fo.write("# LVL extracted by Lemmini # " + fn + "\n");
            // read configuration in big endian word
            readConfiguration(b, fo);

            // bugfix: in some levels, the position is negative (?)
            if (xPos < 0) {
                xPos = -xPos;
            }

            xPos *= SCALE;
            fo.write("xPos = " + xPos + "\n");
            style = b.getWord();
            fo.write("style = " + STYLES[style] + "\n");
            /* extStyle = */b.getWord();
            /* dummy = */ b.getWord();
            // read objects
            fo.write("\n# Objects" + "\n");
            fo.write("# id, xpos, ypos, paint mode (), upside down (0,1)"
                    + "\n");
            fo.write("# paint modes: 8=VIS_ON_TERRAIN, "
                    + "4=NO_OVERWRITE, 0=FULL (only one value possible)\n");
            final byte[] by = new byte[BUFFER_SIZE];
            objects = new ArrayList<LvlObject>();
            readAndWriteObjects(b, fo, by);
            // read terrain
            readTerrain(b, fo, by);
            // read steel blocks
            readSteelBlocks(b, fo, by);
            // read name
            readName(b, fo);
        }
    }

    private static String addFileNameWithoutPathInFirstLine(final String fnIn) {
        int p1 = fnIn.lastIndexOf("/");
        final int p2 = fnIn.lastIndexOf("\\");

        if (p2 > p1) {
            p1 = p2;
        }

        if (p1 < 0) {
            p1 = 0;
        } else {
            p1++;
        }

        final String fn = fnIn.substring(p1);
        return fn;
    }

    private static void readConfiguration(final LevelBuffer b,
            final FileWriter fo) throws IOException {
        releaseRate = b.getWord();
        fo.write("releaseRate = " + releaseRate + "\n");
        numLemmings = b.getWord();
        fo.write("numLemmings = " + numLemmings + "\n");
        numToRescue = b.getWord();
        fo.write("numToRescue = " + numToRescue + "\n");
        timeLimit = b.getWord();
        fo.write("timeLimit = " + timeLimit + "\n");
        numClimbers = b.getWord();
        fo.write("numClimbers = " + numClimbers + "\n");
        numFloaters = b.getWord();
        fo.write("numFloaters = " + numFloaters + "\n");
        numBombers = b.getWord();
        fo.write("numBombers = " + numBombers + "\n");
        numBlockers = b.getWord();
        fo.write("numBlockers = " + numBlockers + "\n");
        numBuilders = b.getWord();
        fo.write("numBuilders = " + numBuilders + "\n");
        numBashers = b.getWord();
        fo.write("numBashers = " + numBashers + "\n");
        numMiners = b.getWord();
        fo.write("numMiners = " + numMiners + "\n");
        numDiggers = b.getWord();
        fo.write("numDiggers = " + numDiggers + "\n");
        xPos = b.getWord();
    }

    private static LevelBuffer getLevelBuffer(final String fnIn)
            throws Exception, IOException, FileNotFoundException {
        LevelBuffer b;
        final File f = new File(fnIn);

        if (f.length() != MAX_FILE_SIZE) {
            throw new Exception(
                    "Lemmings level files must be 2048 bytes in size!");
        }

        try (FileInputStream fi = new FileInputStream(fnIn)) {
            final byte[] buffer = new byte[(int) f.length()];

            if (fi.read(buffer) < 1) {
                System.out.println("0 bytes read from file " + fnIn);
            }

            b = new LevelBuffer(buffer);
        }

        return b;
    }

    private static void readAndWriteObjects(final LevelBuffer b,
            final FileWriter fo, final byte[] by) throws IOException {
        int idx = 0;
        for (int i = 0; i < NUM_OBJECTS; i++) {
            int sum = 0;

            for (int j = 0; j < BUFFER_SIZE; j++) {
                by[j] = b.getByte();
                sum += by[j] & Constants.EIGHT_BIT_MASK;
            }

            if (sum != 0) {
                final LvlObject obj = new LvlObject(by, SCALE);
                objects.add(obj);
                fo.write("object_" + idx + " = " + obj.getId() + ", "
                        + obj.getxPos() + ", " + obj.getyPos() + ", "
                        + obj.getPaintMode() + ", "
                        + (obj.isUpsideDown() ? 1 : 0) + "\n");
                idx++;
            }
        }
    }

    private static void readTerrain(final LevelBuffer b, final FileWriter fo,
            final byte[] by) throws IOException {
        fo.write("\n# Terrain" + "\n");
        fo.write("# id, xpos, ypos, modifier" + "\n");
        fo.write("# modifier: 8=NO_OVERWRITE, "
                + "4=UPSIDE_DOWN, 2=REMOVE (combining allowed, 0=FULL)\n");
        terrain = new ArrayList<Terrain>();
        int idx = 0;

        for (int i = 0; i < NUM_TILES; i++) {
            int mask = Constants.EIGHT_BIT_MASK;

            for (int j = 0; j < BYTES_PER_TILE; j++) {
                by[j] = b.getByte();
                mask &= by[j];
            }

            if (mask != Constants.EIGHT_BIT_MASK) {
                final Terrain ter = new Terrain(by, SCALE);
                terrain.add(ter);
                fo.write("terrain_" + idx + " = " + ter.getId() + ", "
                        + ter.getxPos() + ", " + ter.getyPos() + ", "
                        + ter.getModifier() + "\n");
                idx++;
            }
        }
    }

    private static void readSteelBlocks(final LevelBuffer b,
            final FileWriter fo, final byte[] by) throws IOException {
        fo.write("\n#Steel" + "\n");
        fo.write("# id, xpos, ypos, width, height" + "\n");
        steel = new ArrayList<Steel>();
        int idx = 0;

        for (int i = 0; i < NUM_OBJECTS; i++) {
            int sum = 0;

            for (int j = 0; j < BYTES_PER_TILE; j++) {
                by[j] = b.getByte();
                sum += by[j] & Constants.EIGHT_BIT_MASK;
            }

            if (sum != 0) {
                final Steel stl = new Steel(by, SCALE);
                steel.add(stl);
                fo.write("steel_" + idx + " = " + stl.getxPos() + ", "
                        + stl.getyPos() + ", " + stl.getWidth() + ", "
                        + stl.getHeight() + "\n");
                idx++;
            }
        }
    }

    private static void readName(final LevelBuffer b, final FileWriter fo)
            throws IOException {
        fo.write("\n#Name" + "\n");
        final char[] cName = new char[LEVEL_NAME_SIZE];

        for (int j = 0; j < LEVEL_NAME_SIZE; j++) {
            // replace wrong apostrophes
            char c = (char) (b.getByte() & Constants.EIGHT_BIT_MASK);

            if (c == '�' || c == '`') {
                c = '\'';
            }

            cName[j] = c;
        }

        lvlName = String.valueOf(cName);
        fo.write("name = " + lvlName + "\n");
    }
}
