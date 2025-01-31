package zdream.nsfplayer.ftm.format;

/**
 * Types of sequences
 *
 * @author Zdream
 * @since v0.2.0
 */
public enum FtmSequenceType {

    /**
     * volume
     */
    VOLUME,

    /**
     * Arpeggios
     */
    ARPEGGIO,

    /**
     * Pitch Change
     */
    PITCH,

    /**
     * A hi-pitch change of 1 is equal to a pitch change of 16.
     * <br>FDS does not exist for this sequence type
     */
    HI_PITCH,

    /**
     * Duty / Noise;
     * <br>VRC6 has Pulse Width
     * <br>FDS does not exist for this sequence type
     * <br>N163 contains the Wave (waveform envelope) number
     */
    DUTY;

    public static FtmSequenceType get(int index) {
        return values()[index];
    }
}
