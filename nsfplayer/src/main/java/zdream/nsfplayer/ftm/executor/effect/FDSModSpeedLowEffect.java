package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelFDS;


/**
 * <p>Effect of setting the FDS modulator frequency 8 bits (total 12 bits) lower, Jxx
 * </p>
 *
 * @author Zdream
 * @since v0.2.4
 */
public class FDSModSpeedLowEffect implements IFtmEffect {

    public final int freq;

    private FDSModSpeedLowEffect(int freq) {
        this.freq = freq;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.FDS_MOD_SPEED_LOW;
    }

    /**
     * Creates the effect of controlling the FDS modulator frequency by 8 bits.
     *
     * @param freq Depth value. Range: [0, 255]
     * @return Examples of effects
     * @throws IllegalArgumentException When the tone value <code>freq</code> is not in the specified range
     */
    public static FDSModSpeedLowEffect of(int freq) throws IllegalArgumentException {
        if (freq < 0 || freq > 255) {
            throw new IllegalArgumentException("The frequency (lower 8 bits) must be an integer in the range [0, 255].");
        }
        return new FDSModSpeedLowEffect(freq);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        if (channelCode != INsfChannelCode.CHANNEL_FDS) {
            throw new IllegalStateException("The effect of modifying the FDS modulator frequency" +
                    " can only be triggered on the FDS track, not on the " + channelCode + " track.");
        }

        ChannelFDS ch = (ChannelFDS) runtime.channels.get(channelCode);
        ch.setModFreqLow(freq);
    }

    @Override
    public String toString() {
        return String.format("Freq Low:%02x", freq);
    }
}
