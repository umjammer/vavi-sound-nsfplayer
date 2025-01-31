package zdream.nsfplayer.ftm.format;

/**
 * Constants in Ftm
 *
 * @author Zdream
 * @since v0.2.4
 */
public class FtmStatic {

    /**
     * Maximum number of effects per line and track
     * Number of effect columns allowed
     */
    public static final int MAX_EFFECT_COLUMNS = 4;

    // Other constants

    public static final int OCTAVE_RANGE = 8;

    /**
     * Maximum number of sequences
     * <br>Maximum number of sequence lists
     */
    public static final int MAX_SEQUENCES = 128;

    /**
     * Maximum number of sections (paragraphs, originally called frames) supported.
     * This is the number of sections supported in each {@link FtmTrack}.
     * <p>Maximum number of sections / frames
     */
    public static final int MAX_SECTIONS = 128;

    /**
     * Maximum number of supported instruments
     */
    public static final int MAX_INSTRUMENTS = 128;

    /**
     * loudest volume
     */
    public static final byte MAX_VOLUMN = 16;

    /**
     * Maximum value supported by Tempo.
     */
    public static final int MAX_TEMPO = 255;

    /**
     * <p>Maximum number of lines per frame (paragraph).
     * This value is also explicitly defined in NSF
     * <p>Maximum length of patterns (in rows). 256 is max in NSF
     */
    public static final int MAX_PATTERN_LENGTH = 256;

    /**
     * <p>Maximum number of Patterns per track.
     * Equivalent to the maximum number of paragraphs
     * <p>Maximum number of patterns per channel
     */
    public static final int MAX_PATTERNS = MAX_SECTIONS;

    /**
     * Maximum number of DPCMs
     * Maximum number of DPCM samples,
     * cannot be increased unless the NSF driver is modified.
     */
    public static final int MAX_DSAMPLES = 64;

    /**
     * @see FtmSequence#SEQUENCE_COUNT
     */
    public static final int SEQUENCE_COUNT = FtmSequence.SEQUENCE_COUNT;

    /**
     * @see FtmSequence#SEQUENCE_COUNT_FDS
     */
    public static final int SEQUENCE_COUNT_FDS = FtmSequence.SEQUENCE_COUNT_FDS;
}
