package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundPulse;

import static zdream.nsfplayer.sound.Sound2A03.LENGTH_TABLE;


/**
 * MMC5 Rectangular Channel One / Two
 *
 * @author Zdream
 * @since v0.2.2
 */
public class ChannelMMC5Pulse extends ChannelMMC5 {

    /**
     * @param isPulse1 true if it is an MMC5 rectangle one channel
     *                 false if it's the MMC5 rectangle two channel
     */
    public ChannelMMC5Pulse(boolean isPulse1) {
        super(isPulse1 ? CHANNEL_MMC5_PULSE1 : CHANNEL_MMC5_PULSE2);
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

    //
    // sound device
    //

    /**
     * MMC5 Pulse Audio Speaker
     */
    public final SoundPulse sound = new SoundPulse();

    @Override
    public SoundPulse getSound() {
        return sound;
    }

    @Override
    public void writeToSound() {
//		sound.looping = true; // constant
//		sound.envelopeFix = true; // constant

        if (this.curVolume == 0 || !playing || masterNote == 0) {
            sound.fixedVolume = 0;
            return;
        }

        sound.dutyLength = curDuty;
        sound.fixedVolume = curVolume / 16;

        // 0x4002 and 0x4003
        sound.period = curPeriod;
        sound.lengthCounter = LENGTH_TABLE[0];
    }
}
