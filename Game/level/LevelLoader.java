package game.level;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.util.ArrayList;
import java.util.List;

import game.Core;
import game.LemmException;
import game.ResourceException;
import game.Steel;
import game.Terrain;
import game.lemmings.Lemming;
import gameutil.FaderHandler;
import gameutil.Sprite;
import lemmini.Constants;
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
 * Utility class for loading Levels. Code moved from Level by John Watne
 * 08/2023.
 */
public final class LevelLoader {
    /**
     * Number of int elements in steel_x array.
     */
    private static final int STEEL_X_LENGTH = 4;
    /**
     * Number of int elements in terrain_x array.
     */
    private static final int TERRAIN_X_LENGTH = 4;
    /**
     * Number of int elements in object_x array.
     */
    private static final int OBJECT_X_LENGTH = 5;
    /** array of default styles. */
    private static final String[] STYLES = {"dirt", "fire", "marble", "pillar",
            "crystal", "brick", "rock", "snow", "Bubble", "special"};
    /**
     * Entry animation type.
     */
    private static final int ENTRY_ANIMATION = 3;
    /**
     * Maximum number of Sprite objects.
     */
    private static final int MAX_NUM_SPRITE_OBJECTS = 64;
    /** template color to be replaced with debris color. */
    private static final int TEMPLATE_COLOR = 0xffff00ff;

    /**
     * Private default constructor for utility class.
     */
    private LevelLoader() {

    }

    /**
     * Load a level and all level resources.
     *
     * @param fname file name
     * @param frame the parent component (main frame of the application).
     * @param level the level being loaded.
     * @throws ResourceException
     * @throws LemmException
     */
    public static void loadLevel(final String fname, final Component frame,
            final Level level) throws ResourceException, LemmException {
        level.setReady(false);
        // read level properties from file
        final Props p = new Props();

        if (!p.load(fname)) {
            throw new ResourceException(fname);
        }

        // read name
        level.setLevelName(p.get("name", ""));
        level.setMaxFallDistance(p.get("maxFallDistance",
                FaderHandler.getCurLevelPack().getMaxFallDistance()));
        // read configuration in big endian word
        level.setReleaseRate(p.get("releaseRate", -1));
        level.setNumLemmings(p.get("numLemmings", -1));
        level.setNumToRescue(p.get("numToRescue", -1));
        readTimeLimitInSeconds(level, p);
        level.setNumClimbers(p.get("numClimbers", -1));
        level.setNumFloaters(p.get("numFloaters", -1));
        level.setNumBombers(p.get("numBombers", -1));
        level.setNumBlockers(p.get("numBlockers", -1));
        level.setNumBuilders(p.get("numBuilders", -1));
        level.setNumBashers(p.get("numBashers", -1));
        level.setNumMiners(p.get("numMiners", -1));
        level.setNumDiggers(p.get("numDiggers", -1));
        level.setxPos(p.get("xPos", -1));
        final String strStyle = p.get("style", "");
        int style = -1;

        for (int i = 0; i < STYLES.length; i++) {
            if (strStyle.equalsIgnoreCase(STYLES[i])) {
                style = i;
                break;
            }
        }

        level.setSuperlemming(p.get("superlemming", false));

        // read objects
        final int[] def = readObjects(level, p);
        // read terrain
        readTerrain(level, p, def);
        // read steel blocks
        readSteelBlocks(level, p, def);
        // load objects
        loadObjects(frame, level, strStyle, style);
        level.setReady(true);
    }

    private static void readTimeLimitInSeconds(final Level level,
            final Props p) {
        int timeLimitSeconds = p.get("timeLimitSeconds", -1);

        if (timeLimitSeconds == -1) {
            final int timeLimit = p.get("timeLimit", -1);
            timeLimitSeconds = timeLimit * Constants.SECONDS_PER_MINUTE;
        }

        level.setTimeLimitSeconds(timeLimitSeconds);
    }

    private static int[] readObjects(final Level level, final Props p) {
        var objects = new ArrayList<LvlObject>();
        final int[] def = {-1};

        for (int i = 0; true; i++) {
            final int[] val = p.get("object_" + i, def);

            if (val.length == OBJECT_X_LENGTH) {
                final LvlObject obj = new LvlObject(val);
                objects.add(obj);
            } else {
                break;
            }
        }

        level.setObjects(objects);
        return def;
    }

    private static void loadObjects(final Component frame, final Level level,
            final String strStyle, final int style)
            throws ResourceException, LemmException {
        SpriteObject[] sprObjAvailable = null;
        // first load the data from object descriptor file xxx.ini
        final String fnames = Core
                .findResource("styles/" + strStyle + "/" + strStyle + ".ini");
        var props = new Props();

        if (!props.load(fnames)) {
            if (style != -1) {
                throw new ResourceException(fnames);
            } else {
                throw new LemmException("Style " + strStyle + " not existing.");
            }
        }

        level.setProps(props);
        // load blockset
        level.setTiles(loadTileSet(strStyle, frame, props));
        sprObjAvailable = loadObjects(strStyle, frame, props, level);
        level.setSprObjAvailable(sprObjAvailable);
    }

    private static void readSteelBlocks(final Level level, final Props p,
            final int[] def) {
        var steel = new ArrayList<Steel>();

        for (int i = 0; true/* i < 32 */; i++) {
            final int[] val = p.get("steel_" + i, def);

            if (val.length == STEEL_X_LENGTH) {
                final Steel stl = new Steel(val);
                steel.add(stl);
            } else {
                break;
            }
        }

        level.setSteel(steel);
    }

    private static void readTerrain(final Level level, final Props p,
            final int[] def) {
        var terrain = new ArrayList<Terrain>();

        for (int i = 0; true /* i < 400 */; i++) {
            final int[] val = p.get("terrain_" + i, def);

            if (val.length == TERRAIN_X_LENGTH) {
                final Terrain ter = new Terrain(val);
                terrain.add(ter);
            } else {
                break;
            }
        }

        level.setTerrain(terrain);
    }

    /**
     * Load tile set from a styles folder.
     *
     * @param set   name of the style
     * @param cmp   parent component
     * @param props Properties for loading information from INI files.
     * @return array of images where each image contains one tile
     * @throws ResourceException
     */
    private static Image[] loadTileSet(final String set, final Component cmp,
            final Props props) throws ResourceException {
        final List<Image> images = new ArrayList<Image>(64);
        final MediaTracker tracker = new MediaTracker(cmp);
        final int numTiles = props.get("tiles", 64);

        for (int n = 0; n < numTiles; n++) {
            final String fName = "styles/" + set + "/" + set + "_"
                    + Integer.toString(n) + ".gif";
            final Image img = Core.loadImage(tracker, fName);
            images.add(img);
        }

        try {
            tracker.waitForAll();
        } catch (final InterruptedException ex) {
            System.err.println("Waiting thread interrupted");
        }

        Image[] ret = new Image[images.size()];
        ret = images.toArray(ret);
        // images = null;
        return ret;
    }

    /**
     * Load level sprite objects.
     *
     * @param set   name of the style
     * @param cmp   parent component
     * @param props Properties for loading information from INI files.
     * @param level the Level for which the sprite objects are loaded.∏
     * @return array of images where each image contains one tile
     * @throws ResourceException
     */
    private static SpriteObject[] loadObjects(final String set,
            final Component cmp, final Props props, final Level level)
            throws ResourceException {
        // URLClassLoader urlLoader = (URLClassLoader)
        // this.getClass().getClassLoader();
        final MediaTracker tracker = new MediaTracker(cmp);
        // first some global settings
        setBackgroundColor(props, level);
        setDebrisColor(props, level);
        setParticleColors(props, level);
        // go through all the entries (shouldn't be more than 64)
        List<SpriteObject> sprites = new ArrayList<SpriteObject>(
                MAX_NUM_SPRITE_OBJECTS);

        for (int idx = 0; true; idx++) {
            // get number of animations
            final String sIdx = Integer.toString(idx);
            final int frames = props.get("frames_" + sIdx, -1);

            if (frames < 0) {
                break;
            }

            // load screenBuffer
            String fName = "styles/" + set + "/" + set + "o_"
                    + Integer.toString(idx) + ".gif";
            Image img = Core.loadImage(tracker, fName);

            try {
                tracker.waitForAll();
            } catch (final InterruptedException ex) {
            }

            // get animation mode
            final int anim = props.get("anim_" + sIdx, -1);

            if (anim < 0) {
                break;
            }

            final SpriteObject sprite = new SpriteObject(img, frames);
            handleAnimationMode(anim, sprite);
            // get object type
            final int type = props.get("type_" + sIdx, -1);

            if (type < 0) {
                break;
            }

            img = handleSpriteType(set, tracker, idx, img, sprite, type);
            // get sound
            addSoundAndAddToSprites(props, sprites, sIdx, sprite);
        }

        SpriteObject[] ret = getSpriteObjects(sprites);
        sprites = null;
        return ret;
    }

    private static Image handleSpriteType(final String set,
            final MediaTracker tracker, final int idx, final Image initialImage,
            final SpriteObject sprite, final int type)
            throws ResourceException {
        Image img = initialImage;
        sprite.setType(SpriteObject.getType(type));

        switch (sprite.getType()) {
        case EXIT:
        case NO_DIG_LEFT:
        case NO_DIG_RIGHT:
        case TRAP_DIE:
        case TRAP_REPLACE:
        case TRAP_DROWN:
            // load mask
            final String maskFname = "styles/" + set + "/" + set + "om_"
                    + Integer.toString(idx) + ".gif";
            img = Core.loadImage(tracker, maskFname);
            sprite.setMask(img);
            break;
        default:
            break;
        }

        return img;
    }

    private static void addSoundAndAddToSprites(final Props props,
            final List<SpriteObject> sprites, final String sIdx,
            final SpriteObject sprite) {
        final int sound = props.get("sound_" + sIdx, -1);
        sprite.setSound(sound);
        sprites.add(sprite);
    }

    private static SpriteObject[] getSpriteObjects(
            final List<SpriteObject> sprites) {
        SpriteObject[] ret = new SpriteObject[sprites.size()];
        ret = sprites.toArray(ret);
        return ret;
    }

    private static void handleAnimationMode(final int anim,
            final SpriteObject sprite) {
        switch (anim) {
        case 0: // dont' animate
            sprite.setAnimMode(Sprite.Animation.NONE);
            break;
        case 1: // loop mode
            sprite.setAnimMode(Sprite.Animation.LOOP);
            break;
        case 2: // triggered animation - for the moment handle like loop
            sprite.setAnimMode(Sprite.Animation.TRIGGERED);
            break;
        case ENTRY_ANIMATION: // entry animation
            sprite.setAnimMode(Sprite.Animation.ONCE);
            break;
        default:
            break;
        }
    }

    private static void setParticleColors(final Props props,
            final Level level) {
        final int[] particleCol = props.get("particleColor",
                Level.DEFAULT_PARTICLE_COLORS);

        for (int i = 0; i < particleCol.length; i++) {
            particleCol[i] |= Constants.MAX_ALPHA;
        }

        level.setParticleCol(particleCol);
    }

    private static void setDebrisColor(final Props props, final Level level) {
        final int debrisCol = props.get("debrisColor", Color.WHITE.getRGB())
                | Constants.MAX_ALPHA;
        level.setDebrisColor(debrisCol);
        // replace pink color with debris color
        Lemming.patchColors(TEMPLATE_COLOR, debrisCol);
    }

    private static void setBackgroundColor(final Props props,
            final Level level) {
        final int bgCol = props.get("bgColor", 0x000000) | Constants.MAX_ALPHA;
        level.setBgCol(bgCol);
        level.setBgColor(new Color(bgCol));
    }
}
