package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.tools.NoteLookupTable;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundNoise;


/**
 * 2A03 Noise Track
 *
 * @author Zdream
 * @since v0.2.2
 */
public class ChannelNoise extends ChannelTone {

    public ChannelNoise() {
        super(CHANNEL_2A03_NOISE);
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

    @Override
    protected void calculateDuty() {
        super.calculateDuty();
        curDuty &= 0x1;
    }

    @Override
    public int periodTable(int note) {
        return NoteLookupTable.pal(note);
    }

    //
    // sequence
    //

    /**
     * Update the sequence and write the sequence data back to the track
     */
    private void updateSequence() {
        if (instrumentUpdated) {
            // Replacement sequence
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

        // Write Back
        calculateVolume();
        calculateNoise();
        calculateDuty();
    }

    private void calculateNoise() {
        if (masterNote == 0) {
            curNote = 0;
            return;
        }

        int note = masterNote + curNote + seq.deltaNote;
        note += (-masterPitch + curPeriod + seq.period);

        if (seq.arp != 0) {
            switch (seq.arpSetting) {
                case FtmSequence.ARP_SETTING_ABSOLUTE:
                    note += seq.arp;
                    break;
                case FtmSequence.ARP_SETTING_FIXED: // Reset
                    this.masterNote = note = seq.arp;
                    break;
                case FtmSequence.ARP_SETTING_RELATIVE:
                    this.masterNote += seq.arp;
                    note += seq.arp;
                default:
                    break;
            }
        }

        if (note <= 1) {
            note = 1;
        } else if (note > 16) {
            note = 16;
        }

        curNote = note;
    }

    /*
     * Sounder
     */

    final SoundNoise sound = new SoundNoise();

    @Override
    public SoundNoise getSound() {
        return sound;
    }

    @Override
    public void writeToSound() {
//		sound.envelopeLoop = true;
//		sound.envelopeDisable = true;

        sound.fixedVolume = curVolume / 16;
        if (curVolume == 0 || !playing || masterNote == 0) {
            sound.fixedVolume = 0;
            return;
        }

        int period = (curNote - 1) ^ 0x0F;

        sound.dutySampleRate = ((curDuty & 1) == 1) ?
                SoundNoise.DUTY_SAMPLE_RATE1 : SoundNoise.DUTY_SAMPLE_RATE0;
        sound.periodIndex = period;
        sound.lengthCounter = 0;
    }
}
