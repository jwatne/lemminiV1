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

/**
 * Stores GIF Image in RAM.
 */
public class GIFImage {
    /** width in pixels. */
    private final int width;
    /** height in pixels. */
    private final int height;
    /** pixel data. */
    private final byte[] pixels;
    /** color palette. */
    private final Palette palette;

    /**
     * Constructor.
     *
     * @param w   width in pixels.
     * @param h   height in pixels.
     * @param buf pixel data
     * @param p   color palette
     */
    public GIFImage(final int w, final int h, final byte[] buf,
            final Palette p) {
        width = w;
        height = h;
        pixels = buf;
        palette = p;
    }

    /**
     * Get pixel data.
     *
     * @return pixel data as array of bytes
     */
    public byte[] getPixels() {
        return pixels;
    }

    /**
     * Get width in pixels.
     *
     * @return width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get height in pixels.
     *
     * @return height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get color palette.
     *
     * @return color palette
     */
    public Palette getPalette() {
        return palette;
    }
}
