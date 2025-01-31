package zdream.nsfplayer.sound;

/**
 * Rectangular Wave Loudspeaker
 *
 * @author Zdream
 * @since v0.2.1
 */
public class SoundPulse extends Sound2A03 {

    protected static final boolean[][] DUTY_TABLE = {
            {false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, false, true, true, true, true, false, false, false, false, false, false, false, false, false, false},
            {false, false, true, true, true, true, true, true, true, true, false, false, false, false, false, false},
            {true, true, false, false, false, false, true, true, true, true, true, true, true, true, true, true}
    };

    public SoundPulse() {
        reset();
    }

    //
    // parameters
    //

    //
    // Original Record Parameters
    //

    /**
     * <p>0 position: xx000000
     * <p>unsigned, value range [0, 3]. Indicates the value of the tone,
     * pointing to the first level index of the DUTY_TABLE.
     * </p>
     */
    public int dutyLength;

    /**
     * <p>0 position: 0000xxxx
     * <p>unsigned, range [0, 15]
     * </p>
     */
    public int fixedVolume;

    /*
     * The sweep-related parameters in position 1, and envelopeFix in position 0,
     * have been moved to the SweepSoundPulse subclass.
     *
     * By default the class considers envelopeFix = true and does not change the
     */

    /**
     * <p>Bit 2: xxxxxxxx (lower eight bits), Bit 3: 00000xxx (upper three bits) Total 11 bits
     * <p>is one sixteenth of the wavelength value, i.e., 16 of this value is the wavelength value,
     * and the unit is the CPU clock cycle.
     * <p>unsigned, range [0, 2047]
     * </p>
     */
    public int period;

    /**
     * <p>Position 3: xxxxx000
     * <p>Find index. When setting the value, please set the value to LENGTH_TABLE[?] .
     * </p>
     */
    public int lengthCounter;

    /*
     * Auxiliary parameters
     *
     * Note that bit 0x4015: (Pulse 1) 0000000x, (Pulse 2) 000000x0 is enable, in the superclass
     */

    /**
     * Record the current period (the number of clock cycles is period, one sixteenth of the true wavelength).
     * Number of clock cycles not played.
     */
    private int counter;

    /**
     * The real wavelength is divided into 16 segments, and this parameter records which of
     * the 16 segments the currently playing cycle is.
     * range [0, 15]
     */
    protected int dutyCycle; // m_iStepGen

    //
    // Public method
    //

    /**
     * Reset relevant data
     */
    @Override
    public void reset() {
        // Original Record Parameters

        dutyLength = 0;
        fixedVolume = 0;
        period = 0;

        lengthCounter = LENGTH_TABLE[0];
        counter = 0;
        dutyCycle = 0;

        super.reset();
    }

    @Override
    protected void onProcess(int time) {
        if (period < 8) {
            dutyCycle = 0;
            mix(0);
            return;
        }

        boolean valid = isStatusValid();

        while (time >= counter) {
            time -= counter;
            this.time += counter;
            counter = period + 1;

            int value = processStep(counter);
            mix(valid ? value : 0);

            dutyCycle = (dutyCycle + 1) & 0x0F;
        }

        counter -= time;
    }

    protected int processStep(int period) {
        int volume = fixedVolume;
        if (DUTY_TABLE[dutyLength][dutyCycle]) {
            return volume;
        } else {
            return 0;
        }
    }

    protected boolean isStatusValid() {
        return (period > 7) && isEnable() && (lengthCounter > 0)/* && (sweepResult < 0x800)*/;
    }
}
