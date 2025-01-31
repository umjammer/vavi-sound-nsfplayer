package zdream.nsfplayer.mixer.xgm;

import java.util.ArrayList;

import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.mixer.AbstractNsfSoundMixer;
import zdream.nsfplayer.mixer.ITrackMixer;
import zdream.nsfplayer.mixer.NsfMixerSoundConvertor;
import zdream.nsfplayer.mixer.interceptor.Compressor;
import zdream.nsfplayer.mixer.interceptor.DCFilter;
import zdream.nsfplayer.mixer.interceptor.EchoUnit;
import zdream.nsfplayer.mixer.interceptor.Filter;
import zdream.nsfplayer.mixer.interceptor.ISoundInterceptor;


/**
 * <p>Xgm's single track mixer.
 * <p>Unlike the multi-track mixer {@link XgmMultiSoundMixer},
 * each different track is controlled as a separate unit.
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public class XgmSingerSoundMixer extends AbstractNsfSoundMixer<XgmSingleChannel>
        implements ITrackMixer {

    public NsfCommonParameter param;

    public XgmSingerSoundMixer() {

    }

    /*
     * Orbital Parameters
     */

    /**
     * Global parameter: number of channels. 1 for mono, 2 for stereo, can be 3 or more.
     */
    int trackCount;

    /**
     * @return Current number of channels
     */
    @Override
    public int getTrackCount() {
        return trackCount;
    }

    @Override
    protected ChannelAttr createChannelAttr(byte code) {
        XgmSingleChannel c = new XgmSingleChannel();

        c.store = new XgmAudioChannel();
        c.exp = NsfMixerSoundConvertor.getExpression(code);
        c.setTrackCount(trackCount, param);

        return new ChannelAttr(code, c);
    }

    @Override
    public void setInSample(int id, int inSample) {
        ChannelAttr attr = attrs.get(id);
        if (attr == null) {
            return;
        }

        if (inSample <= 0) {
            attr.inSample = 0;
        } else {
            attr.inSample = inSample;
        }
    }

    /**
     * After the number of channels is changed, the channel volume and interceptor group will be reset,
     * and all previous modifications will be discarded.
     *
     * @param trackCount Number of Channels
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setTrackCount(int trackCount) {
        if (trackCount <= 0) {
            throw new NsfPlayerException("Number of channels: " + trackCount + " is an illegal value");
        }

        this.trackCount = trackCount;

        samples = new short[trackCount][];

        // track
        int len = attrs.size();
        for (ChannelAttr attr : attrs) {
            if (attr == null) {
                continue;
            }

            attr.channel.setTrackCount(trackCount, param);
        }

        // Interceptor Group
        interceptors = new ArrayList[trackCount];
        for (int i = 0; i < trackCount; i++) {
            interceptors[i] = initInterceptors(new ArrayList<>());
        }
        interceptorArray = new ISoundInterceptor[trackCount][];
    }

    /*
     * Audio Synthesis
     */

    /**
     * [channel][sampling]
     */
    short[][] samples;

    /**
     * [Number of channels]
     */
    protected ArrayList<ISoundInterceptor>[] interceptors;

    /**
     * Caching, performance and concurrency considerations
     */
    private ISoundInterceptor[][] interceptorArray;

    private ArrayList<ISoundInterceptor> initInterceptors(ArrayList<ISoundInterceptor> array) {
        // Constructing an interceptor group
        EchoUnit echo = new EchoUnit();
        echo.setRate(param.sampleRate);
        array.add(echo); // Note that the echo is generated here. If you want to remove the echo, modify this

        DCFilter dcf = new DCFilter();
        dcf.setRate(param.sampleRate);
        dcf.setParam(270, 164);
        array.add(dcf);

        Filter f = new Filter();
        f.setRate(param.sampleRate);
        f.setParam(4700, 112);
        array.add(f);

        Compressor cmp = new Compressor();
        cmp.setParam(1, 1, 1);
        array.add(cmp);

        return array;
    }

    /**
     * @param value
     * @param time Number of clock cycles that have passed
     * @param track Channel
     * @return
     */
    int intercept(int value, int time, int track) {
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
     * @param track       Channel
     */
    public void attachIntercept(ISoundInterceptor interceptor, int track) {
        if (interceptor != null) {
            interceptors[track].add(interceptor);
        }
    }

    /*
     * Public Methods
     */

    @Override
    public void reset() {
        for (ChannelAttr attr : attrs) {
            if (attr == null) {
                continue;
            }

            attr.channel.reset();
        }

        for (ArrayList<ISoundInterceptor> interceptor : interceptors) {
            interceptor.forEach(IResetable::reset);
        }
    }

    @Override
    public void readyBuffer() {
        allocateSampleArray();
        for (ChannelAttr attr : attrs) {
            if (attr == null) {
                continue;
            }

            attr.channel.checkCapacity(param.freqPerFrame, param.sampleInCurFrame);
        }
    }

    @Override
    public int finishBuffer() {
        XgmSingleChannel[] chs = new XgmSingleChannel[attrs.size()];
        int chCount = 0;
        for (ChannelAttr attr : attrs) {
            if (attr != null) {
                chs[chCount++] = attr.channel;
            }
        }

        beforeRender(chs, chCount);

        // Actual rendering work
        int length = param.sampleInCurFrame;
        if (samples.length == 1) {
            handleMonoBuffer(chs, chCount, length);
        } else {
            handleMultiTrackBuffer(chs, chCount, length);
        }
        return length;
    }

    /**
     * Handling Single Channel
     *
     * @param chs
     * @param chCount
     * @param length
     */
    private void handleMonoBuffer(XgmSingleChannel[] chs, int chCount, int length) {
        int v;
        short[] ss = samples[0];
        for (int i = 0; i < length; i++) {
            // The process of rendering a frame
            v = 0;

            for (int cidx = 0; cidx < chCount; cidx++) {
                XgmSingleChannel ch = chs[cidx];
                v += ch.render(i, 0);
            }
            v = intercept(v, 1, 0);

            if (v > Short.MAX_VALUE) {
                v = Short.MAX_VALUE;
            } else if (v < Short.MIN_VALUE) {
                v = Short.MIN_VALUE;
            }
            ss[i] = (short) v;
        }
    }

    /**
     * Handling multiple channels
     *
     * @param chs
     * @param chCount
     * @param length
     */
    private void handleMultiTrackBuffer(XgmSingleChannel[] chs, int chCount, int length) {
        int v;
        for (int i = 0; i < length; i++) {
            for (int track = 0; track < samples.length; track++) {
                short[] ss = samples[track];
                // The process of rendering a frame
                v = 0;

                for (int cidx = 0; cidx < chCount; cidx++) {
                    XgmSingleChannel ch = chs[cidx];
                    v += ch.render(i, track);
                }
                v = intercept(v, 1, track);

                if (v > Short.MAX_VALUE) {
                    v = Short.MAX_VALUE;
                } else if (v < Short.MIN_VALUE) {
                    v = Short.MIN_VALUE;
                }
                ss[i] = (short) v;
            }
        }
    }

    private void beforeRender(XgmSingleChannel[] chs, int chCount) {
        for (int i = 0; i < chCount; i++) {
            chs[i].beforeSubmit();
        }

        // The following are performance considerations
        for (int i = 0; i < interceptors.length; i++) {
            ISoundInterceptor[] array = interceptorArray[i];
            ArrayList<ISoundInterceptor> list = interceptors[i];

            if (array == null || array.length != list.size()) {
                interceptorArray[i] = array = new ISoundInterceptor[list.size()];
            }
            list.toArray(array);
        }
    }

    @Override
    public int readBuffer(short[] buf, int offset, int length) {
        int len = Math.min(length / trackCount, param.sampleInCurFrame);

        if (trackCount == 1) {
            System.arraycopy(samples[0], 0, buf, offset, len);
            return len;
        } else {
            int index = 0;
            for (int i = 0; i < len; i++) {
                for (int track = 0; track < trackCount; track++) {
                    buf[index++] = samples[track][i];
                }
            }
            return len * trackCount;
        }
    }

    /**
     * Allocate space for the sample array and create the array.
     * While creating the array, construct output related interceptors.
     * Creating the array requires knowing the value of param.sampleInCurFrame
     */
    private void allocateSampleArray() {
        if (this.samples[0] != null) {
            int oldSize = this.samples[0].length;

            if (oldSize < param.sampleInCurFrame || oldSize - param.sampleInCurFrame > 32) {
                int newSize = param.sampleInCurFrame + 16;
                for (int i = 0; i < samples.length; i++) {
                    samples[i] = new short[newSize];
                }
            }
            return;
        }

        int newSize = param.sampleInCurFrame + 16;
        for (int i = 0; i < samples.length; i++) {
            samples[i] = new short[newSize];
        }
    }
}
