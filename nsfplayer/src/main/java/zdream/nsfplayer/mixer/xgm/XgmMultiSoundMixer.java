package zdream.nsfplayer.mixer.xgm;

import java.util.ArrayList;

import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.mixer.AbstractNsfSoundMixer;
import zdream.nsfplayer.mixer.IMixerHandler;
import zdream.nsfplayer.mixer.ITrackMixer;
import zdream.nsfplayer.mixer.interceptor.Amplifier;
import zdream.nsfplayer.mixer.interceptor.Compressor;
import zdream.nsfplayer.mixer.interceptor.DCFilter;
import zdream.nsfplayer.mixer.interceptor.EchoUnit;
import zdream.nsfplayer.mixer.interceptor.Filter;
import zdream.nsfplayer.mixer.interceptor.ISoundInterceptor;

import static java.util.Objects.requireNonNull;
import static zdream.nsfplayer.core.NsfChannelCode.chipOfChannel;


/**
 * <p>Xgm's merged track mixer, originally the default mixer used by NsfPlayer.
 * <p>Since the Mixer rendering part is separated from the NSF/FTM execution part,
 * both FamiTracker and Nsf parts can use the Xgm mixer as the output mixer.
 *
 * <p>Unlike the Blip mixer, its rendering strategy is to calculate and process each sample and then output it.
 * Since it can manipulate each sample point, it is much more flexible and extensible than the Blip mixer,
 * but the price is also obvious: it is slow.
 *
 * <p>According to the test results, when rendering the same tracks,
 * Xgm mixer takes 1.2 to 10 times longer to render than Blip mixer
 * with all built-in effect blockers enabled.
 * This phenomenon is particularly obvious when only 2A03 + 2A07 tracks are rendered,
 * and the DPCM track does not produce any sound. Therefore,
 * if there is no need to process the audio data, it is recommended to use Blip mixer.
 *
 * <p>Among the built-in effect interceptors, the echo constructor takes the longest time.
 * Therefore, if the playback is stuck, turn off the echo constructor first.
 * How to turn off the built-in echo: Take NsfRenderer as an example:
 * <blockquote><pre>
 *     NsfRenderer renderer;
 *
 *     ...
 *
 *     XgmMixerHandler h = (XgmMixerHandler) renderer.getMixerHandler();
 *     List<ISoundInterceptor> itcs = h.getGlobalInterceptors();
 *     for (ISoundInterceptor itc: itcs) {
 *        if (itc instanceof EchoUnit) {
 *           itc.setEnable(false);
 *        }
 *     }
 * </pre></blockquote>
 * The above method can succeed only if the Xgm mixer is enabled as the output mixer,
 * otherwise <code>renderer.getMixerHandler()</code> will not return
 * an <code>XgmMixerHandler</code> instance.
 * </p>
 *
 * @author Zdream
 * @version v0.2.10
 * <br>Since most Renderer classes have opened access to the mixer operation class {@link IMixerHandler},
 * the use of audio interceptors has finally been opened to users.
 * This also greatly increases the flexibility of the Xgm mixer.
 * <br>In addition, this version has greatly optimized the Xgm mixer,
 * and its operating efficiency has been improved by 10% - 30% compared to version v0.2.9.
 * @since v0.2.1
 */
public class XgmMultiSoundMixer extends AbstractNsfSoundMixer<AbstractXgmAudioChannel>
        implements ITrackMixer {

    public NsfCommonParameter param;

    public XgmMultiSoundMixer() {
    }

    /**
     * Setting Configuration Items
     *
     * @param config Configuration item data
     * @since v0.2.5
     */
    public void setConfig(XgmMixerConfig config) {
    }

    /*
     * Orbital parameters
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

    /**
     * After the number of channels is changed, the channel volume and interceptor group will be reset,
     * and all previous modifications will be discarded.
     *
     * @param trackCount Number of channels
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setTrackCount(int trackCount) {
        if (trackCount <= 0) {
            throw new NsfPlayerException("Number of channels: " + trackCount + " is an illegal value");
        }

        this.trackCount = trackCount;

        samples = new short[trackCount][];

        // channel
//		int len = attrs.size();
//		for (int i = 0; i < len; i++) {
//			ChannelAttr attr = attrs.get(i);
//			if (attr == null) {
//				continue;
//			}
//			
//			attr.channel.setTrackCount(trackCount, param);
//		}

        // Interceptor Group
        interceptors = new ArrayList[trackCount];
        for (int i = 0; i < trackCount; i++) {
            interceptors[i] = initInterceptors(new ArrayList<>());
        }
        interceptorArray = new ISoundInterceptor[trackCount][];
    }

    protected class XgmMultiChannelAttr extends ChannelAttr {

        protected XgmMultiChannelAttr(byte code, AbstractXgmAudioChannel t) {
            super(code, t);
        }

        AbstractXgmMultiMixer multi;
    }

    @Override
    protected XgmMultiChannelAttr createChannelAttr(byte code) {

        AbstractXgmAudioChannel channel;
        for (AbstractXgmMultiMixer multi : multiList) {
            channel = multi.getRemainAudioChannel(code);
            if (channel != null) {
                // Insert the track into the existing merged track
                XgmMultiChannelAttr attr = new XgmMultiChannelAttr(code, channel);
                multi.setEnable(channel, true);
                attr.multi = multi;
                return attr;
            }
        }

        // Here you need to create a merge track
        byte chip = chipOfChannel(code);
        AbstractXgmMultiMixer multi = createMultiChannelMixer(chip);
        multiList.add(multi);
        multiArray = null;

        channel = multi.getRemainAudioChannel(code);
        requireNonNull(channel);

        XgmMultiChannelAttr attr = new XgmMultiChannelAttr(code, channel);
        multi.setEnable(channel, true);
        attr.multi = multi;
        return attr;
    }

    XgmMultiChannelAttr getAttr(int id) {
        if (attrs.size() <= id) {
            return null;
        }
        return (XgmMultiChannelAttr) attrs.get(id);
    }

    @Override
    public void setInSample(int id, int inSample) {
        XgmMultiChannelAttr attr = getAttr(id);
        if (attr == null) {
            return;
        }

        if (inSample <= 0) {
            attr.inSample = 0;
        } else {
            attr.inSample = inSample;
        }
    }

    /*
     * Audio Pipeline
     */

    /*
     * The connection method is:
     * sound (implementation component) >> XgmAudioChannel >> AbstractXgmMultiMixer >> XgmMultiSoundMixer
     *
     * Among them, a sound is connected to an XgmAudioChannel
     * Multiple XgmAudioChannels connected to one IXgmMultiChannelMixer
     * Multiple IXgmMultiChannelMixers connected to one XgmMultiSoundMixer
     * XgmMultiSoundMixer has only one
     */

    private final ArrayList<AbstractXgmMultiMixer> multiList = new ArrayList<>();

    /**
     * Caching, performance considerations
     */
    private AbstractXgmMultiMixer[] multiArray;

    /**
     * Select IXgmMultiChannelMixer according to chip
     */
    private AbstractXgmMultiMixer createMultiChannelMixer(byte chip) {
        AbstractXgmMultiMixer multi = switch (chip) {
            case CHIP_2A03 -> new Xgm2A03Mixer();
            case CHIP_2A07 -> new Xgm2A07Mixer();
            case CHIP_VRC6 -> new XgmVRC6Mixer();
            case CHIP_MMC5 -> new XgmMMC5Mixer();
            case CHIP_FDS -> new XgmFDSMixer();
            case CHIP_N163 -> new XgmN163Mixer();
            case CHIP_VRC7 -> new XgmVRC7Mixer();
            case CHIP_S5B -> new XgmS5BMixer();
            default -> null;
        };

        if (multi != null) {
            Filter f = new Filter();
            f.setRate(param.sampleRate);
            f.setParam(4700, 0);
            multi.attachIntercept(f);

            Amplifier amp = new Amplifier();
            amp.setCompress(100, -1);
            multi.attachIntercept(amp);
        }

        return multi;
    }

    @Override
    public void detachAll() {
        multiList.clear();
        multiArray = null;
        super.detachAll();
    }

    @Override
    public void detach(int id) {
        XgmMultiChannelAttr attr = (XgmMultiChannelAttr) attrs.get(id);
        attr.multi.setEnable(attr.channel, false);

        super.detach(id);
    }

    @Override
    public void reset() {
        multiList.forEach(AbstractXgmMultiMixer::reset);
        for (ArrayList<ISoundInterceptor> list : interceptors) {
            list.forEach(IResetable::reset);
        }
    }

    /*
     * Audio Synthesis
     */

    /**
     * [channel][sampling]
     */
    short[][] samples;

    /**
     * Interceptor Group
     */
    ArrayList<ISoundInterceptor>[] interceptors;

    /**
     * Caching, performance considerations
     */
    private ISoundInterceptor[][] interceptorArray;

    private ArrayList<ISoundInterceptor> initInterceptors(ArrayList<ISoundInterceptor> list) {
        // Constructing an interceptor group
        EchoUnit echo = new EchoUnit();
        echo.setRate(param.sampleRate);
        list.add(echo); // Note that the echo is generated here. If you want to remove the echo, modify this

        DCFilter dcf = new DCFilter();
        dcf.setRate(param.sampleRate);
        dcf.setParam(270, 164);
        list.add(dcf);

        Filter f = new Filter();
        f.setRate(param.sampleRate);
        f.setParam(4700, 112);
        list.add(f);

        Compressor cmp = new Compressor();
        cmp.setParam(1, 1, 1);
        list.add(cmp);

        return list;
    }

    /**
     * @param value
     * @param time  Number of clock cycles that have passed
     * @param array Channel number
     * @return
     */
    int intercept(int value, int time, ISoundInterceptor[] array) {
        int ret = value;
        int length = array.length;
        for (ISoundInterceptor interceptor : array) {
            if (interceptor.isEnable()) {
                ret = interceptor.execute(ret, time);
            }
        }
        return ret;
    }

    @Override
    public void readyBuffer() {
        allocateSampleArray();
        int inSample;
        for (ChannelAttr attr : attrs) {
            if (attr == null) {
                continue;
            }

            XgmMultiChannelAttr a = (XgmMultiChannelAttr) attr;
            inSample = a.inSample;
            if (inSample == 0) {
                inSample = param.freqPerFrame;
            }
            a.channel.checkCapacity(inSample, param.sampleInCurFrame);
        }
    }

    @Override
    public int finishBuffer() {
        beforeRender();

        int length = param.sampleInCurFrame;
        if (trackCount == 1) {
            handleMonoBuffer(length);
        } else {
            handleMultiTrackBuffer(length);
        }

        return length;
    }

    /**
     * Handling the mono case
     *
     * @param length Number of samples
     */
    private void handleMonoBuffer(int length) {
        int v;
        short[] ss = samples[0];
        ISoundInterceptor[] itcpts = this.interceptorArray[0];

        for (int i = 0; i < length; i++) {
            // Rendering a sample process
            v = 0;

            int mlen = multiArray.length;
            for (AbstractXgmMultiMixer multi : multiArray) {
                v += multi.render(i);
            }
            v = intercept(v, 1, itcpts) >> 1;

            if (v > Short.MAX_VALUE) {
                v = Short.MAX_VALUE;
            } else if (v < Short.MIN_VALUE) {
                v = Short.MIN_VALUE;
            }
            ss[i] = (short) v;
        }
    }

    /**
     * Handling multi-channel situations
     *
     * @param length Number of samples
     */
    private void handleMultiTrackBuffer(int length) {
        int v;
        short[] ss;
        int mlen = multiArray.length;

        for (int i = 0; i < length; i++) {
            for (int track = 0; track < trackCount; track++) {
                ss = samples[track];
                v = 0;

                for (AbstractXgmMultiMixer multi : multiArray) {
                    v += multi.render(i);
                }
                v = intercept(v, 1, this.interceptorArray[track]) >> 1;

                if (v > Short.MAX_VALUE) {
                    v = Short.MAX_VALUE;
                } else if (v < Short.MIN_VALUE) {
                    v = Short.MIN_VALUE;
                }
                ss[i] = (short) v;
            }
        }
    }

    private void beforeRender() {
        int len = multiList.size();
        for (AbstractXgmMultiMixer multi : multiList) {
            multi.beforeRender();
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

        if (multiArray == null) {
            multiArray = new AbstractXgmMultiMixer[multiList.size()];
            multiList.toArray(multiArray);
        }
    }

    @Override
    public int readBuffer(short[] buf, int offset, int length) {
        int len = Math.min(length, param.sampleInCurFrame);

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

    /*
     * User Action
     */

    XgmMixerHandler handler;

    @Override
    public XgmMixerHandler getHandler() {
        if (handler == null) {
            handler = new XgmMixerHandler(this);
        }
        return handler;
    }
}
