package zdream.nsfplayer.mixer;

import zdream.nsfplayer.core.IExpression;
import zdream.nsfplayer.core.INsfChannelCode;

import static zdream.nsfplayer.core.NsfChannelCode.typeOfChannel;


/**
 * <p>A tool for converting audio data in Sound into sample data.
 * <p>Currently only single track conversion is supported. Volume is affected after conversion.
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public class NsfMixerSoundConvertor implements INsfChannelCode {

    /**
     * Suitable for 2A03 rectangular track
     */
    public static int pulse(int x) {
        return (x > 0) ? (int) (95.88 * 400 / ((8128.0 / x) + 156.0)) : 0;
    }

    /**
     * Applicable to 2A03 triangle track
     */
    public static int triangle(int x) {
        return (x > 0) ? (int) (46159.29 / (1 / (x / 8227.0) + 30.0)) : 0;
    }

    /**
     * For 2A03 Noise Rail
     */
    public static int noise(int x) {
        return (x > 0) ? (int) (41543.36 / (1 / (x / 12241.0) + 30.0)) : 0;
    }

    /**
     * For use with 2A03 DPCM rails
     */
    public static int dpcm(int x) {
        return (x > 0) ? (int) (33234.69 / (1 / (x / 22638.0) + 30.0)) : 0;
    }

    /**
     * Tracks for VRC6, MMC5 chips
     */
    public static int vrc6(int x) {
        return (x > 0) ? (int) (96 * 360 / ((8000.0 / x) + 180)) : 0;
    }

    /**
     * For FDS tracks
     *
     * @param x range [0, 2016]
     */
    public static int fds(int x) {
        return (x > 0) ? (int) (x / 21.1f) : 0;
    }

    /**
     * Tracks for N163 chip
     */
    public static int n163(int x) {
        return (int) (x / 2.4);
    }

    /**
     * Tracks for VRC7 chips
     */
    public static int vrc7(int x) {
        return (int) (x / 2.0);
    }

    /**
     * Tracks for S5B chips
     */
    public static int s5b(int x) {
        return (int) (x * 1.1708);
    }

    /**
     * Tracks for other sampled data
     */
    public static int sampled(int x) {
        return x;
    }

    /**
     * Get the corresponding data conversion expression
     *
     * @param code NSF track number, or type number
     * @return
     */
    public static IExpression getExpression(byte code) {
        byte type = typeOfChannel(code);

        return switch (type) {
            case CHANNEL_TYPE_PULSE -> NsfMixerSoundConvertor::pulse;
            case CHANNEL_TYPE_TRIANGLE -> NsfMixerSoundConvertor::triangle;
            case CHANNEL_TYPE_NOISE -> NsfMixerSoundConvertor::noise;
            case CHANNEL_TYPE_DPCM -> NsfMixerSoundConvertor::dpcm;
            case CHANNEL_TYPE_MMC5_PULSE, CHANNEL_TYPE_VRC6_PULSE, CHANNEL_TYPE_SAWTOOTH ->
                    NsfMixerSoundConvertor::vrc6;
            case CHANNEL_TYPE_FDS -> NsfMixerSoundConvertor::fds;
            case CHANNEL_TYPE_N163 -> NsfMixerSoundConvertor::n163;
            case CHANNEL_TYPE_VRC7 -> NsfMixerSoundConvertor::vrc7;
            case CHANNEL_TYPE_S5B -> NsfMixerSoundConvertor::s5b;
            default -> NsfMixerSoundConvertor::sampled;
        };
    }
}
