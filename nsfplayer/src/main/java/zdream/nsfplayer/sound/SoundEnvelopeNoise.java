package zdream.nsfplayer.sound;

import vavi.util.win32.WAVE.data;
import zdream.nsfplayer.core.IFrameSequence;


/**
 * Noise generator with envelope section
 *
 * @author Zdream
 * @since v0.2.8
 */
public class SoundEnvelopeNoise extends SoundNoise implements IFrameSequence {

    //
    // parameters
    //

    //
    // Original Record Parameters
    //

    /**
     * <p>0 position: 00x00000
     * <p>True if 1, false if 0
     * </p>
     */
    public boolean envelopeLoop;

    /**
     * <p>0 position: 000x0000
     * <p>True if 1, false if 0
     * </p>
     */
    public boolean envelopeDisable;

    //
    // Auxiliary parameters
    //

    /**
     * How many Frame Sequences have passed in this frame?
     */
    private int sequenceCount;

    /**
     * Number of clocks per Frame Sequence
     */
    private int sequenceStep = SEQUENCE_STEP_NTSC;

    /**
     * How many clocks are needed to reach the next Frame Sequence trigger time?
     */
    private int sequenceRemain;

    //
    // envelope partial data
    //
    private int envelopeCounter;
    private int envelopeDiv;
    private boolean envelopeUpdated;

    //
    // set up
    //

    @Override
    public void setSequenceStep(int clock) {
        this.sequenceStep = clock;
    }

    /**
     * Call this method if you have just updated the envelope section.
     */
    public void onEnvelopeUpdated() {
        envelopeUpdated = true;
    }

    //
    // Public method
    //

    @Override
    public void endFrame() {
        sequenceCount = 0;
        sequenceRemain = 0;
        super.endFrame();
    }

    @Override
    public void reset() {
        envelopeLoop = false;
        envelopeDisable = false;

        envelopeCounter = 0;
        envelopeDiv = 0;
        envelopeUpdated = false;

        super.reset();
    }

    //
    // rendering
    //

    private void sequenceUpdate() {
        sequenceCount++;
        // Provides for a maximum of 4 triggers per frame
        if (sequenceCount > 4) {
            return;
        }

        // 240hz clock
        {
            boolean divider = false;
            if (envelopeUpdated) {
                envelopeUpdated = false;
                envelopeCounter = 15;
                envelopeDiv = 0;
            } else {
                ++envelopeDiv;
                // volume = envelopeDivPeriod
                if (envelopeDiv > fixedVolume) {
                    divider = true;
                    envelopeDiv = 0;
                }
            }

            if (divider) {
                if (envelopeLoop && envelopeCounter == 0)
                    envelopeCounter = 15;
                else if (envelopeCounter > 0)
                    --envelopeCounter;
            }
        }

        // 120hz clock
        if ((sequenceCount & 1) == 1) {
            // noise length counter
            if (!envelopeLoop && (lengthCounter > 0))
                --lengthCounter;
        }
    }

    @Override
    protected int processStep(int period) {
        // Frame Sequence update section
        sequenceRemain -= period;
        if (sequenceRemain < 0) {
            sequenceRemain += sequenceStep;
            sequenceUpdate();
        }

        // rendering section
        int volume;
        if (envelopeDisable) {
            volume = fixedVolume;
        } else {
            volume = envelopeCounter;
        }
        if (lengthCounter <= 0) {
            volume = 0;
        }

        int ret = (shiftReg & 1) != 0 ? volume : 0;
        shiftReg = (((shiftReg << 14) ^ (shiftReg << dutySampleRate)) & 0x4000) | (shiftReg >> 1);
        return ret;
    }

    @Override
    protected void processRemainTime(int period) {
        sequenceRemain -= period;
        if (sequenceRemain < 0) {
            sequenceRemain += sequenceStep;
            sequenceUpdate();
        }
    }
}
