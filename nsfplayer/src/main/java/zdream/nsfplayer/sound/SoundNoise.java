package zdream.nsfplayer.sound;

/**
 * <p>noise generator
 * <p>This speaker does not handle parameters related to Envelope envelopes.
 * If you do need to use this kind of data, you need to use {@link SoundEnvelopeNoise}.
 * </p>
 *
 * @author Zdream
 * @since v0.2.2
 */
public class SoundNoise extends Sound2A03 {

    public static final short[] NOISE_PERIODS_NTSC = {
            4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068
    };

    public static final short[] NOISE_PERIODS_PAL = {
            4, 8, 14, 30, 60, 88, 118, 148, 188, 236, 354, 472, 708, 944, 1890, 3778
    };

    public final short[] PERIOD_TABLE;

    public SoundNoise() {
        PERIOD_TABLE = NOISE_PERIODS_NTSC;
        reset();
    }

    //
    // parameters
    //

    /**
     * A reasonable value for {@link #dutySampleRate}.
     * content:
     * <li>duty == 0 when dutySampleRate = DUTY_SAMPLE_RATE0;
     * <li>duty == 1 when dutySampleRate = DUTY_SAMPLE_RATE1;
     * </li>
     *
     * @see #dutySampleRate
     */
    public static final int
            DUTY_SAMPLE_RATE0 = 13,
            DUTY_SAMPLE_RATE1 = 8;

    /*
     * Original Record Parameters
     * 0 position: (0x400C)
     * 1 position: (0x400D)
     * 2 position: (0x400E)
     * 3 position: (0x400F)
     */

    /*
     * The Envelope envelope-related parameters envelopeLoop and envelopeDisable
     * at position 0 have been moved to the EnvelopeSoundNoise subclass.
     *
     * By default the class considers envelopeLoop = true,
     * envelopeDisable = true and does not change
     */

    /**
     * <p>0 position: 0000xxxx
     * <p>unsigned, range [0, 15]
     * </p>
     */
    public int fixedVolume;

    // Ignore position 1.

    /**
     * <p>2 position: 0000xxxx
     * <p>unsigned, range [0, 15]
     * <p>The index of the wavelength to be queried.
     * PERIOD_TABLE[periodIndex] i.e. get the wavelength value.
     * </p>
     */
    public int periodIndex;

    /**
     * <p>2 position: Process data for x0000000
     * <p>is true if it is 1, Current Sampling Rate = 8；false if 0，Current sampling rate = 13
     * <p>This value is the duty value
     * </p>
     *
     * @see #DUTY_SAMPLE_RATE0
     * @see #DUTY_SAMPLE_RATE1
     */
    public int dutySampleRate;

    /**
     * <p>3 position: xxxxx000 processing data
     * <p>Not a lookup index. lengthCounter = PulseSound.LENGTH_TABLE[v]
     * </p>
     */
    public int lengthCounter;

    /*
     * Auxiliary parameters
     *
     * Note that bit 0x4015: (Pulse 1) 0000000x, (Pulse 2) 000000x0 is enable, in the superclass
     */

    /**
     * Record the current period (the number of clock cycles is period,
     * one sixteenth of the true wavelength). Number of clock cycles not played.
     */
    private int counter;

    /**
     * used in the onProcess() method.
     */
    protected int shiftReg;

    //
    // Public method
    //

    @Override
    public void reset() {
        // Original Record Parameters
        fixedVolume = 0;
        periodIndex = 0;
        dutySampleRate = DUTY_SAMPLE_RATE0;
        lengthCounter = 0;

        // Auxiliary parameters
        counter = 0;
        shiftReg = 1;

        super.reset();
    }

    @Override
    protected void onProcess(int time) {
        int period = PERIOD_TABLE[periodIndex];

        while (time >= counter) {
            time -= counter;
            this.time += counter;

            int value = processStep(counter);
            mix(value);
            counter = period;
        }

        counter -= time;
        this.time += time;
        processRemainTime(time);
    }

    protected int processStep(int period) {
        if (!isEnable()) {
            return 0;
        }

        int ret = ((shiftReg & 1) != 0) ? fixedVolume : 0;
        shiftReg = (((shiftReg << 14) ^ (shiftReg << dutySampleRate)) & 0x4000) | (shiftReg >> 1);
        return ret;
    }

    protected void processRemainTime(int period) {
        // do nothing
    }
}
