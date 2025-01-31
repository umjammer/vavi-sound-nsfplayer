package zdream.nsfplayer.sound;

import zdream.nsfplayer.ftm.format.FtmDPCMSample;


/**
 * <p>DPCM Orbital Phonograph
 *
 * <p>Note that after setting the data, call the {@link #reload()} method once.
 * </p>
 *
 * @author Zdream
 * @since v0.2.2
 */
public class SoundDPCM extends Sound2A03 {

    public static final short[] DMC_PERIODS_NTSC = {
            428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 84, 72, 54
    };

    public SoundDPCM() {
        reset();
    }

    //
    // parameters
    //

    /*
     * Original Record Parameters
     * 0 position: (0x4010)
     * 1 position: (0x4011)
     * 2 position: (0x4012)
     * 3 position: (0x4013)
     */

    /**
     * <p>0 position: 0x000000
     * <p>Whether to loop or not
     * <p>True if 1, false if 0
     * </p>
     */
    public boolean loop;

    /**
     * <p>Position 0: 0000xxxx
     * <p>Indexing of wavelengths. Adjusts the pitch of the playback. The wavelengths used can
     * be found in {@link #DMC_PERIODS_NTSC} according to this index.
     * <p>range [0, 15]
     * </p>
     */
    public int periodIndex;

    /**
     * <p>1 position: 0xxxxxxx
     * <p>Initial volume envelope. The envelope value oscillates in the range [0, 127],
     * and the sound wave emits sound only when it oscillates.
     * So this is the only way to control the volume, but not directly.
     * <p>range [0, 127], when re-reading a new DPCM sample,
     * the dac will read this value as the initial envelope value:
     * Any other value -1, means that the dac value does not change.
     * </p>
     */
    public int deltaCounter;

    /**
     * <p>2 position: (xxxxxxxx) * 64
     * <p>Start reading position.
     * <p>range [0, 16320], accuracy 64
     * </p>
     */
    public int offsetAddress;

    /**
     * <p>3 position: (xxxxxxxx) * 16
     * <p>The length of the entire sample, i.e. the length of the byte array.
     * <p>range [0, 4080], accuracy 16
     * </p>
     */
    public int length;

    /**
     * Sampling data
     */
    public FtmDPCMSample sample;

    //
    // Auxiliary parameters
    //
    // Note that bit 0x4015: (Pulse 1) 0000000x, (Pulse 2) 000000x0 is enable, in the superclass
    //

    /**
     * Record the number of clock cycles that have not been released in the current cycle.
     */
    private int counter;

    /**
     * Flag bit for new byte value that has been read
     */
    private boolean sampleFilled;

    /**
     * The number of bytes remaining to be read.
     */
    private int remaining;

    /**
     * Temporarily stored read byte value. It has been converted to a positive value.
     */
    private int curByte;

    /**
     * The position to read in the sample.
     */
    private int address;

    /**
     * A byte needs to be split up and read bit by bit.
     * This records the number of unread bits remaining on a byte.
     */
    private int byteRemain;

    private int shiftReg;

    /**
     * Signs of whether or not it should be muted
     */
    private boolean silenceFlag;

    /**
     * The actual volume envelope used in the rendering.
     * will be read at initialization, dac = deltaCounter
     * Then the subsequent changes in dac will have nothing to do with deltaCounter.
     */
    public int dac;

    //
    // Public method
    //

    @Override
    public void reset() {
        // Original Record Parameters
        loop = false;
        periodIndex = 0;
        deltaCounter = -1;
        offsetAddress = 0;
        length = 0;
        sample = null;

        // Auxiliary parameters
        counter = 0;
        sampleFilled = false;
        remaining = 0;
        curByte = 0;
        address = 0;
        byteRemain = 0;
        shiftReg = 0;
        silenceFlag = false;

        super.reset();
    }

    @Override
    protected void onProcess(int time) {
        if (sample == null) {
            this.time += time;
            counter = 0;
            return;
        }

        int period = DMC_PERIODS_NTSC[periodIndex];

        while (time >= counter) {
            time -= counter;
            this.time += counter;
            counter = period;

            // DMA Data Read
            // Check if you need to fetch the next byte of data.
            if (!sampleFilled && (remaining > 0)) {
                curByte = Byte.toUnsignedInt(sample.read(address));
                address++;
                remaining--;
                sampleFilled = true;
                if (remaining == 0) {
                    if (loop) {
                        reload();
                    } else {
                        this.sample = null;
                    }
                }
            }

            // Output unit
            if (byteRemain == 0) {
                // Begin new output cycle
                byteRemain = 8;
                if (sampleFilled) {
                    shiftReg = curByte;
                    sampleFilled = false;
                    silenceFlag = false;
                } else {
                    silenceFlag = true;
                }
            }

            if (!silenceFlag) {
                if ((shiftReg & 1) == 1) {
                    if (dac < 126)
                        dac += 2;
                } else {
                    if (dac > 1)
                        dac -= 2;
                }
            }

            shiftReg >>= 1;
            --byteRemain;

            mix(dac);
        }

        counter -= time;
        this.time += time;
    }

    /**
     * Reset the read position.
     * Generally, for cyclic DMA, when you reach the end of reading,
     * you need to reset and start reading from the beginning.
     */
    public void reload() {
        address = offsetAddress;
        remaining = length + 1;
        if (deltaCounter >= 0) {
            dac = deltaCounter;
            deltaCounter = -1;
        }
        if (out != null)
            mix(dac);
    }

    /**
     * Ask if the sample has finished playing
     */
    public boolean isFinish() {
        return sample == null;
    }
}
