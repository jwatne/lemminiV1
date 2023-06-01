package game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Button class for TextDialog.
 *
 * @author Volker Oth
 */
public class Button {
    /** x coordinate in pixels. */
    private final int x;
    /** y coordinate in pixels. */
    private final int y;
    /** width in pixels. */
    private int width;
    /** height in pixels. */
    private int height;
    /** true if button is selected. */
    private boolean selected;
    /** normal button image. */
    private BufferedImage image;
    /** selected button image. */
    private BufferedImage imgSelected;
    /** button identifier. */
    private int id;

    /**
     * Constructor.
     *
     * @param xi  x position in pixels
     * @param yi  y position in pixels
     * @param idi identifier
     */
    Button(final int xi, final int yi, final int idi) {
        x = xi;
        y = yi;
    }

    /**
     * Set normal button image.
     *
     * @param img image
     */
    void setImage(final BufferedImage img) {
        image = img;
        if (image.getHeight() > height) {
            height = image.getHeight();
        }
        if (image.getWidth() > width) {
            width = image.getWidth();
        }
    }

    /**
     * Set selected button image.
     *
     * @param img image
     */
    void setImageSelected(final BufferedImage img) {
        imgSelected = img;
        if (imgSelected.getHeight() > height) {
            height = imgSelected.getHeight();
        }
        if (imgSelected.getWidth() > width) {
            width = imgSelected.getWidth();
        }
    }

    /**
     * Return current button image (normal or selected, depending on state).
     *
     * @return current button image
     */
    BufferedImage getImage() {
        if (selected) {
            return imgSelected;
        } else {
            return image;
        }
    }

    /**
     * Draw the button.
     *
     * @param g graphics object to draw on
     */
    void draw(final Graphics2D g) {
        g.drawImage(getImage(), x, y, null);
    }

    /**
     * Check if a (mouse) position is inside this button.
     *
     * @param xi
     * @param yi
     * @return true if the coordinates are inside this button, false if not
     */
    boolean inside(final int xi, final int yi) {
        return (xi >= x && xi < x + width && yi >= y && yi < y + height);
    }

    /**
     * Returns x coordinate in pixels.
     *
     * @return x coordinate in pixels.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns y coordinate in pixels.
     *
     * @return y coordinate in pixels.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns width in pixels.
     *
     * @return width in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets width in pixels.
     *
     * @param pixelsWidth width in pixels.
     */
    public void setWidth(final int pixelsWidth) {
        this.width = pixelsWidth;
    }

    /**
     * Returns height in pixels.
     *
     * @return height in pixels.
     */
    public final int getHeight() {
        return height;
    }

    /**
     * Sets height in pixels.
     *
     * @param pixelsHeight height in pixels.
     */
    public final void setHeight(final int pixelsHeight) {
        this.height = pixelsHeight;
    }

    /**
     * Returns true if button is selected.
     *
     * @return true if button is selected.
     */
    public final boolean isSelected() {
        return selected;
    }

    /**
     * Sets true if button is selected.
     *
     * @param isSelected true if button is selected.
     */
    public final void setSelected(final boolean isSelected) {
        this.selected = isSelected;
    }

    /**
     * Returns selected button image.
     *
     * @return selected button image.
     */
    public final BufferedImage getImgSelected() {
        return imgSelected;
    }

    /**
     * Sets selected button image.
     *
     * @param selectedImage selected button image.
     */
    public final void setImgSelected(final BufferedImage selectedImage) {
        this.imgSelected = selectedImage;
    }

    /**
     * Returns button identifier.
     *
     * @return button identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets button identifier.
     *
     * @param id button identifier.
     */
    public void setId(final int id) {
        this.id = id;
    }

}
