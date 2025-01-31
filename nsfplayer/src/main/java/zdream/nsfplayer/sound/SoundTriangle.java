package zdream.nsfplayer.sound;

/**
 * Triangle wave generator
 *
 * @author Zdream
 * @since v0.2.2
 */
public class SoundTriangle extends Sound2A03 {

    /**
     * The height of the sound wave in a cycle. It's actually an isosceles triangle.
     */
    private static final byte[] TRIANGLE_WAVE = {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
            0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00
    };

    public SoundTriangle() {
        reset();
    }

    //
    // parameters
    //

    /*
     * 0 position: (0x4008)
     * 1 position: (0x4009)
     * 2 position: (0x400A)
     * 3 position: (0x400B)
     */

    /*
     * The looping and linearLoad at position 0 have been
     * moved to the LinearSoundTriangle subclass.
     *
     * By default, looping is considered to be true in this class and will not be changed.
     */

    // Ignore position 1.

    /**
     * <p>2 position: xxxxxxxx (lower eight bits), 3 position: 00000xxx (upper three bits) total 11 bits
     * <p>is one sixteenth of the wavelength value, i.e., 16 of this value is the wavelength value,
     * and the unit is the CPU clock cycle:
     * <p>unsigned, range [0, 2047]
     * </p>
     */
    public int period;

    /**
     * <p>3 position: xxxxx000
     * <p>Find Index
     * </p>
     */
    public int lengthCounter;

    /*
     * Auxiliary parameters
     *
     * Note that bit 0x4015: (Pulse 1) 0000000x, (Pulse 2) 000000x0 is enable, in the superclass
     */

    /**
     * Record the number of clock cycles that have not been released in the current cycle
     * (the number of clock cycles is period, one sixteenth of the true wavelength).
     */
    private int counter;

    /**
     * The true wavelength is divided into 32 parts. The audio height of each cycle is shown in {@link #TRIANGLE_WAVE}.
     * This parameter records which of the 32 cycles is currently being played.
     * range [0, 31]
     */
    private int dutyCycle;

    /**
     * <p>Record the value of the last period.
     * <p>During NSF playback, some tracks will set the period to 0 to stop playback immediately.
     * At this point if the triangle wave is around period (16 / 32), it is very loud,
     * and due to the period reset, it will drop to 0 very quickly,
     * resulting in a very loud and disturbing tone.
     * <p>The solution to this problem is to cache the value of the previous period.
     * If the period is accidentally reset to 0, then use lastPeriod as a replacement value
     * during rendering, and wait until the delta wave completes one cycle before turning
     * off playback. I call this method “soft landing”.
     * </p>
     *
     * @since v0.2.10
     */
    protected int lastPeriod;

    //
    // Public method
    //

    @Override
    public void reset() {
        // Original Record Parameters
        period = 0;
        lengthCounter = LENGTH_TABLE[0];

        // Auxiliary parameters
        counter = 0;
        dutyCycle = 0;
        lastPeriod = 0;

        super.reset();
    }

    /**
     * <p>Directing the work of loudspeakers.
     * <p>If the track needs to make a sound, output the triangle wave data in a loop:
     * <p>If the track needs to stop sounding, if the triangle wave output of the previous
     * frame has not finished yet, output the previous cycle first, and then stop sounding.
     * </p>
     */
    @Override
    protected void onProcess(int time) {
        if (period >= 8) {
            lastPeriod = period;
        }
        if (lastPeriod < 7) {
            lastPeriod = 7;
        }

        while (time >= counter) {
            time -= counter;
            this.time += counter;

            if (period < 8) {
                counter = lastPeriod + 1;
            } else {
                counter = period + 1;
            }

            int value = processStep(counter);
            mix(value);
        }

        counter -= time;
    }

    protected int processStep(int period) {
        if (isStatusValid()) {
            int ret = TRIANGLE_WAVE[dutyCycle];
            dutyCycle = (dutyCycle + 1) & 0x1F;
            return ret;
        } else {
            if (dutyCycle == 0) {
                if (this.period == 0) {
                    lastPeriod = 7;
                }
                return 0;
            }
            int ret = TRIANGLE_WAVE[dutyCycle];
            dutyCycle = (dutyCycle + 1) & 0x1F;
            return ret;
        }
    }

    protected boolean isStatusValid() {
        return isEnable() && lengthCounter > 0 && period > 1;
    }
}
