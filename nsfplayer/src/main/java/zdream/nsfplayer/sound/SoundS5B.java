package zdream.nsfplayer.sound;

import zdream.nsfplayer.core.NsfStatic;


/**
 * S5B orbital sounders. There are three such orbitals.
 *
 * @author Zdream
 * @since v0.2.8
 */
public class SoundS5B extends AbstractNsfSound {

    public SoundS5B() {
        reset();
        internalRefresh();
    }

    /**
     * volume meter
     */
    public static final int[] VOLT_BL = {
            0x00, 0x01, 0x01, 0x02, 0x02, 0x03, 0x03, 0x04, 0x05, 0x06, 0x07, 0x09, 0x0B, 0x0D, 0x0F, 0x12,
            0x16, 0x1A, 0x1F, 0x25, 0x2D, 0x35, 0x3F, 0x4C, 0x5A, 0x6A, 0x7F, 0x97, 0xB4, 0xD6, 0xEB, 0xFF
    };

    //
    // parameters
    //

    /*
     * Original Record Parameters
     *
     * 00 position: [0x00, 0x02, 0x04] Lower 8 bits of waveform frequency parameter (16 bits total)
     * 01 position: [0x01, 0x03, 0x05] Higher 8 bits of waveform frequency parameter (16 bits total)
     * 06 position: 0x06 Noise Frequency Parameters
     * 07 position: 0x07 Track Shield Identification
     * 08 position: [0x08, 0x09, 0x0A] Volume parameters
     * 11 position: 0x0B Lower 8 bits of envelope playback speed (16 bits total)
     * 12 position: 0x0C High 8 bits of envelope playback speed (16 bits total)
     * 13 position: 0x0D Envelope control parameters
     */

    /**
     * <p>Waveform Frequency Parameters
     * <p>00 position: xxxxxxxx as low 8 bits, 01 position: xxxxxxxx as high 8 bits, total 16 bits
     * <p>range [0, 0xFFFF]
     * </p>
     */
    public int freq;

    /**
     * <p>Noise Frequency Parameters
     * <p>06 position: 000xxxxx, value obtained * 2; if parameter is 0, set to 1.
     * <p>range [1, 62]
     * </p>
     */
    public int noiseFreq;

    /**
     * <p>Waveform Enablement Parameters
     * <p>07 position: 0000000x (track 1), 000000x0 (track 2), 00000x00 (track 3)
     * <p>If 1 then <code>tmask = true</code>, which enables the waveform,
     * otherwise false; which disables it.
     * </p>
     */
    public boolean waveEnable;

    /**
     * <p>Noise Enabling Parameters
     * <p>07 position: 0000x000 (track 1), 000x0000 (track 2), 00x00000 (track 3)
     * <p>true if 1, means noise is enabled, false otherwise; means it is disabled
     * </p>
     */
    public boolean noiseEnable;

    /**
     * <p>Volume parameters
     * <p>08 position: 0000xxxx, the value obtained is noted as <code>a</code>, then:
     * <blockquote><pre>
     * volume = VOLT_BL[a * 2]
     * </pre></blockquote>
     * <p>range [0, 0xFF]
     * </p>
     *
     * @see #VOLT_BL
     */
    public int volume;

    /**
     * <p>Envelope playback speed
     * <p>11 position: xxxxxxxx as low 8 bits, 12 position: xxxxxxxx as high 8 bits, total 16 bits
     * <p>range [1, 0xFFFF]
     * </p>
     */
    public int envelopeSpeed;

    /**
     * <p>The envelope continues to identify
     * <p>13 position: 0000x000
     * <p>If 1 then true, indicates that the envelope continues to play, otherwise false;
     * indicates that the envelope pauses playback
     * </p>
     */
    public boolean envelopeContinue;

    /**
     * <p>envelope striking mark
     * <p>13 position: 00000x00
     * <p>true if 1, false otherwise
     */
    public boolean envelopeAttack;

    /**
     * <p>Envelope Correction Marker
     * <p>13 position: 000000x0
     * <p>true if 1, false otherwise
     */
    public boolean envelopeAlternate;

    /**
     * <p>Envelope HOLD Marker
     * <p>13 position: 0000000x
     * <p>true if 1, false otherwise
     */
    public boolean envelopeHold;

    //
    // Auxiliary parameters
    //

    private boolean envFace;
    private boolean envPause;
    private int envCount;
    private int envPtr;

    private int noiseCount;
    private int noiseSeed;
    private int waveCount;
    private boolean waveEdge;

    /**
     * The state of the audio changes every {@link #step} clock,
     * and the audio value needs to be output to an external source.
     * Record the number of clocks remaining until the next step trigger point.
     */
    private int counter = 8;

    //
    // input method
    //

    public void envelopeReset() {
        envFace = envelopeAttack;
        envPause = false;
        envCount = 0x10000 - envelopeSpeed;
        envPtr = (envFace) ? 0 : 0x1f;
    }

    //
    // private method
    //

    private static final int GETA_BITS = 24;

    /**
     * Increment of baseCount every 8 clocks
     */
    private int baseDelta;
    private int baseCount;

    private void internalRefresh() {
        int clk = NsfStatic.BASE_FREQ_NTSC;
        int rate = clk / 8;
        baseDelta = (int) ((double) clk * (1 << GETA_BITS) / (16 * rate));
    }

    //
    // Public method
    //

    @Override
    public void reset() {
        // TODO S5B Parameter Reset

        noiseSeed = 0xffff;

        super.reset();
    }

    @Override
    protected void onProcess(int time) {
        int value;

        while (time >= counter) {
            time -= counter;
            this.time += counter;
            counter = 8;

            value = this.renderStep();
            mix(value);
        }

        this.time += time;
        counter -= time;
    }

    private int renderStep() {
        int noise;
        int delta; // unsigned

        baseCount += baseDelta;
        delta = (baseCount >> GETA_BITS);
        baseCount &= (1 << GETA_BITS) - 1;

        // Envelope
        envCount += delta;
        if (envelopeSpeed > 0) {
            while (envCount >= 0x10000) {
                if (!envPause) {
                    if (envFace)
                        envPtr = (envPtr + 1) & 0x3f;
                    else
                        envPtr = (envPtr + 0x3f) & 0x3f;
                }

                if ((envPtr & 0x20) != 0) { // if carry or borrow
                    if (envelopeContinue) {
                        if (envelopeAlternate && envelopeHold)
                            envFace &= true;
                        if (envelopeHold)
                            envPause = true;
                        envPtr = (envFace) ? 0 : 0x1f;
                    } else {
                        envPause = true;
                        envPtr = 0;
                    }
                }

                envCount -= envelopeSpeed;
            }
        }

        // Noise
        noiseCount += delta;
        if ((noiseCount & 0x40) != 0) {
            if ((noiseSeed & 1) != 0)
                noiseSeed ^= 0x24000;
            noiseSeed >>= 1;
            noiseCount -= noiseFreq;
        }
        noise = noiseSeed & 1;

        // Tone / Wave
        waveCount += delta;
        if ((waveCount & 0x1000) != 0) {
            if (freq > 1) {
                waveEdge = !waveEdge; // ?
                waveCount -= freq;
            } else {
                waveEdge = true;
            }
        }

        // Out
        int out = 0; // maintaining cout for stereo mix

        if (!isEnable())
            return 0;

        if ((waveEnable || waveEdge) && (noiseEnable || noise != 0)) {
            if ((volume & 32) == 0) {
                out = VOLT_BL[volume & 31];
            } else {
                System.out.println(Integer.toHexString(volume));
                out = VOLT_BL[envPtr];
            }
        }

        return out;
    }
}
