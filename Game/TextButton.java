package game;

import java.awt.image.BufferedImage;

/**
 * Button class for TextDialog.
 *
 * @author Volker Oth
 */
public class TextButton extends Button {
    /**
     * Constructor.
     *
     * @param xi  x position in pixels
     * @param yi  y position in pixels
     * @param idi identifier
     */
    TextButton(final int xi, final int yi, final int idi) {
        super(xi, yi, idi);
    }

    /**
     * Set text which is used as button.
     *
     * @param s     String which contains the button text
     * @param color Color of the button (LemmFont color!)
     */
    void setText(final String s, final LemmingsFontColor color) {
        this.setImage(LemmFont.strImage(s, color));
        final BufferedImage image = getImage();
        final int height = image.getHeight();

        if (height > this.getHeight()) {
            this.setHeight(height);
        }

        final int width = image.getWidth();

        if (width > this.getWidth()) {
            this.setWidth(width);
        }
    }

    /**
     * Set text for selected button.
     *
     * @param s     String which contains the selected button text
     * @param color Color of the button (LemmFont color!)
     */
    void setTextSelected(final String s, final LemmingsFontColor color) {
        this.setImgSelected(LemmFont.strImage(s, color));
        final BufferedImage imgSelected = getImgSelected();
        final int height = imgSelected.getHeight();

        if (height > this.getHeight()) {
            this.setHeight(height);
        }

        final int width = imgSelected.getWidth();

        if (width > this.getWidth()) {
            this.setWidth(width);
        }
    }
}
