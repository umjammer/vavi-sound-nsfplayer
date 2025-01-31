package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;
import zdream.nsfplayer.ftm.executor.tools.VibratoTable;


/**
 * <p>Volume vibrato status, 7xy
 * </p>
 *
 * @author Zdream
 * @see TremoloEffect
 * @since 0.2.2
 */
public class TremoloState implements IFtmState {

    public static final String NAME = "Tremolo";

    /**
     * Vibrato sine wave speed
     */
    public int speed;

    /**
     * amplitude
     */
    public int depth;

    /**
     * Phase number. Valid value is [0, 64), which is one cycle
     */
    private int phase = 0;

    public TremoloState(int speed, int depth) {
        this.speed = speed;
        this.depth = depth;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
        phase = (phase + speed) & 63;
        int x = phase >> 1; // [0, 31]

        int delta = VibratoTable.vibratoValue(depth, x) << 3;
        runtime.channels.get(channelCode).addCurrentVolume(-delta); // The impact is that the volume must not be greater than the original value
    }

    @Override
    public String toString() {
        return NAME + ":" + depth + "#" + speed;
    }

    @Override
    public int priority() {
        return -2;
    }
}
