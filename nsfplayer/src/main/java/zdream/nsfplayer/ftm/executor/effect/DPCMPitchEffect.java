package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelDPCM;


/**
 * <p>Modify the pitch of DPCM samples
 * <p>This effect is only available on DPCM tracks.
 * </p>
 *
 * @author Zdream
 * @since v0.2.3
 */
public class DPCMPitchEffect implements IFtmEffect {

    /**
     * Pitch of DPCM samples
     */
    public final int pitch;

    private DPCMPitchEffect(int pitch) {
        this.pitch = pitch;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.DPCM_PITCH;
    }

    /**
     * Creates an effect that modifies the pitch of DPCM samples
     *
     * @param pitch Pitch of DPCM samples, range [0, 15]
     * @return Effect Examples
     * @throws IllegalArgumentException When <code>pitch</code> is not within the specified range
     */
    public static DPCMPitchEffect of(int pitch) throws IllegalArgumentException {
        if (pitch < 0 || pitch > 15) {
            throw new IllegalArgumentException("The pitch must be an integer in the range [0, 15].");
        }
        return new DPCMPitchEffect(pitch);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        if (channelCode != INsfChannelCode.CHANNEL_2A03_DPCM) {
            throw new IllegalStateException("The effect of modifying the sample start reading bit can only be" +
                    " triggered on DPCM tracks, not on " + channelCode + " tracks.");
        }

        ChannelDPCM ch = (ChannelDPCM) runtime.channels.get(channelCode);
        ch.setMasterPitch(pitch);
    }

    @Override
    public String toString() {
        return "DPCMPitch:" + pitch;
    }
}
