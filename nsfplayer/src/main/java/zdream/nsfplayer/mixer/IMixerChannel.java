package zdream.nsfplayer.mixer;

import zdream.nsfplayer.core.IResetable;


/**
 * <p>Mixer single receive channel interface
 * </p>
 *
 * @author Zdream
 * @since v0.2
 */
public interface IMixerChannel extends IResetable {

    /**
     * Corrected overall volume value
     *
     * @param level Volume value, [0, 1]
     */
    void setLevel(float level);

    /**
     * Get the overall volume value
     *
     * @return Volume value, [0, 1]
     */
    float getLevel();

    void mix(int value, int time);
}
