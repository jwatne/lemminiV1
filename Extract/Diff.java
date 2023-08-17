package extract;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Adler32;

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
 * Simple diff/patch algorithm for text or binary files In contrast to common
 * line based diff utilities, this algorithm works byte based to create small
 * binary difference files. However this very simple approach is only sensible
 * for small files. It is by no way meant as rival to full featured approaches
 * like XDelta.
 *
 * @author Volker Oth
 */
public final class Diff {
    /**
     * Bits to shift by three bytes.
     */
    private static final int THREE_BYTE_SHIFT = 24;
    /**
     * Bits to shift by one byte.
     */
    private static final int ONE_BYTE_SHIFT = 8;
    /**
     * Standard 7 value.
     */
    private static final int DECIMAL7 = 7;
    /**
     * Hexadecimal 0x7f.
     */
    private static final int HEX7F = 0x7f;
    /**
     * Re-synchronization window length.
     */
    private static final int WINDOW_LENGTH = 512;
    /**
     * Re-synchronization length.
     */
    private static final int RESYNC_LENGTH = 4;
    /** insert n bytes... */
    private static final byte INSERT = 0;
    /** delete n bytes.. */
    private static final byte DELETE = 1;
    /** replace n bytes with n bytes.. */
    private static final byte REPLACE = 2;
    /** substitute n bytes with m bytes.. */
    private static final byte SUBSTITUTE = 3;

    /** magic number for header ID.. */
    private static final int HEADER_ID = 0xdeadbeef;
    /** magic number for data ID.. */
    private static final int DATA_ID = 0xfade0ff;

    /** print info to System.out.. */
    private static boolean verbatim = false;

    /** re-synchronization length.. */
    private static int resyncLength = RESYNC_LENGTH;
    /** re-synchronization window length.. */
    private static int windowLength = WINDOW_LENGTH;

    /** target CRC.. */
    private static int targetCRC = 0;

    /**
     * Returns target CRC.
     *
     * @return target CRC.
     */
    public static int getTargetCRC() {
        return targetCRC;
    }

    /**
     * Sets target CRC.
     *
     * @param target target CRC.
     */
    public static void setTargetCRC(final int target) {
        Diff.targetCRC = target;
    }

    /**
     * Private constructor for utility class.
     */
    private Diff() {

    }

    /**
     * Set diff parameters.
     *
     * @param winLen    Length of windows to search for re-synchronization
     * @param resyncLen Number of equal bytes needed for re-synchronization
     */
    public static void setParameters(final int winLen, final int resyncLen) {
        resyncLength = resyncLen;
        windowLength = winLen;
    }

    /**
     * Create diff buffer from the differences between source and target buffer.
     *
     * @param bsrc source buffer (the file to be patched)
     * @param btrg target buffer (the file as it should be)
     * @return buffer of differences
     */
    public static byte[] diffBuffers(final byte[] bsrc, final byte[] btrg) {
        final List<Byte> patch = new ArrayList<Byte>();
        final Buffer src = new Buffer(bsrc);
        final Buffer trg = new Buffer(btrg);

        // compare crcs
        final Adler32 crcSrc = new Adler32();
        crcSrc.update(src.getData());
        final Adler32 crcTrg = new Adler32();
        crcTrg.update(trg.getData());
        targetCRC = (int) crcTrg.getValue();

        if (crcTrg.getValue() == crcSrc.getValue()) {
            return null;
        }

        // write header
        setDWord(patch, HEADER_ID);
        // write lengths to patch list
        setLen(patch, src.length());
        setLen(patch, trg.length());
        // write crcs to patch list
        setDWord(patch, (int) crcSrc.getValue());
        setDWord(patch, (int) crcTrg.getValue());
        setDWord(patch, DATA_ID);

        // examine source buffer
        final int ofs = writeSourceToDest(patch, src, trg);

        // if the files end identically, the offset needs to be written
        writeOffsetIfFilesEndIdentically(patch, ofs);

        // check for stuff to insert in target
        checkForStuffToInsertInTarget(patch, trg);

        if (patch.size() == 0) {
            return null;
        }

        out("Patch length: " + patch.size());

        // convert patch list to output byte array
        final byte[] retVal = convertPatchListToOutputByteAArray(patch);

        return retVal;
    }

    private static int writeSourceToDest(final List<Byte> patch,
            final Buffer src, final Buffer trg) {
        int ofs = 0;

        while (src.getIndex() < src.length()) {
            // search for difference
            final int s = src.getByte();
            final int t = trg.getByte();

            if (s == t) {
                ofs++;
                continue; // Go to next iteration of src.getIndex().
            }

            // reset indeces
            src.setIndex(src.getIndex() - 1);
            trg.setIndex(trg.getIndex() - 1);
            // write offset
            setLen(patch, ofs);
            out("Offset: " + ofs);
            ofs = 0;
            // check for insert, delete, replace
            final int leni = checkInsert(src, trg);
            final int lend = checkDelete(src, trg);
            final int lenr = checkReplace(src, trg);
            final int[] lens = checkSubstitute(src, trg);
            final int len = getLen(leni, lend, lenr, lens);
            int state = -1;

            if (len > windowLength) {
                state = getReplaceOrInsertState(src, trg);
                break; // Exit while loop.
            }

            state = getFinalState(leni, lend, lenr, lens, state);

            switch (state) {
            case INSERT:
                handleInsert(patch, trg, len);
                break;
            case DELETE:
                handleDelete(patch, src, len);
                break;
            case REPLACE:
                // replace
                handleReplace(patch, src, trg, len);
                break;
            case SUBSTITUTE:
                handleSubstitute(patch, src, trg, lens);
                break;
            default:
                break;
            }
        }

        return ofs;
    }

    private static void handleSubstitute(final List<Byte> patch,
            final Buffer src, final Buffer trg, final int[] lens) {
        // substitute
        out("Substitute: " + lens[0] + "/" + lens[1]);
        patch.add(SUBSTITUTE);
        setLen(patch, lens[0]);
        setLen(patch, lens[1]);

        for (int i = 0; i < lens[1]; i++) {
            patch.add((byte) trg.getByte());
        }

        src.setIndex(src.getIndex() + lens[0]);
    }

    private static void handleReplace(final List<Byte> patch, final Buffer src,
            final Buffer trg, final int len) {
        out("Replace: " + len);
        patch.add(REPLACE);
        setLen(patch, len);

        for (int i = 0; i < len; i++) {
            patch.add((byte) trg.getByte());
        }

        src.setIndex(src.getIndex() + len);
    }

    private static void handleDelete(final List<Byte> patch, final Buffer src,
            final int len) {
        // delete
        out("Delete: " + len);
        patch.add(DELETE);
        setLen(patch, len);
        src.setIndex(src.getIndex() + len);
    }

    private static void handleInsert(final List<Byte> patch, final Buffer trg,
            final int len) {
        // insert
        out("Insert: " + len);
        patch.add(INSERT);
        setLen(patch, len);

        for (int i = 0; i < len; i++) {
            patch.add((byte) trg.getByte());
        }
    }

    private static int getLen(final int leni, final int lend, final int lenr,
            final int[] lens) {
        int len = Math.min(leni, lend);
        len = Math.min(len, lenr);
        len = Math.min(len, lens[1]);
        return len;
    }

    private static int getReplaceOrInsertState(final Buffer src,
            final Buffer trg) {
        int state;
        // completely lost synchronisation
        final int rs = src.length() - src.getIndex();
        final int rt = trg.length() - trg.getIndex();

        if (rs == rt) {
            // len = rs;
            state = REPLACE;
        } else {
            // len = rt;
            state = INSERT;
        }

        return state;
    }

    private static int getFinalState(final int leni, final int lend,
            final int lenr, final int[] lens, final int initialState) {
        int state = initialState;
        final int len = getLen(leni, lend, lenr, lens);

        if (len == leni) {
            state = INSERT;
        } else if (len == lend) {
            state = DELETE;
        } else if (len == lenr) {
            state = REPLACE;
        } else if (len == lens[1]) {
            state = SUBSTITUTE;
        }

        return state;
    }

    private static void writeOffsetIfFilesEndIdentically(final List<Byte> patch,
            final int ofs) {
        if (ofs != 0) {
            out("Offset: " + ofs);
            setLen(patch, ofs);
        }
    }

    private static void checkForStuffToInsertInTarget(final List<Byte> patch,
            final Buffer trg) {
        if (trg.getIndex() < trg.length()) {
            patch.add(INSERT);
            final int len = trg.length() - trg.getIndex();
            out("Insert (End): " + len);
            setLen(patch, len);

            for (int i = 0; i < len; i++) {
                patch.add((byte) trg.getByte());
            }
        }
    }

    private static byte[] convertPatchListToOutputByteAArray(
            final List<Byte> patch) {
        final byte[] retVal = new byte[patch.size()];

        for (int i = 0; i < retVal.length; i++) {
            retVal[i] = patch.get(i).byteValue();
        }
        return retVal;
    }

    /**
     * Create a target buffer from a source buffer and a buffer of differences.
     *
     * @param bsrc   source buffer
     * @param bpatch buffer containing differences
     * @return target buffer created from a source buffer and a buffer of
     *         differences
     * @throws DiffException
     */
    public static byte[] patchbuffers(final byte[] bsrc, final byte[] bpatch)
            throws DiffException {
        final Buffer src = new Buffer(bsrc);
        final Buffer patch = new Buffer(bpatch);
        // calculate src crc
        final Adler32 crc = new Adler32();
        crc.update(src.getData());

        // analyze header
        if (patch.getDWord() != Diff.HEADER_ID) {
            throw new DiffException("No header id found in patch");
        }

        final int lenSrc = getLen(patch);

        if (lenSrc != src.length()) {
            throw new DiffException(
                    "Size of source differs from that in patch header");
        }

        final int lenTrg = getLen(patch);
        final int crcPatchSrc = patch.getDWord();

        if (crcPatchSrc != (int) crc.getValue()) {
            throw new DiffException("CRC of source (0x"
                    + Integer.toHexString((int) crc.getValue())
                    + ") differs from that in patch header (0x"
                    + Integer.toHexString(crcPatchSrc) + ")");
        }

        final int crcTrg = patch.getDWord();

        if (patch.getDWord() != Diff.DATA_ID) {
            throw new DiffException("No data id found in patch header");
        }

        final Buffer trg = new Buffer(lenTrg);

        // step through patch buffer
        try {
            stepThroughPatchBuffer(src, patch, trg);
        } catch (final ArrayIndexOutOfBoundsException ex) {
            throw new DiffException(
                    "Array index exceeds bounds. Patch file corrupt...");
        }

        // check length
        if (trg.getIndex() != lenTrg) {
            throw new DiffException(
                    "Size of target differs from that in patch header");
        }

        // compare crc
        crc.reset();
        crc.update(trg.getData());

        if (crcTrg != (int) crc.getValue()) {
            throw new DiffException("CRC of target differs from that in patch");
        }

        return trg.getData();
    }

    private static void stepThroughPatchBuffer(final Buffer src,
            final Buffer patch, final Buffer trg) throws DiffException {
        while (patch.getIndex() < patch.length()) {
            final int ofs = getLen(patch);
            out("Offset: " + ofs);

            // copy bytes from source buffer
            for (int i = 0; i < ofs; i++) {
                trg.setByte((byte) src.getByte());
            }

            // check for patch buffer empty
            if (patch.getIndex() == patch.length()) {
                break;
            }

            // now there must follow a command followed by a
            final int cmdIdx = patch.getIndex(); // just for exception
            final int cmd = patch.getByte();
            final int len = getLen(patch);

            switch (cmd) {
            case Diff.DELETE:
                out("Delete: " + len);
                src.setIndex(src.getIndex() + len);
                break;
            case Diff.REPLACE:
                out("Replace/");
                src.setIndex(src.getIndex() + len);
                //$FALL-THROUGH$
            case Diff.INSERT:
                out("Insert: " + len);

                for (int r = 0; r < len; r++) {
                    trg.setByte((byte) patch.getByte());
                }

                break;
            case Diff.SUBSTITUTE:
                final int lenT = getLen(patch);
                out("Substitute: " + len + "/" + lenT);
                src.setIndex(src.getIndex() + len);

                for (int r = 0; r < lenT; r++) {
                    trg.setByte((byte) patch.getByte());
                }

                break;
            default:
                throw new DiffException("Unknown command " + cmd
                        + " at patch offset " + cmdIdx);
            }
        }
    }

    /**
     * Lengths/Offset are stored as 7bit values. The 8th bit is used as marker
     * if the number is continued in the next byte.
     *
     * @param b Buffer from which to read the length/offset
     * @return integer value of length/offset
     * @throws ArrayIndexOutOfBoundsException
     */
    private static int getLen(final Buffer b)
            throws ArrayIndexOutOfBoundsException {
        int val = 0;
        int v;
        int shift = 0;

        do {
            v = b.getByte();

            if ((v & Constants.HEX80) == 0) {
                // no continue bit set
                val += (v << shift);
                break;
            }

            // erase contine marker bit
            v &= HEX7F;
            val += (v << shift);
            shift += DECIMAL7;
        } while (true);

        return val;
    }

    /**
     * Store length/offset information in 7bit encoding. A set 8th bit means:
     * continued in next byte So 127 is stored as 0x7f, but 128 is stored as
     * 0x80 0x01 (where 0x80 means 0, highest bit is marker)
     *
     * @param l     Patch list to add length/offset in 7bit encoding
     * @param value Value to add in 7bit encoding
     */
    private static void setLen(final List<Byte> l, final int value) {
        int val = value;

        while (val > HEX7F) {
            l.add((byte) (val & HEX7F | Constants.HEX80));
            val >>>= DECIMAL7;
        }

        l.add((byte) val);
    }

    /**
     * Check for "insert" difference.
     *
     * @param src source buffer
     * @param trg target buffer
     * @return number of bytes inserted
     * @throws ArrayIndexOutOfBoundsException
     */
    private static int checkInsert(final Buffer src, final Buffer trg)
            throws ArrayIndexOutOfBoundsException {
        final byte[] bs = src.getData();
        final int is = src.getIndex();
        final byte[] bt = trg.getData();
        final int it = trg.getIndex();
        int len = getLenForCheckingSubstitute(bs, is, bt, it);

        for (int w = 1; w < len; w++) {
            int r;

            for (r = 0; r < resyncLength; r++) {
                if (bs[is + r] != bt[it + w + r]) {
                    break;
                }
            }

            if (r == resyncLength) {
                return w;
            }
        }

        return Integer.MAX_VALUE;
    }

    /**
     * Check for "delete" difference.
     *
     * @param src source buffer
     * @param trg target buffer
     * @return number of bytes deleted
     * @throws ArrayIndexOutOfBoundsException
     */
    private static int checkDelete(final Buffer src, final Buffer trg)
            throws ArrayIndexOutOfBoundsException {
        final byte[] bs = src.getData();
        final int is = src.getIndex();
        final byte[] bt = trg.getData();
        final int it = trg.getIndex();
        int len = getLenForCheckingSubstitute(bs, is, bt, it);

        for (int w = 1; w < len; w++) {
            int r;

            for (r = 0; r < resyncLength; r++) {
                if (bs[is + w + r] != bt[it + r]) {
                    break;
                }
            }

            if (r == resyncLength) {
                return w;
            }
        }

        return Integer.MAX_VALUE;
    }

    /**
     * Check for "replace" difference.
     *
     * @param src source buffer
     * @param trg target buffer
     * @return number of bytes replaced
     * @throws ArrayIndexOutOfBoundsException
     */
    private static int checkReplace(final Buffer src, final Buffer trg)
            throws ArrayIndexOutOfBoundsException {
        final byte[] bs = src.getData();
        final int is = src.getIndex();
        final byte[] bt = trg.getData();
        final int it = trg.getIndex();
        int len = getLenForCheckingSubstitute(bs, is, bt, it);

        for (int w = 1; w < len; w++) {
            int r;

            for (r = 0; r < resyncLength; r++) {
                if (bs[is + w + r] != bt[it + w + r]) {
                    break;
                }
            }

            if (r == resyncLength) {
                return w;
            }
        }

        return Integer.MAX_VALUE;
    }

    /**
     * Check for "substitute" difference.
     *
     * @param src source buffer
     * @param trg target buffer
     * @return integer array: [0]: number of bytes to delete in source, [1]:
     *         number of bytes to insert in target
     * @throws ArrayIndexOutOfBoundsException
     *
     */
    private static int[] checkSubstitute(final Buffer src, final Buffer trg)
            throws ArrayIndexOutOfBoundsException {
        final byte[] bs = src.getData();
        final int is = src.getIndex();
        final byte[] bt = trg.getData();
        final int it = trg.getIndex();
        final int len = getLenForCheckingSubstitute(bs, is, bt, it);
        final List<int[]> solutions = new ArrayList<int[]>();

        for (int ws = 1; ws < len; ws++) {
            for (int wt = 1; wt < len; wt++) {
                int r;

                for (r = 0; r < resyncLength; r++) {
                    if (bs[is + ws + r] != bt[it + wt + r]) {
                        break;
                    }
                }

                if (r == resyncLength) {
                    final int[] retVal = new int[2];
                    retVal[0] = ws;
                    retVal[1] = wt;
                    solutions.add(retVal);
                }
            }
        }

        if (solutions.size() == 0) {
            // nothing found
            final int[] retVal = new int[2];
            retVal[0] = Integer.MAX_VALUE;
            retVal[1] = Integer.MAX_VALUE;
            return retVal;
        }

        // search best solution
        int sMinIdx = 0;

        for (int i = 1; i < solutions.size(); i++) {
            final int[] s = solutions.get(i);
            final int[] sMin = solutions.get(sMinIdx);

            if (s[0] + s[1] < sMin[0] + sMin[1]) {
                sMinIdx = i;
            }
        }

        return solutions.get(sMinIdx);
    }

    private static int getLenForCheckingSubstitute(final byte[] bs,
            final int is, final byte[] bt, final int it) {
        int len = windowLength;

        if (is + len + resyncLength >= bs.length) {
            len = bs.length - is - resyncLength;
        }

        if (it + len + resyncLength >= bt.length) {
            len = bt.length - it - resyncLength;
        }

        return len;
    }

    /**
     * Write DWord to difference list.
     *
     * @param l   difference list
     * @param val DWord value
     */
    private static void setDWord(final List<Byte> l, final int val) {
        l.add((byte) val);
        l.add((byte) (val >> ONE_BYTE_SHIFT));
        l.add((byte) (val >> Constants.SHIFT_16));
        l.add((byte) (val >> THREE_BYTE_SHIFT));
    }

    private static void out(final String s) {
        if (verbatim) {
            System.out.println(s);
        }
    }
}
