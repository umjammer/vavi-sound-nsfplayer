package zdream.nsfplayer.mixer.xgm;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.IResetable;


/**
 * Merge Tracks
 *
 * @author Zdream
 * @since v0.2.3
 */
public interface IXgmMultiChannelMixer extends INsfChannelCode, IResetable {

    /**
     * <p>Get the remaining audio track instances of the corresponding type.
     * <p>If the merged track supports the type, and there are free, unused tracks left,
     * return the audio pipe.
     * </p>
     *
     * @param type The track type.
     * @return An audio track instance.
     * If the merged track does not support tracks of this type,
     * or there are no tracks of this type left, null is returned.
     */
    AbstractXgmAudioChannel getRemainAudioChannel(byte type);

    /**
     * Method called before rendering
     */
    void beforeRender();

    /**
     * Sampling data submission
     *
     * @param index
     * @return The sampled value
     */
    int render(int index);
}
