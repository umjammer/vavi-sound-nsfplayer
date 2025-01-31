package zdream.nsfplayer.mixer.interceptor;

import java.util.Arrays;


/**
 * Echo constructor. Need to get the sampling rate (samples/second*track)
 *
 * @author Zdream
 * @version v0.2.3
 */
public class EchoUnit implements ISoundInterceptor {

    protected int rate;
    protected final int[] echoBuf = new int[1 << 17];
    protected final int[] h = new int[32];
    protected int eidx, edelay;
    final Filter lpf = new Filter();
    final DCFilter hpf = new DCFilter();

    public EchoUnit() {
        reset();
    }

    @Override
    public int execute(int value, int time) {
        int buf = 0;

        int tmp = eidx;
        for (int i = 0; i < 16; i++) {
            echoBuf[tmp & ((1 << 17) - 1)] += (value * h[i]) >> 8;
            tmp += edelay;
        }

        buf = echoBuf[eidx];
        buf = lpf.execute(buf, time);
        buf = hpf.execute(buf, time);
        echoBuf[eidx] = 0;
        eidx = (eidx + 1) & ((1 << 17) - 1);

        value += buf;

        return value;
    }

    /*
     * Parameter settings
     */

    @Override
    public void reset() {
        int[] hdef = new int[] {0, 0, 0, 0, 64, 32, 16, 8, 32, 16, 8, 4, 16, 8, 4, 2,};
        eidx = 0;
        Arrays.fill(echoBuf, 0);
        for (int i = 0; i < 16; i++) {
            h[i] = hdef[i];
        }
        lpf.setParam(4700, 100);
        lpf.reset();
        hpf.setParam(270, 100);
        hpf.reset();
    }

    /**
     * @param rate Sampling rate
     */
    public final void setRate(double rate) {
        edelay = ((int) rate) / 16;
        lpf.setRate(rate);
        hpf.setRate(rate);
    }

    /*
     * Open state
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
