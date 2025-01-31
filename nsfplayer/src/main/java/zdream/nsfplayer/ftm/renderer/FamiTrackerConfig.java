package zdream.nsfplayer.ftm.renderer;

import zdream.nsfplayer.core.ChannelLevelsParameter;
import zdream.nsfplayer.mixer.IMixerConfig;


/**
 * Used to set the startup parameters for starting {@link FamiTrackerRenderer}.
 *
 * @author Zdream
 * @since v0.2.3
 */
public class FamiTrackerConfig implements Cloneable {

    public FamiTrackerConfig() {

    }

    /**
     * Original sound.iSampleRate
     * Rendered sampling rate
     */
    public int sampleRate = 48000;

    /**
     * Original sound.iSampleSize
     * The bit depth of each sample point rendered, in bits
     *
     * v0.2.5 Currently there is no real control over this parameter.
     * No matter how much is written, the final output is always 16 bits.
     * So comment it out first
     */
    // public int sampleSize = 16;

    /**
     * Mixer Parameters
     */
    public IMixerConfig mixerConfig;

    //
    // volume
    //

    /**
     * The default is all 1
     */
    public final ChannelLevelsParameter channelLevels = new ChannelLevelsParameter();

    @Override
    public FamiTrackerConfig clone() {
        FamiTrackerConfig c = new FamiTrackerConfig();

        c.sampleRate = this.sampleRate;
        c.channelLevels.copyFrom(channelLevels);
        if (mixerConfig != null) {
            c.mixerConfig = mixerConfig.clone();
        }

        return c;
    }
}
