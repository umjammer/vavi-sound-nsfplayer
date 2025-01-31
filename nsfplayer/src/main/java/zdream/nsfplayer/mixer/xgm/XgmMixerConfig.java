package zdream.nsfplayer.mixer.xgm;

import zdream.nsfplayer.mixer.IMixerConfig;


/**
 * Xgm mixer configuration options
 *
 * @author Zdream
 * @since v0.2.5
 */
public class XgmMixerConfig implements IMixerConfig {

    /**
     * <p>Merge tracks for playback. Default value
     * </p>
     */
    public static final int TYPE_MULTI = 0;

    /**
     * <p>Single track playback. All tracks are independent of
     * each other and are not affected by other tracks.
     * <p>The impact on the N163 track is relatively large.
     * After setting it to play as a single track,
     * you need to configure the volume of the N163 track separately
     * </p>
     */
    public static final int TYPE_SINGER = 1;

    /**
     * Choose whether to use a single track or a merged track
     */
    public int channelType = TYPE_MULTI;

    @Override
    public XgmMixerConfig clone() {
        try {
            return (XgmMixerConfig) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        return null;
    }
}
