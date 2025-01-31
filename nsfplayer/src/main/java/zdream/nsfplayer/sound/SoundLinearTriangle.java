package zdream.nsfplayer.sound;

import zdream.nsfplayer.core.IFrameSequence;


/**
 * Noise generator with linear voice
 *
 * @author Zdream
 * @since v0.2.9
 */
public class SoundLinearTriangle extends SoundTriangle implements IFrameSequence {

    //
    // parameters
    //

    /*
     * Original Record Parameters
     */

    /**
     * <p>0 position: x0000000
     * <p>True if 1, false if 0
     * </p>
     */
    public boolean looping;

    /**
     * <p>0 position: 0xxxxxxx
     * <p>unsigned, value field [0, 127]
     * </p>
     */
    public int linearLoad;

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

    /*
     * linear part
     */
    private boolean linearCounterHalt;
    private int linearCounter;

    //
    // set up
    //

    @Override
    public void setSequenceStep(int clock) {
        this.sequenceStep = clock;
    }

    /**
     * If you have just updated 3 positions, call this method
     */
    public void onEnvelopeUpdated() {
        linearCounterHalt = true;
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
        // Original Record Parameters
        looping = false;
        linearLoad = 0;

        // Auxiliary parameters
        linearCounterHalt = false;
        linearCounter = 0;

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
            if (linearCounterHalt) {
                linearCounter = linearLoad;
            } else {
                if (linearCounter > 0)
                    --linearCounter;
            }
            if (!looping) {
                linearCounterHalt = false;
            }
        }

        // 120hz clock
        if ((sequenceCount & 1) == 1) {
            if (!looping && (lengthCounter > 0))
                --lengthCounter;
        }
    }

    @Override
    protected int processStep(int period) {
        // Frame Sequence update section
        sequenceRemain -= period;
        while (sequenceRemain < 0) {
            sequenceRemain += sequenceStep;
            sequenceUpdate();
        }

        return super.processStep(period);
    }

    @Override
    protected boolean isStatusValid() {
        return super.isStatusValid() && linearCounter > 0;
    }
}
