package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.tools.NoteLookupTable;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundPulse;
import zdream.nsfplayer.sound.SoundSweepPulse;

import static zdream.nsfplayer.sound.Sound2A03.LENGTH_TABLE;


/**
 * 2A03 Rectangular channel 1 / 2
 *
 * @author Zdream
 * @version v0.2.5 Merged two 2A03 rectangular channels.
 * @since v0.2.1
 */
public final class Channel2A03Pulse extends ChannelTone {

    /**
     * @param isPulse1 If 2A03 Rectangular channel 1, true.
     *                 If 2A03 Rectangular channel 2, false.
     */
    public Channel2A03Pulse(boolean isPulse1) {
        super(isPulse1 ? CHANNEL_2A03_PULSE1 : CHANNEL_2A03_PULSE2);
        sound = new SoundSweepPulse(isPulse1);
    }

    @Override
    public void playNote() {
        // Resets the sweep parameter at the beginning of each frame.
        this.sweepUpdated = false;

        super.playNote();

        // sequence
        updateSequence();
    }

    @Override
    public void reset() {
        sweepPeriod = 0;
        sweepMode = false;
        sweepShift = 0;
        sweepEnable = false;
        this.sweepUpdated = false;

        super.reset();
        seq.reset();
        sound.reset();
    }

    //
    // sweep
    //

    /**
     * <p>range [0, 0xFF]
     * </p>
     *
     * @since v0.2.9
     */
    private int sweepPeriod;
    private boolean sweepMode;
    private int sweepShift;
    private boolean sweepUpdated;
    private boolean sweepEnable;

    /**
     * Setting the sweep parameter
     *
     * @param sweepPeriod range [0, 7]
     * @param sweepMode   true means the pitch slides up, false means the pitch slides down.
     * @param sweepShift  range [0, 7]
     * @since v0.2.9
     */
    public void setSweep(int sweepPeriod, boolean sweepMode, int sweepShift) {
        this.sweepPeriod = sweepPeriod;
        this.sweepMode = sweepMode;
        this.sweepShift = sweepShift;
        this.sweepUpdated = true;

        sweepEnable = (sweepShift > 0);
    }

    /**
     * Clear the sweep parameter
     *
     * @since v0.2.9
     */
    public void clearSweep() {
        this.sweepPeriod = 0;
        this.sweepMode = false;
        this.sweepShift = 0;
        this.sweepUpdated = true;

        sweepEnable = false;
    }

    /**
     * Queries if the channel is running the sweep effect.
     *
     * @return
     */
    public boolean isSweepEnable() {
        return sweepEnable;
    }

    //
    // Sequence
    //

    /**
     * Update the sequence, and write the data from the sequence back to the track.
     */
    private void updateSequence() {
        if (instrumentUpdated) {
            // Replacement Sequence
            FtmSequence[] seqs = getRuntime().querier.getSequences(instrument);
            for (int i = 0; i < seqs.length; i++) {
                FtmSequence s = seqs[i];
                if (s != null) {
                    seq.setupSequence(seqs[i]);
                } else {
                    seq.clearSequence(FtmSequenceType.get(i));
                }
            }
        }

        seq.update();

        // write back
        calculateVolume();
        calculatePeriod();
        calculateDuty();
    }

    @Override
    protected void calculateDuty() {
        super.calculateDuty();
        curDuty &= 0x3;
    }

    /*
     * sound device
     */

    /**
     * 2A03 Pulse 1 Audio Speaker
     */
    public final SoundSweepPulse sound;

    @Override
    public SoundPulse getSound() {
        return sound;
    }

    @Override
    public void writeToSound() {
        sound.envelopeLoop = true;
        sound.envelopeFix = true;

        if (this.curVolume == 0 || !playing || masterNote == 0) {
            sound.fixedVolume = 0;
            return;
        }

        sound.dutyLength = curDuty;
        sound.fixedVolume = curVolume / 16;

        if (sweepEnable) {
            if (sweepUpdated) {
                // Only the first frame of the sweep trigger is allowed to write data to the microphone.
                // Otherwise the period will be rewritten by the track, and the sweep effect will be lost
                // 0x4001
                sound.sweepEnabled = true;
                sound.sweepPeriod = (sweepPeriod) + 1;
                sound.sweepMode = sweepMode;
                sound.sweepShift = sweepShift;
                sound.onSweepUpdated();

                // 0x4002 and 0x4003
                sound.period = curPeriod;
                sound.lengthCounter = LENGTH_TABLE[0];
            }
        } else {
            // 0x4001
            sound.sweepEnabled = false;
            sound.sweepPeriod = 1;
            sound.sweepMode = true;
            sound.sweepShift = 0;
            sound.onSweepUpdated();

            // 0x4002 and 0x4003
            sound.period = curPeriod;
            sound.lengthCounter = LENGTH_TABLE[0];
        }

        sound.onEnvelopeUpdated();
    }

    //
    // others
    //

    /**
     * Query the wavelength value according to the tone key.
     * Tools and methodologies
     */
    @Override
    public int periodTable(int note) {
        return NoteLookupTable.ntsc(note);
    }
}
