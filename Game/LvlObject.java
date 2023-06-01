package game;

/**
 * Storage class for a level object.
 *
 * @author Volker Oth
 */
public class LvlObject {
    /**
     * Index of upside down flag in array passed to constructor.
     */
    private static final int UPSIDE_DOWN_INDEX = 4;
    /**
     * Index of paint mode in array passed to constructor.
     */
    private static final int PAINT_MODE_INDEX = 3;
    /** paint mode: only visible on a terrain pixel. */
    static final int MODE_VIS_ON_TERRAIN = 8;
    /**
     * Paint mode: don't overwrite terrain pixel in the original background
     * image.
     */
    static final int MODE_NO_OVERWRITE = 4;
    /**
     * paint mode: don't overwrite terrain pixel in the current (!) background
     * image. special NO_OVERWRITE case for objects hidden behind terrain.
     */
    static final int MODE_HIDDEN = 5;
    /** paint mode: paint without any further checks. */
    static final int MODE_FULL = 0;

    /** identifier. */
    private int id;

    /** x position in pixels. */
    private int xPos;

    /** y position in pixels. */
    private int yPos;
    /** paint mode. */
    private int paintMode;
    /** flag: paint the object upside down. */
    private boolean upsideDown;

    /**
     * Get identifier.
     *
     * @return identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Set identifier.
     *
     * @param identifier identifier.
     */
    public void setId(final int identifier) {
        this.id = identifier;
    }

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

    /**
     * Indicates whether to paint the object upside down.
     *
     * @return <code>true</code> if the object is to be painted upside down.
     */
    public boolean isUpsideDown() {
        return upsideDown;
    }

    /**
     * Set whether to paint the object upside down.
     *
     * @param isUpsideDown <code>true</code> if the object is to be painted
     *                     upside down.
     */
    public void setUpsideDown(final boolean isUpsideDown) {
        this.upsideDown = isUpsideDown;
    }

    /**
     * Constructor.
     *
     * @param val three values as array [identifier, x position, y position]
     */
    public LvlObject(final int[] val) {
        id = val[0];
        xPos = val[1];
        yPos = val[2];
        paintMode = val[PAINT_MODE_INDEX];
        upsideDown = val[UPSIDE_DOWN_INDEX] != 0;
    }
}
