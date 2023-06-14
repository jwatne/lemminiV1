package game;

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
 * @author Volker Oth Create and evaluate lemmings level codes Based on the
 *         documentation and Basic code of Herman Perk (LemGen)
 */
public final class LevelCode {
    // [nib. = nibble]
    //
    // nib. 0 | nib. 1 | nib. 2 | nib. 3 | nib. 4 | nib. 5 | nib. 6
    // -------|--------|--------|--------|--------|--------|---------
    // 3 2 1 0| 3 2 1 0| 3 2 1 0| 3 2 1 0| 3 2 1 0| 3 2 1 0| 3 2 1 0
    // -------|--------|--------|--------|--------|--------|---------
    // L0 %0 S0 0|S1 L1 0 %1|0 L2 %2 S2|S4 S3 %3 L3|%4 0 L4 S5|0 %5 S6 L5|0 L6 0
    // %6

    /**
     * Array index 6.
     */
    private static final int INDEX_6 = 6;
    /**
     * Array index 9.
     */
    private static final int INDEX_9 = 9;
    /**
     * 4-bit shift.
     */
    private static final int SHIFT_4 = 4;
    /**
     * Mask for bits 5 to 8 = 0xf0.
     */
    private static final int BITS_5_TO_8_MASK = 0xf0;
    /**
     * 4-bit mask value = 0xf = 0x0f.
     */
    private static final int FOUR_BIT_MASK = 0x0f;
    /**
     * Array index 7.
     */
    private static final int INDEX_7 = 7;
    /**
     * Array index 8.
     */
    private static final int INDEX_8 = 8;
    /**
     * 7 bytes.
     */
    private static final int SEVEN_BYTES = 7;
    /**
     * Valid seed length value.
     */
    private static final int SEED_LENGTH = 10;
    /**
     * 32-bit mask.
     */
    private static final int MASK_32BIT = 0xff;
    /**
     * 100% as a percentage int.
     */
    private static final int ONE_HUNDRED_PERCENT = 100;
    /* magic: S */
    /** Magic mask. */
    private static final int[] MMASK = {1, 2, 4, 24, 32, 64, 0};
    /** Magic shift L. */
    private static final int[] MSHIFTL = {1, 2, 0, 0, 0, 0, 0};
    /** Magic shift R. */
    private static final int[] MSHIFTR = {0, 0, 2, 1, 5, 5, 0};
    /* level: L */
    /** Level mask. */
    private static final int[] LMASK = {1, 2, 4, 8, 16, 32, 64};
    /** Level shift L. */
    private static final int[] LSHIFTL = {3, 1, 0, 0, 0, 0, 0};
    /** Level shift R. */
    private static final int[] LSHIFTR = {0, 0, 0, 3, 3, 5, 4};
    /* percent: % */
    /** Percent mask. */
    private static final int[] PMASK = {1, 2, 4, 8, 16, 32, 64};
    /** Percent shift L. */
    private static final int[] PSHIFTL = {2, 0, 0, 0, 0, 0, 0};
    /** Percent shift R. */
    private static final int[] PSHIFTR = {0, 1, 1, 2, 1, 3, 6};
    /** Maximum level number. */
    private static final int MAX_LVL_NUM = 127;

    /**
     * Private constructor for utility class.
     */
    private LevelCode() {

    }

    /**
     * Create a level code from the given parameters.
     *
     * @param seed    The seed string used as base for the level code
     * @param lvl     The level number (0..127)
     * @param percent Percentage of levels saved in the level won to get this
     *                code
     * @param magic   A "magic" number with more or less unknown sense
     * @param offset  Used to get a higher code for the first level
     * @return String containing level code
     */
    public static String create(final String seed, final int lvl,
            final int percent, final int magic, final int offset) {
        if (lvl > MAX_LVL_NUM || percent > MAX_LVL_NUM || magic > MAX_LVL_NUM
                || seed == null || seed.length() != SEED_LENGTH) {
            return null;
        }

        byte[] bi = seed.getBytes();
        byte[] bo = new byte[bi.length];

        // add offset and wrap around
        int level = lvl + offset;
        level %= (MAX_LVL_NUM + 1);

        // create first 7 bytes
        int sum = 0;

        for (int i = 0; i < SEVEN_BYTES; i++) {
            bi[i] += (byte) (((magic & MMASK[i]) << MSHIFTL[i]) >>> MSHIFTR[i]);
            bi[i] += (byte) (((level & LMASK[i]) << LSHIFTL[i]) >>> LSHIFTR[i]);
            bi[i] += (byte) (((percent
                    & PMASK[i]) << PSHIFTL[i]) >>> PSHIFTR[i]);
            bo[(i + INDEX_8 - (level % INDEX_8)) % SEVEN_BYTES] = bi[i];
            // rotate
            sum += bi[i] & MASK_32BIT; // checksum
        }

        // create bytes 8th and 9th byte (level)
        bo[INDEX_7] = (byte) (bi[INDEX_7] + (level & FOUR_BIT_MASK));
        bo[INDEX_8] = (byte) (bi[INDEX_8]
                + ((level & BITS_5_TO_8_MASK) >> SHIFT_4));
        sum += (bo[INDEX_7] + bo[INDEX_8]) & MASK_32BIT;
        // create 10th byte (checksum)
        bo[INDEX_9] = (byte) (bi[INDEX_9] + (sum & FOUR_BIT_MASK));
        return new String(bo);
    }

    /**
     * Extract the level number from the level code and seed.
     *
     * @param seed   The seed string used as base for the level code
     * @param code   Code that contains the level number (amongst other things)
     * @param offset Used to get a higher code for the first level
     * @return Level number extracted from the level code (-1 in case of error)
     */
    public static int getLevel(final String seed, final String code,
            final int offset) {
        byte[] bs = seed.getBytes();
        byte[] bi = code.getBytes();
        byte[] bo = new byte[bi.length];

        if (seed.length() != SEED_LENGTH || code.length() != SEED_LENGTH) {
            return -1;
        }

        int level = ((bi[INDEX_7] - bs[INDEX_7]) & FOUR_BIT_MASK)
                + (((bi[INDEX_8] - bs[INDEX_8]) & FOUR_BIT_MASK) << SHIFT_4);

        // unrotate
        for (int j = 0; j < SEVEN_BYTES; j++) {
            bo[(j + INDEX_6 + (level % INDEX_8)) % SEVEN_BYTES] = bi[j];
        }

        // decode
        int reconstructedLevel = 0;
        int percent = 0;

        for (int i = 0; i < SEVEN_BYTES; i++) {
            int nibble = (bo[i] - bs[i]) & MASK_32BIT; // reconstruct nibble
                                                       // stored
            reconstructedLevel += ((nibble << LSHIFTR[i]) >> LSHIFTL[i])
                    & LMASK[i];
            percent += ((nibble << PSHIFTR[i]) >> PSHIFTL[i]) & PMASK[i];
        }

        if (level != reconstructedLevel || percent > ONE_HUNDRED_PERCENT) {
            return -1;
        }

        level -= offset;

        while (level < 0) {
            level += MAX_LVL_NUM;
        }

        return level;
    }
}
