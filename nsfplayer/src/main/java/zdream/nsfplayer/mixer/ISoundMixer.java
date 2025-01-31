package zdream.nsfplayer.mixer;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.sound.AbstractNsfSound;


/**
 * Audio Synthesizer
 *
 * @author Zdream
 * @since v0.2.1
 */
public interface ISoundMixer extends IResetable, INsfChannelCode {

    /**
     * Usually called after important data such as parameters are set.
     */
    default void init() {
        // do nothing
    }

    /*
     * Audio Pipeline
     */

    /**
     * <p>After calling this method, all audio pipelines connected to the sound generator
     * {@link AbstractNsfSound} are disconnected and no longer used.
     * Therefore, all channel numbers of the previously called {@link #allocateChannel(byte)}
     * method are invalid. You need to call the {@link #allocateChannel(byte)} method again
     * to obtain tracks later.
     * </p>
     */
    void detachAll();

    /**
     * <p>Cancel the specified track, delete it, and no longer use it.
     * The corresponding channel identification number will also be recycled.
     * </p>
     *
     * @param id Represents the identification number of the corresponding track
     * @since v0.3.0
     */
    void detach(int id);

    /**
     * <p>Assign a channel and return the unique identifier of the channel at the same time.
     * <p>The mixer is responsible for allocating and managing identification numbers.
     * Users can use the identification number to get the corresponding channel instance
     * through {@link #getMixerChannel(int)}.
     * <p>Replace the allocateChannel method of v0.2.0.
     * </p>
     *
     * @param code channel number, or channel type number. See static members CHANNEL_*
     * @return Represents the identification number of the corresponding track
     * @since v0.3.0
     */
    int allocateChannel(byte code);

    /**
     * <p>Gets the channel instance. If the channel has not been created by calling {@link #allocateChannel(byte)},
     * null is returned.
     * <p>Replaces the original v0.2.0 getMixerChannel(byte) method.
     * </p>
     *
     * @param id channel ID
     * @return The channel instance, or null
     * @since v0.2.3
     */
    IMixerChannel getMixerChannel(int id);

    /**
     * <p>Set the number of input samples for the next frame, or the next frame,
     * corresponding to the track.
     * <p>What the mixer needs to do is to convert the audio data corresponding to
     * the input sampling rate into the data of the output sampling rate.
     * * This method needs to be called before writing data to the {@link IMixerChannel} track,
     * and specifies the current frame, and the audio data with the sampling number that
     * {@link AbstractNsfSound} will input to the track.
     * This requirement arises because the sampling rate of NSF often reaches more than 1.77 million,
     * while the sampling rate of ordinary sampled audio, such as MPEG, is mostly 44.1 kilobytes
     * (44100 Hz). It is necessary to coordinate the mixing of these two types of audio,
     * and it is necessary to know their input sampling rates or related data in advance.
     * <p>If <code>inSample</code> is set to 0 or a negative number,
     * or this method is not called, the global input sample count is used by default.
     * </p>
     *
     * @param id       channel ID
     * @param inSample The number of input samples for each frame, for the next frame and all subsequent frames.
     *                 <br>Once this value is set, it will not change when rendering subsequent frames until it is set again.
     *                 <br>If set to 0 or a negative number, it means clearing the last input sampling number setting,
     *                 and the system will use the global input sampling number to replace the input sampling number of this track.
     * @since v0.3.0
     */
    default void setInSample(int id, int inSample) {
    }

    /**
     * Called before enabling the mixer on each frame
     */
    void readyBuffer();

    /**
     * End the frame. Call before {@link #readBuffer(short[], int, int)}
     *
     * @return Returns the number of audio samples (in mono)
     */
    int finishBuffer();

    /**
     * The interface for the outside world to obtain audio data. The audio data will fill the buf array.
     *
     * @param buf    An array for holding audio data
     * @param offset
     * @param length
     * @return
     */
    int readBuffer(short[] buf, int offset, int length);

    /*
     * User Action
     */

    /**
     * @return Mixer operation class
     * @since v0.2.10
     */
    default IMixerHandler getHandler() {
        return null;
    }

    /**
     * Set the volume of a track
     *
     * @param id    channel ID
     * @param level volume. range [0, 1.0f]
     * @since v0.2.3
     */
    default void setLevel(int id, float level) {
        IMixerChannel ch = getMixerChannel(id);
        if (ch != null) {
            ch.setLevel(level);
        }
    }

    /**
     * Get the volume of a track
     *
     * @param id channel ID
     * @return volume. range [0, 1.0f]
     * @throws NullPointerException When there is no channel corresponding to <code>code</code>
     * @since v0.2.3
     */
    default float getLevel(int id) throws NullPointerException {
        return getMixerChannel(id).getLevel();
    }
}
