package game;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
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
 * Handle small number font. Meant to print out values between 0 and 99.
 *
 * @author Volker Oth
 */
public final class NumFont {

    /**
     * Maximum value displayed with small number font.
     */
    private static final int MAX_DISPLAYED_VALUE = 99;
    /**
     * Numeric value 10 - use to determine characters to display in 10's and 1's
     * places.
     */
    private static final int TEN = 10;
    /**
     * Count of numeric values that might be displayed in the small number font.
     */
    private static final int COUNT_OF_NUMBERS = 100;
    /**
     * Number by which the height of the source image is divided to obtain final
     * height value.
     */
    private static final int SOURCE_HEIGHT_DIVISOR = 10;
    /** width in pixels. */
    private static int width;
    /** height in pixels. */
    private static int height;
    /** array of images - one for each cipher 0..9. */
    private static BufferedImage[] numImg;

    /**
     * Private default constructor for utility class.
     */
    private NumFont() {

    }

    /**
     * Load and initialize the font.
     *
     * @param frame the parent component (main frame of the application).
     * @throws ResourceException
     */
    public static void init(final Component frame) throws ResourceException {
        final Image sourceImg = Core.loadImage("misc/numfont.gif", frame);
        final BufferedImage[] img = ToolBox.getAnimation(sourceImg, 10,
                Transparency.OPAQUE);
        width = sourceImg.getWidth(null);
        height = sourceImg.getHeight(null) / SOURCE_HEIGHT_DIVISOR;
        numImg = new BufferedImage[COUNT_OF_NUMBERS];

        for (int i = 0; i < COUNT_OF_NUMBERS; i++) {
            numImg[i] = ToolBox.createImage(width * 2, height,
                    Transparency.OPAQUE);
            final Graphics2D g = numImg[i].createGraphics();
            g.drawImage(img[i / TEN], 0, 0, null);
            g.drawImage(img[i % TEN], width, 0, null);
            g.dispose();
        }
    }

    /**
     * Get an image for a number between 0 and 99.
     *
     * @param n number (0..99)
     * @return image of the number
     */
    public static BufferedImage numImage(final int n) {
        int num;

        if (n > MAX_DISPLAYED_VALUE) {
            num = MAX_DISPLAYED_VALUE;
        } else if (n < 0) {
            num = 0;
        } else {
            num = n;
        }

        return numImg[num];
    }
}
