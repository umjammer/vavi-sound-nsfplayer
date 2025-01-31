package zdream.nsfplayer.mixer.xgm;

import java.util.ArrayList;

import zdream.nsfplayer.core.IExpression;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.mixer.ITrackChannel;
import zdream.nsfplayer.mixer.interceptor.Amplifier;
import zdream.nsfplayer.mixer.interceptor.Filter;
import zdream.nsfplayer.mixer.interceptor.ISoundInterceptor;


/**
 * <p>Xgm's single track.
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public class XgmSingleChannel extends AbstractXgmAudioChannel implements ITrackChannel {

    /**
     * It serves as a temporary storage. The store.level data will be ignored.
     */
    AbstractXgmAudioChannel store;
    IExpression exp;
    /**
     * Number of input samples
     */
    int inSample;

    @Override
    public void mix(int value, int time) {
        // Sample conversion, master volume
        int v = (int) (exp.f(value) * level);

        // Storage Tracks
        store.mix(v, time);
    }

    @Override
    public void reset() {
        store.reset();
        for (ArrayList<ISoundInterceptor> interceptor : interceptors) {
            interceptor.forEach(IResetable::reset);
        }
    }

    @Override
    protected void beforeSubmit() {
        // Temporary storage part
        store.beforeSubmit();

        // Interceptor part
        for (int i = 0; i < interceptorArray.length; i++) {
            ISoundInterceptor[] array = interceptorArray[i];
            if (array == null || array.length != interceptors[i].size()) {
                interceptorArray[i] = new ISoundInterceptor[interceptors[i].size()];
            }
            interceptors[i].toArray(interceptorArray[i]);
        }
    }

    @Override
    protected void checkCapacity(int inSample, int outSample) {
        if (this.inSample != 0) {
            inSample = this.inSample;
        }
        store.checkCapacity(inSample, outSample);
    }

    @Override
    protected float read(int index) {
        return store.read(index);
    }

    /**
     * Sampling data submission
     *
     * @param index
     * @param track Channel number
     * @return The sampled value
     */
    public int render(int index, int track) {
        float lv = trackLevel[track];
        float v = (lv == 0) ? 0 : read(index) * lv * 12;
        return intercept((int) v, 1, track);
    }

    /*
     * Output Channels
     */

    /**
     * Volume of each track
     */
    private float[] trackLevel;

    /**
     * Set the number of channels.
     * After the number of channels is changed, the channel volume and interceptor group
     * will be reset, and all previous modifications will be discarded.
     *
     * @param count Number of channels
     * @param param
     */
    @SuppressWarnings("unchecked")
    void setTrackCount(int count, NsfCommonParameter param) {
        interceptors = new ArrayList[count];
        for (int i = 0; i < interceptors.length; i++) {
            interceptors[i] = initInterceptor(param, new ArrayList<>());
        }
        interceptorArray = new ISoundInterceptor[count][];

        trackLevel = new float[count];
        for (int i = 0; i < trackLevel.length; i++) {
            trackLevel[i] = 1.0f;
        }
    }

    @Override
    public void setTrackLevel(float level, int track) {
        trackLevel[track] = level;
    }

    @Override
    public float getTrackLevel(int track) {
        return trackLevel[track];
    }

    /*
     * Interceptor Group
     */

    /**
     * [Number of channels]
     */
    protected ArrayList<ISoundInterceptor>[] interceptors;

    /**
     * Caching, performance considerations
     */
    private ISoundInterceptor[][] interceptorArray;

    private ArrayList<ISoundInterceptor> initInterceptor(
            NsfCommonParameter param, ArrayList<ISoundInterceptor> array) {
        Filter f = new Filter();
        f.setRate(param.sampleRate);
        f.setParam(4700, 0);
        array.add(f);

        Amplifier amp = new Amplifier();
        amp.setCompress(100, -1);
        array.add(amp);

        return array;
    }

    /**
     * @param value
     * @param time  The number of elapsed times, usually 1. The unit is a sampling rate time interval
     * @param track Channel number
     * @return
     */
    protected int intercept(int value, int time, int track) {
        int ret = value;
        ISoundInterceptor[] array = interceptorArray[track];
        int length = array.length;

        for (ISoundInterceptor interceptor : array) {
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
     * @param track       Channel number
     */
    public void attachIntercept(ISoundInterceptor interceptor, int track) {
        if (interceptor != null) {
            interceptors[track].add(interceptor);
        }
    }
}
