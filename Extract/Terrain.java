package extract;

/**
 * Storage class for terrain tiles.
 *
 * @author Volker Oth
 */
class Terrain {
    /**
     * 6-bit mask = 0x3f.
     */
    private static final int SIX_BIT_MASK = 0x3f;
    /**
     * Index of id data in buffer array.
     */
    private static final int ID_INDEX = 3;
    /**
     * Number of pixels per tile.
     */
    private static final int PIXELS_PER_TILE = 4;
    /**
     * Amount to subtract from yPos if bleed through from prior 9-bit value.
     */
    private static final int YPOS_9_BIT_CORRECTION = 512;
    /**
     * Mask for 9th bit = 256.
     */
    private static final int BIT_9_MASK = 256;
    /**
     * 7-bit shift.
     */
    private static final int SHIFT_7 = 7;
    /**
     * Mask for 8th bit = 0x80.
     */
    private static final int BIT_8_MASK = 0x80;
    /**
     * 2nd index of yPos information in buffer.
     */
    private static final int YPOS_INDEX_2 = 3;
    /**
     * Offset to subtract from buffer values to obtain x position.
     */
    private static final int X_OFFSET = 16;
    /**
     * 8-bit mask.
     */
    private static final int EIGHT_BIT_MASK = 0xff;
    /**
     * 8-bit shift.
     */
    private static final int SHIFT_8 = 8;
    /**
     * 4-bit mask = 0x0f.
     */
    private static final int FOUR_BIT_MASK = 0x0f;
    /**
     * 4-bit shift.
     */
    private static final int SHIFT_4 = 4;
    /**
     * Mask for bits 5-8 = 0xf0.
     */
    private static final int BITS_5_TO_8_MASK = 0xf0;
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

    /** modifier - must be one of the ExtractLevel MODEs. */
    private int modifier;

    /**
     * Returns modifier - must be one of the ExtractLevel MODEs.
     *
     * @return modifier - must be one of the ExtractLevel MODEs.
     */
    public int getModifier() {
        return modifier;
    }

    /**
     * Sets modifier - must be one of the ExtractLevel MODEs.
     *
     * @param modeModifier modifier - must be one of the ExtractLevel MODEs.
     */
    public void setModifier(final int modeModifier) {
        this.modifier = modeModifier;
    }

    /**
     * Constructor.
     *
     * @param b     buffer
     * @param scale Scale (to convert lowres levels into hires levels)
     */
    Terrain(final byte[] b, final int scale) {
        // xpos: 0x0000..0x063F. 0x0000 = -16, 0x0008 = -8, 0x0010 = 0, 0x063f =
        // 1583.
        // note: the xpos also contains modifiers. the first nibble can be
        // 8 (do no overwrite existing terrain), 4 (display upside-down), or
        // 2 (remove terrain instead of add it). you can add them together.
        // 0 indicates normal.
        // eg: 0xC011 means draw at xpos=1, do not overwrite, upside-down.
        modifier = (b[0] & BITS_5_TO_8_MASK) >> SHIFT_4;
        xPos = ((b[0] & FOUR_BIT_MASK) << SHIFT_8) + (b[1] & EIGHT_BIT_MASK)
                - X_OFFSET;
        xPos *= scale;
        // y pos : 9-bit value. min 0xEF0, max 0x518. 0xEF0 = -38, 0xEF8 = -37,
        // 0x020 = 0, 0x028 = 1, 0x030 = 2, 0x038 = 3, ... , 0x518 = 159
        // note: the ypos value bleeds into the next value since it is 9bits.
        yPos = (((b[2] & EIGHT_BIT_MASK) << 1)
                + ((b[YPOS_INDEX_2] & BIT_8_MASK) >> SHIFT_7));

        if ((yPos & BIT_9_MASK) != 0) {
            yPos -= YPOS_9_BIT_CORRECTION;
        }

        yPos -= PIXELS_PER_TILE;
        yPos *= scale;
        // terrain id: min 0x00, max 0x3F. not all graphic sets have all 64
        // graphics.
        id = b[ID_INDEX] & SIX_BIT_MASK;
    }
}
