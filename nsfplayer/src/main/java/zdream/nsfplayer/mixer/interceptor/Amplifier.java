package zdream.nsfplayer.mixer.interceptor;

/**
 * Audio amplifier. Additional functions include: volume will be limited to a certain range
 *
 * @author Zdream
 * @since v0.2.3
 */
public class Amplifier implements ISoundInterceptor {

    private int
            volume = 128,
            threshold = 32767,
            weight = -1;

    public Amplifier() {
        setCompress(100, -1);
    }

    protected int compress(int d) {
        if (weight < 0) {
            return d;
        }

        if (d > threshold)
            return threshold;
        else if (d < -threshold)
            return -threshold;
        else
            return d;
    }

    public void setVolume(int v) {
        this.volume = v;
        this.setCompress(threshold, weight);
    }

    public int getVolume() {
        return volume;
    }

    public void setCompress(int t, int w) {
        threshold = 32767 * t / 100;
        if (threshold < 32768)
            weight = 0;
        else
            weight = -1;
    }

    @Override
    public void reset() {

    }

    @Override
    public int execute(int value, int time) {
        return compress((value * volume) / 16);
    }

    /*
     * Open Status
     */

    boolean enable = true;

    @Override
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }
}
