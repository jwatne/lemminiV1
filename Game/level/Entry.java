package game.level;
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
 * Storage class for level Entries.
 *
 * @author Volker Oth
 */
public class Entry {
    /** identifier. */
    private int id;

    /** x position in pixels. */
    private int xPos;

    /** y position in pixels. */
    private int yPos;

    /**
     * Returns identifier.
     *
     * @return identifier.
     */
    public final int getId() {
        return id;
    }

    /**
     * Sets identifier.
     *
     * @param identifier identifier.
     */
    public final void setId(final int identifier) {
        this.id = identifier;
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
     * Constructor.
     *
     * @param x x position in pixels
     * @param y y position in pixels
     */
    public Entry(final int x, final int y) {
        xPos = x;
        yPos = y;
    }

}
