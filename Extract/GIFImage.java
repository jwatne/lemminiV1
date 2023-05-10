package Extract;

/**
 * Stores GIF Image in RAM.
 */
class GIFImage {
    /** width in pixels */
    private final int width;
    /** height in pixels */
    private final int height;
    /** pixel data */
    private final byte[] pixels;
    /** color palette */
    final Palette palette;

    /**
     * Constructor.
     * 
     * @param w   width in pixels.
     * @param h   height in pixels.
     * @param buf pixel data
     * @param p   color palette
     */
    public GIFImage(final int w, final int h, final byte[] buf, final Palette p) {
        width = w;
        height = h;
        pixels = buf;
        palette = p;
    }

    /**
     * Get pixel data.
     * 
     * @return pixel data as array of bytes
     */
    public byte[] getPixels() {
        return pixels;
    }

    /**
     * Get width in pixels.
     * 
     * @return width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get height in pixels.
     * 
     * @return height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get color palette.
     * 
     * @return color palette
     */
    public Palette getPalette() {
        return palette;
    }
}