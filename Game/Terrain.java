package game;

/**
 * Storage class for a terrain/background tiles.
 *
 * @author Volker Oth
 */
public class Terrain {
    /** paint mode: don't overwrite existing terrain pixel. */
    public static final int MODE_NO_OVERWRITE = 8;
    /** paint mode: upside down. */
    public static final int MODE_UPSIDE_DOWN = 4;
    /**
     * paint mode: remove existing terrain pixels instead of overdrawing them.
     */
    public static final int MODE_REMOVE = 2;
    /**
     * Index of modifier element in array passed to constructor.
     */
    private static final int MODIFIER_INDEX = 3;

    /** identifier. */
    private int id;
    /** x position in pixels. */
    private int xPos;
    /** y position in pixels. */
    private int yPos;
    /** modifier. */
    private int modifier;

    /**
     * Constructor.
     *
     * @param val three values as array [identifier, x position, y position]
     */
    public Terrain(final int[] val) {
        id = val[0];
        xPos = val[1];
        yPos = val[2];
        modifier = val[MODIFIER_INDEX];
    }

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

    /**
     * Returns x position in pixels.
     *
     * @return x position in pixels.
     */
    public final int getxPos() {
        return xPos;
    }

    /**
     * Sets x position in pixels.
     *
     * @param xPosition x position in pixels.
     */
    public final void setxPos(final int xPosition) {
        this.xPos = xPosition;
    }

    /**
     * Returns y position in pixels.
     *
     * @return y position in pixels.
     */
    public final int getyPos() {
        return yPos;
    }

    /**
     * Sets y position in pixels.
     *
     * @param yPosition y position in pixels.
     */
    public final void setyPos(final int yPosition) {
        this.yPos = yPosition;
    }

    /**
     * Returns modifier.
     *
     * @return modifier.
     */
    public final int getModifier() {
        return modifier;
    }

    /**
     * Sets modifier.
     *
     * @param mode modifier.
     */
    public final void setModifier(final int mode) {
        this.modifier = mode;
    }
}
