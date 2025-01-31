package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundVRC6Pulse;


/**
 * VRC6 Rectangular channel I / II
 *
 * @author Zdream
 * @since v0.2.3
 */
public class ChannelVRC6Pulse extends ChannelVRC6 {

    /**
     * @param isPulse1 true if this is a VRC6 one rectangle track
     *                 If it's the VRC6 number two rectangular track, false.
     */
    public ChannelVRC6Pulse(boolean isPulse1) {
        super(isPulse1 ? CHANNEL_VRC6_PULSE1 : CHANNEL_VRC6_PULSE2);
    }

    @Override
    public void playNote() {
        super.playNote();

        // sequence
        updateSequence();
    }

    @Override
    public void reset() {
        super.reset();
        seq.reset();
        sound.reset();
    }

    //
    // sequences
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
        curDuty &= 0x7;
    }

    /*
     * sound device
     */

    /**
     * VRC6 Pulse Audio Speaker
     */
    public final SoundVRC6Pulse sound = new SoundVRC6Pulse();

    @Override
    public SoundVRC6Pulse getSound() {
        return sound;
    }

    @Override
    public void writeToSound() {
        sound.period = this.curPeriod;
        sound.volume = this.curVolume / 16;
        sound.duty = this.curDuty;

        if (!playing || masterNote == 0) {
            sound.setEnable(false);
        } else {
            sound.setEnable(true);
        }
    }
}
