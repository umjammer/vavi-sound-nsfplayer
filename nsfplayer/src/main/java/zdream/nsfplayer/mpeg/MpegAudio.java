package zdream.nsfplayer.mpeg;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Audio data in Mpeg format
 * <p>Mpeg format audio, the most common is mp3 file audio.<br>
 * The file consists of ID3V2(TAG_V2), Frame(Layer)[], ID3V1(TAG_V1).
 * <ul>
 *  <li>ID3V2 contains information such as author, composer, album, etc., with variable length,
 *  which expands the amount of information in ID3V1.<br>
 *  ID3V2 is not necessary
 *  <li>Frame A series of frames, the number of which is determined by the file size and frame length<br>
 *  The length of each Frame may be variable or fixed, depending on the bitrate.
 *  Each Frame is divided into two parts: frame header and data entity.<br>
 *  The frame header records the bit rate, sampling rate, version and other information of the MP3.
 *  Each frame is independent of each other.
 *  <li>ID3V1 contains information such as author, composer, album, etc.,
 *  and its length is fixed at 128 bytes<br>
 *  ID3V1 is not necessary
 * </ul>
 *
 * @author Zdream
 * @date 2018-01-16
 * @since v0.1
 */
public class MpegAudio {

    public MpegAudio() {
    }

    /**
     * Original data image (after removing the ID3V2 part)
     */
    byte[] data;

    final List<MpegFrame> frames = new ArrayList<>();

    public MpegFrame detectFrame() {
        MpegFrame frame = new MpegFrame(this, frames.size());
        frames.add(frame);

        return frame;
    }
}
