package zdream.nsfplayer.ftm.format;

/**
 * <p>FTM data for each valid pattern.
 * <p>It holds all the note data for a given track of a pattern (or paragraph),
 * i.e. a column of FamiTracker data.
 *
 * @author Zdream
 * @since v0.1
 */
public class FtmPattern {

    public FtmNote[] notes;

    /**
     * section chief
     *
     * @return
     * @since v0.2.10
     */
    public int length() {
        return (notes == null) ? 0 : notes.length;
    }
}
