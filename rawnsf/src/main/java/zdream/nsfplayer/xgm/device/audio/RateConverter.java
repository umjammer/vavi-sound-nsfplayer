package zdream.nsfplayer.xgm.device.audio;

import zdream.nsfplayer.xgm.device.IRenderable0;


/**
 * <p>Sampler / Rate Converter
 * <p>When sampling, it will perform multiple samplings on the connected audio equipment,
 * The obtained sampling results are finally merged into one sample according to the weighted average and handed over to the superior.
 *
 * @author Zdream
 */
public class RateConverter implements IRenderable0 {

    protected IRenderable0 target;
    protected double clock = 0, rate = 0;
    protected int mult = 0; // Sampling magnification (odd number)

    /**
     * <p>
     * <p>Originally [128][2]
     */
    protected int[][] tap;

    /**
     * To merge multiple sub-samples into one sample, you need to calculate the weight of each sub-sample.
     */
    protected double[] hr; // H(z), for each
    protected int clocks = 0; // clocks pending Tick execution
    protected SimpleFIR fir;

    public final void attach(IRenderable0 t) {
        target = t;
    }

    public final void reset() {
        clocks = 0; // cancel any pending ticks

        if (clock > 0 && rate > 0) {
            mult = (int) (clock / rate);
            if (mult < 2)
                return;

            // generate resampling window
            hr = new double[mult + 1];

            hr[0] = FilterTools.window(0, mult);
            double gain = hr[0];
            for (int i = 1; i <= mult; i++) {
                hr[i] = FilterTools.window(i, mult);
                gain += hr[i] * 2;
            }

            // normalize window
            for (int i = 0; i <= mult; i++) {
                hr[i] /= gain;
            }

            int length = mult * 2 + 1;
            tap = new int[length][2];
            for (int i = 0; i < length; i++)
                tap[i][0] = tap[i][1] = 0;
        }
    }

    /**
     * The magnification is an odd multiple
     *
     * @param clock
     */
    public final void setClock(double clock) {
        this.clock = clock;
    }

    public final void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public void tick(int clocks) {
        this.clocks += clocks;
    }

    @Override
    public int render(int[] bs) {
        return fastRender(bs);
    }

    /**
     * The initial values are all invalid
     *
     * @param bs
     * @return
     */
    public final int fastRender(int[] bs) {
        double[] out = new double[2];

        for (int i = 0; i <= mult; i++) {
            tap[i][0] = tap[i + mult][0];
            tap[i][1] = tap[i + mult][1];
        }

        // divide clock ticks among samples evenly
        int mclocks = 0;
        for (int i = 1; i <= mult; i++) {
            mclocks += clocks;
            if (mclocks >= mult) {
                int sub_clocks = mclocks / mult;
                target.tick(sub_clocks);
                mclocks -= (sub_clocks * mult);
            }
            target.render(tap[mult + i]);
        }
        assert (mclocks == 0); // all clocks must be used
        clocks = 0;

        out[0] = hr[0] * tap[mult][0];
        out[1] = hr[0] * tap[mult][1];

        for (int i = 1; i <= mult; i++) {
            out[0] += hr[i] * (tap[mult + i][0] + tap[mult - i][0]);
            out[1] += hr[i] * (tap[mult + i][1] + tap[mult - i][1]);
        }

        bs[0] = (int) out[0];
        bs[1] = (int) out[1];

        return 2;
    }

}
