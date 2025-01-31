package zdream.nsfplayer.sound;

import zdream.nsfplayer.core.IEnable;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.mixer.IMixerChannel;


/**
 * Superclass for audio generators
 *
 * @author Zdream
 * @since 0.2.1
 */
public abstract class AbstractNsfSound implements IResetable, IEnable {

    /**
     * Pipes to Synthesizer Buffer
     */
    protected IMixerChannel out;

    /**
     * Record the number of clocks that have been rendered.
     * At the end of each frame, call {@link #endFrame()} to reset to 0.
     */
    protected int time;

    /**
     * Flag bit for enable or disable, 4015 Control bit
     */
    private boolean enable = true;

    /**
     * Flag that controls whether audio data is sent to the synthesizer pipeline.
     * If set to true, no data will be sent to the synthesizer pipeline {@link #out}.
     * will not be reset by {@link #reset()}.
     */
    private boolean muted = false;

    /**
     * Called at the end of each frame
     */
    public void endFrame() {
        time = 0;
    }

    /**
     * @param out {@link #out}
     */
    public void setOut(IMixerChannel out) {
        this.out = out;
    }

    /**
     * @return {@link #enable}
     */
    @Override
    public boolean isEnable() {
        return enable;
    }

    /**
     * @param enable {@link #enable}
     */
    @Override
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * @return {@link #muted}
     */
    public boolean isMuted() {
        return muted;
    }

    /**
     * @param muted {@link #muted}
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    @Override
    public void reset() {
        enable = true;
        this.endFrame();
    }

    /**
     * <p>The number of clock cycles to start the operation time.
     * <p>If the speaker is enabled ({@link #enable} == true) it will pass audio
     * data to {@link #out} otherwise it will skip this time.
     * </p>
     *
     * @param time
     */
    public final void process(int time) {
        if (time < 1) {
            return;
        }

        int destTime = this.time + time;
        if (enable) {
            onProcess(time);
        }
        this.time = destTime;
    }

    /**
     * The speaker works, and passes audio-related data to {@link #out}.
     *
     * @param time
     */
    protected abstract void onProcess(int time);

    protected void mix(int value) {
        if (!muted)
            out.mix(value, time);
    }

    /**
     * Passes the audio value at a certain point in time to the mixer.
     *
     * @param value  audio value
     * @param offset The time is this.time + offset
     * @since v0.2.9
     */
    protected void mix(int value, int offset) {
        if (!muted)
            out.mix(value, time + offset);
    }
}
