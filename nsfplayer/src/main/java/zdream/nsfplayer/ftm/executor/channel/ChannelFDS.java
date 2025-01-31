package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.context.DefaultSequenceHandler;
import zdream.nsfplayer.ftm.executor.tools.NoteLookupTable;
import zdream.nsfplayer.ftm.format.FtmInstrumentFDS;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundFDS;


/**
 * FDS channel
 *
 * @author Zdream
 * @since v0.2.4
 */
public class ChannelFDS extends AbstractFtmChannel {

    public ChannelFDS() {
        super(CHANNEL_FDS);
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
        haltSound();
    }

    /*
     * Instrument Sequence
     */

    public final DefaultSequenceHandler seq = new DefaultSequenceHandler();

    /**
     * Current instrument used
     */
    private FtmInstrumentFDS currentInst;

    /**
     * Some parameters in FDS instruments
     */
    private int modSpeed, modDepth, modDelay;

    /**
     * Flag whether to reset sound.modPos
     */
    private boolean resetMod = false;

    /**
     * Sets modDepth. This method is called by effect Hxx
     */
    public void setModDepth(int modDepth) {
        this.modDepth = modDepth;
    }

    /**
     * Sets the high 4 bits of modSpeed. This method is called by effect Ixx
     */
    public void setModFreqHigh(int freq) {
        this.modSpeed = (this.modSpeed & 0xFF) | (freq << 8);
    }

    /**
     * Sets the lower 8 bits of modSpeed. This method is called by effect Jxx
     */
    public void setModFreqLow(int freq) {
        this.modSpeed = (this.modSpeed & 0xF00) | (freq);
    }

    /**
     * Update the sequence and write the sequence data back to the track
     */
    private void updateSequence() {
        if (instrumentUpdated) {
            currentInst = getRuntime().querier.getFDSInstrument(instrument);
            if (currentInst == null) {
                seq.reset();
                haltSound();
            } else {
                // Replacement sequence
                FtmSequence[] seqs = new FtmSequence[]
                        {currentInst.seqVolume, currentInst.seqArpeggio, currentInst.seqPitch};
                for (int i = 0; i < seqs.length; i++) {
                    FtmSequence s = seqs[i];
                    if (s != null) {
                        seq.setupSequence(seqs[i]);
                    } else {
                        seq.clearSequence(FtmSequenceType.get(i));
                    }
                }

                modSpeed = currentInst.modulationSpeed;
                modDepth = currentInst.modulationDepth;
                modDelay = currentInst.modulationDelay;
                resetMod = true;
            }
        }

        seq.update();

        // Write Back
        calculateVolume();
        calculatePeriod();
    }

    /**
     * Turn off {@link #sound}, so that it does not make any sound
     */
    private void haltSound() {
        // $4090 0x00
        sound.setEnable(false);
        // $4080 0x80
        sound.wavEnvDisable = true;
        sound.wavEnvMode = false;
        sound.masterEnvSpeed = 0;
        sound.resetWavCounter();
        // $4083 0x80
        sound.wavHalt = true;
        sound.envHalt = false;
        // $408A 0xFF
        sound.masterEnvSpeed = 0xFF;
        // $4087 0x80
        sound.modHalt = true;
    }

    /**
     * Calculate the volume, combine the volumes from the sequence,
     * and finally limit the volume to the range [0, 480].
     * <br>masterVolume range [0, 15]
     * <br>seq.volume range [0, 31]
     */
    protected void calculateVolume() {
        int volume = masterVolume * 16 + curVolume; // Accuracy 240
        if (volume <= 0) {
            curVolume = 0;
            return;
        }

        volume = (seq.volume * volume) / 15; // The maximum value can reach 496

        if (volume > 480) {
            curVolume = 480;
        } else if (volume < 1) {
            curVolume = (seq.volume == 0) ? 0 : 1;
        } else {
            curVolume = volume;
        }
    }

    /**
     * Calculate the wavelength, combine the wavelength, pitch, key obtained from the sequence,
     * and the pitch and key values obtained from other effects to finally get the wavelength value
     */
    protected void calculatePeriod() {
        if (masterNote == 0) {
            // Do not play
            curNote = 0;
            curPeriod = 0;
            return;
        }

        int note = masterNote + curNote + seq.deltaNote;
        int period = -masterPitch + curPeriod + seq.period;

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

    /**
     * Query wavelength value according to the key.
     * Tools and Methods
     */
    @Override
    public int periodTable(int note) {
        return NoteLookupTable.fds(note);
    }

    /*
     * Sounder
     */

    public final SoundFDS sound = new SoundFDS();

    @Override
    public SoundFDS getSound() {
        return sound;
    }

    @Override
    public void writeToSound() {
        if (!playing || masterNote == 0) {
            sound.setEnable(false);
            sound.wavEnvSpeed = 0;
            return;
        }

        if (instrumentUpdated && this.currentInst != null) {
            // Write wave envelope data
            // Originally, when NSF was running, it was necessary to turn on
            // the writable flag before writing data into it.
            // Because we use Sound to write directly here, we can skip this step.

            System.arraycopy(currentInst.samples, 0, sound.wave, 0, 64);
            sound.wavWrite = false;

            // Writing mods
            sound.modHalt = true;
            sound.modPos = 0;
            for (int i = 0; i < currentInst.modulation.length; ++i)
                sound.writeMods(currentInst.modulation[i]);
        }

        /*
         * ChannelHandlerFDS.refreshChannel()
         */

        // Write frequency value (wavelength related)
        sound.wavFreq = this.curPeriod;
        sound.wavHalt = false;
        sound.envHalt = false;

        // Write volume
        sound.wavEnvDisable = true;
        sound.wavEnvMode = false;
        sound.setEnable(true);
        sound.wavEnvSpeed = this.curVolume / 16;

        if (resetMod)
            sound.modPos = 0;
        resetMod = false;

        if (modDelay == 0) {
            sound.modHalt = false;
            sound.modFreq = this.modSpeed;

            sound.modEnvDisable = true;
            sound.modEnvMode = false;
            sound.modEnvSpeed = this.modDepth;
        } else {
            sound.modHalt = true;
            this.modDelay--;
        }
    }
}
