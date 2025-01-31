package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelDPCM;


/**
 * <p>Modify DAC value of DPCM
 * <p>This effect is only available on DPCM tracks.
 * </p>
 *
 * @author Zdream
 * @since v0.2.3
 */
public class DPCM_DACSettingEffect implements IFtmEffect {

    /**
     * Reset DAC value
     */
    public final int dac;

    private DPCM_DACSettingEffect(int dac) {
        this.dac = dac;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.DAC;
    }

    /**
     * This has the effect of modifying the DAC value of the DPCM
     *
     * @param dac DAC value. Must be in the range [0, 127]
     * @return Effect Examples
     * @throws IllegalArgumentException When <code>dac</code> is not within the specified range
     */
    public static DPCM_DACSettingEffect of(int dac) throws IllegalArgumentException {
        if (dac > 127 || dac < 0) {
            throw new IllegalArgumentException("Error value: " + dac + ", DAC must be an integer value between 0 and 127");
        }
        return new DPCM_DACSettingEffect(dac);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        if (channelCode != INsfChannelCode.CHANNEL_2A03_DPCM) {
            throw new IllegalStateException("Effects that modify DAC values can only be triggered on DPCM tracks, not on "
                    + channelCode + " tracks.");
        }

        ChannelDPCM ch = (ChannelDPCM) runtime.channels.get(channelCode);
        ch.setDeltaCounter(dac);
    }

    @Override
    public String toString() {
        return "DAC:" + dac;
    }
}
