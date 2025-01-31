package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelFDS;


/**
 * <p>Controls the FDS frequency modulator, the depth of the source (Modulation Depth), Hxx
 * </p>
 *
 * @author Zdream
 * @since v0.2.4
 */
public class FDSModDepthEffect implements IFtmEffect {

    public final int depth;

    private FDSModDepthEffect(int depth) {
        this.depth = depth;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.FDS_MOD_DEPTH;
    }

    /**
     * Effect that controls the depth of the FDS frequency modulator
     *
     * @param depth Depth value. Range: [0, 63]
     * @return Effect Examples
     * @throws IllegalArgumentException When the timbre value <code>depth</code> is not within the specified range
     */
    public static FDSModDepthEffect of(int depth) throws IllegalArgumentException {
        if (depth < 0 || depth > 63) {
            throw new IllegalArgumentException("Depth must be an integer in the range [0, 63].");
        }
        return new FDSModDepthEffect(depth);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        if (channelCode != INsfChannelCode.CHANNEL_FDS) {
            throw new IllegalStateException("Effects that modify the depth of the FDS frequency modulator source can " +
                    "only be triggered on FDS tracks, not on " + channelCode + " tracks.");
        }

        ChannelFDS ch = (ChannelFDS) runtime.channels.get(channelCode);
        ch.setModDepth(depth);
    }

    @Override
    public String toString() {
        return "Depth:" + depth;
    }
}
