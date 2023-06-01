package game;

/**
 * Storage class for level Entries.
 *
 * @author Volker Oth
 */
class Entry {
    /** identifier. */
    private int id;

    public int getId() {
        return id;
    }

    public void setId(final int identifier) {
        this.id = identifier;
    }

    /** x position in pixels. */
    private int xPos;

    public int getxPos() {
        return xPos;
    }

    public void setxPos(final int xPosition) {
        this.xPos = xPosition;
    }

    /** y position in pixels. */
    private int yPos;

    public int getyPos() {
        return yPos;
    }

    public void setyPos(final int yPosition) {
        this.yPos = yPosition;
    }

    /**
     * Constructor.
     *
     * @param x x position in pixels
     * @param y y position in pixels
     */
    Entry(final int x, final int y) {
        xPos = x;
        yPos = y;
    }

}
