package lemmini;
/*
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
 * Class containing shared constants used by multiple classes within Lemmini
 * application.
 *
 * @author John Watne
 */
public final class Constants {
    /**
     * Bits in two bytes.
     */
    public static final int TWO_BYTES = 16;
    /**
     * Maximum alpha ARGB value.
     */
    public static final int MAX_ALPHA = 0xff000000;
    /**
     * Amount to subtract from buffer values to obtain xPos in pixels.
     */
    public static final int X_OFFSET = 16;
    /**
     * Number of seconds per minute.
     */
    public static final int SECONDS_PER_MINUTE = 60;
    /**
     * One half.
     */
    public static final double HALF = 0.5;
    /**
     * 100% = multiplier to convert decimal value to percentage.
     */
    public static final int ONE_HUNDRED_PERCENT = 100;
    /**
     * 4-bit mask value = 0xf = 0x0f.
     */
    public static final int FOUR_BIT_MASK = 0x0f;
    /**
     * Mask for bits 5 to 8 = 0xf0.
     */
    public static final int BITS_5_TO_8_MASK = 0xf0;
    /**
     * 8th bit mask = 0x80.
     */
    public static final int BIT_8_MASK = 0x80;
    /**
     * 4-bit shift.
     */
    public static final int SHIFT_4 = 4;
    /**
     * 7-bit shift.
     */
    public static final int SHIFT_7 = 7;
    /**
     * 8-bit shift.
     */
    public static final int SHIFT_8 = 8;
    /**
     * 16-bit shift.
     */
    public static final int SHIFT_16 = 16;
    /**
     * Array index = 6.
     */
    public static final int INDEX_6 = 6;
    /**
     * Array index = 7.
     */
    public static final int INDEX_7 = 7;

    /**
     * Hexidecimal value 0xff.
     */
    public static final int EIGHT_BIT_MASK = 0xff;
    /**
     * Hexadecimal value 0x80.
     */
    public static final int HEX80 = 0x80;

    /**
     * 3 constant.
     */
    public static final int THREE = 3;
    /**
     * 5 constant.
     */
    public static final int FIVE = 5;
    /**
     * 6 constant.
     */
    public static final int SIX = 6;
    /**
     * Standard value of 10.
     */
    public static final int DECIMAL_10 = 10;

    /**
     * Private default constructor for utility class.
     */
    private Constants() {

    }
}
