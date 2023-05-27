package game;

import java.awt.Transparency;
import java.awt.image.BufferedImage;

import tools.ToolBox;

/**
 * Storage class for a Lemming.
 *
 * @author Volker Oth
 */
public class LemmingResource {
    /** relative foot X position in pixels inside bitmap. */
    int footX;
    /** relative foot Y position in pixels inside bitmap. */
    int footY;
    /** mask collision ("mid") position above foot in pixels. */
    int size;
    /** width of image in pixels. */
    int width;
    /** height of image in pixels. */
    int height;
    /** number of animation frames. */
    int frames;
    /** animation mode. */
    Lemming.Animation animMode;
    /** number of directions (1 or 2). */
    int dirs;
    int maskStep;
    /** array of images to store the animation [Direction][AnimationFrame]. */
    private final BufferedImage img[][];
    /**
     * array of removal masks used for digging/bashing/mining/explosions etc.
     * [Direction]
     */
    private final Mask mask[];
    /** array of check masks for indestructible pixels [Direction]. */
    private final Mask iMask[];

    /**
     * Constructor.
     *
     * @param sourceImg  image containing animation frames (one above the other)
     * @param animFrames number of animation frames.
     * @param directions number of directions (1 or 2)
     */
    LemmingResource(final BufferedImage sourceImg, final int animFrames, final int directions) {
        img = new BufferedImage[directions][];
        mask = new Mask[directions];
        iMask = new Mask[directions];
        frames = animFrames;
        width = sourceImg.getWidth(null);
        height = sourceImg.getHeight(null) / animFrames;
        dirs = directions;
        animMode = Lemming.Animation.NONE;
        img[Lemming.Direction.RIGHT.ordinal()] = ToolBox.getAnimation(sourceImg, animFrames, Transparency.BITMASK);
        if (dirs > 1)
            img[Lemming.Direction.LEFT.ordinal()] = ToolBox.getAnimation(ToolBox.flipImageX(sourceImg), animFrames,
                    Transparency.BITMASK);
    }

    /**
     * Get the mask for stencil manipulation.
     *
     * @param dir Direction
     * @return mask for stencil manipulation
     */
    Mask getMask(final Lemming.Direction dir) {
        if (dirs > 1)
            return mask[dir.ordinal()];
        else
            return mask[0];
    }

    /**
     * Set the mask for stencil manipulation.
     *
     * @param dir Direction
     * @param m   mask for stencil manipulation
     */
    void setMask(final Lemming.Direction dir, final Mask m) {
        if (dirs > 1)
            mask[dir.ordinal()] = m;
        else
            mask[0] = m;
    }

    /**
     * Get the mask for checking of indestructible pixels.
     *
     * @param dir Direction
     * @return mask for checking of indestructible pixels
     */
    Mask getImask(final Lemming.Direction dir) {
        if (dirs > 1)
            return iMask[dir.ordinal()];
        else
            return iMask[0];
    }

    /**
     * Set the mask for checking of indestructible pixels
     *
     * @param dir Direction
     * @param m   mask for checking of indestructible pixels
     */
    void setImask(final Lemming.Direction dir, final Mask m) {
        if (dirs > 1)
            iMask[dir.ordinal()] = m;
        else
            iMask[0] = m;
    }

    /**
     * Get specific animation frame.
     *
     * @param dir   Direction.
     * @param frame Index of animation frame.
     * @return specific animation frame
     */
    BufferedImage getImage(final Lemming.Direction dir, final int frame) {
        if (dirs > 1)
            return img[dir.ordinal()][frame];
        else
            return img[0][frame];
    }
}
