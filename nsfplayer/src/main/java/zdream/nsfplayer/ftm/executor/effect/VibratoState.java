package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;
import zdream.nsfplayer.ftm.executor.tools.VibratoTable;


/**
 * <p>Vibrato status, 4xy
 * </p>
 *
 * @author Zdream
 * @see VibratoEffect
 * @since 0.2.2
 */
public class VibratoState implements IFtmState {

    public static final String NAME = "Vibrato";

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

    public VibratoState(int speed, int depth) {
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

        int delta = VibratoTable.vibratoValue(depth, phase);
        runtime.channels.get(channelCode).addCurrentPeriod(delta);
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
