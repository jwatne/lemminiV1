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
 * Abstraction layer for binary level in byte buffer.
 *
 * @author Volker Oth
 */
class LevelBuffer {
    /**
     * 8-bit shift.
     */
    private static final int SHIFT_8 = 8;
    /**
     * 8-bit mask = 0xff.
     */
    private static final int EIGHT_BIT_MASK = 0xff;
    /** data buffer. */
    private final byte[] buffer;
    /** byte offset. */
    private int ofs;

    /**
     * Constructor.
     *
     * @param b array of byte to use as buffer
     */
    LevelBuffer(final byte[] b) {
        buffer = b;
        ofs = 0;
    }

    /**
     * Get word (2 bytes, little endian) at current position.
     *
     * @return word at current position
     * @throws ArrayIndexOutOfBoundsException
     */
    int getWord() throws ArrayIndexOutOfBoundsException {
        return ((buffer[ofs++] & EIGHT_BIT_MASK) << SHIFT_8) + buffer[ofs++];
    }

    /**
     * Get byte at current position.
     *
     * @return byte at current position
     * @throws ArrayIndexOutOfBoundsException
     */
    byte getByte() throws ArrayIndexOutOfBoundsException {
        return buffer[ofs++];
    }
}
