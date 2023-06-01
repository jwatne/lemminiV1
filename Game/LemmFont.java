package game;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

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
 * Handle the main bitmap font.
 *
 * @author Volker Oth
 */
public final class LemmFont {

    /**
     * Maximum green value for ARGB value.
     */
    private static final int MAX_GREEN = 0xff00;
    /**
     * 7-bit shift.
     */
    private static final int SHIFT_7 = 7;
    /**
     * 8-bit shift.
     */
    private static final int SHIFT_8 = 8;
    /**
     * Max RGB color channel value.
     */
    private static final int MAX_COLOR = 0xff;
    /**
     * 16-bit shift.
     */
    private static final int SHIFT_16 = 16;
    /**
     * Maximum alpha ARGB value.
     */
    private static final int MAX_ALPHA = 0xff000000;
    /**
     * Number of color indexes in img.
     */
    private static final int NUM_COLORS = 7;

    /** default width of one character in pixels. */
    private static final int SPACING = 18;
    /** character map. */
    private static final String CHARS = "!\"#$%&'()*+,-./0123456789:"
            + ";<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]"
            + "^_�abcdefghijklmnopqrstuvwxyz{|}~";

    /** width of one character in pixels. */
    private static int width;
    /** height of one character pixels. */
    private static int height;
    /** array of array of images [color,character]. */
    private static BufferedImage[][] img;

    /**
     * Private default constructor for utility class.
     */
    private LemmFont() {

    }

    /**
     * Initialization.
     *
     * @param frame the parent component (main frame of the application).
     * @throws ResourceException
     */
    public static void init(final Component frame) throws ResourceException {
        final BufferedImage sourceImg = ToolBox.imageToBuffered(
                Core.loadImage("misc/lemmfont.gif", frame),
                Transparency.BITMASK);

        width = SPACING; // sourceImg.getWidth(null);
        height = sourceImg.getHeight(null) / CHARS.length();
        final BufferedImage redImg = ToolBox.createImage(sourceImg.getWidth(),
                sourceImg.getHeight(), Transparency.BITMASK);
        final BufferedImage blueImg = ToolBox.createImage(sourceImg.getWidth(),
                sourceImg.getHeight(), Transparency.BITMASK);
        final BufferedImage turquoiseImg = ToolBox.createImage(
                sourceImg.getWidth(), sourceImg.getHeight(),
                Transparency.BITMASK);
        final BufferedImage brownImg = ToolBox.createImage(sourceImg.getWidth(),
                sourceImg.getHeight(), Transparency.BITMASK);
        final BufferedImage violetImg = ToolBox.createImage(
                sourceImg.getWidth(), sourceImg.getHeight(),
                Transparency.BITMASK);
        img = new BufferedImage[NUM_COLORS][];
        img[0] = ToolBox.getAnimation(sourceImg, CHARS.length(),
                Transparency.BITMASK, width);

        for (int xp = 0; xp < sourceImg.getWidth(null); xp++) {
            for (int yp = 0; yp < sourceImg.getHeight(null); yp++) {
                int col = sourceImg.getRGB(xp, yp); // A R G B
                final int a = col & MAX_ALPHA; // transparent part
                final int r = (col >> SHIFT_16) & MAX_COLOR;
                final int g = (col >> SHIFT_8) & MAX_COLOR;
                final int b = col & MAX_COLOR;
                // patch image to red version by swapping red and green
                // components
                col = a | (g << SHIFT_16) | (r << SHIFT_8) | b;
                redImg.setRGB(xp, yp, col);
                // patch image to blue version by swapping blue and green
                // components
                col = a | (r << SHIFT_16) | (b << SHIFT_8) | g;
                blueImg.setRGB(xp, yp, col);
                // patch image to turquoise version by setting blue component to
                // value of green
                // component
                col = a | (r << SHIFT_16) | (g << SHIFT_8) | g;
                turquoiseImg.setRGB(xp, yp, col);
                // patch image to yellow version by setting red component to
                // value of green
                // component
                col = a | (g << SHIFT_16) | (g << SHIFT_8) | b;
                brownImg.setRGB(xp, yp, col);
                // patch image to violet version by exchanging red and blue with
                // green
                col = a | (g << SHIFT_16) | (((r + b) << SHIFT_7) & MAX_GREEN)
                        | g;
                violetImg.setRGB(xp, yp, col);
            }
        }
        img[LemmingsFontColor.RED.ordinal()] = ToolBox.getAnimation(redImg,
                CHARS.length(), Transparency.BITMASK, width);
        img[LemmingsFontColor.BLUE.ordinal()] = ToolBox.getAnimation(blueImg,
                CHARS.length(), Transparency.BITMASK, width);
        img[LemmingsFontColor.TURQUOISE.ordinal()] = ToolBox.getAnimation(
                turquoiseImg, CHARS.length(), Transparency.BITMASK, width);
        img[LemmingsFontColor.BROWN.ordinal()] = ToolBox.getAnimation(brownImg,
                CHARS.length(), Transparency.BITMASK, width);
        img[LemmingsFontColor.VIOLET.ordinal()] = ToolBox.getAnimation(
                violetImg, CHARS.length(), Transparency.BITMASK, width);
    }

    /**
     * Draw string into graphics object in given color.
     *
     * @param g     graphics object to draw to.
     * @param s     string to draw.
     * @param sx    x coordinate in pixels
     * @param sy    y coordinate in pixels
     * @param color Color
     */
    public static void strImage(final Graphics2D g, final String s,
            final int sx, final int sy, final LemmingsFontColor color) {
        for (int i = 0, x = sx; i < s.length(); i++, x += SPACING) {
            final char c = s.charAt(i);
            if (c == ' ') {
                continue;
            }
            final int pos = CHARS.indexOf(c);
            if (pos > -1 && pos < CHARS.length()) {
                g.drawImage(img[color.ordinal()][pos], x, sy, null);
            }
        }
        return;
    }

    /**
     * Draw string into graphics object in given color.
     *
     * @param g     graphics object to draw to.
     * @param s     string to draw.
     * @param color Color
     */
    public static void strImage(final Graphics2D g, final String s,
            final LemmingsFontColor color) {
        strImage(g, s, 0, 0, color);
        return;
    }

    /**
     * Create image of string in given color.
     *
     * @param s     string to draw
     * @param color Color
     * @return a buffered image of the needed size that contains an image of the
     *         given string
     */
    public static BufferedImage strImage(final String s,
            final LemmingsFontColor color) {
        final BufferedImage image = ToolBox.createImage(width * s.length(),
                height, Transparency.BITMASK);
        strImage(image.createGraphics(), s, color);
        return image;
    }

    /**
     * Create image of string in default color (green).
     *
     * @param s string to draw
     * @return a buffered image of the needed size that contains an image of the
     *         given string
     */
    public static BufferedImage strImage(final String s) {
        return strImage(s, LemmingsFontColor.GREEN);
    }

    /**
     * Draw string into graphics object in default color (green).
     *
     * @param g graphics object to draw to.
     * @param s string to draw.
     */
    public static void strImage(final Graphics2D g, final String s) {
        strImage(g, s, LemmingsFontColor.GREEN);
    }

    /**
     * Get width of one character in pixels.
     *
     * @return width of one character in pixels
     */
    public static int getWidth() {
        return width;
    }

    /**
     * Get height of one character in pixels.
     *
     * @return height of one character in pixels
     */
    public static int getHeight() {
        return height;
    }

}
