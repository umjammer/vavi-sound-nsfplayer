package zdream.nsfplayer.mpeg;

import zdream.utils.common.BytesUtils;


/**
 * <p>mpeg decoder.
 * <p>This decoder specifies how to convert Mpeg format audio into a byte array.
 *
 * @author Zdream
 * @date 2018-01-16
 * @since v0.1
 */
public class MpegDecoder {

    static final int HEADER_MASK = 0xffe00000;

    /**
     * Records the position being decoded, this value is the index pointing to audio.data.
     */
    private int pos;
    private int endPos;

    private boolean end;

    /**
     * Keep the header data of the first frame (only keep a few bits for checking) as the basis
     * for judging whether the following frame headers are legal
     */
    private int firstH;

    /**
     * Layer for decoding
     */
    private AbstractLayer layer;

    private final MpegFrameHead header = new MpegFrameHead();
    // private AbstractLayer layer;

    private MpegAudio audio;

    public void ready(MpegAudio audio) {
        this.audio = audio;

        pos = 0; // TODO ID3V2 should be skipped
        endPos = audio.data.length; // TODO ID3V1 should be skipped
        firstH = 0;
        end = false;
    }

    /**
     * The main method of the decoder. Each time this method is called,
     * the decoder decodes the next frame
     */
    public byte[] decode() {
        // Find frame header
        if (!detectFrameHead()) {
            if (end) {
                // Record to the end of audio
            }
            return null;
        }

        // The main method of decoding
        return decode0();
    }

    boolean detectTerminal() {
        if (pos >= endPos) {
            // TODO This is not the case when reading, but reading another piece of data
            return end = true;
        }

        return false;
    }

    public boolean isEnd() {
        return end;
    }

    /**
     * <p>Starting from the position pointed to by pos (including pos),
     * find the position of the frame header.
     * <p>After finding it, the position of the member variable pos
     * will be changed to point to the frame header;<br>
     * And read the frame header data
     *
     * @return If the frame header is not found, it returns false, otherwise it returns true
     */
    boolean detectFrameHead() {

        if (end) {
            return false;
        }

        while (true) {
            if (audio.data[pos] == -1) { // 0xFF -> -1
                if (isLegalHead()) { // This method will parse the header itself
                    break;
                }
            }
            pos++;

            // The frame header must be 4 bytes. If it is less than 4 bytes,
            // it is definitely not a frame header.
            if (endPos - pos < 4) {
                return false;
            }
        }

        // Now pos points to the frame header of audio.data

        // TODO Now you can get the frame length. If you use stream buffering,
        //  check whether the frame has been completely read out
        // Frame length: header.getFrameSize();

        // If you have found the last one, it means there is no data field,
        // which definitely cannot be counted.
        return true;
    }

    /**
     * Check if audio.data[pos] points to a valid frame header position
     *
     * @return If yes, returns true, and header has parsed the frame header data,
     *         otherwise returns false
     */
    boolean isLegalHead() {
        int h = BytesUtils.bytes2Int(audio.data, pos);
        if ((h & HEADER_MASK) == HEADER_MASK
                && ((h >> 19) & 3) != 1 // version ID:  '01' - reserved
                && ((h >> 17) & 3) != 0 // Layer index: '00' - reserved
                && ((h >> 12) & 15) != 15 // Bitrate Index: '1111' - reserved
                && ((h >> 12) & 15) != 0 // Bitrate Index: '0000' - free
                && ((h >> 10) & 3) != 3) {// Sampling Rate Index: '11' - reserved

            // Next, we will judge whether it is legal according to
            // the frame header of the first frame.
            if (firstH == 0) {
                // Set the header information of the first frame
                firstH = 0xffe00000 |
                        (h & 0x180000) | // version ID
                        (h & 0x60000) | // Layer index
                        (h & 0xc00); // sampling_frequency
            } else {
                // Take the first frame header information as the inspection object and
                // check this header information
                int mask = 0xffe00000;        // syncword
                mask |= h & 0x180000;    // version ID
                mask |= h & 0x60000;    // Layer index
                mask |= h & 0xc00;        // sampling_frequency
                // mode, mode_extension are not the same for every frame

                if (firstH != mask) {
                    return false;
                }
            }

            byte l = header == null ? 0 : header.getLayer();

            // Frame header decoding
            header.decode(h);

            // Check the frame header information to see if the Layer needs to be changed.
            if (l != header.getLayer()) {
                if (header.isLayer3()) {
                    layer = new Layer3();
                } else if (header.isLayer2()) {
                } else if (header.isLayer1()) {
                }
            }

            return true;
        }
        return false;
    }

    /**
     * Decode with the corresponding decoder
     */
    byte[] decode0() {
        // Now pos points to the frame header

        int ptr = pos + 4; // Skip frame header
        if (header.isProtected())
            ptr += 2; // Ignore the CRC-word field

        int length = header.getMainDataSize();

        byte[] ret = null;

        if (ptr + length <= audio.data.length && layer != null) {
            layer.ready(header);
            ret = layer.decode(audio.data, ptr, length);
        }

        // Now pos points to the end of the frame, which may be the beginning of the next frame.
        pos += header.getFrameSize();
        detectTerminal();

        return ret;
    }
}
