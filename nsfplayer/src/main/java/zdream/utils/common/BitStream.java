package zdream.utils.common;

/**
 * Gets the bitstream from the input byte stream.
 *
 * @author jmp123
 */
public class BitStream {

    protected int bitPos;
    protected int bytePos;
    protected byte[] bitReservoir;
    private int endPos; // Number of bytes the bitReservoir has been filled with
    private final int maxOff;

    /**
     * Creates a BitStream object, the buffer size len of the bitstream is specified,
     * and the length of the empty buffer tail of the bitstream is specified by extra.
     *
     * @param len   Buffer Accessible Length <br>
     *              The buffer is used to decode frame side information when len is 9, 17 or 32.<br>
     *              The length of the buffer used to decode the main data (main_data) is not less
     *              than the maximum frame length of 512+1732.
     * @param extra The number of empty bytes at the end of the buffer to prevent buffer overflow
     *              due to errors in the bitstream during Huffman decoding.
     *              Empty 512 bytes at the end (length of part2_3_length, 2^12 bits).
     */
    public BitStream(int len, int extra) {
        maxOff = len;
        bitReservoir = new byte[len + extra];
    }

    /**
     * Adds len bytes to the buffer.
     *
     * @param b   Source data.
     * @param off Source data offset.
     * @param len The length of the source data.
     * @return The number of bytes actually filled into the buffer.
     */
    public int append(byte[] b, int off, int len) {
        if (len + endPos > maxOff) {
            // Move buffer bytePos and subsequent (unprocessed) data to the head of the buffer.
            System.arraycopy(bitReservoir, bytePos, bitReservoir, 0, endPos - bytePos);
            endPos -= bytePos;
            bitPos = bytePos = 0;
        }
        if (len + endPos > maxOff)
            len = maxOff - endPos;
        System.arraycopy(b, off, bitReservoir, endPos, len);
        endPos += len;
        return len;
    }

    /**
     * Specify the buffer as b, with the initial offset of the buffer specified by off.
     * The difference with the {@link #append(byte[], int, int)} method is that
     * this method does not copy data from the source data b.
     *
     * @param b   Source data.
     * @param off Source data offset.
     */
    public void feed(byte[] b, int off) {
        bitReservoir = b;
        bytePos = off;
        bitPos = 0;
    }

    /**
     * Reads one bit from the buffer.
     *
     * @return 0 or 1.
     */
    public int get1Bit() {
        int bit = bitReservoir[bytePos] << bitPos;
        bit >>= 7;
        bit &= 0x1;
        bitPos++;
        bytePos += bitPos >> 3;
        bitPos &= 0x7;
        return bit;
    }

    /**
     * Read n bits from the buffer. For runtime speed reasons, if the number of bits to be read is not greater than 9,
     * consider using the {@link #getBits9(int)} method to be more efficient.
     *
     * @param n The number of bits, n=2..17 calls this method.
     * @return The value of n bits.
     */
    public int getBits17(int n) {
        int iret = bitReservoir[bytePos];
        iret <<= 8;
        iret |= bitReservoir[bytePos + 1] & 0xff;
        iret <<= 8;
        iret |= bitReservoir[bytePos + 2] & 0xff;
        iret <<= bitPos;
        iret &= 0xff_ffff; // High 8 position 0
        iret >>= 24 - n;
        bitPos += n;
        bytePos += bitPos >> 3;
        bitPos &= 0x7;
        return iret;
    }

    /**
     * Read n bits from the buffer.
     *
     * @param n The number of bits, n=2..9 calls this method.
     * @return The value of n bits.
     */
    public int getBits9(int n) {
        int iret = bitReservoir[bytePos];
        iret <<= 8;
        iret |= bitReservoir[bytePos + 1] & 0xff;
        iret <<= bitPos;
        iret &= 0xffff; // High 16 position 0
        iret >>= 16 - n;
        bitPos += n;
        bytePos += bitPos >> 3;
        bitPos &= 0x7;
        return iret;
    }

    /**
     * Gets a pointer to the buffer byte.
     *
     * @return Buffer byte pointer.
     */
    public int getBytePos() {
        return bytePos;
    }

    /**
     * Gets the number of bytes the buffer has been filled with.
     *
     * @return The number of bytes the buffer has been filled with.
     */
    public int getSize() {
        return endPos;
    }

    /**
     * The buffer discards n bytes and the buffer bit pointer is reset.
     *
     * @param n The number of bytes discarded.
     */
    public void skipBytes(int n) {
        bytePos += n;
        bitPos = 0;
    }

    /**
     * The buffer discards or rolls back the specified bit.
     *
     * @param n Discard n bits if n>0 and fallback -n bits if n<0.
     */
    public void skipBits(int n) {
        bitPos += n;
        bytePos += bitPos >> 3;
        bitPos &= 0x7;
    }
}
