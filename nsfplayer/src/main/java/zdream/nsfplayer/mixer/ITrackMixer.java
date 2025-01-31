package zdream.nsfplayer.mixer;

/**
 * <p>Multi-channel audio synthesizer
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public interface ITrackMixer extends ISoundMixer {

    /**
     * Get the number of channels
     *
     * @return Number of channels
     */
    int getTrackCount();

    /**
     * Set the number of channels
     *
     * @param trackCount The number of channels. This value must be greater than 0.
     *                  1 means mono, 2 means stereo, and so on.
     */
    void setTrackCount(int trackCount);
}
