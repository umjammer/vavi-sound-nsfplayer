package zdream.utils.common;

/**
 * byte array reader
 *
 * @author Zdream
 * @date 2018-04-25
 */
public class BytesReader {

    protected byte[] bs; // Read the full data of the file here
    protected int offset;

    public BytesReader() {

    }

    public BytesReader(byte[] bs) {
        set(bs);
    }

    public void set(byte[] bs) {
        this.bs = bs;
    }

    public int read(byte[] bs, int offset, int len) {
        int len0 = (len > this.bs.length - this.offset) ? this.bs.length - this.offset : len;
        System.arraycopy(this.bs, this.offset, bs, offset, len0);
        this.offset += len0;
        return len0;
    }

    public int read(byte[] bs) {
        int len = (bs.length > this.bs.length - this.offset) ? this.bs.length - this.offset : bs.length;
        System.arraycopy(this.bs, this.offset, bs, 0, len);
        offset += len;
        return len;
    }

    /**
     * Reads int data in the format in which it is stored in the C language.
     * The high bit of the value is placed at the end and the low bit at the beginning. 4x8 bit data
     *
     * @return
     */
    public int readAsCInt() {
        if (offset + 4 > bs.length) {
            throw new ArrayIndexOutOfBoundsException("Remaining " + (bs.length - offset) + " data, can't read 4 values");
        }
        int value = (bs[offset] & 0xFF) | ((bs[offset + 1] & 0xFF) << 8)
                | ((bs[offset + 2] & 0xFF) << 16) | ((bs[offset + 3] & 0xFF) << 24);
        offset += 4;
        return value;
    }

    /**
     * Get a byte of data
     *
     * @return
     */
    public byte readByte() {
        if (offset + 1 > bs.length) {
            throw new ArrayIndexOutOfBoundsException("Remaining " + (bs.length - offset) + " data, can't read 4 values");
        }
        return bs[offset++];
    }

    /**
     * Get a byte of data converted to a positive number.
     *
     * @return
     */
    public int readUnsignedByte() {
        if (offset + 1 > bs.length) {
            throw new ArrayIndexOutOfBoundsException("Remaining " + (bs.length - offset) + " data, can't read 4 values");
        }
        return (bs[offset++] & 0xFF);
    }

    /**
     * <p>Converts the following length data to a string.
     * <p>If the string is followed by a bunch of '\0's, the conversion removes them.
     * <p>When the last read reaches the bottom of the data, an array out-of-bounds error is not thrown.
     *
     * @param length
     * @return
     */
    public String readAsString(int length) {
        byte[] bs = new byte[length];
        int byteReads = read(bs);

        int end = byteReads - 1;
        for (; end >= 0; end--) {
            if (bs[end] == '\0') {
                continue;
            }
            break;
        }

        return new String(bs, 0, end + 1);
    }

    /**
     * <p>Converts the following data to a string as if it were string data.
     * It stops when it reaches a position where the data is 0.
     * <p>When you finally get to the bottom of the data,
     * an array out-of-bounds error is thrown if the last bit is not zero.
     *
     * @return
     */
    public String readAsString() {
        byte c;
        StringBuilder b = new StringBuilder();

        while ((c = readByte()) != 0) {
            b.append((char) c);
        }

        return b.toString();
    }

    /**
     * Skip data
     */
    public void skip(int length) {
        offset += length;
    }

    /**
     * Rollback data location
     */
    public void rollback(int length) {
        offset -= length;
    }

    public boolean isFinished() {
        return offset >= bs.length;
    }

    public final byte[] bytes() {
        return bs;
    }

    public int length() {
        return bs.length;
    }

    public int getOffset() {
        return offset;
    }
}
