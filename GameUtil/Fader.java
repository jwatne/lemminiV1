package gameutil;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import lemmini.Constants;
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
 * Simple fader class. instead of doing painfully slow pixel wise gamma
 * manipulation, use a square with transparency with is drawn over the whole
 * graphic context.
 *
 * @author Volker Oth
 */
public final class Fader {
    /**
     * Maximum hex value for RGBA channel.
     */
    private static final int MAX_CHANNEL_VALUE = 0xff;
    /**
     * White RGB hex value.
     */
    private static final int WHITE = 0xffffff;
    /**
     * Half transparent RGBA alpha value.
     */
    private static final int HALF_TRANSPARENT_ALPHA = 0x80;
    /**
     * Fade step size.
     */
    private static final int FADE_STEP_SIZE = 14;
    /** width of square to use for fading. */
    private static final int WIDTH = 64;
    /** height of square to use for fading. */
    private static final int HEIGHT = 64;
    /** maximum alpha (opaque). */
    private static final int MAX_ALPHA_VALUE = 0xff;

    /** current alpha value. */
    private static int fadeValue;
    /** current fade state. */
    private static FaderState fadeState = FaderState.OFF;
    /** step size for fading. */
    private static int fadeStep = FADE_STEP_SIZE;
    /** color of the fading rectangle. */
    private static int color = 0; // black
    /** alpha value of the fading rectangle. */
    private static int alpha = HALF_TRANSPARENT_ALPHA; // half transparent
    /** width of faded area. */
    private static int width;
    /** height of faded area. */
    private static int height;
    /** the image used as fading rectangle. */
    private static BufferedImage alphaImg = null;
    /**
     * The graphics used as fading rectangle (static to avoid multiple
     * allocation).
     */
    private static Graphics2D alphaGfx;

    /**
     * Private constructor for utility class.
     */
    private Fader() {

    }

    /**
     * Set color to be used for fading.
     *
     * @param c RGB color
     */
    public static synchronized void setColor(final int c) {
        color = c & WHITE;
        init();
    }

    /**
     * Set alpha value to be used for fading.
     *
     * @param a 8bit alpha value
     */
    public static synchronized void setAlpha(final int a) {
        alpha = a & MAX_CHANNEL_VALUE;
        init();
    }

    /**
     * Set bounds of fading area.
     *
     * @param w width in pixels
     * @param h height pixels
     */
    public static synchronized void setBounds(final int w, final int h) {
        width = w;
        height = h;
    }

    /**
     * Initialize fader.
     */
    private static void init() {
        Color fillColor; /*
                          * ARGB color of the fading rectangle composed from
                          * alpha and color
                          */
        // create alpha image if needed
        if (alphaImg == null) {
            alphaImg = ToolBox.createImage(WIDTH, HEIGHT,
                    Transparency.TRANSLUCENT);
            alphaGfx = alphaImg.createGraphics();
        }
        // fill with alpha blended color
        fillColor = new Color(
                (color >> Constants.TWO_BYTES) & MAX_CHANNEL_VALUE,
                (color >> Constants.TWO_BYTES) & MAX_CHANNEL_VALUE,
                color & MAX_CHANNEL_VALUE, alpha);
        alphaGfx.setBackground(fillColor);
        alphaGfx.clearRect(0, 0, WIDTH, HEIGHT);
    }

    /**
     * Apply fader without changing the fader state.
     *
     * @param g graphics to apply fader to
     */
    public static synchronized void apply(final Graphics g) {
        for (int y = 0; y < height; y += HEIGHT) {
            for (int x = 0; x < width; x += WIDTH) {
                g.drawImage(alphaImg, x, y, null);
            }
        }
    }

    /**
     * Set fader state.
     *
     * @param s state
     */
    public static synchronized void setState(final FaderState s) {
        fadeState = s;

        switch (fadeState) {
        case IN:
            fadeValue = MAX_ALPHA_VALUE; // opaque
            setAlpha(fadeValue);
            break;
        case OUT:
            fadeValue = 0; // transparent
            setAlpha(fadeValue);
            break;
        default:
            break;
        }
    }

    /**
     * Get fader state.
     *
     * @return fader state.
     */
    public static synchronized FaderState getState() {
        return fadeState;
    }

    /**
     * Set step size.
     *
     * @param step
     */
    public static void setStep(final int step) {
        fadeStep = step & MAX_CHANNEL_VALUE;
    }

    /**
     * Fade.
     *
     * @param g graphics to fade
     */
    public static synchronized void fade(final Graphics g) {
        switch (fadeState) {
        case IN:
            if (fadeValue >= fadeStep) {
                fadeValue -= fadeStep;
            } else {
                fadeValue = 0;
                fadeState = FaderState.OFF;
            }
            Fader.setAlpha(fadeValue);
            Fader.apply(g);
            // System.out.println(fadeValue);
            break;
        case OUT:
            if (fadeValue <= MAX_ALPHA_VALUE - fadeStep) {
                fadeValue += fadeStep;
            } else {
                fadeValue = MAX_ALPHA_VALUE;
                fadeState = FaderState.OFF;
            }
            Fader.setAlpha(fadeValue);
            Fader.apply(g);
            // System.out.println(fadeValue);
            break;
        default:
            break;
        }
    }
}
