package zdream.nsfplayer.mixer;

import java.util.Arrays;


/**
 * <p>Split a multi-track input source into multiple mono outputs.
 * It is a solution for multi-track output when combined with {@link SplitMixerChannel}
 * </p>
 *
 * @author Zdream
 * @see SplitMixerChannel
 * @since v0.3.0
 */
public class SplitTrackChannel implements ITrackChannel {

    public SplitTrackChannel() {
        setTrackCount(1);
    }

    /*
     * Channel Track
     */

    private int trackCount;
    private IMixerChannel[] outs;

    public int getTrackCount() {
        return trackCount;
    }

    /**
     * Set the number of tracks
     *
     * @param trackCount The number of trackss. Must be greater than or equal to 1
     * @throws IllegalArgumentException When the number of trackss is less than 1
     */
    public void setTrackCount(int trackCount) {
        if (trackCount < 1) {
            throw new IllegalArgumentException("Number of tracks: " + trackCount + " must be greater than 1");
        }

        if (outs != null) {
            outs = Arrays.copyOf(outs, trackCount);
        } else {
            outs = new IMixerChannel[trackCount];
        }

        if (levels != null) {
            int oldLen = this.trackCount;
            levels = Arrays.copyOf(levels, trackCount);
            if (trackCount > oldLen) {
                Arrays.fill(levels, oldLen, trackCount, 1.0f);
            }
        } else {
            levels = new float[trackCount];
        }

        this.trackCount = trackCount;
    }

    //
    // volume
    //

    /**
     * The total track volume value.
     */
    private float masterLevel;
    private float[] levels;

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
        for (IMixerChannel ch : outs) {
            if (ch != null) {
                ch.mix(value, time);
            }
        }
    }

    @Override
    public void reset() {
        for (IMixerChannel ch : outs) {
            if (ch != null) {
                ch.reset();
            }
        }
    }

    @Override
    public void setTrackLevel(float level, int track) {
        this.levels[track] = level;
    }

    @Override
    public float getTrackLevel(int track) {
        return this.levels[track];
    }
}
