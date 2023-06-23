package extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lemmini.Constants;

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
     * Large sprite line offset.
     */
    private static final int LARGE_SPRITE_LINE_OFFSET = 0x7f;

    /**
     * High byte multiplier.
     */
    private static final int HIGH_BYTE_MULTIPLIER = 256;

    /**
     * SPR header index 0 value.
     */
    private static final int SPR_HEADER_0_VALUE = 0x53;
    /**
     * SPR header index 1 value.
     */
    private static final int SPR_HEADER_1_VALUE = 0x52;
    /**
     * SPR header index 2 value.
     */
    private static final int SPR_HEADER_2_VALUE = 0x4c;

    /**
     * SPR header index 3 value.
     */
    private static final int SPR_HEADER_3_VALUE = 0x45;

    /**
     * Palette header index 3 value.
     */
    private static final int PALETTE_HEADER_3_VALUE = 0x50;
    /**
     * Palette header index 2 value.
     */
    private static final int PALETTE_HEADER_2_VALUE = 0x41;
    /**
     * Array index = 3.
     */
    private static final int INDEX_3 = 3;
    /**
     * Array index = 4.
     */
    private static final int INDEX_4 = 4;

    /**
     * Array index = 5.
     */
    private static final int INDEX_5 = 5;

    /**
     * Palette header index 0 value.
     */
    private static final int PALETTE_HEADER_0_VALUE = 0x20;

    /**
     * Palette header index 1 value.
     */
    private static final int PALETTE_HEADER_1_VALUE = 0x4c;

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
        if (buffer[0] != PALETTE_HEADER_0_VALUE
                || buffer[1] != PALETTE_HEADER_1_VALUE
                || buffer[2] != PALETTE_HEADER_2_VALUE
                || buffer[INDEX_3] != PALETTE_HEADER_3_VALUE) {
            throw new ExtractException(
                    "File " + fname + " ist not a lemmings palette file");
        }

        paletteSize = unsigned(buffer[INDEX_4])
                + unsigned(buffer[INDEX_5]) * HIGH_BYTE_MULTIPLIER;
        // number of palette entries

        byte[] r = new byte[paletteSize];
        byte[] g = new byte[paletteSize];
        byte[] b = new byte[paletteSize];
        int ofs = Constants.INDEX_6; // skip two bytes which contain number o
                                     // palette (?)

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
     * Convert byte in unsigned int.
     *
     * @param b Byte to convert
     * @return Unsigned value of byte
     */
    private static int unsigned(final byte b) {
        return b & Constants.EIGHT_BIT_MASK;
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
        if (buffer[0] != SPR_HEADER_0_VALUE || buffer[1] != SPR_HEADER_1_VALUE
                || buffer[2] != SPR_HEADER_2_VALUE
                || buffer[INDEX_3] != SPR_HEADER_3_VALUE) {
            throw new ExtractException(
                    "File " + fname + " ist not a lemmings sprite file");
        }

        images = processFrames(fname, buffer);
        return images;
    }

    /**
     * Processes each frame in the file.
     *
     * @param fname  Name of SPR file
     * @param buffer Input buffer for SPR file.
     * @return array of GIF images.
     * @throws ExtractException if an error occurs.
     */
    private GIFImage[] processFrames(final String fname, final byte[] buffer)
            throws ExtractException {
        // get number of frames
        final int frames = unsigned(buffer[INDEX_4])
                + unsigned(buffer[INDEX_5]) * HIGH_BYTE_MULTIPLIER;
        int ofs = unsigned(buffer[Constants.INDEX_6])
                + unsigned(buffer[Constants.INDEX_7]) * HIGH_BYTE_MULTIPLIER;

        images = new GIFImage[frames];
        byte b;
        int lineOfs;

        for (int frame = 0; frame < frames; frame++) {
            // get header info
            final int xOfs = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * HIGH_BYTE_MULTIPLIER;
            // x offset of data in output image

            final int yOfs = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * HIGH_BYTE_MULTIPLIER;
            // y offset of data in output image

            final int maxLen = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * HIGH_BYTE_MULTIPLIER;
            // maximum length of a data line

            final int lines = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * HIGH_BYTE_MULTIPLIER;
            // number of data lines

            final int width = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * HIGH_BYTE_MULTIPLIER;
            // width of output image

            final int height = unsigned(buffer[ofs++])
                    + unsigned(buffer[ofs++]) * HIGH_BYTE_MULTIPLIER;
            // height of output image

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

                if (!((b & Constants.HEX80) == Constants.HEX80)) {
                    // additional line offset
                    lineOfs += (b & LARGE_SPRITE_LINE_OFFSET);
                    b = buffer[ofs++]; // start character
                }

                // get line length
                final int len = (b & Constants.EIGHT_BIT_MASK)
                        - Constants.HEX80;

                if (len < 0 || len > LARGE_SPRITE_LINE_OFFSET || len > maxLen) {
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
                            // compresse palette
                            final byte pixVal = (byte) (lookupBuf[buffer[ofs++]
                                    & LARGE_SPRITE_LINE_OFFSET]
                                    & Constants.EIGHT_BIT_MASK);
                            pixels[y + xOfs + lineOfs + pixel
                                    + pxOffset] = pixVal;
                        }
                    } catch (final ArrayIndexOutOfBoundsException ex) {
                        throw new ExtractException(
                                "Index out of bounds in line " + line
                                        + " of frame " + frame + " of " + fname
                                        + " (ofs:" + ofs + ")");
                    }

                    b = buffer[ofs++]; // end character must be HEX80

                    if ((b & Constants.EIGHT_BIT_MASK) != Constants.HEX80) {
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
