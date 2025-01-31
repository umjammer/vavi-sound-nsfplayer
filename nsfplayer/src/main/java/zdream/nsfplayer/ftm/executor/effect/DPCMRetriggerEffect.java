package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelDPCM;


/**
 * <p>Make the DPCM sampling loop.
 * An instance of this effect can only trigger one loop at most.
 * If multiple loops are needed, multiple instances of this effect will be generated.
 * <p>This effect is only used on DPCM tracks
 * </p>
 *
 * @author Zdream
 * @since v0.2.3
 */
public class DPCMRetriggerEffect implements IFtmEffect {

    /**
     * The duration of the loop. How long does it take to loop once? Unit: frame
     */
    public final int duration;

    private DPCMRetriggerEffect(int duration) {
        this.duration = duration;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.RETRIGGER;
    }

    /**
     * Creates a looping effect of DPCM sampling
     *
     * @param duration Loop duration, range is non-negative
     * @return Effect Examples
     * @throws IllegalArgumentException When <code>duration</code> is not within the specified range
     */
    public static DPCMRetriggerEffect of(int duration) throws IllegalArgumentException {
        if (duration < 0) {
            throw new IllegalArgumentException("The loop duration must be a non-negative integer value.");
        }
        return new DPCMRetriggerEffect(duration);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        if (channelCode != INsfChannelCode.CHANNEL_2A03_DPCM) {
            throw new IllegalStateException("The effect of modifying the sample start reading bit can only be" +
                    " triggered on DPCM tracks, not on " + channelCode + " tracks.");
        }

        ChannelDPCM ch = (ChannelDPCM) runtime.channels.get(channelCode);
        ch.setRetrigger(duration);
    }

    @Override
    public String toString() {
        return "Retrigger:" + duration;
    }
}
