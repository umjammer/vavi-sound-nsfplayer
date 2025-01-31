package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.tools.NoteLookupTable;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundVRC6Sawtooth;


/**
 * VRC6 Sawtooth track. This channel has no controls for timbre
 *
 * @author Zdream
 * @since v0.2.3
 */
public class ChannelVRC6Sawtooth extends ChannelVRC6 {

    public ChannelVRC6Sawtooth() {
        super(CHANNEL_VRC6_SAWTOOTH);
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
        calculateDuty(); // The calculation of volume requires the use of duty
        calculateVolume();
        calculatePeriod();
    }

    @Override
    protected void calculateVolume() {
        int volume = masterVolume * 16 + curVolume; // Precision 240
        if (volume <= 0) {
            curVolume = 0;
            return;
        } else if ((this.curDuty & 1) == 1) {
            // VRC6 If there is an odd number of timbre entries in a jagged channel (e.g. V01),
            // the volume will be raised by one notch.
            volume += 240;
        }
        // volume Accuracy 480

        volume = (seq.volume * volume) / 15;

        if (volume > 480) {
            curVolume = 480;
        } else if (volume < 1) {
            curVolume = (seq.volume == 0) ? 0 : 1;
        } else {
            curVolume = volume;
        }
    }

    @Override
    public int periodTable(int note) {
        return NoteLookupTable.saw(note);
    }

    /*
     * sound device
     */

    /**
     * VRC6 Pulse Audio Speaker
     */
    public final SoundVRC6Sawtooth sound = new SoundVRC6Sawtooth();

    @Override
    public SoundVRC6Sawtooth getSound() {
        return sound;
    }

    @Override
    public void writeToSound() {
        if (!playing || masterNote == 0) {
            sound.period = 0;
            sound.volume = 0;
        } else {
            sound.period = this.curPeriod;
            sound.volume = this.curVolume * 64 / 480;
        }
    }
}
