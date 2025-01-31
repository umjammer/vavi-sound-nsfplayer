package zdream.nsfplayer.core;

/**
 * <p>NSF is related to constants and constant calculations, and stores more parameters and
 * constant values that the NSF structure needs to rely on when running.
 * </p>
 *
 * @author Zdream
 * @since v0.2.9
 */
public class NsfCommonParameter {

    public NsfCommonParameter() {

    }

    /*
     * Clock cycle
     */

    /**
     * The number of clock cycles per frame, now FTM is the precise value, NSF is the fuzzy value
     */
    public int freqPerFrame;

    /**
     * The number of clock cycles per second, exact value
     */
    public int freqPerSec;

    /**
     * The number of samples in the current frame
     */
    public int sampleInCurFrame;

    /**
     * Sampling rate, number of samples per second
     */
    public int sampleRate;

    /**
     * Frame rate, how many frames per second.
     */
    public int frameRate;

    //
    // volume
    //
    public final ChannelLevelsParameter levels = new ChannelLevelsParameter();

    /*
     * Playback parameters
     */

    /**
     * Flag indicating whether it is finished
     */
    public boolean finished;

    /**
     * Playback speed. Default is 1.0f, valid range is positive number
     *
     * @since v0.2.9
     */
    public float speed = 1.0f;
}
