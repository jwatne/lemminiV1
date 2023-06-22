package game;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import game.lemmings.Lemming;
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
 * Implementation of the Lemmini selection cursor.
 *
 * @author Volker Oth
 */
public final class LemmCursor {

    /**
     * Hidden cursor height.
     */
    private static final int HIDDEN_CURSOR_HEIGHT = 3;
    /**
     * Hidden cursor width.
     */
    private static final int HIDDEN_CURSOR_WIDTH = 3;
    /**
     * Number of cursors types.
     */
    private static final int NUM_CURSORS = 5;
    /**
     * Initial number of frames.
     */
    private static final int INITIAL_FRAMES = 8;
    /**
     * Distance from center of cursor to be used to detect Lemmings under the
     * cursor.
     */
    private static final int HIT_DISTANCE = 12;

    /** cursor type. */
    public enum Type {
        /** empty image to hide cursor. */
        HIDDEN,
        /** normal cursor. */
        NORMAL,
        /** select left cursor. */
        LEFT,
        /** select right cursor. */
        RIGHT,
        /** select walkers cursor. */
        WALKER,
        /** normal cursor with selection box. */
        BOX_NORMAL,
        /** select left cursor with selection box. */
        BOX_LEFT,
        /** select right cursor with selection box. */
        BOX_RIGHT,
        /** select walkers cursor with selection box. */
        BOX_WALKER,
    }

    /** x position in pixels. */
    private static int x;
    /** y position in pixels. */
    private static int y;
    /** current cursor type. */
    private static Type type;
    /** array of images - one for each cursor type. */
    private static BufferedImage[] img;
    /** array of AWT cursor Objects. */
    private static Cursor[] cursor;
    /** is Mouse cursor hidden?. */
    private static boolean enabled;

    /**
     * Private default constructor for utility class.
     */
    private LemmCursor() {

    }

    /**
     * Initialization.
     *
     * @param frame the parent component (main frame of the application).
     * @throws ResourceException
     */
    public static void init(final Component frame) throws ResourceException {
        img = ToolBox.getAnimation(Core.loadImage("misc/cursor.gif", frame),
                INITIAL_FRAMES, Transparency.BITMASK);
        cursor = new Cursor[NUM_CURSORS];
        final int w = getImage(Type.NORMAL).getWidth() / 2;
        final int h = getImage(Type.NORMAL).getHeight() / 2;
        cursor[Type.HIDDEN.ordinal()] = Toolkit.getDefaultToolkit()
                .createCustomCursor(new BufferedImage(HIDDEN_CURSOR_WIDTH,
                        HIDDEN_CURSOR_HEIGHT, BufferedImage.TYPE_INT_ARGB),
                        new Point(0, 0), "");
        cursor[Type.NORMAL.ordinal()] = Toolkit.getDefaultToolkit()
                .createCustomCursor(LemmCursor.getImage(Type.NORMAL),
                        new Point(w, h), "");
        cursor[Type.LEFT.ordinal()] = Toolkit.getDefaultToolkit()
                .createCustomCursor(LemmCursor.getImage(Type.LEFT),
                        new Point(w, h), "");
        cursor[Type.RIGHT.ordinal()] = Toolkit.getDefaultToolkit()
                .createCustomCursor(LemmCursor.getImage(Type.RIGHT),
                        new Point(w, h), "");
        cursor[Type.WALKER.ordinal()] = Toolkit.getDefaultToolkit()
                .createCustomCursor(LemmCursor.getImage(Type.WALKER),
                        new Point(w, h), "");
        type = Type.NORMAL;
        setX(0);
        setY(0);
        enabled = true;
    }

    /**
     * Set enable state for Mouse cursor.
     *
     * @param en true to show, false to hide
     */
    public static void setEnabled(final boolean en) {
        enabled = en;
    }

    /**
     * Get enable state.
     *
     * @return true if shows, false if hidden
     */
    public static boolean getEnabled() {
        return enabled;
    }

    /**
     * Get image for a certain cursor type.
     *
     * @param t cursor type
     * @return image for the given cursor type
     */
    public static BufferedImage getImage(final Type t) {
        return img[t.ordinal() - 1];
    }

    /**
     * Get image for current cursor type.
     *
     * @return image for current cursor type
     */
    public static BufferedImage getImage() {
        return getImage(type);
    }

    /**
     * Get boxed version of image for the current cursor type.
     *
     * @return boxed version of image for the current cursor type
     */
    public static BufferedImage getBoxImage() {
        Type t;
        switch (type) {
        case NORMAL:
            t = Type.BOX_NORMAL;
            break;
        case LEFT:
            t = Type.BOX_LEFT;
            break;
        case RIGHT:
            t = Type.BOX_RIGHT;
            break;
        case WALKER:
            t = Type.BOX_WALKER;
            break;
        default:
            t = type; // should never happen
        }
        return getImage(t);
    }

    /**
     * Get current cursor as AWT cursor object.
     *
     * @return current cursor as AWT cursor object
     */
    public static Cursor getCursor() {
        if (enabled) {
            return cursor[type.ordinal()];
        } else {
            return cursor[Type.HIDDEN.ordinal()];
        }
    }

    /**
     * Get current cursor type.
     *
     * @return current cursor type
     */
    public static Type getType() {
        return type;
    }

    /**
     * Set current cursor type.
     *
     * @param t cursor type
     */
    public static void setType(final Type t) {
        type = t;
    }

    /**
     * Check if a Lemming is under the cursor.
     *
     * @param l    Lemming to check
     * @param xOfs screen x offset
     * @return true if the Lemming is under the Cursor, else false.
     */
    public static boolean doesCollide(final Lemming l, final int xOfs) {
        // get center of lemming
        final int lx = l.midX() - xOfs;
        final int ly = l.midY();

        // calculate center of cursor
        final int cx = getX();
        final int cy = getY();

        // calculate distance
        final int dx = Math.abs(lx - cx);
        final int dy = Math.abs(ly - cy);

        return dx <= HIT_DISTANCE && dy <= HIT_DISTANCE;
    }

    /**
     * Set x position in pixels.
     *
     * @param xPosition x position in pixels.
     */
    public static void setX(final int xPosition) {
        LemmCursor.x = xPosition;
    }

    /**
     * Get x position in pixels.
     *
     * @return x position in pixels
     */
    public static int getX() {
        return x;
    }

    /**
     * Set y position in pixels.
     *
     * @param yPosition y position in pixels
     */
    public static void setY(final int yPosition) {
        LemmCursor.y = yPosition;
    }

    /**
     * Get y position in pixels.
     *
     * @return y position in pixels
     */
    public static int getY() {
        return y;
    }
}
