package zdream.nsfplayer.mixer;

import java.util.ArrayList;
import java.util.List;

import zdream.nsfplayer.core.IResetable;

import static java.util.Objects.requireNonNull;


/**
 * <p>Split audio tracks into multiple outputs.
 * <p>Split the data of an input source into multiple output paths.
 * It is a solution for multi-channel output when combined with {@link SplitTrackChannel}
 * </p>
 *
 * @author Zdream
 * @see SplitTrackChannel
 * @since v0.3.0
 */
public class SplitMixerChannel implements IMixerChannel {

    /**
     * The total channel volume value.
     */
    private float masterLevel;

    /*
     * Lower level track
     */

    /**
     * The collection of all subordinate tracks
     */
    private final ArrayList<IMixerChannel> channels = new ArrayList<>();

    /**
     * Adding Sub-Tracks
     *
     * @param channel Lower Track
     * @throws NullPointerException When Lower channel channel = null
     */
    public void addOutputChannel(IMixerChannel channel) {
        requireNonNull(channel, "channel = null");
        channels.add(channel);
    }

    /**
     * Delete Lower Track
     *
     * @param channel
     * @return {@link List#remove(Object)}
     */
    public boolean removeOutputChannel(IMixerChannel channel) {
        return channels.remove(channel);
    }

    /**
     * Clear Lower Track
     */
    public void clearOutputChannel() {
        channels.clear();
    }

    /*
     * Public interface
     */

    @Override
    public void reset() {
        channels.forEach(IResetable::reset);
    }

    @Override
    public void setLevel(float level) {
        if (level > 1) {
            level = 1;
        } else if (level < 0) {
            level = 0;
        }
        this.masterLevel = level;
    }

    @Override
    public float getLevel() {
        return masterLevel;
    }

    @Override
    public void mix(int value, int time) {
        channels.forEach(c -> c.mix(value, time));
    }
}
