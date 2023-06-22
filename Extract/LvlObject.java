package extract;
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
 * Storage class for level objects.
 *
 * @author Volker Oth
 */
class LvlObject {
    /**
     * Buffer value indicating upside down.
     */
    private static final int UPSIDE_DOWN = 0x8f;
    /**
     * Index of upside down indicator in array.
     */
    private static final int UPSIDE_DOWN_INDEX = 7;
    /**
     * Second buffer value for visible while on terrain paint mode.
     */
    private static final int VIS_ON_TERRAIN_VALUE_2 = 0xc0;
    /**
     * First buffer value for visible while on terrain paint mode.
     */
    private static final int VIS_ON_TERRAIN_VALUE_1 = 0x40;
    /**
     * Buffer value for no overwrite paint mode.
     */
    private static final int NO_OVERWRITE_VALUE = 0x80;
    /**
     * Index of paint mode element in array.
     */
    private static final int PAINT_MODE_INDEX = 6;
    /**
     * Index of 2nd id element in array.
     */
    private static final int ID_INDEX_2 = 5;
    /**
     * Index of 1st id element in array.
     */
    private static final int ID_INDEX_1 = 4;
    /**
     * Index of 2nd y position element in buffer.
     */
    private static final int Y_POS_INDEX_2 = 3;
    /**
     * Offset subtracted to obtain x position.
     */
    private static final int X_OFFSET = 16;
    /**
     * 8-bit shift.
     */
    private static final int SHIFT_8 = 8;
    /**
     * 8-bit mask = 0xff.
     */
    private static final int EIGHT_BIT_MASK = 0xff;
    /** paint mode: only visible on a terrain pixel. */
    private static final int MODE_VIS_ON_TERRAIN = 8;
    /**
     * Paint mode: don't overwrite terrain pixel in the original background
     * image.
     */
    private static final int MODE_NO_OVERWRITE = 4;
    /** paint mode: paint without any further checks. */
    private static final int MODE_FULL = 0;

    /** x position in pixels. */
    private int xPos;

    /**
     * Returns x position in pixels.
     *
     * @return x position in pixels.
     */
    public int getxPos() {
        return xPos;
    }

    /**
     * Sets x position in pixels.
     *
     * @param xPosition x position in pixels.
     */
    public void setxPos(final int xPosition) {
        this.xPos = xPosition;
    }

    /** y position in pixels. */
    private int yPos;

    /**
     * Returns y position in pixels.
     *
     * @return y position in pixels.
     */
    public int getyPos() {
        return yPos;
    }

    /**
     * Sets y position in pixels.
     *
     * @param yPosition y position in pixels.
     */
    public void setyPos(final int yPosition) {
        this.yPos = yPosition;
    }

    /** identifier. */
    private int id;

    /**
     * Returns identifier.
     *
     * @return identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets identifier.
     *
     * @param identifier identifier.
     */
    public void setId(final int identifier) {
        this.id = identifier;
    }

    /** paint mode. */
    private int paintMode;

    /**
     * Returns paint mode.
     *
     * @return paint mode.
     */
    public int getPaintMode() {
        return paintMode;
    }

    /**
     * Sets paint mode.
     *
     * @param mode paint mode.
     */
    public void setPaintMode(final int mode) {
        this.paintMode = mode;
    }

    /** flag: paint object upsdie down. */
    private boolean upsideDown;

    /**
     * Indicates whether paint object is upside down.
     *
     * @return <code>true</code> if paint object is upside down.
     */
    public boolean isUpsideDown() {
        return upsideDown;
    }

    /**
     * Sets whether paint object is upside down.
     *
     * @param isUpsideDown <code>true</code> if paint object is upside down.
     */
    public void setUpsideDown(final boolean isUpsideDown) {
        this.upsideDown = isUpsideDown;
    }

    /**
     * Constructor.
     *
     * @param b     buffer
     * @param scale Scale (to convert lowres levels into hires levels)
     */
    LvlObject(final byte[] b, final int scale) {
        // x pos : min 0xFFF8, max 0x0638. 0xFFF8 = -24, 0x0000 = -16, 0x0008 =
        // -8
        // 0x0010 = 0, 0x0018 = 8, ... , 0x0638 = 1576 note: should be multiples
        // of 8
        xPos = (short) (((b[0] & EIGHT_BIT_MASK) << SHIFT_8)
                + (b[1] & EIGHT_BIT_MASK)) - X_OFFSET;
        xPos *= scale;
        // y pos : min 0xFFD7, max 0x009F. 0xFFD7 = -41, 0xFFF8 = -8, 0xFFFF =
        // -1
        // 0x0000 = 0, ... , 0x009F = 159. note: can be any value in the
        // specified range
        yPos = (short) (((b[2] & EIGHT_BIT_MASK) << SHIFT_8)
                + (b[Y_POS_INDEX_2] & EIGHT_BIT_MASK));
        yPos *= scale;
        // obj id : min 0x0000, max 0x000F. the object id is different in each
        // graphics set, however 0x0000 is always an exit and 0x0001 is always a
        // start.
        id = ((b[ID_INDEX_1] & EIGHT_BIT_MASK) << SHIFT_8)
                + (b[ID_INDEX_2] & EIGHT_BIT_MASK);
        // modifier : first byte can be 80 (do not overwrite existing terrain)
        // or 40
        // (must have terrain underneath to be visible). 00 specifies always
        // draw full
        // graphic.
        // second byte can be 8F (display graphic upside-down) or 0F (display
        // graphic
        // normally)
        switch (b[PAINT_MODE_INDEX] & EIGHT_BIT_MASK) {
        case NO_OVERWRITE_VALUE:
            paintMode = MODE_NO_OVERWRITE;
            break;
        case VIS_ON_TERRAIN_VALUE_1:
        case VIS_ON_TERRAIN_VALUE_2: // bug in original level 36: overwrite AND
                                     // visible on
            // terrain: impossible
            paintMode = MODE_VIS_ON_TERRAIN;
            break;
        default:
            paintMode = MODE_FULL;
            break;
        }

        upsideDown = ((b[UPSIDE_DOWN_INDEX] & EIGHT_BIT_MASK) == UPSIDE_DOWN);
    }
}
