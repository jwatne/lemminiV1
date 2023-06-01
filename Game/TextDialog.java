package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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
 * Class to create text screens which can be navigated with the mouse. Uses
 * {@link LemmFont} as bitmap font.
 *
 * @author Volker Oth
 */
public class TextDialog {
    /** list of buttons. */
    private final List<Button> buttons;
    /** image used as screen buffer. */
    private final BufferedImage screenBuffer;
    /** graphics object to draw in screen buffer. */
    private final Graphics2D gScreen;
    /** image used as 2nd screen buffer for offscreen drawing. */
    private final BufferedImage backBuffer;
    /** graphics object to draw in 2nd (offscreen) screen buffer. */
    private final Graphics2D gBack;
    /** width of screen in pixels. */
    private final int width;
    /** height of screen in pixels. */
    private final int height;
    /** horizontal center of the screen in pixels. */
    private final int centerX;
    /** vertical center of the screen in pixels. */
    private final int centerY;

    /**
     * Create dialog text screen.
     *
     * @param w Width of screen to create
     * @param h Height of screen to create
     */
    public TextDialog(final int w, final int h) {
        width = w;
        height = h;
        centerX = width / 2;
        centerY = height / 2;
        screenBuffer = ToolBox.createImage(w, h, Transparency.OPAQUE);
        gScreen = screenBuffer.createGraphics();
        gScreen.setClip(0, 0, width, height);
        backBuffer = ToolBox.createImage(w, h, Transparency.OPAQUE);
        gBack = backBuffer.createGraphics();
        gBack.setClip(0, 0, width, height);
        buttons = new ArrayList<Button>();
    }

    /**
     * Initialize/reset the text screen.
     */
    public void init() {
        buttons.clear();
        gScreen.setBackground(Color.BLACK);
        gScreen.clearRect(0, 0, width, height);
    }

    /**
     * Get image containing current (on screen) screenbuffer.
     *
     * @return image containing current (on screen) screenbuffer
     */
    public BufferedImage getScreen() {
        return screenBuffer;
    }

    /**
     * Fill brackground with tiles.
     *
     * @param tile Image used as tile
     */
    public void fillBackground(final BufferedImage tile) {
        for (int x = 0; x < width; x += tile.getWidth()) {
            for (int y = 0; y < width; y += tile.getHeight()) {
                gBack.drawImage(tile, x, y, null);
            }
        }
        gScreen.drawImage(backBuffer, 0, 0, null);
    }

    /**
     * Copy back buffer to front buffer.
     */
    public void copyToBackBuffer() {
        gBack.drawImage(screenBuffer, 0, 0, null);
    }

    /**
     * Set Image as background. The image will appear centered.
     *
     * @param image Image to use as background
     */
    public void setBackground(final BufferedImage image) {
        final int x = (width - image.getWidth()) / 2;
        final int y = (height - image.getHeight()) / 2;
        gBack.setBackground(Color.BLACK);
        gBack.clearRect(0, 0, width, height);
        gBack.drawImage(image, x, y, null);
        gScreen.drawImage(backBuffer, 0, 0, null);
    }

    /**
     * Restore whole background from back buffer.
     */
    public void restore() {
        gScreen.drawImage(backBuffer, 0, 0, null);
    }

    /**
     * Restore a rectangle of the background from backbuffer.
     *
     * @param x               x position of upper left corner of rectangle
     * @param y               y position of upper left corner of rectangle
     * @param rectangleWidth  width of rectangle
     * @param rectangleHeight height of rectangle
     */
    public void restoreRect(final int x, final int y, final int rectangleWidth,
            final int rectangleHeight) {
        gScreen.drawImage(backBuffer, x, y, x + rectangleWidth,
                y + rectangleHeight, x, y, x + rectangleWidth,
                y + rectangleHeight, null);
    }

    /**
     * Restore a rectangle of the background from backbuffer that might be
     * invalidated by a text starting at x,y and having a length of len
     * characters.
     *
     * @param x0 x position of upper left corner of rectangle expressed in
     *           character widths
     * @param y0 y position of upper left corner of rectangle expressed in
     *           character heights
     * @param l  Length of text
     */
    public void restoreText(final int x0, final int y0, final int l) {
        final int x = x0 * LemmFont.getWidth();
        final int y = y0 * (LemmFont.getHeight() + 4);
        final int len = l * LemmFont.getWidth();
        final int h = LemmFont.getHeight() + 4;
        gScreen.drawImage(backBuffer, x, y, x + len, y + h, x, y, x + len,
                y + h, null);
    }

    /**
     * Draw string.
     *
     * @param s   String
     * @param x0  X position relative to center expressed in character widths
     * @param y0  Y position relative to center expressed in character heights
     * @param col LemmFont color
     */
    public void print(final String s, final int x0, final int y0,
            final LemmingsFontColor col) {
        final int x = x0 * LemmFont.getWidth();
        final int y = y0 * (LemmFont.getHeight() + 4);
        LemmFont.strImage(gScreen, s, centerX + x, centerY + y, col);
    }

    /**
     * Draw string.
     *
     * @param s String
     * @param x X position relative to center expressed in character widths
     * @param y Y position relative to center expressed in character heights
     */
    public void print(final String s, final int x, final int y) {
        print(s, x, y, LemmingsFontColor.GREEN);
    }

    /**
     * Draw string horizontally centered.
     *
     * @param s   String
     * @param y0  Y position relative to center expressed in character heights
     * @param col LemmFont color
     * @return Absolute x position
     */
    public int printCentered(final String s, final int y0,
            final LemmingsFontColor col) {
        final int y = y0 * (LemmFont.getHeight() + 4);
        final int x = centerX - s.length() * LemmFont.getWidth() / 2;
        LemmFont.strImage(gScreen, s, x, centerY + y, col);
        return x;
    }

    /**
     * Draw string horizontally centered.
     *
     * @param s String
     * @param y Y position relative to center expressed in character heights
     * @return Absolute x position
     */
    public int printCentered(final String s, final int y) {
        return printCentered(s, y, LemmingsFontColor.GREEN);
    }

    /**
     * Draw Image.
     *
     * @param img Image
     * @param x   X position relative to center
     * @param y   Y position relative to center
     */
    public void drawImage(final BufferedImage img, final int x, final int y) {
        gScreen.drawImage(img, centerX + x, centerY + y, null);
    }

    /**
     * Draw Image horizontally centered.
     *
     * @param img Image
     * @param y   Y position relative to center
     */
    public void drawImage(final BufferedImage img, final int y) {
        final int x = centerX - img.getWidth() / 2;
        gScreen.drawImage(img, x, centerY + y, null);
    }

    /**
     * Add Button.
     *
     * @param x           X position relative to center in pixels
     * @param y           Y position relative to center in pixels
     * @param img         Button image
     * @param imgSelected Button selected image
     * @param id          Button ID
     */
    public void addButton(final int x, final int y, final BufferedImage img,
            final BufferedImage imgSelected, final int id) {
        final Button b = new Button(centerX + x, centerY + y, id);
        b.setImage(img);
        b.setImageSelected(imgSelected);
        buttons.add(b);
    }

    /**
     * Add text button.
     *
     * @param x0   X position relative to center (in characters)
     * @param y0   Y position relative to center (in characters)
     * @param id   Button ID
     * @param t    Button text
     * @param ts   Button selected text
     * @param col  Button text color
     * @param cols Button selected text color
     */
    public void addTextButton(final int x0, final int y0, final int id,
            final String t, final String ts, final LemmingsFontColor col,
            final LemmingsFontColor cols) {
        final int x = x0 * LemmFont.getWidth();
        final int y = y0 * (LemmFont.getHeight() + 4);
        final TextButton b = new TextButton(centerX + x, centerY + y, id);
        b.setText(t, col);
        b.setTextSelected(ts, cols);
        buttons.add(b);
    }

    /**
     * React on left click.
     *
     * @param x Absolute x position in pixels
     * @param y Absolute y position in pixels
     * @return Button ID if button clicked, else -1
     */
    public int handleLeftClick(final int x, final int y) {
        for (int i = 0; i < buttons.size(); i++) {
            final Button b = buttons.get(i);
            if (b.inside(x, y)) {
                return b.getId();
            }
        }
        return -1;
    }

    /**
     * React on mouse hover.
     *
     * @param x Absolute x position
     * @param y Absolute y position
     */
    public void handleMouseMove(final int x, final int y) {
        for (int i = 0; i < buttons.size(); i++) {
            final Button b = buttons.get(i);
            b.setSelected(b.inside(x, y));
        }
    }

    /**
     * Draw buttons on screen.
     */
    public void drawButtons() {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).draw(gScreen);
        }
    }

    /**
     * React on right click.
     *
     * @param x Absolute x position
     * @param y Absolute y position
     * @return Button ID if button clicked, else -1
     */
    public int handleRightClick(final int x, final int y) {
        for (int i = 0; i < buttons.size(); i++) {
            final Button b = buttons.get(i);

            if (b.inside(x, y)) {
                return b.getId();
            }
        }

        return -1;
    }
}
