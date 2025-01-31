package zdream.nsfplayer.mixer.factory;

import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.mixer.IMixerConfig;
import zdream.nsfplayer.mixer.ISoundMixer;
import zdream.nsfplayer.mixer.blip.BlipMixerConfig;
import zdream.nsfplayer.mixer.blip.BlipSoundMixer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.nsfplayer.mixer.xgm.XgmMultiSoundMixer;
import zdream.nsfplayer.mixer.xgm.XgmSingerSoundMixer;

import static java.util.Objects.requireNonNull;


/**
 * <p>NSF mixer production factory
 *
 * @author Zdream
 * @since v0.3.0
 */
public class NsfSoundMixerFactory {

    public ISoundMixer create(IMixerConfig config, NsfCommonParameter param) {
        requireNonNull(config, "config = null");
        requireNonNull(param, "param = null");

        ISoundMixer m;
        if (config instanceof XgmMixerConfig c) {
            // Use Xgm audio mixer (originally used by NsfPlayer)

            switch (c.channelType) {
                case XgmMixerConfig.TYPE_SINGER: {
                    XgmSingerSoundMixer mixer = new XgmSingerSoundMixer();
                    mixer.param = param;
                    mixer.setTrackCount(1);
                    m = mixer;
                }
                break;

                case XgmMixerConfig.TYPE_MULTI:
                default: {
                    XgmMultiSoundMixer mixer = new XgmMultiSoundMixer();
                    mixer.param = param;
                    mixer.setTrackCount(1);
                    mixer.setConfig(c);
                    m = mixer;
                }
                break;
            }
        } else if (config instanceof BlipMixerConfig c) {
            // Adopts Blip audio mixer (originally used by FamiTracker)

            BlipSoundMixer mixer = new BlipSoundMixer();
            mixer.sampleRate = param.sampleRate;
            mixer.setConfig(c);
            mixer.param = param;
            m = mixer;
        } else {
            // TODO MixerConfig other than xgm and blip is not supported yet
            throw new NsfPlayerException("MixerConfig other than xgm and blip is not supported yet");
        }

        m.init();
        return m;
    }
}
