package extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * Extract graphics from "Lemming for Win95" SPR data files.
 *
 * @author Volker Oth
 */
public class ExtractSPR {

    /**
     * Mandatory value for end character read from file.
     */
    private static final int END_CHARACTER = 0x80;

    /**
     * 7-bit mask = 0x7f.
     */
    private static final int SEVEN_BIT_MASK = 0x7f;

    /**
     * Maximum line length value allowed.
     */
    private static final int MAX_LINE_LENGTH = 0x7f;

    /**
     * Cutoff value for determining if additional line offset is needed.
     */
    private static final int ADD_LINE_OFFSET_CUTOFF = 0x80;

    /**
     * Special line offset for large sprites.
     */
    private static final int LARGE_SPRITE_LINE_OFFSET = 0x7f;

    /**
     * Second index for ofs data in buffer.
     */
    private static final int OFS_INDEX_2 = 7;

    /**
     * First index for ofs data in buffer.
     */
    private static final int OFS_INDEX_1 = 6;

    /**
     * Second index for frames data in buffer.
     */
    private static final int FRAMES_INDEX_2 = 5;

    /**
     * First index for frames data in buffer.
     */
    private static final int FRAMES_INDEX_1 = 4;

    /**
     * Value for index 3 of buffer for header for sprite file.
     */
    private static final int SPRITE_HEADER3_VALUE = 0x45;

    /**
     * Value for index 1 of buffer for header for sprite file.
     */
    private static final int SPRITE_HEADER1_VALUE = 0x52;

    /**
     * Value for index 0 of buffer for header for sprite file.
     */
    private static final int SPRITE_HEADER0_VALUE = 0x53;

    /**
     * 8-bit mask = 0xFF.
     */
    private static final int EIGHT_BIT_MASK = 0xFF;

    /**
     * Offset into color information - skip 2 bytes?
     */
    private static final int COLOR_OFFSET = 6;

    /**
     * Decimal (base 10) value 256.
     */
    private static final int DECIMAL_256 = 256;

    /**
     * Second index of pallette size data in buffer array.
     */
    private static final int PALLETE_SIZE_INDEX_2 = 5;

    /**
     * First index of pallette size data in buffer array.
     */
    private static final int PALLETTE_SIZE_INDEX_1 = 4;

    /**
     * Hexidecimal value 0x50.
     */
    private static final int HEX50 = 0x50;

    /**
     * Array index 3.
     */
    private static final int INDEX3 = 3;

    /**
     * Hexidecimal value 0x41.
     */
    private static final int HEX_41 = 0x41;

    /**
     * Hexidecimal value 0x4c.
     */
    private static final int HEX_4C = 0x4c;

    /**
     * Hexidecimal value 0x20.
     */
    private static final int HEX_20 = 0x20;

    /** palette index of transparent color. */
    private static final int TRANSPARENT_INDEX = 0;

    /** array of GIF images to store to disk. */
    private GIFImage[] images;
    /** color palette. */
    private Palette palette = null;
    /**
     * Buffer used to compress the palette (remove double entries) to work
     * around issues on MacOS.
     */
    private int[] lookupBuf;

    /**
     * Load palette.
     *
     * @param fname Name of palette file
     * @return ColorModel representation of Palette
     * @throws ExtractException
     */
    Palette loadPalette(final String fname) throws ExtractException {
        byte[] buffer;
        // read file into buffer
        int paletteSize = 0;
        final File f = new File(fname);

        try (FileInputStream fi = new FileInputStream(fname)) {
            buffer = new byte[(int) f.length()];

            if (fi.read(buffer) < 1) {
                System.out.println("0 bytes read from file " + fname);
            }
        } catch (final FileNotFoundException e) {
            throw new ExtractException("File " + fname + " not found");
        } catch (final IOException e) {
            throw new ExtractException("I/O error while reading " + fname);
        }

        // check header
        if (buffer[0] != HEX_20 || buffer[1] != HEX_4C || buffer[2] != HEX_41
                || buffer[INDEX3] != HEX50) {
            throw new ExtractException(
                    "File " + fname + " ist not a lemmings palette file");
        }

        paletteSize = unsigned(buffer[PALLETTE_SIZE_INDEX_1])
                + unsigned(buffer[PALLETE_SIZE_INDEX_2]) * DECIMAL_256;
        // number of palette entries
        byte[] r = new byte[paletteSize];
        byte[] g = new byte[paletteSize];
        byte[] b = new byte[paletteSize];
        int ofs = COLOR_OFFSET; // skip two bytes which contain number of
                                // palettes (?)

        for (int idx = 0; idx < paletteSize; idx++) {
            r[idx] = buffer[ofs++];
            g[idx] = buffer[ofs++];
            b[idx] = buffer[ofs++];
            ofs++;
        }

        // search for double entries, create
        // new palette without double entries
        // and lookup table to fix the pixel values
        final byte[] compressedR = new byte[paletteSize];
        final byte[] compressedG = new byte[paletteSize];
        final byte[] compressedB = new byte[paletteSize];
        lookupBuf = new int[paletteSize];
        Arrays.fill(lookupBuf, -1); // mark all entries invalid
        Arrays.fill(compressedR, (byte) 0);
        Arrays.fill(compressedG, (byte) 0);
        Arrays.fill(compressedB, (byte) 0);
        int compressedIndex = 0;

        for (int i = 0; i < paletteSize; i++) {
            if (lookupBuf[i] == -1) { // if -1, this value is no doublette of
                                      // a lower index
                compressedR[compressedIndex] = r[i]; // copy value to compressed
                                                     // buffer
                compressedG[compressedIndex] = g[i];
                compressedB[compressedIndex] = b[i];

                if (i != TRANSPARENT_INDEX) { // don't search doublettes of
                                              // transparent color
                    // search for doublettes at higher indeces
                    for (int j = i + 1; j < paletteSize; j++) {
                        if (j == TRANSPARENT_INDEX) { // transparent color can't
                                                      // be a doublette of
                                                      // another color
                            continue;
                        }

                        if ((r[i] == r[j]) && (g[i] == g[j])
                                && (b[i] == b[j])) {
                            lookupBuf[j] = compressedIndex; // mark double
                                                            // entry in
                                                            // lookupBuffer
                        }
                    }
                }

                lookupBuf[i] = compressedIndex++;
            }
        }

        if (paletteSize != compressedIndex) {
            // paletteSize = compressedIndex;
            r = compressedR;
            g = compressedG;
            b = compressedB;
        }

        palette = new Palette(r, g, b);
        return palette;
    }

    /**
     * Converts byte to unsigned int.
     *
     * @param b Byte to convert
     * @return Unsigned value of byte
     */
    private static int unsigned(final byte b) {
        return b & EIGHT_BIT_MASK;
    }

    /**
     * Load SPR file. Load palette first!
     *
     * @param fname Name of SPR file
     * @return Array of Images representing all images stored in the SPR file
     * @throws ExtractException
     */
    GIFImage[] loadSPR(final String fname) throws ExtractException {
        byte[] buffer;

        if (palette == null) {
            throw new ExtractException("Load Palette first!");
        }

        // read file into buffer
        final File f = new File(fname);

        try (FileInputStream fi = new FileInputStream(fname)) {
            buffer = new byte[(int) f.length()];

            if (fi.read(buffer) < 1) {
                System.out.println("0 bytes read from file " + fname);
            }
        } catch (final FileNotFoundException e) {
            throw new ExtractException("File " + fname + " not found");
        } catch (final IOException e) {
            throw new ExtractException("I/O error while reading " + fname);
        }

        // check header
        if (buffer[0] != SPRITE_HEADER0_VALUE
                || buffer[1] != SPRITE_HEADER1_VALUE || buffer[2] != HEX_4C
                || buffer[INDEX3] != SPRITE_HEADER3_VALUE) {
            throw new ExtractException(
                    "File " + fname + " ist not a lemmings sprite file");
        }

        // get number of frames
        final int frames = unsigned(buffer[FRAMES_INDEX_1])
                + unsigned(buffer[FRAMES_INDEX_2]) * DECIMAL_256;
        int ofs = unsigned(buffer[OFS_INDEX_1])
                + unsigned(buffer[OFS_INDEX_2]) * DECIMAL_256;

        images = new GIFImage[frames];
        byte b;
        int lineOfs;

        for (int frame = 0; frame < frames; frame++) {
            // get header info
            final int xOfs = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * DECIMAL_256; // x offset of data
                                                             // in
            // output
            // image
            final int yOfs = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * DECIMAL_256; // y offset of data
                                                             // in
            // output
            // image
            final int maxLen = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * DECIMAL_256; // maximum length
                                                             // of a data
            // line
            final int lines = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * DECIMAL_256; // number of data
                                                             // lines
            final int width = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * DECIMAL_256; // width of output
                                                             // image
            final int height = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * DECIMAL_256; // height of output
                                                             // image

            final byte[] pixels = new byte[width * height];

            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = TRANSPARENT_INDEX;
            }

            int y = yOfs * width;

            int pxOffset = 0; // additional offset for lines broken in several
                              // packets

            for (int line = 0; line < lines;) {
                // read line
                b = buffer[ofs++]; // start character including length (>= 0x80)
                                   // or line offset (<0x80)
                lineOfs = 0;

                while (b == LARGE_SPRITE_LINE_OFFSET) { // special line offset
                                                        // for large sprites
                    lineOfs += LARGE_SPRITE_LINE_OFFSET;
                    b = buffer[ofs++];
                }

                if (!((b & ADD_LINE_OFFSET_CUTOFF) == ADD_LINE_OFFSET_CUTOFF)) {
                    // additional line offset
                    lineOfs += (b & LARGE_SPRITE_LINE_OFFSET);
                    b = buffer[ofs++]; // start character
                }

                // get line length
                final int len = (b & EIGHT_BIT_MASK) - ADD_LINE_OFFSET_CUTOFF;

                if (len < 0 || len > MAX_LINE_LENGTH || len > maxLen) {
                    throw new ExtractException(
                            "Maximum data line length exceeded in line " + line
                                    + " of frame " + frame + " of " + fname
                                    + " (ofs:" + ofs + ")");
                }

                if (len > 0) {
                    try {
                        for (int pixel = 0; pixel < len; pixel++) {
                            // none of the extracted images uses more than 128
                            // colors (indeed much less)
                            // but some use higher indeces. Instead of mirroring
                            // the palette, just and every
                            // entry with 0x7f.
                            // The lookup table is needed to get new index in
                            // compressed palette
                            final byte pixVal = (byte) (lookupBuf[buffer[ofs++]
                                    & SEVEN_BIT_MASK] & EIGHT_BIT_MASK);
                            pixels[y + xOfs + lineOfs + pixVal
                                    + pxOffset] = pixVal;
                        }
                    } catch (final ArrayIndexOutOfBoundsException ex) {
                        throw new ExtractException(
                                "Index out of bounds in line " + line
                                        + " of frame " + frame + " of " + fname
                                        + " (ofs:" + ofs + ")");
                    }

                    b = buffer[ofs++]; // end character must be 0x80

                    if ((b & EIGHT_BIT_MASK) != END_CHARACTER) {
                        // if this is not the end character, the line is
                        // continued after an offset
                        pxOffset += (lineOfs + len);
                        ofs--;
                        continue;
                    }
                }

                pxOffset = 0;
                line++;
                y += width;
            }

            // convert byte array into BufferedImage
            images[frame] = new GIFImage(width, height, pixels, palette);
        }

        return images;
    }

    /**
     * Save all images of currently loaded SPR file.
     *
     * @param fname     Filename of GIF files to export. "_N.gif" will be
     *                  appended with N being the image number.
     * @param keepAnims If true, consequently stored imaged with same size will
     *                  be stored inside one GIF (one beneath the other)
     * @return Array of all the filenames stored
     * @throws ExtractException
     */
    String[] saveAll(final String fname, final boolean keepAnims)
            throws ExtractException {
        int width = images[0].getWidth();
        int height = images[0].getHeight();
        int startIdx = 0;
        int animNum = 0;
        final List<String> files = new ArrayList<String>();

        for (int idx = 1; idx <= images.length; idx++) {
            // search for first image with different size
            if (keepAnims) {
                if (idx < images.length) {
                    if (images[idx].getWidth() == width
                            && images[idx].getHeight() == height) {
                        continue;
                    }
                }
            }
            // now save all the images in one: one above the other
            int num;
            if (keepAnims) {
                num = idx - startIdx;
            } else {
                num = 1;
            }
            final byte[] pixels = new byte[width * num * height];
            final GIFImage anim = new GIFImage(width, num * height, pixels,
                    palette);
            for (int n = 0; n < num; n++) {
                System.arraycopy(images[startIdx + n].getPixels(), 0, pixels,
                        n * height * width,
                        images[startIdx + n].getPixels().length);
            }

            startIdx = idx;
            // construct filename
            final String fn = fname + "_" + Integer.toString(animNum++)
                    + ".gif";
            // save gif
            saveGif(anim, fn);
            files.add(fn.toLowerCase());
            // remember new size
            if (idx < images.length) {
                width = images[idx].getWidth();
                height = images[idx].getHeight();
            }
        }
        final String[] fileArray = new String[files.size()];
        return files.toArray(fileArray);
    }

    /**
     * Save a number of images of currently loaded SPR file into one GIF (one
     * image beneath the other).
     *
     * @param fname    Name of GIF file to create (".gif" will NOT be appended)
     * @param startIdx Index of first image to store
     * @param frames   Number of frames to store
     * @throws ExtractException
     */
    void saveAnim(final String fname, final int startIdx, final int frames)
            throws ExtractException {
        final int width = images[startIdx].getWidth();
        final int height = images[startIdx].getHeight();

        final byte[] pixels = new byte[width * frames * height];
        final GIFImage anim = new GIFImage(width, frames * height, pixels,
                palette);
        for (int n = 0; n < frames; n++) {
            System.arraycopy(images[startIdx + n].getPixels(), 0, pixels,
                    n * height * width,
                    images[startIdx + n].getPixels().length);
        }
        // save gif
        saveGif(anim, fname);
    }

    /**
     * Save one image as GIF.
     *
     * @param img   Image object to save
     * @param fname Name of GIF file to create (".gif" will NOT be appended)
     * @throws ExtractException
     */
    public static void saveGif(final GIFImage img, final String fname)
            throws ExtractException {
        final GifEncoder gifEnc = new GifEncoder(img.getWidth(),
                img.getHeight(), img.getPixels(), img.getPalette().getRed(),
                img.getPalette().getGreen(), img.getPalette().getBlue());

        try (FileOutputStream f = new FileOutputStream(fname)) {
            gifEnc.setTransparentPixel(TRANSPARENT_INDEX);
            gifEnc.write(f);
        } catch (final FileNotFoundException ex) {
            throw new ExtractException(
                    "Can't open file " + fname + " for writing.");
        } catch (final IOException ex) {
            throw new ExtractException("I/O error while writing file " + fname);
        }
    }
}
