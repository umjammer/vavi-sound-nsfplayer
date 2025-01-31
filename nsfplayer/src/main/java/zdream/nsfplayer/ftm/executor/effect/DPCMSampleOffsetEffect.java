package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelDPCM;


/**
 * <p>Modify the starting reading position of DPCM samples
 * <p>This effect is only available on DPCM tracks.
 * </p>
 *
 * @author Zdream
 * @since v0.2.3
 */
public class DPCMSampleOffsetEffect implements IFtmEffect {

    /**
     * Start reading bit, in byte
     */
    public final int offset;

    private DPCMSampleOffsetEffect(int offset) {
        this.offset = offset;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.SAMPLE_OFFSET;
    }

    /**
     * This has the effect of modifying the start read bit of the DPCM sample.
     *
     * @param offset The starting reading bit. If it is the effect of FTM reading the file,
     *               this value is usually an integer multiple of 64.
     *               The explanation in the original FamiTracker project was that
     *               it was due to hardware limitations. OK.
     *               <br>Range: non-negative numbers are fine
     * @return Effect Examples
     * @throws IllegalArgumentException When <code>offset</code> is not within the specified range
     */
    public static DPCMSampleOffsetEffect of(int offset) throws IllegalArgumentException {
        if (offset < 0) {
            throw new IllegalArgumentException("The starting position to read offset must be a non-negative integer value");
        }
        return new DPCMSampleOffsetEffect(offset);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        if (channelCode != INsfChannelCode.CHANNEL_2A03_DPCM) {
            throw new IllegalStateException("The effect of modifying the sample start reading bit can only be triggered on DPCM tracks, not on "
                    + channelCode + " tracks.");
        }

        ChannelDPCM ch = (ChannelDPCM) runtime.channels.get(channelCode);
        ch.setOffset(offset);
    }

    @Override
    public String toString() {
        return "SampleOffset:" + offset;
    }
}
