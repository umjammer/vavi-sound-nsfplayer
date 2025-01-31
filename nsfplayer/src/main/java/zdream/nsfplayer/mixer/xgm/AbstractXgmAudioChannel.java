package zdream.nsfplayer.mixer.xgm;

import zdream.nsfplayer.mixer.IMixerChannel;


/**
 * Abstract Xgm mixer track
 *
 * @author Zdream
 * @since v0.2.10
 */
public abstract class AbstractXgmAudioChannel implements IMixerChannel {

    /**
     * Volume. Although the volume is stored here, the volume is not actually
     * implemented in this class, but in IXgmMultiChannelMixer
     */
    protected float level = 1.0f;

    @Override
    public void setLevel(float level) {
        this.level = level;
    }

    @Override
    public float getLevel() {
        return level;
    }

    /*
     * XGM Mixer
     */

    /**
     * Called after the write operation is completed and before the read operation begins
     */
    protected abstract void beforeSubmit();

    /**
     * Called before each frame write operation, allowing the pipeline to check whether
     * the capacity is appropriate and modify it
     *
     * @param inSample  The number of input samples, i.e. the capacity. The general unit is clock
     * @param outSample The number of samples output
     */
    protected abstract void checkCapacity(int inSample, int outSample);

    /**
     * Get the reading at a certain moment.
     *
     * @param index time, the range is [0, the number of output samples of the current frame)
     * @return
     * @since v0.3.0
     */
    protected abstract float read(int index);
}
