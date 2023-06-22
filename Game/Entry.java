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
