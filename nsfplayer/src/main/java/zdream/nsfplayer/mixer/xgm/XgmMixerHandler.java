package zdream.nsfplayer.mixer.xgm;

import java.util.List;

import zdream.nsfplayer.mixer.IMixerHandler;
import zdream.nsfplayer.mixer.interceptor.ISoundInterceptor;
import zdream.nsfplayer.mixer.xgm.XgmMultiSoundMixer.XgmMultiChannelAttr;


/**
 * Xgm mixer operation class
 *
 * @author Zdream
 * @since v0.2.10
 */
public class XgmMixerHandler implements IMixerHandler {

    private final XgmMultiSoundMixer mixer;

    XgmMixerHandler(XgmMultiSoundMixer mixer) {
        this.mixer = mixer;
    }

    /**
     * Returns the global mixin interceptor group
     *
     * @param track Channel number
     * @return Xgm Global interceptor group for mixer
     */
    public List<ISoundInterceptor> getGlobalInterceptors(int track) {
        return mixer.interceptors[track];
    }

    /**
     * <p>Returns the interceptor group for the track ID.
     * <p>If the selected mixer is {@link XgmMultiSoundMixer},
     * then the interceptor group corresponding to the merged track ID is returned.
     * </p>
     *
     * @param id <br>Represents the identification number of the corresponding track
     * @return Interceptor Group
     */
    public List<ISoundInterceptor> getChipInterceptors(int id) {
        XgmMultiChannelAttr attr = mixer.getAttr(id);
        AbstractXgmMultiMixer multi = attr.multi;

        if (multi == null) {
            return null;
        }
        return multi.interceptors;
    }
}
