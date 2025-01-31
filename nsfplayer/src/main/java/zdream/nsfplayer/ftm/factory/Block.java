package zdream.nsfplayer.ftm.factory;

import zdream.utils.common.BytesReader;


/**
 * FTM file blocks
 *
 * @author Zdream
 * @date 2018-04-25
 */
class Block extends BytesReader {

    /**
     * File header identifier
     */
    String id;

    public void setId(byte[] id) {
        // Ignore all spaces and \0 after the array
        int end = id.length - 1;
        for (; end > 0; end--) {
            if (id[end] == '\0' || id[end] == ' ') {
                continue;
            }
            break;
        }

        this.id = new String(id, 0, end + 1);
    }

    /**
     * Block version number
     */
    int version;
    /**
     * Block size (bytes)
     */
    int size;

    /**
     * <p>The location of this file block in the entire file data. That is,
     * the index value of the unit [0] of this block in the entire file data.
     * <p>This value is useful when checking for data errors,
     * so that the error location can be specified when an error is thrown.
     * </p>
     *
     * @since v0.2.5
     */
    int blockOffset;

    public void setSize(int size) {
        this.size = size;
        bs = new byte[size];
    }
}