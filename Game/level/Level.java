package game.level;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.List;

import game.GameController;
import game.Steel;
import game.Terrain;
import lemmini.Constants;
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
 * Load a level, paint level, create background stencil.
 *
 * @author Volker Oth
 */
public class Level {
    /**
     * Mask bits 9-16 = 0xff00.
     */
    private static final int MASK_BITS_9_TO_16 = 0xff00;
    /**
     * Hexadecimal value 0x60.
     */
    private static final int HEX_0X60 = 0x60;
    /**
     * Number of RGB color channels (R, G, and B).
     */
    private static final int NUM_RGB_CHANNELS = 3;
    /** maximum width of level. */
    public static final int WIDTH = 1664 * 2;
    /** maximum height of level. */
    public static final int HEIGHT = 160 * 2;
    /** array of default ARGB colors for particle effects. */
    public static final int[] DEFAULT_PARTICLE_COLORS = {0xff00ff00, 0xff0000ff,
            0xffffffff, 0xffffffff, 0xffff0000};

    /**
     * Array of normal sprite objects - no transparency, drawn behind background
     * image.
     */
    private SpriteObject[] sprObjBehind;

    /**
     * Array of special sprite objects - with transparency, drawn above
     * background image.
     */
    private SpriteObject[] sprObjFront;
    /** array of all sprite objects (in front and behind). */
    private SpriteObject[] sprObjects;
    /** array of level entries. */
    private Entry[] entries;

    /** release rate : 0 is slowest, 0x0FA (250) is fastest. */
    private int releaseRate;
    /**
     * Number of Lemmings in this level (maximum 0x0072 in original LVL format).
     */
    private int numLemmings;
    /**
     * Number of Lemmings to rescue : should be less than or equal to number of
     * Lemmings.
     */
    private int numToRescue;
    /** time limit in seconds. */
    private int timeLimitSeconds;
    /** number of climbers in this level : max 0xfa (250). */
    private int numClimbers;
    /** number of floaters in this level : max 0xfa (250). */
    private int numFloaters;
    /** number of bombers in this level : max 0xfa (250). */
    private int numBombers;
    /** number of blockers in this level : max 0xfa (250). */
    private int numBlockers;
    /** number of builders in this level : max 0xfa (250). */
    private int numBuilders;
    /** number of bashers in this level : max 0xfa (250). */
    private int numBashers;
    /** number of miners in this level : max 0xfa (250). */
    private int numMiners;
    /** number of diggers in this level : max 0xfa (250). */
    private int numDiggers;
    /** start screen x pos : 0 - 0x04f0 (1264) rounded to modulo 8. */
    private int xPos;
    /** background color as ARGB. */
    private int bgCol;
    /** background color. */
    private Color bgColor;
    /** color used for steps and debris. */
    private int debrisCol;
    /** array of ARGB colors used for particle effects. */
    private int[] particleCol;
    /** maximum safe fall distance. */
    private int maxFallDistance;
    /** this level is a SuperLemming level (runs faster). */
    private boolean superlemming;
    /** level is completely loaded. */
    private boolean ready = false;
    /**
     * Objects like doors - originally 32 objects where each consists of 8
     * bytes.
     */
    private List<LvlObject> objects;
    /**
     * Background tiles - every pixel in them is interpreted as brick in the
     * stencil.
     */
    private Image[] tiles;

    /** sprite objects of all sprite objects available in this style. */
    private SpriteObject[] sprObjAvailable;

    /**
     * terrain the Lemmings walk on etc. - originally 400 tiles, 4 bytes each
     */
    private List<Terrain> terrain;

    /**
     * Steel areas which are indestructible - originally 32 objects, 4 bytes
     * each.
     */
    private List<Steel> steel;

    /** level name - originally 32 bytes ASCII - filled with whitespaces. */
    private String lvlName;
    /** used to read in the configuration file. */
    private Props props;

    /**
     * Returns array of level entries.
     *
     * @return array of level entries.
     */
    public Entry[] getEntries() {
        return entries;
    }

    /**
     * Sets array of level entries.
     *
     * @param levelEntries array of level entries.
     */
    public void setEntries(final Entry[] levelEntries) {
        this.entries = levelEntries;
    }

    /**
     * Returns array of all sprite objects (in front and behind).
     *
     * @return array of all sprite objects (in front and behind).
     */
    public SpriteObject[] getSprObjects() {
        return sprObjects;
    }

    /**
     * Sets array of all sprite objects (in front and behind).
     *
     * @param allSprites array of all sprite objects (in front and behind).
     */
    public void setSprObjects(final SpriteObject[] allSprites) {
        this.sprObjects = allSprites;
    }

    /**
     * Returns Array of normal sprite objects - no transparency, drawn behind
     * background image.
     *
     * @return Array of normal sprite objects - no transparency, drawn behind
     *         background image.
     */
    public SpriteObject[] getSprObjBehind() {
        return sprObjBehind;
    }

    /**
     * Sets Array of normal sprite objects - no transparency, drawn behind
     * background image.
     *
     * @param spritesBehindBackground Array of normal sprite objects - no
     *                                transparency, drawn behind background
     *                                image.
     */
    public void setSprObjBehind(final SpriteObject[] spritesBehindBackground) {
        this.sprObjBehind = spritesBehindBackground;
    }

    /**
     * Returns background color as ARGB.
     *
     * @return background color as ARGB.
     */
    public int getBgCol() {
        return bgCol;
    }

    /**
     * Sets background color as ARGB.
     *
     * @param argbColor background color as ARGB.
     */
    public void setBgCol(final int argbColor) {
        this.bgCol = argbColor;
    }

    /**
     * Returns background tiles.
     *
     * @return background tiles.
     */
    public Image[] getTiles() {
        return tiles;
    }

    /**
     * Sets background tiles.
     *
     * @param backgroundTiles background tiles.
     */
    public void setTiles(final Image[] backgroundTiles) {
        this.tiles = backgroundTiles;
    }

    /**
     * Returns props used to read in the configuration file.
     *
     * @return props used to read in the configuration file.
     */
    public Props getProps() {
        return props;
    }

    /**
     * Sets props used to read in the configuration file.
     *
     * @param configProps props used to read in the configuration file.
     */
    public void setProps(final Props configProps) {
        this.props = configProps;
    }

    /**
     * Returns sprite objects of all sprite objects available in this style.
     *
     * @return sprite objects of all sprite objects available in this style.
     */
    public SpriteObject[] getSprObjAvailable() {
        return sprObjAvailable;
    }

    /**
     * Sets sprite objects of all sprite objects available in this style.
     *
     * @param spriteObjects sprite objects of all sprite objects available in
     *                      this style.
     */
    public void setSprObjAvailable(final SpriteObject[] spriteObjects) {
        this.sprObjAvailable = spriteObjects;
    }

    /**
     * Returns Steel areas which are indestructible.
     *
     * @return Steel areas which are indestructible.
     */
    public List<Steel> getSteel() {
        return steel;
    }

    /**
     * Sets Steel areas which are indestructible.
     *
     * @param steelList Steel areas which are indestructible.
     */
    public void setSteel(final List<Steel> steelList) {
        this.steel = steelList;
    }

    /**
     * Returns Objects like doors - originally 32 objects where each consists of
     * 8 bytes.
     *
     * @return Objects like doors - originally 32 objects where each consists of
     *         8 bytes.
     */
    public List<LvlObject> getObjects() {
        return objects;
    }

    /**
     * Sets Objects like doors - originally 32 objects where each consists of 8
     * bytes.
     *
     * @param objectList Objects like doors - originally 32 objects where each
     *                   consists of 8 bytes.
     */
    public void setObjects(final List<LvlObject> objectList) {
        this.objects = objectList;
    }

    /**
     * Returns terrain the Lemmings walk on etc.
     *
     * @return terrain the Lemmings walk on etc.
     */
    public List<Terrain> getTerrain() {
        return terrain;
    }

    /**
     * Sets terrain the Lemmings walk on etc.
     *
     * @param terrainList terrain the Lemmings walk on etc.
     */
    public void setTerrain(final List<Terrain> terrainList) {
        this.terrain = terrainList;
    }

    /**
     * Draw opaque objects behind background image.
     *
     * @param g     graphics object to draw on
     * @param width width of screen
     * @param xOfs  level offset position
     */
    public void drawBehindObjects(final Graphics2D g, final int width,
            final int xOfs) {
        // draw "behind" objects
        if (sprObjBehind != null) {
            for (int n = 0; n < sprObjBehind.length; n++) {
                try {
                    final SpriteObject spr = sprObjBehind[n];
                    final BufferedImage img = spr.getImage();
                    if (spr.getX() + spr.getWidth() > xOfs
                            && spr.getX() < xOfs + width) {
                        g.drawImage(img, spr.getX() - xOfs, spr.getY(), null);
                        // spr.drawHidden(offImg,xOfsTemp);
                    }
                } catch (final ArrayIndexOutOfBoundsException ex) {
                }
            }
        }
    }

    /**
     * Draw transparent objects in front of background image.
     *
     * @param g     graphics object to draw on
     * @param width width of screen
     * @param xOfs  level offset position
     */
    public void drawInFrontObjects(final Graphics2D g, final int width,
            final int xOfs) {
        // draw "in front" objects
        if (sprObjFront != null) {
            for (int n = 0; n < sprObjFront.length; n++) {
                try {
                    final SpriteObject spr = sprObjFront[n];
                    final BufferedImage img = spr.getImage();
                    if (spr.getX() + spr.getWidth() > xOfs
                            && spr.getX() < xOfs + width) {
                        g.drawImage(img, spr.getX() - xOfs, spr.getY(), null);
                    }
                } catch (final ArrayIndexOutOfBoundsException ex) {
                }
            }
        }
    }

    /**
     * Create a mini map for this level.
     *
     * @param image   image to re-use (if null or wrong size, it will be
     *                recreated)
     * @param bgImage background image used as source for the mini map
     * @param scaleX  integer X scaling factor (2 -> half width)
     * @param scaleY  integer Y scaling factor (2 -> half height)
     * @param tint    apply a greenish color tint
     * @return image with mini map
     */
    public BufferedImage createMiniMap(final BufferedImage image,
            final BufferedImage bgImage, final int scaleX, final int scaleY,
            final boolean tint) {
        final Level level = GameController.getLevel();
        int backgroundColor;
        final int width = bgImage.getWidth() / scaleX;
        final int height = bgImage.getHeight() / scaleY;
        BufferedImage img;

        if (image == null || image.getWidth() != width
                || image.getHeight() != height) {
            img = ToolBox.createImage(width, height, Transparency.OPAQUE);
        } else {
            img = image;
        }
        final Graphics2D gx = img.createGraphics();
        // clear background
        gx.setBackground(bgColor);
        gx.clearRect(0, 0, width, height);
        // read back background color to avoid problems with 16bit mode
        // (bgColor written can be slightly different from the one read)
        backgroundColor = img.getRGB(0, 0);
        // draw "behind" objects
        if (level != null && level.sprObjBehind != null) {
            for (int n = 0; n < level.sprObjBehind.length; n++) {
                try {
                    final SpriteObject spr = level.sprObjBehind[n];
                    final BufferedImage sprImg = spr.getImage();
                    gx.drawImage(sprImg, spr.getX() / scaleX,
                            spr.getY() / scaleY, spr.getWidth() / scaleX,
                            spr.getHeight() / scaleY, null);
                } catch (final ArrayIndexOutOfBoundsException ex) {
                }
            }
        }
        gx.drawImage(bgImage, 0, 0, width, height, 0, 0, bgImage.getWidth(),
                bgImage.getHeight(), null);
        // draw "in front" objects
        if (level != null && level.sprObjFront != null) {
            for (int n = 0; n < level.sprObjFront.length; n++) {
                try {
                    final SpriteObject spr = level.sprObjFront[n];
                    final BufferedImage sprImg = spr.getImage();
                    gx.drawImage(sprImg, spr.getX() / scaleX,
                            spr.getY() / scaleY, spr.getWidth() / scaleX,
                            spr.getHeight() / scaleY, null);
                } catch (final ArrayIndexOutOfBoundsException ex) {
                }
            }
        }

        gx.dispose();

        // now tint in green
        if (tint) {
            doTint(backgroundColor, img);
        }

        return img;
    }

    private void doTint(final int backgroundColor, final BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int c = img.getRGB(x, y);

                if (c == backgroundColor) {
                    c = Constants.MAX_ALPHA; // make backgroud black instead of
                                             // dark
                } else {
                    c = tintNonBackgroundColor(c);
                }

                img.setRGB(x, y, c);
            }
        }
    }

    private int tintNonBackgroundColor(final int initialColor) {
        int c = initialColor;
        int sum = 0;

        for (int i = 0; i < NUM_RGB_CHANNELS; i++, c >>= Constants.SHIFT_8) {
            sum += (c & Constants.EIGHT_BIT_MASK);
        }

        sum /= NUM_RGB_CHANNELS; // mean value

        if (sum != 0) {
            sum += HEX_0X60;
        }

        // sum *= 3; // make lighter
        if (sum > Constants.EIGHT_BIT_MASK) {
            sum = Constants.EIGHT_BIT_MASK;
        }

        c = Constants.MAX_ALPHA
                + ((sum << Constants.SHIFT_8) & MASK_BITS_9_TO_16);
        return c;
    }

    /**
     * Get level sprite object via index.
     *
     * @param idx index
     * @return level sprite object
     */
    public SpriteObject getSprObject(final int idx) {
        return sprObjects[idx];
    }

    /**
     * Get number of level sprite objects.
     *
     * @return number of level sprite objects
     */
    public int getSprObjectNum() {
        if (sprObjects == null) {
            return 0;
        }
        return sprObjects.length;
    }

    /**
     * Get level Entry via idx.
     *
     * @param idx index
     * @return level Entry
     */
    public Entry getEntry(final int idx) {
        return entries[idx];
    }

    /**
     * Get number of entries for this level.
     *
     * @return number of entries.
     */
    public int getEntryNum() {
        if (entries == null) {
            return 0;
        }
        return entries.length;
    }

    /**
     * Get background color.
     *
     * @return background color.
     */
    public Color getBgColor() {
        return bgColor;
    }

    /**
     * Sets background color.
     *
     * @param background background color.
     */
    public void setBgColor(final Color background) {
        bgColor = background;
    }

    /**
     * Get ready state of level.
     *
     * @return true if level is completely loaded.
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Sets ready state of level.
     *
     * @param isReady true if level is completely loaded.
     */
    public void setReady(final boolean isReady) {
        this.ready = isReady;
    }

    /**
     * Get maximum safe fall distance.
     *
     * @return maximum safe fall distance
     */
    public int getMaxFallDistance() {
        return maxFallDistance;
    }

    /**
     * Sets maximum safe fall distance.
     *
     * @param maxSafeDistance maximum safe fall distance.
     */
    public void setMaxFallDistance(final int maxSafeDistance) {
        this.maxFallDistance = maxSafeDistance;
    }

    /**
     * Get array of ARGB colors used for particle effects.
     *
     * @return array of ARGB colors used for particle effects
     */
    public int[] getParticleCol() {
        // Return clone to avoid malicious code vulnerability of potentially
        // exposing internal representation by returning a reference to a
        // mutable object.
        return particleCol.clone();
    }

    /**
     * Sets array of ARGB colors used for particle effects.
     *
     * @param particleColors array of ARGB colors used for particle effects.
     */
    public void setParticleCol(final int[] particleColors) {
        particleCol = particleColors.clone();
    }

    /**
     * Get start screen x position : 0 - 0x04f0 (1264) rounded to modulo 8.
     *
     * @return start screen x position
     */
    public int getXpos() {
        return xPos;
    }

    /**
     * Sets start screen x position.
     *
     * @param position start screen x position.
     */
    public void setxPos(final int position) {
        this.xPos = position;
    }

    /**
     * Get number of climbers in this level : max 0xfa (250).
     *
     * @return number of climbers in this level
     */
    public int getNumClimbers() {
        return numClimbers;
    }

    /**
     * Sets number of climbers in this level.
     *
     * @param climbers number of climbers in this level.
     */
    public void setNumClimbers(final int climbers) {
        this.numClimbers = climbers;
    }

    /**
     * Get number of floaters in this level : max 0xfa (250).
     *
     * @return number of floaters in this level
     */
    public int getNumFloaters() {
        return numFloaters;
    }

    /**
     * Sets number of floaters in this level.
     *
     * @param floaters number of floaters in this level.
     */
    public void setNumFloaters(final int floaters) {
        this.numFloaters = floaters;
    }

    /**
     * Get number of bombers in this level : max 0xfa (250).
     *
     * @return number of bombers in this level
     */
    public int getNumBombers() {
        return numBombers;
    }

    /**
     * Sets number of bombers in this level.
     *
     * @param bombers number of bombers in this level.
     */
    public void setNumBombers(final int bombers) {
        this.numBombers = bombers;
    }

    /**
     * Get number of blockers in this level : max 0xfa (250).
     *
     * @return number of blockers in this level
     */
    public int getNumBlockers() {
        return numBlockers;
    }

    /**
     * Sets number of blockers in this level.
     *
     * @param blockers number of blockers in this level.
     */
    public void setNumBlockers(final int blockers) {
        this.numBlockers = blockers;
    }

    /**
     * Get number of builders in this level : max 0xfa (250).
     *
     * @return number of builders in this level
     */
    public int getNumBuilders() {
        return numBuilders;
    }

    /**
     * Sets number of builders in this level.
     *
     * @param builders number of builders in this level.
     */
    public void setNumBuilders(final int builders) {
        this.numBuilders = builders;
    }

    /**
     * Get number of bashers in this level : max 0xfa (250).
     *
     * @return number of bashers in this level
     */
    public int getNumBashers() {
        return numBashers;
    }

    /**
     * Sets number of bashers in this level.
     *
     * @param bashers number of bashers in this level.
     */
    public void setNumBashers(final int bashers) {
        this.numBashers = bashers;
    }

    /**
     * Get number of miners in this level : max 0xfa (250).
     *
     * @return number of miners in this level
     */
    public int getNumMiners() {
        return numMiners;
    }

    /**
     * Sets number of miners in this level.
     *
     * @param miners number of miners in this level.
     */
    public void setNumMiners(final int miners) {
        this.numMiners = miners;
    }

    /**
     * Get number of diggers in this level : max 0xfa (250).
     *
     * @return number of diggers in this level
     */
    public int getNumDiggers() {
        return numDiggers;
    }

    /**
     * Sets number of diggers in this level.
     *
     * @param diggers number of diggers in this level.
     */
    public void setNumDiggers(final int diggers) {
        this.numDiggers = diggers;
    }

    /**
     * Get time limit in seconds.
     *
     * @return time limit in seconds
     */
    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    /**
     * Sets time limit in seconds.
     *
     * @param timeLimit time limit in seconds.
     */
    public void setTimeLimitSeconds(final int timeLimit) {
        this.timeLimitSeconds = timeLimit;
    }

    /**
     * Get number of Lemmings to rescue : should be less than or equal to number
     * of Lemmings.
     *
     * @return number of Lemmings to rescue
     */
    public int getNumToRescue() {
        return numToRescue;
    }

    /**
     * Sets number of Lemmings to rescue.
     *
     * @param numberToRescue number of Lemmings to rescue.
     */
    public void setNumToRescue(final int numberToRescue) {
        this.numToRescue = numberToRescue;
    }

    /**
     * Get number of Lemmings in this level (maximum 0x0072 = 114 in original
     * LVL format).
     *
     * @return number of Lemmings in this level
     */
    public int getNumLemmings() {
        return numLemmings;
    }

    /**
     * Sets number of Lemmings in this level (maximum 0x0072 = 114 in original
     * LVL format).
     *
     * @param lemmingsInLevel number of Lemmings in this level (maximum 0x0072 =
     *                        114 in original LVL format).
     */
    public void setNumLemmings(final int lemmingsInLevel) {
        this.numLemmings = lemmingsInLevel;
    }

    /**
     * Get color of debris pixels (to be replaced with level color).
     *
     * @return color of debris pixels as ARGB
     */
    public int getDebrisColor() {
        return debrisCol;
    }

    /**
     * Sets color of debris pixels as ARGB.
     *
     * @param debrisColor color of debris pixels as ARGB.
     */
    public void setDebrisColor(final int debrisColor) {
        debrisCol = debrisColor;
    }

    /**
     * Get release rate : 0 is slowest, 0x0FA (250) is fastest.
     *
     * @return release rate : 0 is slowest, 0x0FA (250) is fastest
     */
    public int getReleaseRate() {
        return releaseRate;
    }

    /**
     * Sets release rate : 0 is slowest, 0x0FA (250) is fastest.
     *
     * @param rate release rate : 0 is slowest, 0x0FA (250) is fastest.
     */
    public void setReleaseRate(final int rate) {
        this.releaseRate = rate;
    }

    /**
     * Check if this is a SuperLemming level (runs faster).
     *
     * @return true if this is a SuperLemming level, false otherwise
     */
    public boolean isSuperLemming() {
        return superlemming;
    }

    /**
     * Sets whether this is a SuperLemming level.
     *
     * @param superLemmingLevel true if this is a SuperLemming level, false
     *                          otherwise.
     */
    public void setSuperlemming(final boolean superLemmingLevel) {
        this.superlemming = superLemmingLevel;
    }

    /**
     * Get level name.
     *
     * @return level name
     */
    public String getLevelName() {
        return lvlName;
    }

    /**
     * Sets level name.
     *
     * @param name level name.
     */
    public void setLevelName(final String name) {
        lvlName = name;
    }

    /**
     * Returns Array of special sprite objects - with transparency, drawn above
     * background image.
     *
     * @return Array of special sprite objects - with transparency, drawn above
     *         background image.
     */
    public SpriteObject[] getSprObjFront() {
        return sprObjFront;
    }

    /**
     * Sets Array of special sprite objects - with transparency, drawn above
     * background image.
     *
     * @param spriteObjects Array of special sprite objects - with transparency,
     *                      drawn above background image.
     */
    public void setSprObjFront(final SpriteObject[] spriteObjects) {
        this.sprObjFront = spriteObjects;
    }
}
