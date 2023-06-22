package game.lemmings;

import java.awt.Transparency;
import java.awt.image.BufferedImage;

import game.Direction;
import game.Mask;
import tools.ToolBox;
/*
 * Copyright 2009 Volker Oth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Storage class for a Lemming.
 *
 * @author Volker Oth
 */
public class LemmingResource {
    /** relative foot X position in pixels inside bitmap. */
    private int footX;

    /**
     * Returns relative foot X position in pixels inside bitmap.
     *
     * @return relative foot X position in pixels inside bitmap.
     */
    public final int getFootX() {
        return footX;
    }

    /**
     * Sets relative foot X position in pixels inside bitmap.
     *
     * @param xPosition relative foot X position in pixels inside bitmap.
     */
    public final void setFootX(final int xPosition) {
        this.footX = xPosition;
    }

    /** relative foot Y position in pixels inside bitmap. */
    private int footY;

    /**
     * Returns relative foot Y position in pixels inside bitmap.
     *
     * @return relative foot Y position in pixels inside bitmap.
     */
    public final int getFootY() {
        return footY;
    }

    /**
     * Sets relative foot Y position in pixels inside bitmap.
     *
     * @param yPosition relative foot Y position in pixels inside bitmap.
     */
    public final void setFootY(final int yPosition) {
        this.footY = yPosition;
    }

    /** mask collision ("mid") position above foot in pixels. */
    private int size;

    /**
     * Returns mask collision ("mid") position above foot in pixels.
     *
     * @return mask collision ("mid") position above foot in pixels.
     */
    public final int getSize() {
        return size;
    }

    /**
     * Sets mask collision ("mid") position above foot in pixels.
     *
     * @param midPosition mask collision ("mid") position above foot in pixels.
     */
    public final void setSize(final int midPosition) {
        this.size = midPosition;
    }

    /** width of image in pixels. */
    private int width;

    /**
     * Returns width of image in pixels.
     *
     * @return width of image in pixels.
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Sets width of image in pixels.
     *
     * @param imageWidth width of image in pixels.
     */
    public final void setWidth(final int imageWidth) {
        this.width = imageWidth;
    }

    /** height of image in pixels. */
    private int height;

    /**
     * Returns height of image in pixels.
     *
     * @return height of image in pixels.
     */
    public final int getHeight() {
        return height;
    }

    /**
     * Sets height of image in pixels.
     *
     * @param imageHeight height of image in pixels.
     */
    public final void setHeight(final int imageHeight) {
        this.height = imageHeight;
    }

    /** number of animation frames. */
    private int frames;

    /**
     * Returns number of animation frames.
     *
     * @return number of animation frames.
     */
    public final int getFrames() {
        return frames;
    }

    /**
     * Sets number of animation frames.
     *
     * @param numFrames number of animation frames.
     */
    public final void setFrames(final int numFrames) {
        this.frames = numFrames;
    }

    /** animation mode. */
    private Animation animMode;

    /**
     * Returns animation mode.
     *
     * @return animation mode.
     */
    public final Animation getAnimMode() {
        return animMode;
    }

    /**
     * Sets animation mode.
     *
     * @param mode animation mode.
     */
    public final void setAnimMode(final Animation mode) {
        this.animMode = mode;
    }

    /** number of directions (1 or 2). */
    private int dirs;

    /**
     * Returns number of directions (1 or 2).
     *
     * @return number of directions (1 or 2).
     */
    public final int getDirs() {
        return dirs;
    }

    /**
     * Sets number of directions (1 or 2).
     *
     * @param numberOfDirections number of directions (1 or 2).
     */
    public final void setDirs(final int numberOfDirections) {
        this.dirs = numberOfDirections;
    }

    /**
     * Mask step.
     */
    private int maskStep;

    /**
     * Returns mask step.
     *
     * @return mask step.
     */
    public final int getMaskStep() {
        return maskStep;
    }

    /**
     * Sets mask step.
     *
     * @param step mask step.
     */
    public final void setMaskStep(final int step) {
        this.maskStep = step;
    }

    /** array of images to store the animation [Direction][AnimationFrame]. */
    private final BufferedImage[][] img;
    /**
     * array of removal masks used for digging/bashing/mining/explosions etc.
     * [Direction]
     */
    private final Mask[] mask;
    /** array of check masks for indestructible pixels [Direction]. */
    private final Mask[] iMask;

    /**
     * Constructor.
     *
     * @param sourceImg  image containing animation frames (one above the other)
     * @param animFrames number of animation frames.
     * @param directions number of directions (1 or 2)
     */
    public LemmingResource(final BufferedImage sourceImg, final int animFrames,
            final int directions) {
        img = new BufferedImage[directions][];
        mask = new Mask[directions];
        iMask = new Mask[directions];
        frames = animFrames;
        width = sourceImg.getWidth(null);
        height = sourceImg.getHeight(null) / animFrames;
        dirs = directions;
        animMode = Animation.NONE;
        img[Direction.RIGHT.ordinal()] = ToolBox.getAnimation(sourceImg,
                animFrames, Transparency.BITMASK);
        if (dirs > 1) {
            img[Direction.LEFT.ordinal()] = ToolBox.getAnimation(
                    ToolBox.flipImageX(sourceImg), animFrames,
                    Transparency.BITMASK);
        }
    }

    /**
     * Get the mask for stencil manipulation.
     *
     * @param dir Direction
     * @return mask for stencil manipulation
     */
    public Mask getMask(final Direction dir) {
        if (dirs > 1) {
            return mask[dir.ordinal()];
        } else {
            return mask[0];
        }
    }

    /**
     * Set the mask for stencil manipulation.
     *
     * @param dir Direction
     * @param m   mask for stencil manipulation
     */
    public void setMask(final Direction dir, final Mask m) {
        if (dirs > 1) {
            mask[dir.ordinal()] = m;
        } else {
            mask[0] = m;
        }
    }

    /**
     * Get the mask for checking of indestructible pixels.
     *
     * @param dir Direction
     * @return mask for checking of indestructible pixels
     */
    public Mask getImask(final Direction dir) {
        if (dirs > 1) {
            return iMask[dir.ordinal()];
        } else {
            return iMask[0];
        }
    }

    /**
     * Set the mask for checking of indestructible pixels.
     *
     * @param dir Direction
     * @param m   mask for checking of indestructible pixels
     */
    public void setImask(final Direction dir, final Mask m) {
        if (dirs > 1) {
            iMask[dir.ordinal()] = m;
        } else {
            iMask[0] = m;
        }
    }

    /**
     * Get specific animation frame.
     *
     * @param dir   Direction.
     * @param frame Index of animation frame.
     * @return specific animation frame
     */
    public BufferedImage getImage(final Direction dir, final int frame) {
        if (dirs > 1) {
            return img[dir.ordinal()][frame];
        } else {
            return img[0][frame];
        }
    }
}
