package zdream.nsfplayer.mixer.blip;

import zdream.nsfplayer.mixer.IMixerConfig;


/**
 * Blip Mixer Configuration Options
 *
 * @author Zdream
 * @since v0.2.5
 */
public class BlipMixerConfig implements IMixerConfig {

    public final int bassFilter = 30;
    public final int trebleFilter = 12000;
    public final int trebleDamping = 24;

    @Override
    public BlipMixerConfig clone() {
        try {
            return (BlipMixerConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
