package zdream.nsfplayer.mixer.blip;

import zdream.nsfplayer.mixer.IMixerHandler;


/**
 * Blip mixer operation class
 *
 * @author Zdream
 * @since v0.2.10
 */
public class BlipMixerHandler implements IMixerHandler {

    @SuppressWarnings("unused")
    private final BlipSoundMixer mixer;

    BlipMixerHandler(BlipSoundMixer mixer) {
        this.mixer = mixer;
    }
}
