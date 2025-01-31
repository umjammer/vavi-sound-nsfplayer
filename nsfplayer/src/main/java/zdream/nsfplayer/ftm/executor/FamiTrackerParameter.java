package zdream.nsfplayer.ftm.executor;

/**
 * <p>NSF is related to constants and constant calculations,
 * such as storing the number of clock cycles per frame, etc.
 * </p>
 *
 * @author Zdream
 * @since v0.2.1
 */
public class FamiTrackerParameter {

    /**
     * Now only instantiation is allowed within a package
     */
    FamiTrackerParameter() {
        super();
    }

    /*
     * Playback parameters
     */

    /**
     * Flag indicating whether it is finished
     */
    public boolean finished;

    /**
     * The number of the track currently playing
     */
    public int trackIdx;

    /**
     * Record the line number currently being played
     */
    public int curRow;

    /**
     * The segment number currently being played
     */
    public int curSection;
}
