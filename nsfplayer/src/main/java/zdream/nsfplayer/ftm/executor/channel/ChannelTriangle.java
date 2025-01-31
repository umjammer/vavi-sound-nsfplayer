package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.tools.NoteLookupTable;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundTriangle;


/**
 * 2A03 Triangle channel
 *
 * @author Zdream
 * @since v0.2.2
 */
public class ChannelTriangle extends ChannelTone {

    public ChannelTriangle() {
        super(CHANNEL_2A03_TRIANGLE);
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

        // Write Back (Triangle wave channel has no tone value)
        calculateVolume();
        calculatePeriod();
    }

    /**
     * Calculate the volume, due to the specificity of the triangular wave track,
     * the final decision here is whether the triangular wave should sound or not,
     * and finally the volume is limited to the range of [0, 1].
     */
    @Override
    protected void calculateVolume() {
        if (seq.volume == 0) {
            curVolume = 0;
        } else {
            curVolume = 1;
        }
    }

    /*
     * sound device
     */

    /**
     * 2A03 Triangle Audio Speaker
     */
    final SoundTriangle sound = new SoundTriangle();

    @Override
    public SoundTriangle getSound() {
        return sound;
    }

    @Override
    public void writeToSound() {
        if (this.curVolume > 0 && playing && masterNote > 0) {
            sound.setEnable(true);
            sound.period = this.curPeriod;
        } else {
            sound.setEnable(false);
        }
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
