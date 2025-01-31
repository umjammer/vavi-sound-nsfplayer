package zdream.nsfplayer.core;

/**
 * <p>Frame Sequence Connection.
 * <p>2A03 (+ 2A07) Comparatively, the demand for the road is actually longer than the time it takes to walk 1.
 * For example:
 * <li>The Pulse track's Envelope updates, triggered approximately every 0.25 frames (240 Hz);
 * <li>The Pulse track's Sweep, which is triggered approximately every 0.5 frames (120 Hz);
 * <li>The Length Counter of the Triangle track is updated, approximately every 0.25 frames (240 Hz);
 * <li>The Noise track's Envelope updates, triggering approximately every 0.25 frames (240 Hz);
 * <li>The Noise track's Length Counter is updated, approximately every 0.5 frames (120 Hz);
 * </li>
 * </p>
 *
 * @author Zdream
 * @since v0.2.8
 */
public interface IFrameSequence {

    int SEQUENCE_STEP_NTSC = 7458;
    int SEQUENCE_STEP_PAL = 8314;

    /**
     * Set the number of clocks per Frame Sequence update
     *
     * @param clock The number of clocks per Frame Sequence. This number of clocks roughly
     *             corresponds to 0.25 frames at NTSC 60Hz. The value of this value varies
     *             in different formats.
     * @see #SEQUENCE_STEP_NTSC
     * @see #SEQUENCE_STEP_PAL
     */
    void setSequenceStep(int clock);
}
