package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.tools.NoteLookupTable;
import zdream.nsfplayer.ftm.format.FtmInstrumentN163;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundN163;


/**
 * N163 channel
 *
 * @author Zdream
 * @since v0.2.6
 */
public class ChannelN163 extends ChannelTone {

    /**
     * @param num The number of N163 channel. range [0, 7]
     */
    public ChannelN163(int num) {
        super((byte) (CHANNEL_N163_1 + num));
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
        lastDuty = -1;
    }

    /*
     * Instrument sequence
     */

    /**
     * Current instrument used
     */
    private FtmInstrumentN163 currentInst;

    /**
     * The duty value used in the previous frame, that is,
     * the wave number used in the previous frame
     */
    private int lastDuty = -1;

    /**
     * Update the sequence and write the sequence data back to the track
     */
    private void updateSequence() {
        if (instrumentUpdated) {
            lastDuty = -1;
            currentInst = getRuntime().querier.getN163Instrument(instrument);
            if (currentInst == null) {
                seq.reset();
                haltSound();
            } else {
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
        }

        seq.update();

        // Write Back
        calculateVolume();
        calculatePeriod();
        calculateDuty(); // duty is used to select the wave
    }

    /**
     * Turn off {@link #sound}, so that it does not make any sound
     */
    private void haltSound() {
        sound.setEnable(false);
        sound.volume = 0;
    }

    /**
     * Calculate the wavelength, combine the wavelength, pitch, key obtained from the sequence,
     * and the pitch and key values obtained from other effects to finally get the wavelength value
     */
    @Override
    protected void calculatePeriod() {
        if (masterNote == 0) {
            // Do not play
            curNote = 0;
            curPeriod = 0;
            return;
        }

        int ch = getRuntime().querier.audio.getNamcoChannels();
        int note = masterNote + curNote + seq.deltaNote;
        int period = masterPitch * ch + curPeriod + seq.period;

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
        } else if (note > 96) {
            note = 96;
        }

        period += periodTable(note);
        if (period < 1) {
            period = 1;
        }

        curNote = note;
        curPeriod = period;
    }

    @Override
    public int periodTable(int note) {
        int ch = getRuntime().querier.audio.getNamcoChannels();
        return NoteLookupTable.n163(note) * ch;
    }

    /*
     * Sounder
     */

    public final SoundN163 sound = new SoundN163();

    @Override
    public SoundN163 getSound() {
        return sound;
    }

    @Override
    public void writeToSound() {
        if (!playing || masterNote == 0) {
            haltSound();
            return;
        }

        int duty = this.curDuty;
        int waveCount = this.currentInst.waves.length;
        duty %= waveCount;

        if (duty != lastDuty) {
            // Write wave
            byte[] wave = this.currentInst.waves[duty];
            System.arraycopy(wave, 0, sound.wave, 0, wave.length);
            sound.length = wave.length;

            lastDuty = duty;
        }

        // Number of tracks for N163
        int ch = getRuntime().querier.audio.getNamcoChannels();
        sound.step = 15 * ch;

        // Other parameters
        sound.period = this.curPeriod * 4;
        sound.volume = this.curVolume / 16;
        sound.setEnable(true);
    }
}
