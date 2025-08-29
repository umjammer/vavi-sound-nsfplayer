package zdream.nsfplayer.xgm.player;

import zdream.nsfplayer.vcm.ObserverI;
import zdream.nsfplayer.xgm.device.ITrackInfo;


public abstract class Player implements ObserverI {

    protected PlayerConfig config;

    /**
     * Return value of getLoopCount()
     */
    public static final int
            NEVER_LOOP = 0,
            INFINITE_LOOP = 1;

    /**
     * Attach an object to PlayerConfig
     * <p>
     * Observer of PlayerConfig object, this object will be registered automatically.
     * </P>
     *
     * @param pc The PlayerConfig object to attach
     */
    public void setConfig(PlayerConfig pc) {
        config = pc;
        config.attachObserver(this);
    }

    public PlayerConfig getConfig() {
        return config;
    }

    @Override
    public void notify(int v) {
    }

    /**
     * Load song data to be played
     * <p>
     * The Player object does not store a copy of the performance data internally
     * * Therefore, do not let the performance data disappear when playing the Player object
     * * The generation and disappearance of performance data are managed outside the Player object
     * </P>
     *
     * @param data Song data to be played
     * @return true on success, false on failure
     */
    public abstract boolean load(SoundData sdat);

    /**
     * Initialization
     */
    public abstract void reset();

    /**
     * Set playback speed
     */
    public abstract void setPlayFreq(double rate);

    /**
     * Number of channels to output.
     */
    public abstract void setChannels(int channels);

    /**
     * Render audio data
     * <p>
     * The buffer size needs samples * sizeof(INT16) [C++], here still use byte[]
     * </P>
     *
     * @param buf    The buffer is used to store rendered data
     * @param offset Where should the buf array start reading from
     * @param size   Number of samples
     *               Even if 0 is given, it should not hang
     * @return Actual number of samples produced
     */
    public abstract int render(byte[] buf, int offset, int size);

    /**
     * Fade out
     */
    public abstract void fadeOut(int fade_in_ms);

    /**
     * How much data is skipped in the rendering of audio data
     *
     * @param samples Number of samples to skip
     *                Even if 0 is given, it should not hang
     * @return Actual number of samples skipped
     */
    public abstract int skip(int samples);

    /**
     * Whether the performance has stopped
     *
     * @return If the performance is stopped, return true. If it is playing, return false.
     */
    public abstract boolean isStopped();

    /**
     * Return the number of loop performances
     * <p>
     * Take the first performance as the first loop.
     * </P>
     *
     * @return 演奏次数
     * In the case of NEVER_LOOP, it is non-looping data, and in the case of INFINITE_LOOP, it is infinitely looping data.
     */
    public int getLoopCount() {
        return NEVER_LOOP;
    }

    public String getTitleString() {
        return "UNKNOWN";
    }

    public int getLength() {
        return 5 * 60 * 1000;
    }

    /**
     * Get device information at time id, number is id time == -1 returns current device information
     */
    public ITrackInfo getInfo(int time_in_ms, int device_id) {
        return null;
    }

}
