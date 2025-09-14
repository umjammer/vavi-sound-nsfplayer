package zdream.nsfplayer.xgm.player.nsf;

/**
 * <p>This class is used to store the playback status data and related parameters of {@link NsfPlayer}.</p>
 *
 * <p>Originally, the data in it was stored in {@link NsfAudio}, but now it is split into two classes.
 * NsfPlayerStatus is one of them.</p>
 *
 * <p>The time unit used here is a sampling section without explanation.
 * If you play at a sampling rate of 48000 Hz, then one unit is 1/48000 second.</p>
 *
 * @author Zdream
 * @date 2017-09-21
 */
public class NsfPlayerStatus {

    /**
     * The currently selected song number, starting from 0
     */
    public int song;

    /**
     * Sample rate
     */
    public double rate;

    /**
     * Time sung | seconds<br>
     * This data will be deleted in the future.
     *
     * @see NsfPlayerStatus#time
     */
    public int time_in_ms;

    /**
     * Number of samples sung
     */
    public int time;


    /** Default playback time */
    public int default_playtime;
    /** Loop time */
    public int loop_in_ms;
    /** Fade-out time */
    public int fade_in_ms, default_fadetime;
    /** Number of loops */
    public int loop_num, default_loopnum;
    /** Enable when the performance time is unknown (default performance time) */
    public boolean playtime_unknown;

    final NsfPlayer player;

    /**
     * <p>Whether to <b>pause</b></p>
     * It is written here that it needs to be paused, because the playback operation is actually rendered part by part.
     * It should not be interrupted when the rendering is in the middle.<br>
     * All only after the rendering of that part is over, when the rendering of the next part starts, will come to check the properties of this parameter.<br>
     * If you need to pause, stop rendering.<br>
     * <p>This parameter is generally controlled by the task</p>
     */
    public boolean pause;

    /**
     * <p>Whether to <b>perform song switching operation</b></p>
     * <p>This parameter is generally controlled by the task</p>
     */
    public boolean replace;

    public NsfPlayerStatus(NsfPlayer player) {
        this.player = player;
    }

}
