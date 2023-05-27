package game;

import java.awt.Component;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import tools.ToolBox;

/**
 * Used to manage the font for the explosion counter.
 *
 * @author Volker Oth
 */
public class ExplodeFont {
    /** Number of animation frames. */
    private static final int ANIMATION_FRAMES = 5;

    /**
     * Constructor.
     *
     * @param cmp the parent component (main frame of the application).
     * @throws ResourceException
     */
    ExplodeFont(final Component cmp) throws ResourceException {
        final Image sourceImg = Core.loadImage("misc/countdown.gif", cmp);
        img = ToolBox.getAnimation(sourceImg, ANIMATION_FRAMES,
                Transparency.BITMASK);
    }

    /**
     * Get image for a counter value (0..9).
     *
     * @param num counter value (0..9)
     * @return image for the counter value.
     */
    BufferedImage getImage(final int num) {
        return img[num];
    }

    /** array of images for each counter value. */
    private final BufferedImage[] img;
}
