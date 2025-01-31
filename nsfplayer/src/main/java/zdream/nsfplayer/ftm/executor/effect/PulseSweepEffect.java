package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.NsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.Channel2A03Pulse;


/**
 * <p>Sweep the sound up or down over time, Hxy, Ixy
 * <p>When sweeping is in effect, all other effects that change the key or pitch
 * are temporarily disabled until the sweeping effect ends.
 * <p>This effect only exists on the 2A03 rectangular wave track
 * </p>
 *
 * @author Zdream
 * @see PulseSweepState
 * @since v0.2.9
 */
public class PulseSweepEffect implements IFtmEffect {

    /**
     * Indicates the number of time intervals at which the sweep frequency changes.
     * The valid value is [0, 7].
     */
    public final int period;

    /**
     * Indicates the change parameter of the sweep frequency in each time period, valid value [0, 7]
     */
    public final int shift;

    /**
     * Sharp (Hxy) is true, flat (Ixy) is false
     */
    public final boolean mode;

    private PulseSweepEffect(int period, int shift, boolean mode) {
        this.period = period;
        this.shift = shift;
        this.mode = mode;
    }

    /**
     * Creates an effect where the sound sweeps up or down over time
     *
     * @param period Indicates the number of time intervals at which the sweep frequency changes.
     *              The valid value is [0, 7].
     * @param shift  Indicates the change parameter of the sweep frequency in each time period, valid value [0, 7]
     * @param mode   Mode. Indicates whether the sweep is sliding up or down.
     *               <br>true, indicating that the sweep tone slides upward;
     *               <br>false, indicating that the sweep tone slides downward;
     * @return Effect Examples
     * @throws IllegalArgumentException When the sliding speed <code>speed</code> is not within the specified range
     */
    public static PulseSweepEffect of(int period, int shift, boolean mode) throws IllegalArgumentException {
        if (period < 0 || period > 7) {
            throw new IllegalArgumentException("The note sliding unit time period must be an integer value between 0 and 7");
        }
        if (shift < 0 || shift > 7) {
            throw new IllegalArgumentException("The note shift value must be an integer between 0 and 7.");
        }
        return new PulseSweepEffect(period, shift, mode);
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.SWEEP;
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        if (NsfChannelCode.typeOfChannel(channelCode) != NsfChannelCode.CHANNEL_TYPE_PULSE) {
            throw new IllegalStateException("The sweep effect can only be triggered on the 2A03 Pulse track, not on the "
                    + channelCode + " track.");
        }

        Channel2A03Pulse ch = (Channel2A03Pulse) runtime.channels.get(channelCode);
        ch.setSweep(period, mode, shift);

        // Guardian status
        ch.addState(new PulseSweepState());
    }

    @Override
    public String toString() {
        return "Sweep:" + (mode ? "up#" : "down#") + period + "#" + mode;
    }
}
