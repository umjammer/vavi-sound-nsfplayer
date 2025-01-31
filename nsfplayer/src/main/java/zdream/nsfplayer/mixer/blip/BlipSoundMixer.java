package zdream.nsfplayer.mixer.blip;

import zdream.nsfplayer.core.IExpression;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.mixer.AbstractNsfSoundMixer;
import zdream.nsfplayer.mixer.NsfMixerSoundConvertor;


/**
 * <p>Blip's audio synthesizer, originally designed for FamiTracker
 * <p>It doesn't have a lot of features, but it processes very quickly.
 * If you are in a real-time scenario and do not need other mixing effects,
 * it is recommended to use this mixer
 * </p>
 *
 * @author Zdream
 * @since 0.2.1
 */
public class BlipSoundMixer extends AbstractNsfSoundMixer<BlipMixerChannel> {

    public int sampleRate;
    public int bassFilter, trebleDamping, trebleFilter;

    public NsfCommonParameter param;

    /**
     * The size of the previous frame buffer
     */
    private int oldSize;

    @Override
    public void init() {
        int size = sampleRate / 50; // The frame rate is set to a minimum of 50, which ensures that higher frame rates (such as 60) are also compatible.
        oldSize = (size * 1000 * 2) / sampleRate;

        buffer.setSampleRate(sampleRate, oldSize);
        buffer.bassFreq(bassFilter);
    }

    @Override
    public void reset() {
        buffer.clockRate(param.freqPerSec);
    }

    /**
     * Reset parameters according to configuration items
     *
     * @param config Configuration item data
     * @since v0.2.5
     */
    public void setConfig(BlipMixerConfig config) {
        this.bassFilter = config.bassFilter;
        this.trebleDamping = config.trebleDamping;
        this.trebleFilter = config.trebleFilter;
    }

    /*
     * Channel Parameters
     */

    @Override
    protected ChannelAttr createChannelAttr(byte type) {
        BlipMixerChannel c = new BlipMixerChannel(this);

        configMixChannel(type, c);
        c.synth.output(buffer);

        // EQ
        BlipEQ eq = new BlipEQ(-trebleDamping, trebleFilter, sampleRate, 0);
        c.synth.trebleEq(eq);
        c.synth.volume(1.0);

        return new ChannelAttr(type, c);
    }

    @Override
    public void setInSample(int id, int inSample) {
        ChannelAttr a = attrs.get(id);
        if (a == null) {
            return;
        }

        BlipMixerChannel c = a.channel;
        c.synth.in_sample_rate(inSample * param.frameRate);
    }

    /*
     * Audio Pipeline
     */

    /**
     * Configuring Audio Tracks
     *
     * @param type Track Type
     */
    private static void configMixChannel(byte type, BlipMixerChannel mixer) {
        IExpression exp = NsfMixerSoundConvertor.getExpression(type);
        mixer.setExpression(exp);

        switch (type) {
            case CHANNEL_TYPE_PULSE:
            case CHANNEL_TYPE_TRIANGLE:
            case CHANNEL_TYPE_NOISE:
            case CHANNEL_TYPE_DPCM:
            case CHANNEL_TYPE_MMC5_PULSE:
            case CHANNEL_TYPE_VRC6_PULSE:
            case CHANNEL_TYPE_SAWTOOTH: {
                mixer.updateSetting(12, -500);
            }
            break;

            case CHANNEL_TYPE_FDS: {
                mixer.updateSetting(12, -420);
            }
            break;

            case CHANNEL_TYPE_N163:
            case CHANNEL_TYPE_VRC7: {
                mixer.updateSetting(12, -600);
            }
            break;

            case CHANNEL_TYPE_S5B: {
                mixer.updateSetting(12, -800);
            }
            break;

            default: {
                mixer.updateSetting(12, -1);
            }
            break;
        }
    }

    /*
     * Audio Synthesis
     */

    /**
     * Audio Cache
     */
    final BlipBuffer buffer = new BlipBuffer();

    @Override
    public void readyBuffer() {
        int size = param.sampleInCurFrame;
        this.sampleRate = param.sampleRate;
        int newSize = (size * 1000 * 2) / sampleRate;

        // A reasonable amplitude is 4
        if (newSize > oldSize + 4 || newSize < oldSize - 4) {
            buffer.setSampleRate(sampleRate, newSize);
            oldSize = newSize;
        }
    }

    @Override
    public int finishBuffer() {
        int freq = param.freqPerFrame;
        buffer.endFrame(freq);

        return buffer.samplesAvail();
    }

    @Override
    public int readBuffer(short[] buf, int offset, int length) {
        int ret = buffer.readSamples(buf, offset, length, false);

        // Here, in order to avoid overflow of the mixer buffer, some methods are used
        buffer.removeSamples(buffer.samplesAvail());

        return ret;
    }

    /*
     * User Action
     */

    BlipMixerHandler handler;

    @Override
    public BlipMixerHandler getHandler() {
        if (handler == null) {
            handler = new BlipMixerHandler(this);
        }
        return handler;
    }
}
