package extract;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.OutputStream;

import lemmini.Constants;

/**
 * GifEncoder - writes out an image as a GIF.
 *
 * Transparency handling and variable bit size courtesy of Jack Palevich.
 *
 * Some hacks for compatibility with JVM on MacOS by Volker Oth
 *
 * Copyright (C) 1996 by Jef Poskanzer <jef(at)acme.com>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Visit the ACME Labs Java page for up-to-date versions of this and other fine
 * Java utilities: http://www.acme.com/java/
 *
 * @author Jef Poskanzer / Volker Oth
 */

public class GifEncoder {
    /**
     * Packet size.
     */
    private static final int PACKET_SIZE = 254;
    /**
     * Buffer size for accumulator.
     */
    private static final int ACCUM_BUF_SIZE = 256;
    /**
     * Bits per character = 8.
     */
    private static final int BITS_PER_CHARACTER = 8;
    /**
     * Hash code max range top bound(?).
     */
    private static final int HASH_CODE_MAX_RANGE = 8;
    /**
     * 65K bytes.
     */
    private static final int SIXTY_FIVE_K = 65536;
    /**
     * Byte value indicating GIF is interlaced.
     */
    private static final int INTERLACED = 0x40;
    /**
     * 3rd byte to write for transparent indicator.
     */
    private static final int TRANSPARENT_BYTE_3 = 4;
    /**
     * 2nd byte value to write for transparent indicator.
     */
    private static final int TRANSPARENT_BYTE_2 = 0xf9;
    /**
     * Value indicating there is a color map.
     */
    private static final int COLOR_MAP_FLAG = 0x80;
    /**
     * 8 bits per pixel.
     */
    private static final int EIGHT_BITS_PER_PIXEL = 8;
    /**
     * 4 bits per pixel.
     */
    private static final int FOUR_BITS_PER_PIXEL = 4;
    /**
     * 16 colors.
     */
    private static final int SIXTEEN_COLORS = 16;
    /**
     * 4 colors.
     */
    private static final int FOUR_COLORS = 4;
    /** Indicates whether GIF should be interlaced. */
    private boolean interlace = false;
    /** Width of GIF. */
    private final int width;
    /** Height of GIF. */
    private final int height;
    /** Pixels. */
    private final byte[] pixels;
    /** Red color components. */
    private final byte[] r;
    /** Green color components. */
    private final byte[] g;
    /** Blue color components. */
    private final byte[] b;
    /** Pixel index. */
    private int pixelIndex;
    /** Number of pixels. */
    private final int numPixels;
    /** Transparent pixel. */
    private int transparentPixel = -1; // hpm

    /**
     * Constructs a new GifEncoder.
     *
     * @param imageWidth  The image width.
     * @param imageHeight The image height.
     * @param pixelData   The pixel data.
     * @param red         The red look-up table.
     * @param green       The green look-up table.
     * @param blue        The blue look-up table.
     */
    public GifEncoder(final int imageWidth, final int imageHeight,
            final byte[] pixelData, final byte[] red, final byte[] green,
            final byte[] blue) {
        this.width = imageWidth;
        this.height = imageHeight;
        this.pixels = pixelData;
        this.r = red;
        this.g = green;
        this.b = blue;
        interlace = false;
        pixelIndex = 0;
        numPixels = imageWidth * imageHeight;
    }

    /**
     * Constructs a new GifEncoder using an 8-bit AWT Image. The image is
     * assumed to be fully loaded.
     *
     * @param img Image
     */
    public GifEncoder(final BufferedImage img) {
        width = img.getWidth(null);
        height = img.getHeight(null);
        pixels = new byte[width * height];
        /*
         * VO: Pixelgrabber seems to behave differently on MacOS (uses first of
         * two identical palette entries instead of the original one. Therefore
         * we need to "grab" the pixels manually
         */
        final ColorModel cm = img.getColorModel();
        if (cm instanceof IndexColorModel) {
            final IndexColorModel icm = (IndexColorModel) cm;
            setTransparentPixel(icm.getTransparentPixel());
        } else {
            throw new IllegalArgumentException("Image must be 8-bit");
        }

        /* VO: manual pixel grabbing */
        for (int y = 0; y < height; y++) {
            final int line = y * width;
            for (int x = 0; x < width; x++) {
                final int colIdx = img.getRaster().getDataBuffer()
                        .getElem(x + line);
                pixels[line + x] = (byte) colIdx;
            }
        }

        final IndexColorModel m = (IndexColorModel) cm;
        final int mapSize = m.getMapSize();
        r = new byte[mapSize];
        g = new byte[mapSize];
        b = new byte[mapSize];
        m.getReds(r);
        m.getGreens(g);
        m.getBlues(b);
        interlace = false;
        pixelIndex = 0;
        numPixels = width * height;
    }

    /**
     * Saves the image as a GIF file.
     *
     * @param out Output stream to write to
     * @throws IOException
     */
    public void write(final OutputStream out) throws IOException {
        // Figure out how many bits to use.
        final int numColors = r.length;
        int bitsPerPixel;

        if (numColors <= 2) {
            bitsPerPixel = 1;
        } else if (numColors <= FOUR_COLORS) {
            bitsPerPixel = 2;
        } else if (numColors <= SIXTEEN_COLORS) {
            bitsPerPixel = FOUR_BITS_PER_PIXEL;
        } else {
            bitsPerPixel = EIGHT_BITS_PER_PIXEL;
        }

        final int colorMapSize = 1 << bitsPerPixel;
        final byte[] reds = new byte[colorMapSize];
        final byte[] grns = new byte[colorMapSize];
        final byte[] blus = new byte[colorMapSize];

        for (int i = 0; i < numColors; i++) {
            reds[i] = r[i];
            grns[i] = g[i];
            blus[i] = b[i];
        }

        final Palette palette = new Palette(reds, grns, blus);
        final GIFImage image = new GIFImage(width, height, null, palette);
        // hpm
        gifEncode(image, out, interlace, (byte) 0, getTransparentPixel(),
                bitsPerPixel);
    }

    // hpm
    /**
     * Set transparent pixel color (palette index).
     *
     * @param pixel transparent pixel color (palette index)
     */
    public void setTransparentPixel(final int pixel) {
        transparentPixel = pixel;
    }

    // hpm
    /**
     * Get transparent pixel color (palette index).
     *
     * @return transparent pixel color (palette index)
     */
    public int getTransparentPixel() {
        return transparentPixel;
    }

    static void writeString(final OutputStream out, final String str)
            throws IOException {
        final byte[] buf = str.getBytes();
        out.write(buf);
    }

    // Adapted from ppmtogif, which is based on GIFENCOD by David
    // Rowley <mgardi@watdscu.waterloo.edu>. Lempel-Zim compression
    // based on "compress".

    final void gifEncode(final GIFImage image, final OutputStream outs,
            final boolean gifInterlace, final byte background,
            final int transparent, final int bitsPerPixel) throws IOException {
        final int imageWidth = image.getWidth();
        final int imageHeight = image.getHeight();
        final Palette palette = image.getPalette();
        final byte[] red = palette.getRed();
        final byte[] green = palette.getGreen();
        final byte[] blue = palette.getBlue();
        byte byteToOutput;
        int leftOfs;
        int topOfs;
        int colorMapSize;
        int initCodeSize;
        int i;

        colorMapSize = 1 << bitsPerPixel;
        topOfs = 0;
        leftOfs = 0;

        // The initial code size
        if (bitsPerPixel <= 1) {
            initCodeSize = 2;
        } else {
            initCodeSize = bitsPerPixel;
        }

        // Write the Magic header
        writeString(outs, "GIF89a");

        // Write out the screen width and height
        putWord(imageWidth, outs);
        putWord(imageHeight, outs);

        // Indicate that there is a global colour map
        byteToOutput = (byte) COLOR_MAP_FLAG; // Yes, there is a color map
        // OR in the resolution
        byteToOutput |= (byte) ((EIGHT_BITS_PER_PIXEL
                - 1) << Constants.SHIFT_4);
        // Not sorted
        // OR in the Bits per Pixel
        byteToOutput |= (byte) ((bitsPerPixel - 1));

        // Write it out
        putByte(byteToOutput, outs);

        // Write out the Background colour
        putByte(background, outs);

        // Pixel aspect ratio - 1:1.
        // Putbyte( (byte) 49, outs );
        // Java's GIF reader currently has a bug, if the aspect ratio byte is
        // not zero it throws an ImageFormatException. It doesn't know that
        // 49 means a 1:1 aspect ratio. Well, whatever, zero works with all
        // the other decoders I've tried so it probably doesn't hurt.
        putByte((byte) 0, outs);

        // Write out the Global Colour Map
        for (i = 0; i < colorMapSize; ++i) {
            putByte(red[i], outs);
            putByte(green[i], outs);
            putByte(blue[i], outs);
        }

        // Write out extension for transparent colour index, if necessary.
        if (transparent != -1) {
            putByte((byte) '!', outs);
            putByte((byte) TRANSPARENT_BYTE_2, outs);
            putByte((byte) TRANSPARENT_BYTE_3, outs);
            putByte((byte) 1, outs);
            putByte((byte) 0, outs);
            putByte((byte) 0, outs);
            putByte((byte) transparent, outs);
            putByte((byte) 0, outs);
        }

        // Write an Image separator
        putByte((byte) ',', outs);

        // Write the Image header
        putWord(leftOfs, outs);
        putWord(topOfs, outs);
        putWord(imageWidth, outs);
        putWord(imageHeight, outs);

        // Write out whether or not the image is interlaced
        if (gifInterlace) {
            putByte((byte) INTERLACED, outs);
        } else {
            putByte((byte) 0x00, outs);
        }

        // Write out the initial code size
        putByte((byte) initCodeSize, outs);

        // Go and actually compress the data
        compress(initCodeSize + 1, outs);

        // Write out a Zero-length packet (to end the series)
        putByte((byte) 0, outs);

        // Write the GIF file terminator
        putByte((byte) ';', outs);
    }

    /**
     * EOF flag.
     */
    static final int EOF = -1;

    // Return the next pixel from the image
    final int gifNextPixel() {
        if (pixelIndex == numPixels) {
            return EOF;
        } else {
            return pixels[pixelIndex++] & Constants.EIGHT_BIT_MASK;
        }
    }

    // Write out a word to the GIF file
    final void putWord(final int w, final OutputStream outs)
            throws IOException {
        putByte((byte) (w & Constants.EIGHT_BIT_MASK), outs);
        putByte((byte) ((w >> Constants.SHIFT_8) & Constants.EIGHT_BIT_MASK),
                outs);
    }

    // Write out a byte to the GIF file
    final void putByte(final byte byteToWrite, final OutputStream outs)
            throws IOException {
        outs.write(byteToWrite);
    }

    // GIFCOMPR.C - GIF Image compression routines
    //
    // Lempel-Ziv compression based on 'compress'. GIF modifications by
    // David Rowley (mgardi@watdcsu.waterloo.edu)

    // General DEFINEs

    /**
     * Bits.
     */
    private static final int BITS = 12;

    /** 80 % occupancy. */
    private static final int HSIZE = 5003;

    // GIF Image compression - modified 'compress'
    //
    // Based on: compress.c - File compression ala IEEE Computer, June 1984.
    //
    // By Authors: Spencer W. Thomas (decvax!harpo!utah-cs!utah-gr!thomas)
    // Jim McKie (decvax!mcvax!jim)
    // Steve Davies (decvax!vax135!petsd!peora!srd)
    // Ken Turkowski (decvax!decwrl!turtlevax!ken)
    // James A. Woods (decvax!ihnp4!ames!jaw)
    // Joe Orost (decvax!vax135!petsd!joe)

    /** Number of bits/code. */
    private int numBits;
    /** User settable max number of bits/code. */
    private static final int MAXBITS = BITS;
    /** Maximum code, given numBits. */
    private int maxcode;
    /** Should NEVER generate this code. */
    private static final int MAX_MAX_CODE = 1 << BITS;

    final int getMaxCode(final int numberOfBits) {
        return (1 << numberOfBits) - 1;
    }

    /** Hash table. */
    private final int[] htab = new int[HSIZE];
    /** Code table. */
    private final int[] codetab = new int[HSIZE];
    /** First unused entry. */
    private int freeEntry = 0;

    /**
     * block compression parameters -- after all codes are used up, and
     * compression rate changes, start over.
     */
    private boolean clearFlag = false;

    // Algorithm: use open addressing double hashing (no chaining) on the
    // prefix code / next character combination. We do a variant of Knuth's
    // algorithm D (vol. 3, sec. 6.4) along with G. Knott's relatively-prime
    // secondary probe. Here, the modular division first probe is gives way
    // to a faster exclusive-or manipulation. Also do block compression with
    // an adaptive reset, whereby the code table is cleared when the compression
    // ratio decreases, but after the table fills. The variable-length output
    // codes are re-sized at this point, and a special CLEAR code is generated
    // for the decompressor. Late addition: construct the table according to
    // file size for noticeable speed improvement on small files. Please direct
    // questions about this implementation to ames!jaw.

    /**
     * Global initial number of bits.
     */
    private int globalInitBits;
    /** Clear code. */
    private int clearCode;
    /** EOF code. */
    private int eofCode;

    final void compress(final int initBits, final OutputStream outs)
            throws IOException {
        int fcode;
        int i /* = 0 */;
        int c;
        int ent;
        int disp;
        int hsizeReg;
        int hshift;

        // Set up the globals: g_init_bits - initial number of bits
        globalInitBits = initBits;

        // Set up the necessary values
        clearFlag = false;
        numBits = globalInitBits;
        maxcode = getMaxCode(numBits);

        clearCode = 1 << (initBits - 1);
        eofCode = clearCode + 1;
        freeEntry = clearCode + 2;

        charInit();

        ent = gifNextPixel();

        hshift = 0;

        for (fcode = HSIZE; fcode < SIXTY_FIVE_K; fcode *= 2) {
            ++hshift;
        }

        hshift = HASH_CODE_MAX_RANGE - hshift; // set hash code range bound

        hsizeReg = HSIZE;
        clHash(hsizeReg); // clear hash table

        output(clearCode, outs);

        outer_loop: while ((c = gifNextPixel()) != EOF) {
            fcode = (c << MAXBITS) + ent;
            i = (c << hshift) ^ ent; // xor hashing

            if (htab[i] == fcode) {
                ent = codetab[i];
                continue;
            } else if (htab[i] >= 0) { // non-empty slot
                disp = hsizeReg - i; // secondary hash (after G. Knott)

                if (i == 0) {
                    disp = 1;
                }

                do {
                    i -= disp;

                    if (i < 0) {
                        i += hsizeReg;
                    }

                    if (htab[i] == fcode) {
                        ent = codetab[i];
                        continue outer_loop;
                    }
                } while (htab[i] >= 0);
            }

            output(ent, outs);
            ent = c;

            if (freeEntry < MAX_MAX_CODE) {
                codetab[i] = freeEntry++; // code -> hashtable
                htab[i] = fcode;
            } else {
                clBlock(outs);
            }
        }
        // Put out the final code.
        output(ent, outs);
        output(eofCode, outs);
    }

    // output
    //
    // OutputDialog the given code.
    // Inputs:
    // code: A n_bits-bit integer. If == -1, then EOF. This assumes
    // that n_bits =< wordsize - 1.
    // Outputs:
    // Outputs code to the file.
    // Assumptions:
    // Chars are 8 bits long.
    // Algorithm:
    // Maintain a BITS character long buffer (so that 8 codes will
    // fit in it exactly). Use the VAX insv instruction to insert each
    // code in turn. When the buffer fills up empty it and start over.

    /** Current accumulated. */
    private int curAccum = 0;
    /** Current bits. */
    private int curBits = 0;
    /** Mask values. */
    private final int[] masks = {0x0000, 0x0001, 0x0003, 0x0007, 0x000F, 0x001F,
            0x003F, 0x007F, 0x00FF, 0x01FF, 0x03FF, 0x07FF, 0x0FFF, 0x1FFF,
            0x3FFF, 0x7FFF, 0xFFFF};

    final void output(final int code, final OutputStream outs)
            throws IOException {
        curAccum &= masks[curBits];

        if (curBits > 0) {
            curAccum |= (code << curBits);
        } else {
            curAccum = code;
        }

        curBits += numBits;

        while (curBits >= Constants.SHIFT_8) {
            charOut((byte) (curAccum & Constants.EIGHT_BIT_MASK), outs);
            curAccum >>= Constants.SHIFT_8;
            curBits -= BITS_PER_CHARACTER;
        }

        // If the next entry is going to be too big for the code size,
        // then increase it, if possible.
        if (freeEntry > maxcode || clearFlag) {
            if (clearFlag) {
                numBits = globalInitBits;
                maxcode = getMaxCode(numBits);
                clearFlag = false;
            } else {
                ++numBits;

                if (numBits == MAXBITS) {
                    maxcode = MAX_MAX_CODE;
                } else {
                    maxcode = getMaxCode(numBits);
                }
            }
        }

        if (code == eofCode) {
            // At EOF, write the rest of the buffer.
            while (curBits > 0) {
                charOut((byte) (curAccum & Constants.EIGHT_BIT_MASK), outs);
                curAccum >>= Constants.SHIFT_8;
                curBits -= BITS_PER_CHARACTER;
            }

            flushChar(outs);
        }
    }

    // Clear out the hash table

    // table clear for block compress
    final void clBlock(final OutputStream outs) throws IOException {
        clHash(HSIZE);
        freeEntry = clearCode + 2;
        clearFlag = true;

        output(clearCode, outs);
    }

    // reset code table
    final void clHash(final int hsize) {
        for (int i = 0; i < hsize; ++i) {
            htab[i] = -1;
        }
    }

    // GIF Specific routines

    /** Number of characters so far in this 'packet'. */
    private int aCount;

    // Set up the 'byte output' routine
    final void charInit() {
        aCount = 0;
    }

    /** Define the storage for the packet accumulator. */
    private final byte[] accum = new byte[ACCUM_BUF_SIZE];

    // Add a character to the end of the current packet, and if it is 254
    // characters, flush the packet to disk.
    final void charOut(final byte c, final OutputStream outs)
            throws IOException {
        accum[aCount++] = c;

        if (aCount >= PACKET_SIZE) {
            flushChar(outs);
        }
    }

    // Flush the packet to disk, and reset the accumulator
    final void flushChar(final OutputStream outs) throws IOException {
        if (aCount > 0) {
            outs.write(aCount);
            outs.write(accum, 0, aCount);
            aCount = 0;
        }
    }

}
