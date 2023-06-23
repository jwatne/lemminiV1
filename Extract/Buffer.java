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

import lemmini.Constants;

/**
 * Buffer class that manages reading/writing from/to a byte buffer.
 *
 * @author Volker Oth
 */
class Buffer {
    /**
     * Bits in three bytes.
     */
    private static final int THREE_BYTES = 24;
    /**
     * Bits in one byte.
     */
    private static final int ONE_BYTE = 8;
    /** array of byte which defines the data buffer.. */
    private final byte[] buffer;
    /** byte index in buffer.. */
    private int index;

    /**
     * Constructor.
     *
     * @param size buffer size in bytes
     */
    Buffer(final int size) {
        index = 0;
        buffer = new byte[size];
    }

    /**
     * Constructor.
     *
     * @param b array of byte to use as buffer
     */
    Buffer(final byte[] b) {
        index = 0;
        buffer = b;
    }

    /**
     * Get size of buffer.
     *
     * @return size of buffer in bytes
     */
    int length() {
        return buffer.length;
    }

    /**
     * Get current byte index.
     *
     * @return current byte index
     */
    int getIndex() {
        return index;
    }

    /**
     * Get data buffer.
     *
     * @return data buffer
     */
    byte[] getData() {
        return buffer;
    }

    /**
     * Set index to new byte position.
     *
     * @param idx index to new byte position
     */
    void setIndex(final int idx) {
        index = idx;
    }

    /**
     * Get byte at current position.
     *
     * @return byte at current position
     * @throws ArrayIndexOutOfBoundsException
     */
    int getByte() throws ArrayIndexOutOfBoundsException {
        return buffer[index++] & Constants.EIGHT_BIT_MASK;
    }

    /**
     * Set byte at current position, increase index by 1.
     *
     * @param val byte value to write
     * @throws ArrayIndexOutOfBoundsException
     */
    void setByte(final byte val) throws ArrayIndexOutOfBoundsException {
        buffer[index++] = val;
    }

    /**
     * Get word (2 bytes, little endian) at current position.
     *
     * @return word at current position
     * @throws ArrayIndexOutOfBoundsException
     */
    int getWord() throws ArrayIndexOutOfBoundsException {
        return getByte() | (getByte() << ONE_BYTE);
    }

    /**
     * Set word (2 bytes, little endian) at current position, increase index by
     * 2.
     *
     * @param val word to write at current position
     * @throws ArrayIndexOutOfBoundsException
     */
    void setWord(final int val) throws ArrayIndexOutOfBoundsException {
        setByte((byte) val);
        setByte((byte) (val >> ONE_BYTE));
    }

    /**
     * Get double word (4 bytes, little endian) at current position.
     *
     * @return dword at current position
     * @throws ArrayIndexOutOfBoundsException
     */
    int getDWord() throws ArrayIndexOutOfBoundsException {
        return getByte() | (getByte() << ONE_BYTE)
                | (getByte() << Constants.TWO_BYTES)
                | (getByte() << THREE_BYTES);
    }

    /**
     * Set double word (4 bytes, little endian) at current position, increase
     * index by 4.
     *
     * @param val dword to write at current position
     * @throws ArrayIndexOutOfBoundsException
     */
    void setDWord(final int val) throws ArrayIndexOutOfBoundsException {
        setByte((byte) val);
        setByte((byte) (val >> ONE_BYTE));
        setByte((byte) (val >> Constants.TWO_BYTES));
        setByte((byte) (val >> THREE_BYTES));
    }
}
