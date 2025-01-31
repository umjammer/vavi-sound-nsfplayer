package zdream.nsfplayer.sound;

/**
 * VRC6 Sounders with rectangular channel. Two such channels exist in total
 *
 * @author Zdream
 * @since v0.2.3
 */
public class SoundVRC6Pulse extends SoundVRC6 {

    public SoundVRC6Pulse() {
        reset();
    }

    //
    // parameters
    //

    /*
     * Original Record Parameters
     * 0 position: (0x9000 to 0xA000)
     * 1 position: (0x9001 to 0xA001)
     * 2 position: (0x9002 to 0xA002)
     */

    /**
     * <p>0 position: x0000000
     * <p>Whether it needs to be played or not
     * <p>True if 1, false if 0
     * </p>
     */
    public boolean gate;

    /**
     * <p>0 position: 0xxx0000
     * <p>Tone. range [0, 7]
     * </p>
     */
    public int duty;

    /**
     * <p>0 position: 0000xxxx
     * <p>Voluem. range [0, 15]
     * </p>
     */
    public int volume;

    /**
     * <p>1 position: xxxxxxxx as low 8 bits, 2 position: 0000xxxx as high 4 bits, total 12 bits
     * <p>Wavelength (affects pitch). range [0, 0xFFF]
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
     * (the number of clock cycles is period, one sixteenth of the true wavelength).
     */
    private int counter;

    /**
     * Divide a wavelength into 16 parts, this value records the current rendering to the first of the 16 parts.
     * range [0, 15]
     */
    private int dutyCycleCounter;

    /*
     * Public method
     */

    @Override
    public void reset() {
        // Original Record Parameters
        gate = false;
        duty = volume = period = 0;

        // Auxiliary parameters
        counter = 0;
        dutyCycleCounter = 0;

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

            dutyCycleCounter = (dutyCycleCounter + 1) & 0x0F;
            mix((gate || dutyCycleCounter > duty) ? volume : 0);
        }

        counter -= time;
        this.time += time;
    }
}
