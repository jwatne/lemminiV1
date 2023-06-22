package game.level;

import java.awt.image.BufferedImage;

import game.GameController;

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
 * Masks are used to erase or draw elements in the background stencil. E.g.
 * digger, bashers and explosions erase elements from the stencil via a mask.
 * Also stairs created by the builder are handled via a mask.
 *
 * @author Volker Oth
 */
public class Mask {
    /**
     * Maximum Red (R) and Blue (B) RGBA value.
     */
    private static final int MAX_R_B = 0xff00ff00;
    /**
     * Number by which to divide total number of pixels to get maxMaskPixels.
     */
    private static final int MAX_MASK_PIXELS_DIVIDER = 3;
    /**
     * Maximum alpha ARGB value.
     */
    private static final int MAX_ALPHA = 0xff000000;
    /**
     * number of pixels in mask that may be indestructible before action is
     * stopped.
     */
    private final int[] maxMaskPixels;
    /** width of mask in pixels. */
    private final int width;
    /** height of mask in pixels. */
    private final int height;
    /**
     * array of byte arrays. Note: masks may be animated and thus contain
     * multiple frames..
     */
    private final byte[][] mask;

    /**
     * Constructor.
     *
     * @param img    image which may contain several animation frames one above
     *               each other
     * @param frames number of animation frames
     */
    public Mask(final BufferedImage img, final int frames) {
        width = img.getWidth(null);
        height = img.getHeight(null) / frames;
        mask = new byte[frames][];
        maxMaskPixels = new int[frames];

        for (int i = 0; i < frames; i++) {
            int pos = 0;
            final int y0 = i * height;
            maxMaskPixels[i] = 0;
            mask[i] = new byte[width * height];

            for (int y = y0; y < y0 + height; y++, pos += width) {
                for (int x = 0; x < width; x++) {
                    final int rgba = img.getRGB(x, y);

                    if (((rgba & MAX_ALPHA) != 0)) {
                        mask[i][pos + x] = (byte) 1;
                        maxMaskPixels[i]++;
                    } else {
                        mask[i][pos + x] = (byte) 0;
                    }
                }
            }

            /*
             * now maxMaskPixels[i] contains the exact amount of active pixels
             * in the mask however, it works better to stop a mask action
             * already if there are only about a third of the pixels
             * indestructible, so divide by 3 critical: for diggers, the mask is
             * 34 pixels in size but a maximum of 17 pixels is in the mask.
             */
            maxMaskPixels[i] /= MAX_MASK_PIXELS_DIVIDER;
        }
    }

    /**
     * Apply erase mask (to background image, MiniMap and Stencil).
     *
     * @param x0        x position in pixels
     * @param y0        y position in pixels
     * @param maskNum   index of mask if there are multiple animation frames,
     *                  else 0
     * @param checkMask Stencil bitmask with attributes that make the pixel
     *                  indestructible
     * @return true if the number of indestructible pixels was > than
     *         maxMaskPixels
     */
    public boolean eraseMask(final int x0, final int y0, final int maskNum,
            final int checkMask) {
        int ctrIndestructable = 0;
        final BufferedImage bgImage = GameController.getBgImage();
        final BufferedImage bgImageSmall = MiniMap.getImage();
        final Stencil stencil = GameController.getStencil();
        final byte[] m = mask[maskNum];
        int sPos = y0 * bgImage.getWidth();
        int pos = 0;
        final int scaleX = bgImage.getWidth() / bgImageSmall.getWidth();
        final int scaleY = bgImage.getHeight() / bgImageSmall.getHeight();
        int yMax = y0 + height;

        if (yMax >= bgImage.getHeight()) {
            yMax = bgImage.getHeight();
        }
        int xMax = x0 + width;

        if (xMax >= bgImage.getWidth()) {
            xMax = bgImage.getWidth();
        }

        final int bgCol = 0 /* GameController.level.bgCol */;

        for (int y = y0; y < yMax; y++, pos += width, sPos += bgImage
                .getWidth()) {

            if (y < 0) {
                continue;
            }

            final boolean drawSmallY = (y % scaleY) == 0;

            for (int x = x0; x < xMax; x++) {
                if (x < 0) {
                    continue;
                }

                final boolean drawSmallX = (x % scaleX) == 0;
                final int s = stencil.get(sPos + x);

                if (m[pos + x - x0] != 0) {
                    if ((s & checkMask) == 0) {
                        // special handling for objects with "NO DIG" stencil
                        // (basically arrows)
                        if ((s & Stencil.MSK_NO_DIG) != 0) {
                            // get object
                            final SpriteObject spr = GameController.getLevel()
                                    .getSprObject(Stencil.getObjectID(s));
                            // remove pixel from all object images
                            spr.setPixel(x - spr.getX(), y - spr.getY(), 0);
                        }

                        // erase pixel
                        stencil.set(sPos + x, s & Stencil.MSK_ERASE); // erase
                                                                      // brick
                                                                      // in
                                                                      // stencil
                        bgImage.setRGB(x, y, bgCol); // erase pixel in bgIMage

                        if (drawSmallX && drawSmallY) {
                            bgImageSmall.setRGB(x / scaleX, y / scaleY,
                                    MAX_ALPHA/* bgCol */); // erase pixel in
                                                           // bgIMageSmall
                        }
                    } else {
                        ctrIndestructable++;
                    }
                }
            }
        }

        return ctrIndestructable > maxMaskPixels[maskNum]; // to be checked
    }

    /**
     * Paint one step (of a stair created by a Builder).
     *
     * @param x0      x position in pixels
     * @param y0      y position in pixels
     * @param maskNum index of mask if there are multiple animation frames, else
     *                0
     * @param color   Color to use to paint the step in the background image
     */
    public void paintStep(final int x0, final int y0, final int maskNum,
            final int color) {
        final BufferedImage bgImage = GameController.getBgImage();
        final BufferedImage bgImageSmall = MiniMap.getImage();
        final Stencil stencil = GameController.getStencil();
        final byte[] m = mask[maskNum];
        int sPos = y0 * bgImage.getWidth();
        int pos = 0;
        final int scaleX = bgImage.getWidth() / bgImageSmall.getWidth();
        final int scaleY = bgImage.getHeight() / bgImageSmall.getHeight();
        int yMax = y0 + height;

        if (yMax >= bgImage.getHeight()) {
            yMax = bgImage.getHeight();
        }

        int xMax = x0 + width;

        if (xMax >= bgImage.getWidth()) {
            xMax = bgImage.getWidth();
        }

        for (int y = y0; y < yMax; y++, pos += width, sPos += bgImage
                .getWidth()) {
            final boolean drawSmallY = (y % scaleY) == 0;

            if (y < 0) {
                continue;
            }

            for (int x = x0; x < xMax; x++) {
                if (x < 0) {
                    continue;
                }

                final boolean drawSmallX = (x % scaleX) == 0;
                int s = stencil.get(sPos + x);

                if (m[pos + x
                        - x0] != 0 /* && (s & Stencil.MSK_WALK_ON) == 0 */) {
                    // mask pixel set
                    if ((s & Stencil.MSK_WALK_ON) == 0) {
                        s |= Stencil.MSK_BRICK;
                    }

                    stencil.set(sPos + x, s | Stencil.MSK_STAIR); // set type in
                                                                  // stencil
                    bgImage.setRGB(x, y, color);

                    if (drawSmallX && drawSmallY) {
                        bgImageSmall.setRGB(x / scaleX, y / scaleY,
                                color & MAX_R_B); // green pixel in
                                                  // bgIMageSmall
                    }
                }
            }
        }
    }

    /**
     * Create stopper mask in the Stencil only (Lemming is assigned a
     * Blocker/Stopper).
     *
     * @param x0   x position in pixels
     * @param y0   y position in pixels
     * @param xMid x position of Lemming's foot
     */
    public void setStopperMask(final int x0, final int y0, final int xMid) {
        final BufferedImage bgImage = GameController.getBgImage();
        final Stencil stencil = GameController.getStencil();
        final byte[] m = mask[0];
        int sPos = y0 * bgImage.getWidth();
        int pos = 0;
        int yMax = y0 + height;

        if (yMax >= bgImage.getHeight()) {
            yMax = bgImage.getHeight();
        }

        int xMax = x0 + width;

        if (xMax >= bgImage.getWidth()) {
            xMax = bgImage.getWidth();
        }

        for (int y = y0; y < yMax; y++, pos += width, sPos += bgImage
                .getWidth()) {
            if (y < 0) {
                continue;
            }

            for (int x = x0; x < xMax; x++) {
                if (x < 0) {
                    continue;
                }

                if (m[pos + x - x0] != 0) {
                    final int s = stencil.get(sPos + x);

                    // set type in stencil
                    if (x <= xMid) {
                        stencil.set(sPos + x, s | Stencil.MSK_STOPPER_LEFT);
                    } else {
                        stencil.set(sPos + x, s | Stencil.MSK_STOPPER_RIGHT);
                    }
                }
            }
        }
    }

    /**
     * Use mask to check bitmask properties of Stencil.
     *
     * @param x0      x position in pixels
     * @param y0      y position in pixels
     * @param maskNum index of mask if there are multiple animation frames, else
     *                0
     * @param type    Stencil bitmask to check (may contain several attributes)
     * @return true if at least one pixel with one of the given attributes is
     *         found
     */
    public boolean checkType(final int x0, final int y0, final int maskNum,
            final int type) {
        final Stencil stencil = GameController.getStencil();
        final byte[] m = mask[maskNum];
        int sPos = y0 * stencil.getWidth();
        int pos = 0;
        int yMax = y0 + height;

        if (yMax >= stencil.getHeight()) {
            yMax = stencil.getHeight();
        }

        int xMax = x0 + width;

        if (xMax >= stencil.getWidth()) {
            xMax = stencil.getWidth();
        }

        for (int y = y0; y < yMax; y++, pos += width, sPos += stencil
                .getWidth()) {
            if (y < 0) {
                continue;
            }

            for (int x = x0; x < xMax; x++) {
                if (x < 0) {
                    continue;
                }

                if (m[pos + x - x0] != 0) {
                    final int s = stencil.get(sPos + x);
                    if ((s & type) != 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Erase certain properties from Stencil bitmask.
     *
     * @param x0      x position in pixels
     * @param y0      y position in pixels
     * @param maskNum index of mask if there are multiple animation frames, else
     *                0
     * @param type    Stencil bitmask to erase (may contain several attributes)
     */
    public void clearType(final int x0, final int y0, final int maskNum,
            final int type) {
        final BufferedImage bgImage = GameController.getBgImage();
        final Stencil stencil = GameController.getStencil();
        final byte[] m = mask[maskNum];
        int sPos = y0 * bgImage.getWidth();
        int pos = 0;
        int yMax = y0 + height;

        if (yMax >= bgImage.getHeight()) {
            yMax = bgImage.getHeight();
        }

        int xMax = x0 + width;

        if (xMax >= bgImage.getWidth()) {
            xMax = bgImage.getWidth();
        }

        for (int y = y0; y < yMax; y++, pos += width, sPos += bgImage
                .getWidth()) {
            if (y < 0) {
                continue;
            }

            for (int x = x0; x < xMax; x++) {
                if (x < 0) {
                    continue;
                }

                if (m[pos + x - x0] != 0) {
                    final int s = stencil.get(sPos + x);
                    stencil.set(sPos + x, s & ~type); // erase type in stencil
                    // bgImage.setRGB(x, y, 0xffff0000); // debug
                }
            }
        }
    }

    /**
     * Get width.
     *
     * @return width in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get height.
     *
     * @return height in pixels.
     */
    public int getHeight() {
        return height;
    }
}
