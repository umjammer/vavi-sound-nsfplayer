package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelFDS;


/**
 * <p>The effect of setting the FDS modulator frequency to the upper 4 bits (total 12 bits), Ixx
 * </p>
 *
 * @author Zdream
 * @since v0.2.4
 */
public class FDSModSpeedHighEffect implements IFtmEffect {

    public final int freq;

    private FDSModSpeedHighEffect(int freq) {
        this.freq = freq;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.FDS_MOD_SPEED_HIGH;
    }

    /**
     * This has the effect of controlling the FDS modulator frequency 4 bits higher.
     *
     * @param freq Depth value. Range: [0, 15]
     * @return Effect Examples
         * @throws IllegalArgumentException When the timbre value <code>freq</code> is not within the specified range
     */
    public static FDSModSpeedHighEffect of(int freq) throws IllegalArgumentException {
        if (freq < 0 || freq > 15) {
            throw new IllegalArgumentException("Frequency (upper 4 bits) must be an integer in the range [0, 15]");
        }
        return new FDSModSpeedHighEffect(freq);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        if (channelCode != INsfChannelCode.CHANNEL_FDS) {
            throw new IllegalStateException("Effects that modify the FDS modulator frequency can only be triggered on" +
                    " FDS tracks, not on " + channelCode + " tracks.");
        }

        ChannelFDS ch = (ChannelFDS) runtime.channels.get(channelCode);
        ch.setModFreqHigh(freq);
    }

    @Override
    public String toString() {
        return "Freq High:" + Integer.toHexString(freq);
    }
}
