package zdream.nsfplayer.sound;

/**
 * VRC6 Sounders with sawtooth channels
 *
 * @author Zdream
 * @since v0.2.3
 */
public class SoundVRC6Sawtooth extends SoundVRC6 {

    public SoundVRC6Sawtooth() {
        reset();
    }

    //
    // parameters
    //

    /*
     * Original Record Parameters
     * 0 position: (0xB000)
     * 1 position: (0xB001)
     * 2 position: (0xB002)
     */

    /**
     * <p>0 position: 00xxxxxx
     * <p>Volume. range [0, 63], Instead of [0, 15]
     * </p>
     */
    public int volume;

    /**
     * <p>1 position: xxxxxxxx as low 8 bits, 2 position: 0000xxxx as high 4 bits, total 12 bits
     * <p>1/14 wavelength (affects pitch). range [0, 0xFFF]
     * </p>
     */
    public int period;

    /*
     * 2 position: x0000000 i.e. the enabled parameter in the parent class
     */

    /*
     * Auxiliary parameters
     */

    /**
     * Record the number of clock cycles that have not been released in the current cycle
     * (the number of clock cycles is period, the true number of wavelengths in 14ths).
     */
    private int counter;

    /**
     * Divide a wavelength into 14 parts, this value records the current rendering to the first of the 14 parts.
     * range [1, 14] (note that everything starts at 1 except for initialization, which starts at 0)
     */
    private int cycleCounter;

    /**
     * Calculate the amplitude parameters of the audiovisual
     */
    private int phaseAccumulator;

    //
    // Public methods
    //

    @Override
    public void reset() {
        // Original Record Parameters
        volume = period = 0;

        // Auxiliary parameters
        counter = 0;
        cycleCounter = 0;
        phaseAccumulator = 0;

        super.reset();
    }

    @Override
    protected void onProcess(int time) {
        if (period == 0) {
            this.time += time;
            return;
        }

        while (time >= counter) {
            time -= counter;
            this.time += counter;
            counter = period + 1;

            if ((cycleCounter & 1) != 0)
                phaseAccumulator = (phaseAccumulator + volume) & 0xFF;

            cycleCounter++;

            if (cycleCounter == 14) {
                phaseAccumulator = 0;
                cycleCounter = 0;
            }

            // The 5 highest bits of accumulator are sent to the mixer
            mix(phaseAccumulator >> 3);
        }

        counter -= time;
        this.time += time;
    }
}
