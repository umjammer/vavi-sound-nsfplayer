package zdream.nsfplayer.ftm.executor.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * <p>Vibrato effect, 4xy
 * </p>
 *
 * @author Zdream
 * @see VibratoState
 * @since 0.2.2
 */
public class VibratoEffect implements IFtmEffect {

    /**
     * Vibrato sine wave speed
     */
    public final int speed;

    /**
     * amplitude
     */
    public final int depth;

    private VibratoEffect(int speed, int depth) {
        this.speed = speed;
        this.depth = depth;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.VIBRATO;
    }

    /**
     * Create a vibrato effect
     *
     * @param speed Vibrato speed. range [0, 15]
     * @param depth Amplitude. range [0, 15]
     * @return Effect Examples
     * @throws IllegalArgumentException When the changes <code>speed</code> and <code>depth</code>
     *         are not within the specified range
     */
    public static VibratoEffect of(int speed, int depth) throws IllegalArgumentException {
        if (speed < -15 || speed > 15) {
            throw new IllegalArgumentException("Vibrato speed must be between 0 and 15");
        }
        if (depth < -15 || depth > 15) {
            throw new IllegalArgumentException("Amplitude must be between 0 and 15");
        }
        return new VibratoEffect(speed, depth);
    }

    /**
     * Whether to turn off the vibrato effect. When depth == 0, the vibrato is turned off
     *
     * @return
     */
    public boolean isClose() {
        return depth == 0;
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        if (isClose()) {
            ch.removeStates(VibratoState.NAME);
            return;
        }

        /*
         * Here we need to ensure that a channel has at most one vibrato state
         */
        HashSet<IFtmState> set = ch.filterStates(VibratoState.NAME);
        VibratoState s = null;

        if (!set.isEmpty()) {
            s = (VibratoState) set.iterator().next();
            s.speed = speed;
            s.depth = depth;
        } else {
            s = new VibratoState(speed, depth);
            ch.addState(s);
        }
    }

    @Override
    public String toString() {
        return "Vibrato:" + depth + "#" + speed;
    }

    @Override
    public final int priority() {
        return -2;
    }
}
