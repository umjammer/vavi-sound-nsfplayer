package zdream.nsfplayer.mpeg;

/**
 * <p>Audio data frame in Mpeg format
 *
 * @author Zdream
 * @date 2018-01-16
 * @since v0.1
 */
public class MpegFrame {

    MpegFrame(MpegAudio audio, int seq) {
        this.audio = audio;
        this.seq = seq;
    }

    /**
     * Serial number
     */
    public final int seq;

    /**
     * Which audio does it belong to?
     */
    public final MpegAudio audio;

    int offset;
    int length;
}
