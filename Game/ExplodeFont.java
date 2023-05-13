package Game;

import java.awt.Component;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import Tools.ToolBox;

/**
 * Used to manage the font for the explosion counter.
 * 
 * @author Volker Oth
 */
public class ExplodeFont {
    /**
     * Constructor.
     * 
     * @param cmp the parent component (main frame of the application).
     * @throws ResourceException
     */
    ExplodeFont(final Component cmp) throws ResourceException {
        final Image sourceImg = Core.loadImage("misc/countdown.gif", cmp);
        img = ToolBox.getAnimation(sourceImg, 5, Transparency.BITMASK);
    }

    /**
     * Get image for a counter value (0..9)
     * 
     * @param num counter value (0..9)
     * @return
     */
    BufferedImage getImage(final int num) {
        return img[num];
    }

    /** array of images for each counter value */
    private final BufferedImage img[];
}