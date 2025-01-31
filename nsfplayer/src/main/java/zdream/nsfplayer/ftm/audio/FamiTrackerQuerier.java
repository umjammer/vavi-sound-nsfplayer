package zdream.nsfplayer.ftm.audio;

import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.format.AbstractFtmInstrument;
import zdream.nsfplayer.ftm.format.FtmInstrument2A03;
import zdream.nsfplayer.ftm.format.FtmInstrumentFDS;
import zdream.nsfplayer.ftm.format.FtmInstrumentN163;
import zdream.nsfplayer.ftm.format.FtmInstrumentVRC6;
import zdream.nsfplayer.ftm.format.FtmInstrumentVRC7;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmPattern;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmTrack;

import static zdream.nsfplayer.core.FtmChipType.FDS;
import static zdream.nsfplayer.core.FtmChipType.N163;
import static zdream.nsfplayer.core.FtmChipType.VRC6;
import static zdream.nsfplayer.core.FtmChipType.VRC7;
import static zdream.nsfplayer.core.FtmChipType._2A03;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.ARPEGGIO;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.DUTY;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.HI_PITCH;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.PITCH;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.VOLUME;


/**
 * A queryer for FamiTracker's text data.
 *
 * @author Zdream
 * @date 2018-06-09
 * @since 0.2.1
 */
public class FamiTrackerQuerier implements INsfChannelCode {

    /**
     * Original Ftm audio data
     */
    public final FtmAudio audio;

    /**
     * @param audio Original Ftm audio data
     * @throws NullPointerException When audio = null
     */
    public FamiTrackerQuerier(FtmAudio audio) {
        if (audio == null) {
            throw new NullPointerException("FTM audio data is null");
        }
        this.audio = audio;
        this.frameRate = audio.getFrameRate();
    }

    //
    // parameter
    //

    int frameRate;

    public int getFrameRate() {
        return frameRate;
    }

    //
    // channel
    //

    /**
     * Calculate the total number of channels
     *
     * @return
     */
    public int channelCount() {
        return audio.handler.channelCount();
    }

    /**
     * View the channel channel of the {@code channel}th track
     *
     * @return
     */
    public byte channelCode(int channel) {
        return audio.handler.channelCode(channel);
    }

    //
    // Instruments
    //

    /**
     * Returns the instrument. If the instrument at the specified position is empty, returns null
     *
     * @param instrument Instrument number
     * @return
     * @since v0.2.2
     */
    public AbstractFtmInstrument getInstrument(int instrument) {
        if (instrument >= audio.instrumentCount()) {
            return null;
        }
        return audio.getInstrument(instrument);
    }

    /**
     * Returns the chip status of the instrument.
     * If the instrument at the specified position is empty, returns null
     *
     * @param instrument Instrument number
     * @return
     * @since v0.2.5
     */
    public FtmChipType getInstrumentType(int instrument) {
        AbstractFtmInstrument inst = getInstrument(instrument);
        if (inst != null) {
            return inst.instType();
        }
        return null;
    }

    /**
     * Returns the 2A03 instrument. If the instrument at the specified position is empty,
     * or is not of type 2A03, returns null
     *
     * @param instrument Instrument Number
     * @return
     * @since v0.2.2
     */
    public FtmInstrument2A03 get2A03Instrument(int instrument) {
        AbstractFtmInstrument inst = getInstrument(instrument);
        if (inst == null) {
            return null;
        }
        if (inst.instType() != _2A03) {
            return null;
        }
        return (FtmInstrument2A03) inst;
    }

    /**
     * Returns the FDS instrument. If the instrument at the specified position is empty,
     * or is not of type FDS, returns null
     *
     * @param instrument Instrument Number
     * @return
     * @since v0.2.4
     */
    public FtmInstrumentFDS getFDSInstrument(int instrument) {
        AbstractFtmInstrument inst = getInstrument(instrument);
        if (inst == null) {
            return null;
        }
        if (inst.instType() != FDS) {
            return null;
        }
        return (FtmInstrumentFDS) inst;
    }

    /**
     * Returns the N163 instrument. If the instrument at the specified position is empty,
     * or is not of type N163, returns null
     *
     * @param instrument Instrument Number
     * @return
     * @since v0.2.6
     */
    public FtmInstrumentN163 getN163Instrument(int instrument) {
        AbstractFtmInstrument inst = getInstrument(instrument);
        if (inst == null) {
            return null;
        }
        if (inst.instType() != N163) {
            return null;
        }
        return (FtmInstrumentN163) inst;
    }

    /**
     * Returns the N163 instrument. If the instrument at the specified position is empty,
     * or is not of type N163, returns null
     *
     * @param instrument Instrument Number
     * @return
     * @since v0.2.6
     */
    public FtmInstrumentVRC7 getVRC7Instrument(int instrument) {
        AbstractFtmInstrument inst = getInstrument(instrument);
        if (inst == null) {
            return null;
        }
        if (inst.instType() != VRC7) {
            return null;
        }
        return (FtmInstrumentVRC7) inst;
    }

    public FtmSequence[] getSequences(int instrument) {
        AbstractFtmInstrument inst = getInstrument(instrument);
        if (inst == null) {
            return new FtmSequence[5];
        }

        return switch (inst.instType()) {
            case _2A03 -> {
                FtmInstrument2A03 i2 = (FtmInstrument2A03) inst;
                yield new FtmSequence[] {
                        i2.vol == -1 ? null : audio.getSequence(_2A03, VOLUME, i2.vol),
                        i2.arp == -1 ? null : audio.getSequence(_2A03, ARPEGGIO, i2.arp),
                        i2.pit == -1 ? null : audio.getSequence(_2A03, PITCH, i2.pit),
                        i2.hip == -1 ? null : audio.getSequence(_2A03, HI_PITCH, i2.hip),
                        i2.dut == -1 ? null : audio.getSequence(_2A03, DUTY, i2.dut),
                };
            }
            case VRC6 -> {
                FtmInstrumentVRC6 i2 = (FtmInstrumentVRC6) inst;
                yield new FtmSequence[] {
                        i2.vol == -1 ? null : audio.getSequence(VRC6, VOLUME, i2.vol),
                        i2.arp == -1 ? null : audio.getSequence(VRC6, ARPEGGIO, i2.arp),
                        i2.pit == -1 ? null : audio.getSequence(VRC6, PITCH, i2.pit),
                        i2.hip == -1 ? null : audio.getSequence(VRC6, HI_PITCH, i2.hip),
                        i2.dut == -1 ? null : audio.getSequence(VRC6, DUTY, i2.dut),
                };
            }
            case FDS -> {
                FtmInstrumentFDS i2 = (FtmInstrumentFDS) inst;
                yield new FtmSequence[] {i2.seqVolume, i2.seqArpeggio, i2.seqPitch};
            }
            case N163 -> {
                FtmInstrumentN163 i2 = (FtmInstrumentN163) inst;
                yield new FtmSequence[] {
                        i2.vol == -1 ? null : audio.getSequence(N163, VOLUME, i2.vol),
                        i2.arp == -1 ? null : audio.getSequence(N163, ARPEGGIO, i2.arp),
                        i2.pit == -1 ? null : audio.getSequence(N163, PITCH, i2.pit),
                        i2.hip == -1 ? null : audio.getSequence(N163, HI_PITCH, i2.hip),
                        i2.dut == -1 ? null : audio.getSequence(N163, DUTY, i2.dut),
                };
            }
            default -> new FtmSequence[5];
        };

    }

    /*
     * section, note
     */

    /**
     * Determine the number of segments in a given track
     *
     * @param track Track Number
     */
    public int trackCount(int track) {
        return audio.getTrack(track).orders.length;
    }

    /**
     * Determines the maximum number of lines in a given track
     *
     * @param track Track Number
     */
    public int maxRow(int track) {
        return audio.getTrack(track).length;
    }

    /**
     * Get key data
     *
     * @param track   Track Number
     * @param section Segment number
     * @param channel channel number, starting from 0
     * @param row     Line Number
     * @return
     */
    public FtmNote getNote(int track, int section, int channel, int row) {
        FtmTrack t = audio.getTrack(track);
        int order = t.orders[section][channel];
        if (order >= t.patterns.length) {
            return null;
        }
        FtmPattern p = t.patterns[order][channel];
        if (p == null) {
            return null;
        }
        return p.notes[row];
    }
}
