package zdream.nsfplayer.nsf.renderer;

/**
 * <p>NSF as well as constants and constant calculations,
 * such as storing the number of clock cycles per frame, etc.
 * </p>
 *
 * @author Zdream
 * @since v0.2.1
 */
public class NsfParameter {

    /**
     * Instantiation is now only allowed within packages
     */
    NsfParameter() {
        super();
    }

    /**
     * The user specifies which standard to use for playback, NTSC or PAL.
     */
    public int region;

    /**
     * Sample rate, samples per second
     */
    public int sampleRate;

    /**
     * Number of clock cycles per second, exact value
     */
    public int freqPerSec;

}
