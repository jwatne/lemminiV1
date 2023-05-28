package extract;

/**
 * Storage class for palettes.
 *
 * @author Volker Oth
 */
public class Palette {
    /** byte array of red components. */
    private final byte[] red;
    /** byte array of green components. */
    private final byte[] green;
    /** byte array of blue components. */
    private final byte[] blue;

    /**
     * Create palette from array of color components.
     *
     * @param r byte array of red components
     * @param g byte array of green components
     * @param b byte array of blue components
     */
    public Palette(final byte[] r, final byte[] g, final byte[] b) {
        red = r;
        green = g;
        blue = b;
    }

    /**
     * Get blue components.
     *
     * @return byte array of blue components
     */
    public byte[] getBlue() {
        return blue;
    }

    /**
     * Get green components.
     *
     * @return byte array of green components
     */
    public byte[] getGreen() {
        return green;
    }

    /**
     * Get red components.
     *
     * @return byte array of red components
     */
    public byte[] getRed() {
        return red;
    }
}
