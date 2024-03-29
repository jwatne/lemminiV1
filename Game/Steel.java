package game;
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
 * Storage class for steel tiles.
 *
 * @author Volker Oth
 */
public class Steel {
    /**
     * Index of height in array passed to constructor.
     */
    private static final int HEIGHT_INDEX = 3;
    /** x position in pixels. */
    private int xPos;
    /** y position in pixels. */
    private int yPos;
    /** width in pixels. */
    private int width;
    /** height in pixels. */
    private int height;

    /**
     * Constructor.
     *
     * @param val four values as array [x position, y position, width, height]
     */
    public Steel(final int[] val) {
        xPos = val[0];
        yPos = val[1];
        width = val[2];
        height = val[HEIGHT_INDEX];
    }

    /**
     * Returns x position in pixels.
     *
     * @return x position in pixels.
     */
    public final int getxPos() {
        return xPos;
    }

    /**
     * Sets x position in pixels.
     *
     * @param xPosition x position in pixels.
     */
    public final void setxPos(final int xPosition) {
        this.xPos = xPosition;
    }

    /**
     * Returns y position in pixels.
     *
     * @return y position in pixels.
     */
    public final int getyPos() {
        return yPos;
    }

    /**
     * Sets y position in pixels.
     *
     * @param yPosition y position in pixels.
     */
    public final void setyPos(final int yPosition) {
        this.yPos = yPosition;
    }

    /**
     * Returns width in pixels.
     *
     * @return width in pixels.
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Sets width in pixels.
     *
     * @param pixelsWidth width in pixels.
     */
    public final void setWidth(final int pixelsWidth) {
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
}
