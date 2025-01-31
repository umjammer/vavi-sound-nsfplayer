package zdream.nsfplayer.sound;

import java.util.Arrays;


/**
 * <p>Sounders for FDS tracks
 * <p>The range of values output by this transmitter is [0, 2016].
 * </p>
 *
 * @author Zdream
 * @since v0.2.4
 */
public class SoundFDS extends AbstractNsfSound {

    public SoundFDS() {
        reset();
    }

    //
    // parameters
    //

    /*
     * Original Record Parameters
     *
     * Waveform envelope table section:
     *   [0x4040, 0x407F]
     * Other parameter sections:
     * 00 position: 0x4080 Waveform envelope control
     * 01 position: 0x4081 not have
     * 02 position: 0x4082 Lower 8 bits of waveform frequency parameter (12 bits total)
     * 03 position: 0x4083 Higher 4 bits of waveform frequency parameter (12 bits total), disable flag
     * 04 position: 0x4084 Modulation Envelope Control
     * 05 position: 0x4085 modulation phase
     * 06 position: 0x4086 Lower 8 bits of modulation frequency parameter (12 bits total)
     * 07 position: 0x4087 Higher 4 bits of the modulation frequency parameter (12 bits total), disable flag
     * 08 position: 0x4088 Modulation Envelope Table Input
     * 09 position: 0x4089 Waveform Write Logo, Total Volume
     * 10 position: 0x408A Envelope playback speed
     */

    /**
     * <p>[0x4040, 0x407F]
     * <p>Waveform envelope, range [0, 63] of values for each cell
     * </p>
     */
    public final byte[] wave = new byte[64];

    /**
     * <p>08 position: 00000xxx Table data for cumulative values
     * <p>Modulation envelope, range [0, 7] for each cell value.
     * <p>It is recommended to use the {@link #writeMods(byte)} method to write the value.
     * </p>
     */
    public final byte[] mods = new byte[64];

    /**
     * <p>00 position: x0000000
     * <p>Waveform envelope disable sign.
     * <p>true when 1, waveform envelope is disabled; false when 0, waveform envelope is enabled
     * </p>
     */
    public boolean wavEnvDisable;

    /**
     * <p>00 position: 0x000000
     * <p>waveform envelope pattern
     * <p>True if 1, false if 0
     * </p>
     */
    public boolean wavEnvMode;

    /**
     * <p>00 position: 00xxxxxx
     * <p>Waveform envelope playback speed / overall waveform volume.
     * Which it means depends on the value of wavEnvDisable.
     * When <code>wavEnvDisable == true</code>, this value indicates the overall volume.
     * <p>range [0, 63]
     * </p>
     */
    public int wavEnvSpeed;

    /**
     * <p>02 position: xxxxxxxx as low 8 bits, 03 position: 0000xxxx as high 4 bits, total 12 bits
     * <p>The waveform frequency parameter can be considered as the value of
     * the step of the waveform phase forward for each clock.
     * <p>range [0, 0xFFF]
     * </p>
     */
    public int wavFreq;

    /**
     * <p>03 position: x0000000
     * <p>Waveform mute sign
     * <p>true for 1, mute waveform; false for 0
     * </p>
     */
    public boolean wavHalt;

    /**
     * <p>03 position: 0x000000
     * <p>envelope pause marker (computing)
     * <p>true for 1, envelope pause; false for 0
     * </p>
     */
    public boolean envHalt;

    /**
     * <p>04 position: x0000000
     * <p>Modulation envelope disable flag.
     * <p>A value of 1 is true, the modulation envelope is disabled;
     * a value of 0 is false, the modulation envelope is enabled.
     * </p>
     */
    public boolean modEnvDisable;

    /**
     * <p>04 position: 0x000000
     * <p>Modulation Envelope Mode
     * <p>True if 1, false if 0
     * </p>
     */
    public boolean modEnvMode;

    /**
     * <p>04 position: 00xxxxxx
     * <p>Modulation envelope playback speed
     * <p>range [0, 63]
     * </p>
     */
    public int modEnvSpeed;

    /**
     * <p>05 position: 0xxxxxxx
     * <p>Modulation Envelope Phase
     * <p>range [0, 127]
     * </p>
     */
    public int modPos;

    /**
     * <p>06 position: xxxxxxxx as low 8 bits, 07 position: 0000xxxx as high 4 bits, total 12 bits
     * <p>The modulation frequency parameter can be considered as the value
     * of the step forward of the modulation phase for each clock.
     * <p>range [0, 0xFFF]
     * </p>
     */
    public int modFreq;

    /**
     * <p>07 position: x0000000
     * <p>modulation pause symbol
     * <p>true if 1, modulation is paused; false if 0
     * </p>
     */
    public boolean modHalt;

    /**
     * <p>09 position: x0000000
     * <p>Waveforms can be written to flags
     * <p>true if 1; false if 0
     * </p>
     */
    public boolean wavWrite;

    /**
     * <p>09 position: 000000xx
     * <p>Total Volume Selection Parameters
     * <p>range [0, 3], the volume ratios of the 4 stops are 30 : 20 : 15 : 12 respectively.
     * </p>
     */
    public int masterVolume;

    /**
     * <p>10 position: xxxxxxxx
     * <p>Total Envelope Velocity
     * <p>range [0, 255]
     * </p>
     */
    public int masterEnvSpeed;

    //
    // Auxiliary parameters
    //

    /**
     * <p>waveform phase
     * The value of the phase is processed by << 16 bits from the original
     * value in order to increase the accuracy.
     * <p>For every 0x10000 of this value, the index of {@link #wave} is incremented by 1
     * if the wave advances by one frame.
     * </p>
     */
    private int wavPhase;

    /**
     * modulation phase
     * The value of the phase is processed by << 16 bits from the original
     * value in order to increase the accuracy.
     */
    private int modPhase;

    /**
     * Waveform Clock Counter
     */
    private int wavEnvCounter;

    /**
     * Modulation Clock Counter
     */
    private int modEnvCounter;

    /**
     * waveform output
     */
    private int wavEnvOut;

    /**
     * modulated output
     */
    private int modEnvOut;

    /**
     * Now the overall output value
     */
    private int curOut;

    //
    // input method
    //

    /**
     * <p>Write the mod value to the modulation envelope table.
     * <p>In the original NSF program, the table was controlled by writing to the 0x4088 cell,
     * and this method emulates that behavior.
     * </p>
     *
     * @param mod range [0, 7]
     */
    public void writeMods(int mod) {
        if (modHalt) {
            // Here are the original words
            // writes to current playback position (there is no direct way to set phase)
            mods[(modPhase >> 16) & 0x3F] = (byte) (mod & 0x07);
            modPhase = (modPhase + 0x010000) & 0x3FFFFF;
            mods[(modPhase >> 16) & 0x3F] = (byte) (mod & 0x07);
            modPhase = (modPhase + 0x010000) & 0x3FFFFF;
        }
    }

    //
    // Search method
    //

    public int getWavEnvOut() {
        return wavEnvOut;
    }

    public int getModEnvOut() {
        return modEnvOut;
    }

    //
    // Public method
    //

    @Override
    public void reset() {
        // Original Record Parameters
        Arrays.fill(wave, (byte) 0);
        Arrays.fill(mods, (byte) 0);
        wavEnvDisable = true;
        wavEnvMode = false;
        wavEnvSpeed = 0;
        wavFreq = 0;
        wavHalt = true;
        envHalt = false;
        modEnvDisable = true;
        modEnvMode = false;
        modEnvSpeed = 0;
        modPos = 0;
        modFreq = 0;
        modHalt = true;
        wavWrite = false;
        masterVolume = 0;
        masterEnvSpeed = 0xE8;

        // Auxiliary parameters
        wavPhase = modPhase = 0;
        wavEnvCounter = modEnvCounter = 0;
        wavEnvOut = modEnvOut = 0;
        curOut = 0;

        super.reset();

        // supplemental
        // write(0x408A, 0xE8, 0);
        // 0x4080
        // 0x4083
        // 0x4084
        // 0x4085
        // 0x4087
    }

    public void resetCounter() {
        wavEnvCounter = modEnvCounter = 0;
    }

    public void resetWavCounter() {
        wavEnvCounter = 0;
    }

    public void resetModCounter() {
        modEnvCounter = 0;
    }

    @Override
    protected void onProcess(int time) {
        // pre-programmed operation
        if (wavHalt) {
            wavPhase = 0;
        }

        if (modHalt) {
            modPhase &= 0x3F0000; // Reset accumulated phases
        }

        if (envHalt) {
            resetCounter();
        }

        if (wavEnvDisable) {
            wavEnvOut = wavEnvSpeed;
        }

        // That's the appetizer on top, and the main course is below.

        /*
         * I've made some adjustments here, because I'm not sure about the value of time above,
         * the NSF part of the call time is the time of one sample, the FTM part of the call
         * time is the time of one frame, these two times are very different.
         * So I decided to use the phase represented by wavPhase as a uniform time step.
         *
         * When the phase represented by wavPhase is moved forward by one frame,
         *  all the corresponding parts, including wavCounter, modCounter, modTable,
         *  execute this part of the time.
         *
         * This will have a very subtle (and imperceptible) difference to the real audio output,
         * but the CPU and readability of the code will be significantly improved.
         */
        int clockLeft = time;

        if (!wavHalt) {
            int wavePhaseDelta = countWavePhaseDelta();
            int waveLeft = (((wavPhase >> 16) + 1) << 16) - wavPhase;
            int clockAccum = 0;

            while (clockLeft > 0) {
                waveLeft -= wavePhaseDelta;
                wavPhase += wavePhaseDelta;
                if (waveLeft < 0) {
                    wavPhase &= 0x3FFFFF;
                    // When the above condition is met, the index of the wave array
                    // pointed to by wavPhase is moved one frame forward.
                    stepAll(clockAccum);
                    putOut(wavEnvOut);
                    clockAccum = 0;
                    waveLeft = (((wavPhase >> 16) + 1) << 16) - wavPhase;
                }

                clockAccum++;
                clockLeft--;
                this.time++;
            }

            if (clockAccum > 0) {
                stepAll(clockAccum);
            }
        } else {
            if (!modHalt) {
                modTableStep(time);
            }
            this.time += time;
        }
    }

    private void stepAll(int time) {
        if (!envHalt && !wavHalt && (masterEnvSpeed != 0)) {
            if (!wavEnvDisable) {
                wavCounterStep(time);
            }

            if (!modEnvDisable) {
                modCounterStep(time);
            }
        }
        if (!modHalt) {
            modTableStep(time);
        }
    }

    private void wavCounterStep(int time) {
        wavEnvCounter += time;
        int period = ((wavEnvSpeed + 1) * masterEnvSpeed) << 3;
        while (wavEnvCounter >= period) {
            // envelope moves forward by the clock
            // clock the envelope
            if (wavEnvMode) {
                if (wavEnvOut < 32)
                    ++wavEnvOut;
            } else {
                if (wavEnvOut > 0)
                    --wavEnvOut;
            }
            this.time += period;
            wavEnvCounter -= period;
        }
    }

    /**
     * mod Counter forward
     */
    private void modCounterStep(int time) {
        modEnvCounter += time;
        int modPeriod = ((modEnvSpeed + 1) * masterEnvSpeed) << 3;
        while (modEnvCounter >= modPeriod) {
            // clock the envelope
            if (modEnvMode) {
                if (modEnvOut < 32)
                    ++modEnvOut;
            } else {
                if (modEnvOut > 0)
                    --modEnvOut;
            }
            modEnvCounter -= modPeriod;
        }
    }

    /**
     * mod modem forward
     *
     * @param time
     */
    private void modTableStep(int time) {
        if (!modHalt) {
            // Calculate the pre-forward and post-forward phases separately
            // advance phase, adjust for modulator | unsigned
            int start_pos = modPhase >> 16;
            modPhase += (time * modFreq);
            int end_pos = modPhase >> 16;

            // modPhase contains the lower 24 bits of cumulative value bits,
            // and the upper 6 bits of true phase data, range [0, 64 * 0xFFFF - 1].
            // wrap the phase to the 64-step table (+ 16 bit accumulator)
            modPhase = modPhase & 0x3FFFFF;

            // execute all clocked steps
            for (int p = start_pos; p < end_pos; ++p) {
                int wv = mods[p & 0x3F];
                if (wv == 4) // 4 means reset mod position
                    modPos = 0;
                else {
                    int[] BIAS = {0, 1, 2, 4, 0, -4, -2, -1};
                    modPos += BIAS[wv];
                    modPos &= 0x7F; // 7-bit numeric, no character position
                }
            }
        }
    }

    /**
     * Calculate how many values will be added to wavFreq per clock
     */
    private int countWavePhaseDelta() {
        int mod = 0;
        if (modEnvOut != 0) { // skip if modulator off
            // convert mod_pos to 7-bit signed
            int pos = (modPos < 64) ? modPos : (modPos - 128);

            // multiply pos by gain,
            // shift off 4 bits but with odd "rounding" behaviour
            int temp = pos * modEnvOut;
            int rem = temp & 0x0F;
            temp >>= 4;
            if ((rem > 0) && ((temp & 0x80) == 0)) {
                if (pos < 0)
                    temp -= 1;
                else
                    temp += 2;
            }

            // The range of temp is [-64, 191]. If it exceeds the range, modify it
            // wrap if range is exceeded
            while (temp >= 192)
                temp -= 256;
            while (temp < -64)
                temp += 256;

            // Multiply by pitch value, shift right by 6 bits
            // multiply result by pitch,
            // shift off 6 bits, round to nearest
            temp = wavFreq * temp;
            rem = temp & 0x3F;
            temp >>= 6;
            if (rem >= 32)
                temp += 1;

            mod = temp;
        }

        return wavFreq + mod;
    }

    private void putOut(int vol_out) {
        if (vol_out > 32)
            vol_out = 32;

        // final output
        if (!wavWrite)
            curOut = wave[(wavPhase >> 16) & 0x3F] * vol_out;

        // Volume levels
        int v = switch (masterVolume) {
            case 0 -> 30;
            case 1 -> 20;
            case 2 -> 15;
            case 3 -> 12;
            default -> 0;
        };

        v = (curOut * v) / 30;

        mix(v);
    }
}
