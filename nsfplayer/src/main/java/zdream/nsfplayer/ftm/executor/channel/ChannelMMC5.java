package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.context.DefaultSequenceHandler;
import zdream.nsfplayer.ftm.executor.tools.NoteLookupTable;
import zdream.nsfplayer.ftm.format.FtmSequence;


/**
 * There are two types of tracks for the MMC5 chip
 *
 * @author Zdream
 * @since v0.2.2
 */
public abstract class ChannelMMC5 extends AbstractFtmChannel {

    public ChannelMMC5(byte channelCode) {
        super(channelCode);
    }

    //
    // sequence
    //
    public final DefaultSequenceHandler seq = new DefaultSequenceHandler();

    /**
     * Calculate the volume, combine the volumes from the sequence,
     * and finally limit the volume to the range [0, 240]
     */
    protected void calculateVolume() {
        int volume = masterVolume * 16 + curVolume; // Accuracy 240
        if (volume <= 0) {
            curVolume = 0;
            return;
        }

        volume = (seq.volume * volume) / 15;

        if (volume > 240) {
            curVolume = 240;
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
     * Calculating Timbre
     */
    protected void calculateDuty() {
        if (seq.duty >= 0) {
            curDuty = seq.duty;
        } else {
            curDuty = masterDuty;
        }

        if (curDuty < 0 || curDuty > 3) {
            curDuty = curDuty & 0x3;
        }
    }

    /**
     * Query wavelength value according to the key.
     * Tools and Methods
     */
    @Override
    public int periodTable(int note) {
        return NoteLookupTable.ntsc(note);
    }

    @Override
    public void doRelease() {
        seq.setRelease(true);
    }
}
