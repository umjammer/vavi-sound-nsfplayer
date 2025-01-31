package zdream.nsfplayer.ftm.format;

import zdream.nsfplayer.core.FtmChipType;


/**
 * FDS Chip Instruments
 *
 * @author Zdream
 * @since v0.2.4
 */
public class FtmInstrumentFDS extends AbstractFtmInstrument {

    public static final int SAMPLE_LENGTH = 64;
    public static final int MODULATION_LENGTH = 32;

    /**
     * Data on changes in volume envelope within a cycle
     */
    public final byte[] samples = new byte[SAMPLE_LENGTH];

    /**
     * Modulation parameters
     */
    public final byte[] modulation = new byte[MODULATION_LENGTH];
    public int modulationSpeed;
    public int modulationDepth;
    public int modulationDelay;

    /**
     * Volume Sequence
     */
    public FtmSequence seqVolume;
    /**
     * Arpeggio sequence
     */
    public FtmSequence seqArpeggio;
    /**
     * Pitch sequence
     */
    public FtmSequence seqPitch;

    @Override
    public FtmChipType instType() {
        return FtmChipType.FDS;
    }

    @Override
    public String toString() {
        return String.format("FDS Instrument #%d %s", seq, name);
    }
}
