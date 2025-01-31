package zdream.nsfplayer.nsf.renderer;

import zdream.nsfplayer.core.ChannelLevelsParameter;
import zdream.nsfplayer.mixer.IMixerConfig;
import zdream.nsfplayer.nsf.executor.NsfExecutor;


/**
 * NSF Renderer Configuration
 *
 * @author Zdream
 * @version 2018-05-09
 * @since v0.1
 */
public class NsfRendererConfig implements Cloneable {

    /**
     * frame rate
     */
    public int sampleRate = 48000;

    /**
     * The user specifies which standard to use for playback, NTSC or PAL.
     */
    public int region;
    /**
     * Follows the format specified in the Nsf file.
     */
    public static final int REGION_FOLLOW_AUDIO = NsfExecutor.REGION_FOLLOW_AUDIO;
    /**
     * Mandatory NTSC
     */
    public static final int REGION_FORCE_NTSC = NsfExecutor.REGION_FORCE_NTSC;
    /**
     * Mandatory PAL
     */
    public static final int REGION_FORCE_PAL = NsfExecutor.REGION_FORCE_PAL;
    /**
     * Mandatory DENDY
     */
    public static final int REGION_FORCE_DENDY = NsfExecutor.REGION_FORCE_DENDY;

    /**
     * Mixer Parameters
     */
    public IMixerConfig mixerConfig;

    //
    // volume
    //

    /**
     * The default is all 1.
     */
    public final ChannelLevelsParameter channelLevels = new ChannelLevelsParameter();

    @Override
    public NsfRendererConfig clone() {
        NsfRendererConfig c = new NsfRendererConfig();

        c.sampleRate = this.sampleRate;
        c.region = this.region;
        c.channelLevels.copyFrom(channelLevels);
        if (mixerConfig != null) {
            c.mixerConfig = mixerConfig.clone();
        }

        return c;
    }
}
