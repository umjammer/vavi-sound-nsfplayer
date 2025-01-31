package zdream.nsfplayer.core;

import java.util.Set;


/**
 * Abstract NSF sound source renderer, used to output PCM audio data organized as byte / short array
 *
 * @author Zdream
 * @since v0.2.4
 */
public interface INsfRendererHandler<T extends AbstractNsfAudio>
        extends INsfChannelCode {

    /**
     * <p>Set the playback pause position to the beginning of the specified track.
     * </p>
     *
     * @param track Track number, starting from 0
     */
    void ready(int track);

    /**
     * <p>Let the renderer read the corresponding audio data.
     * <p>Set the playback pause position to the first segment of the specified track (segment 0)
     * </p>
     *
     * @param audio
     * @param track Track number, starting from 0
     */
    void ready(T audio, int track);

    /*
     * Instrument panel area
     */

    /**
     * @return The number of the track currently playing
     * @since v0.2.8
     */
    int getCurrentTrack();

    /**
     * Returns a collection of all channel numbers.
     * The channel number parameters are written in {@link INsfChannelCode}
     *
     * @return A collection of all channel numbers. If the ready(...) method is not called,
     * an empty collection is returned.
     * @since v0.2.8
     */
    Set<Byte> allChannelSet();

    /**
     * Set the volume of a channel
     *
     * @param channelCode channel number
     * @param level       volume. range [0, 1]
     * @since v0.2.8
     */
    void setLevel(byte channelCode, float level);

    /**
     * Get the volume of a channel
     *
     * @param channelCode channel number
     * @return volume. range [0, 1]
     * @throws NullPointerException When there is no channel corresponding to <code>channelCode</code>
     * @since v0.2.8
     */
    float getLevel(byte channelCode) throws NullPointerException;

    /**
     * Sets whether the channel should emit sound
     *
     * @param channelCode channel number
     * @param mask        false, makes the channel sound; true, mutes it
     * @since v0.2.8
     */
    void setChannelMuted(byte channelCode, boolean mask);

    /**
     * Check if the channel can produce sound
     *
     * @param channelCode channel number
     * @return false, indicating that the channel is not blocked; true, indicating that it has been blocked
     * @throws NullPointerException When there is no channel corresponding to <code>channelCode</code>
     * @since v0.2.8
     */
    boolean isChannelMuted(byte channelCode) throws NullPointerException;

}
