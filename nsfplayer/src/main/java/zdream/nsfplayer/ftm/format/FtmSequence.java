package zdream.nsfplayer.ftm.format;

/**
 * <p>Sequence. The changes in volume, pitch, and timbre of each instrument are quantified by numerical values,
 * and this is a sequence.
 * <p>The sequence of all chip types is FtmSequence.
 *
 * @author Zdream
 * @date 2018-04-25
 * @since v0.2.0
 */
public class FtmSequence {

    /**
     * <p>2A03 and VRC6 have 5 types of FtmSequence
     * </p>
     */
    public static final int SEQUENCE_COUNT = 5;

    /**
     * <p>FDS only has 3 types: volume (VOLUME), arpeggio (ARPEGGIO), pitch (PITCH)
     * </p>
     */
    public static final int SEQUENCE_COUNT_FDS = 3;

    /**
     * <p>Arpeggio settings. {@link FtmSequenceType#ARPEGGIO} sequence setting has the following three optional values:
     *
     * <li><b>ARP_SETTING_ABSOLUTE  Absolute mode (default)</b>
     * <br>If this value is set, the final key produced is the key calculated from the current state plus the value of the arpeggio.
     * <br>For example, if the sum of the other states gives a key of C-4, and the arpeggio value is 3, then the final key is 3 semitones higher than C-4, which is D#4.
     * </li>
     * <p><li><b>ARP_SETTING_RELATIVE  Relative mode</b>
     * <br>If this value is set, the value generated in the first frame is the same as the absolute value.
     * <br>But in the second frame, the value added by the key is equal to the sum of the values of the previous two frames.
     * <br>For example, if the sum of the other states is C-4, and the first frame arpeggio value is 3, then the first frame's key is 3 semitones higher than C-4, which is D#4.
     * The second frame's arpeggio value is 2, and the sum of the first two frames' arpeggio values is 5, and the key is 5 semitones higher than C-4, which is F-4.
     * </li>
     * <p><li><b>ARP_SETTING_FIXED  Correction method</b>
     * <br>If this value is set, the system will ignore the key calculated in other states and use the arpeggio value as the key instead.
     * </li></p>
     */
    public static final byte
            ARP_SETTING_ABSOLUTE = 0,
            ARP_SETTING_FIXED = 1,
            ARP_SETTING_RELATIVE = 2;

    /**
     * type
     */
    public final FtmSequenceType type;

    /**
     * Cycle points.
     * <br>The default is -1, which means no looping.
     */
    public int loopPoint;

    /**
     * Release point.
     * <br>The default value is -1, which means no release effect.
     */
    public int releasePoint;

    /**
     * Other optional data.
     * Only used by ARPEGGIO, indicates how this sequence affects the pitch.
     */
    public byte settings;

    /**
     * data
     */
    public byte[] data;

    public FtmSequence(FtmSequenceType type) {
        this.type = type;
    }

    public void clear() {
        loopPoint = -1;
        releasePoint = -1;
        settings = 0;
        data = null;
    }

    public int length() {
        return (data == null) ? 0 : data.length;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(60);

        if (data != null) {
            b.append("Sequence").append('[');
            int length = data.length - 1;
            for (int i = 0; i < length; i++) {
                if (loopPoint == i) {
                    b.append("| ");
                }
                if (releasePoint == i) {
                    b.append("\\ ");
                }
                b.append(data[i]).append(' ');
            }
            b.append(data[length]).append(']');
        } else {
            b.append("Empty Sequence");
        }

        return b.toString();
    }
}
