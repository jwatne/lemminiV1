package extract;

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
