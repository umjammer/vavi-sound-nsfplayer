package zdream.nsfplayer.mixer;

/**
 * <p>Mixer multi-Track single receive channel interface
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public interface ITrackChannel extends IMixerChannel {

    /**
     * Set the volume for each track. The final volume is the total volume
     * of the track volume
     *
     * @param level Channel volume value, [0, 1]
     * @param track Track number
     */
    void setTrackLevel(float level, int track);

    /**
     * Get the track volume value. The final volume presented is the total volume
     * of the track, not the volume of a single track.
     *
     * @param track Track number
     * @return Channel volume value, [0, 1]
     */
    float getTrackLevel(int track);
}
