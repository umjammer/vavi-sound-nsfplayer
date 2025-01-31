package zdream.utils.common;

public class BytesUtils {

    /**
     * <p>Converts 4 byte data into 1 int type data, and reads it according to
     * the storage format where the high bit comes after.
     * <p>Data is stored in an array in the format [high][low]
     *
     * @param bs     arrays
     * @param offset Offset, index of the first of the 4 byte data to be transferred in the array
     * @return
     */
    public static int bytes2Int(byte[] bs, int offset) {
        return (bs[offset] << 24)
                | (bs[offset + 1] & 0xff) << 16
                | (bs[offset + 2] & 0xff) << 8
                | (bs[offset + 3] & 0xff);
    }
}
