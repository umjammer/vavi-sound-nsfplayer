package zdream.nsfplayer.mixer.xgm;

import java.util.ArrayList;

import zdream.nsfplayer.mixer.interceptor.ISoundInterceptor;


/**
 * Merge Track Parent
 *
 * @author Zdream
 * @since v0.2.3
 */
public abstract class AbstractXgmMultiMixer implements IXgmMultiChannelMixer {

    protected final ArrayList<ISoundInterceptor> interceptors = new ArrayList<>();

    /**
     * Caching, performance considerations
     */
    private ISoundInterceptor[] interceptorArray;

    /**
     * @param value
     * @param time  The number of elapsed times, usually 1. The unit is a sampling rate time interval
     * @return
     */
    protected int intercept(int value, int time) {
        int ret = value;
        int length = interceptorArray.length;
        for (ISoundInterceptor interceptor : interceptorArray) {
            if (interceptor.isEnable()) {
                ret = interceptor.execute(ret, time);
            }
        }
        return ret;
    }

    /**
     * Add an interceptor for audio data
     *
     * @param interceptor
     */
    public void attachIntercept(ISoundInterceptor interceptor) {
        if (interceptor != null) {
            interceptors.add(interceptor);
        }
    }

    @Override
    public void reset() {
        for (ISoundInterceptor i : interceptors) {
            i.reset();
        }
    }

    @Override
    public void beforeRender() {
        if (interceptorArray == null || interceptorArray.length != interceptors.size()) {
            interceptorArray = new ISoundInterceptor[interceptors.size()];
        }
        interceptors.toArray(interceptorArray);
    }

    /**
     * <p>Set whether to enable
     * <p>Considering that AbstractXgmAudioChannel can be obtained by users,
     * users can use the enable method to directly modify the logic of matching AbstractXgmAudioChannel,
     * which will cause Mixer instability. Therefore, starting from version v0.3.0,
     * the enable parameter will be moved to the merge track setting.
     * </p>
     *
     * @param channel Track Examples
     * @param enable  Enable Logo
     * @see #isEnable(AbstractXgmAudioChannel)
     * @since v0.3.0
     */
    public abstract void setEnable(AbstractXgmAudioChannel channel, boolean enable);

    /**
     * <p>Get whether it is enabled
     * </p>
     *
     * @param channel Track Examples
     * @see #setEnable(AbstractXgmAudioChannel, boolean)
     * @since v0.3.0
     */
    public abstract boolean isEnable(AbstractXgmAudioChannel channel);
}
