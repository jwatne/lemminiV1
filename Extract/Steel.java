package extract;
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

import lemmini.Constants;

/**
 *
 * Storage class for steel areas.
 *
 * @author Volker Oth
 */
class Steel {
    /**
     * 7-bit mask = 0x7f.
     */
    private static final int SEVEN_BIT_MASK = 0x7f;
    /**
     * Pixels per nibble = 4.
     */
    private static final int PIXELS_PER_NIBBLE = 4;
    /** x position in pixels. */
    private int xPos;

    /**
     * Returns x position in pixels.
     *
     * @return x position in pixels.
     */
    public int getxPos() {
        return xPos;
    }

    /**
     * Sets x position in pixels.
     *
     * @param xPosition x position in pixels.
     */
    public void setxPos(final int xPosition) {
        this.xPos = xPosition;
    }

    /** y position in pixels. */
    private int yPos;

    /**
     * Returns y position in pixels.
     *
     * @return y position in pixels.
     */
    public int getyPos() {
        return yPos;
    }

    /**
     * Sets y position in pixels.
     *
     * @param yPosition y position in pixels.
     */
    public void setyPos(final int yPosition) {
        this.yPos = yPosition;
    }

    /** width in pixels. */
    private int width;

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

    /** height in pixels. */
    private int height;

    /**
     * Returns height in pixels.
     *
     * @return height in pixels.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets height in pixels.
     *
     * @param pixelsHeight height in pixels.
     */
    public void setHeight(final int pixelsHeight) {
        this.height = pixelsHeight;
    }

    /**
     * Constructor.
     *
     * @param b     buffer
     * @param scale Scale (to convert lowres levels into hires levels)
     */
    Steel(final byte[] b, final int scale) { // note: last byte is always 0
        // xpos: 9-bit value: 0x000..0x178). 0x000 = -16, 0x178 = 1580
        xPos = (((b[0] & Constants.EIGHT_BIT_MASK) << 1)
                + ((b[1] & Constants.BIT_8_MASK) >> Constants.SHIFT_7))
                * PIXELS_PER_NIBBLE - Constants.X_OFFSET;
        xPos *= scale;
        // ypos: 0x00..0x27. 0x00 = 0, 0x27 = 156 - each hex value represents 4
        // pixels
        yPos = (b[1] & SEVEN_BIT_MASK) * PIXELS_PER_NIBBLE;
        yPos *= scale;
        // area: 0x00..max 0xFF. first nibble is the x-size, from 0..F
        // (represents 4
        // pixels)
        // second nibble is the y-size. 0x00 = (4,4), 0x11 = (8,8), 0x7F =
        // (32,64)
        width = ((b[2] & Constants.BITS_5_TO_8_MASK) >> Constants.SHIFT_4)
                * PIXELS_PER_NIBBLE + PIXELS_PER_NIBBLE;
        width *= scale;
        height = (b[2] & Constants.FOUR_BIT_MASK) * PIXELS_PER_NIBBLE
                + PIXELS_PER_NIBBLE;
        height *= scale;
    }
}
