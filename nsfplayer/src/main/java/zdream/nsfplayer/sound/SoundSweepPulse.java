package zdream.nsfplayer.sound;

import zdream.nsfplayer.core.IFrameSequence;


/**
 * Rectangular wave sounders with sweep and envelope voices
 *
 * @author Zdream
 * @since v0.2.9
 */
public class SoundSweepPulse extends SoundPulse implements IFrameSequence {

    public SoundSweepPulse() {
        super();
        this.isFirstChannel = true;
    }

    /**
     * @param isFirstChannel 2A03 has two tracks. If it is the first track, enter true;
     *                       if it is the second track, enter false
     */
    public SoundSweepPulse(boolean isFirstChannel) {
        super();
        this.isFirstChannel = isFirstChannel;
    }

    //
    // parameters
    //

    /**
     * <p>2A03 There are two tracks. If it is the first track, true; if it is the second track, false.
     * <p>This parameter is not reset by the {@link #reset()} method.
     * </p>
     */
    public final boolean isFirstChannel;

    /*
     * Original Record Parameters
     */

    /**
     * <p>Bit 0: 00x00000
     * <p>True if 1, false if 0
     * </p>
     */
    public boolean envelopeLoop;

    /**
     * <p>0 position: 000x0000
     * <p>True if 1, false if 0
     * </p>
     */
    public boolean envelopeFix;

    /**
     * <p>1 position: x0000000
     * <p>True if 1, false if 0
     * </p>
     */
    public boolean sweepEnabled;

    /**
     * <p>1 position: 0xxx0000, Add 1 to get the value,
     * Indicates how many units of time interval the frequency of the sweep changes
     * <p>unsigned, range [1, 8]
     * </p>
     */
    public int sweepPeriod;

    /**
     * <p>1 position: 0000x000
     * <p>A value of 1 is true for ascending, a value of 0 is false for descending.
     * </p>
     */
    public boolean sweepMode;

    /**
     * <p>1 position: 00000xxx, Offset Bits.
     * Indicates the parameter of the amount of variation in the frequency of the swept tone for each time period
     * <p>unsigned, range [0, 7]
     * </p>
     */
    public int sweepShift;

    /*
     * Auxiliary parameters
     */

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
     * sweep Partial data
     */

    /**
     * Records whether sweep-related parameters have been modified
     */
    public boolean sweepUpdated;

    /**
     * Wavelength correction calculated in the Sweep section
     */
    private int sweepResult;
    private int sweepDiv;

    /*
     * envelope partial data
     */
    private int envelopeCounter; // That is, the volume correction value calculated in the envelope section
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
     * Call this method if you just updated the sweep section.
     */
    public void onSweepUpdated() {
        sweepUpdated = true;
        calcSweepPeriod();
    }

    /**
     * Call this method if the envelope section (position 3) has just been updated.
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
        // Original Record Parameters
        sweepEnabled = false;
        sweepPeriod = 1;
        sweepMode = false;
        sweepShift = 0;

        envelopeFix = false;

        // Auxiliary parameters
        sweepUpdated = false;
        sweepResult = 0;
        sweepDiv = 1;

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

        // 240hz clock, envelope section
        {
            boolean divider = false;
            if (envelopeUpdated) {
                envelopeUpdated = false;
                envelopeCounter = 15;
                envelopeDiv = 0;
            } else {
                ++envelopeDiv;
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
            // envelope section
            if (!envelopeLoop && (lengthCounter > 0))
                --lengthCounter;

            // sweep section
            if (sweepEnabled) {
                --sweepDiv;
                if (sweepDiv <= 0) {
                    calcSweepPeriod(); // Recalculate the frequency data after sweep

                    if (period >= 8 && sweepResult < 0x800 && sweepShift > 0) {
                        // If the frequency data is appropriate, it will be updated.
                        period = sweepResult < 0 ? 0 : sweepResult;
                    }
                    sweepDiv = sweepPeriod + 1;
                }

                if (sweepUpdated) {
                    sweepDiv = sweepPeriod + 1;
                    sweepUpdated = false;
                }
            }
        }
    }

    /**
     * <p>Calculate the true frequency value due to the target sweep effect.
     * <p>The true frequency number will be placed in the variable {@link #sweepResult},
     * replacing {@link SoundPulse#period} when used.
     * </p>
     */
    private void calcSweepPeriod() {
        int shifted = this.period >> sweepShift;
        if (isFirstChannel && sweepMode)
            shifted += 1;
        sweepResult = period + (sweepMode ? -shifted : shifted);
    }

    @Override
    protected int processStep(int period) {
        // Frame Sequence update section
        sequenceRemain -= period;
        if (sequenceRemain < 0) {
            sequenceRemain += sequenceStep;
            sequenceUpdate();
        }

        // Render section volume
        int volume = envelopeFix ? fixedVolume : envelopeCounter;
        if (DUTY_TABLE[dutyLength][dutyCycle]) {
            return volume;
        } else {
            return 0;
        }
    }

    @Override
    protected boolean isStatusValid() {
        return super.isStatusValid() && (sweepResult < 0x800);
    }
}
