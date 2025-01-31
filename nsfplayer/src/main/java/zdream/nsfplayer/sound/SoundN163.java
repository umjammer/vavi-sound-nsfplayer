package zdream.nsfplayer.sound;

import java.util.Arrays;


/**
 * <p>N163 channel sounders, up to 8 channels in the NSF
 * <p>The range of values output from this audiophile is [0, 120]
 * </p>
 *
 * @author Zdream
 * @since v0.2.6
 */
public class SoundN163 extends AbstractNsfSound {

    public SoundN163() {
        reset();
    }

    //
    // parameters
    //

    /*
     * Original Record Parameters
     *
     * Waveform envelope table section:
     * Variable-length arrays, indeterminate starting position
     *
     * Other parameter sections:
     * If this is the nth channel (n is in the range [0, 7]), then:
     *
     * position 00: 0x40+(n*8) Lower 8 bits of frequency parameter（0 - 7 bits, total 18 bits）
     * position 01: 0x41+(n*8) Lower 8 bits of phase (0 - 7 bits, 24 bits total)
     * position 02: 0x42+(n*8) Middle 8 bits of frequency parameter (8 - 15 bits, 18 bits total)
     * position 03: 0x43+(n*8) Middle 8 bits of phase (8 - 15 bits, 24 bits total)
     * position 04: 0x44+(n*8) Higher 2 bits (16 - 17 bits, 18 bits total) of frequency parameter; Volume envelope length parameter
     * position 05: 0x45+(n*8) Higher 8 bits of phase (16 - 23 bits, 24 bits total)
     * position 06: 0x46+(n*8) Envelope starting point parameter (not recorded)
     * position 07: 0x47+(n*8) volume
     */

    /**
     * <p>waveform envelope (math.)
     * <p>per unit range [0, 15]
     * </p>
     */
    public final byte[] wave = new byte[240];

    /**
     * <p>Bit 00: xxxxxxxxxx as low 8 bits, Bit 02: xxxxxxxxxx as mid 8 bits, and
     * Bit 04: 000000xx as high 2 bits, total 18 bits
     * <p>Frequency parameter (although it's actually more accurate to think of it as wavelength), controls pitch
     * <p>range [0, 0x3FFFF]
     * </p>
     */
    public int period;

    /**
     * <p>Bit 01: xxxxxxxxxx as low 8 bits, Bit 03: xxxxxxxxxx as mid 8 bits, and
     * Bit 05: xxxxxxxxxx as high 8 bits, total 24 bits
     * <p>phase (waves)
     * <p>range [0, 0xFFFFFF]
     * </p>
     */
    public int phase;

    /**
     * <p>Bit 04: xxxxxx00, the value obtained is noted as <code>a</code>, then.
     * <blockquote><pre>
     * length = 256 - (4 * a)
     * </pre></blockquote>
     * <p>Volume envelope length parameter. That is, the effective length of {@link #wave}
     * <p>range [4, 256], and the value is divisible by 4
     * </p>
     */
    public int length;

//	/**
//	 * <p>position 06: xxxxxxxxxx
//	 * <p>Volume Envelope Starting Point Parameters
//	 * <p>range [0, 255]
//	 * </p>
//	 */
//	public int offset;

    /**
     * <p>position 07: 0000xxxx
     * <p>volume
     * <p>range [0, 15]
     * </p>
     */
    public int volume;

    //
    // Auxiliary parameters
    //

    /**
     * <p>The number of clocks between each audio state change. This value requires an external input.
     * The value is calculated as follows:
     * <blockquote><pre>
     * step = 15 * channelCount
     * </pre></blockquote>
     * where channelCount is the number of channels in N163.
     * <p>step will not be reset by {@link #reset()}.
     * </p>
     */
    public int step;

    /**
     * The state of the audio changes every {@link #step} clock,
     * and the audio value needs to be output to an external source.
     * Record the number of clocks remaining until the next step trigger point.
     */
    private int counter;

    //
    // Public method
    //

    @Override
    public void reset() {
        // Original Record Parameters
        period = 0;
        phase = 0;
        length = 0;
        volume = 0;
        Arrays.fill(wave, (byte) 0);

        // Auxiliary parameters
        counter = step;
        // step No initialization

        super.reset();
    }

    @Override
    protected void onProcess(int time) {
        // Be sure to set the value of step
        if (step <= 0) {
            this.counter = 0;
            this.time += time;
            return;
        }

        while (time >= counter) {
            time -= counter;
            this.time += counter;
            counter = step;

            phase = (phase + period) & 0x00ff_ffff;

            // phase boundary value
            int hiLen = length << 16;
            // Phase control within phase boundary values
            if (hiLen > 0) {
                while (phase >= hiLen)
                    phase -= hiLen;
            }

            // NsfPlayer Project:
            // fetch sample (note: N163 output is centred at 8, and inverted w.r.t 2A03)
            // It means that the N163 output is centered on 8, which is fundamentally different from the 2A03.
            int index = (phase >> 16);
            int sample = 8 - wave[index];

            mix(sample * volume);
        }

        this.time += time;
        counter -= time;
    }
}
