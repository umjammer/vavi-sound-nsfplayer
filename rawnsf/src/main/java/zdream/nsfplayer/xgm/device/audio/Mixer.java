package zdream.nsfplayer.xgm.device.audio;

import java.util.ArrayList;
import java.util.List;

import zdream.nsfplayer.xgm.device.IRenderable0;


public class Mixer implements IRenderable0 {

    /**
     * When a song is about to end, it will enter the fade-out stage. This parameter will record the number of samples the song has played since the beginning of the fade-out.<br>
     * If this parameter is 0, it means that the song has not entered the fade-out stage.
     */
    int fadePos;

    /**
     * Indicates the total time required for fade-out (number of samples).<br>
     * When the song has not entered the fade-out stage, this parameter is 1. Generally an invalid value;
     * When the song enters the fade-out stage, this value will calculate the real effective value.
     */
    int fadeEnd;

    final List<IRenderable0> dlist = new ArrayList<>();

    public Mixer() {
        reset();
    }

    public final void reset() {
        fadePos = 0;
        fadeEnd = 1;
    }

    public final void detachAll() {
        dlist.clear();
    }

    public final void attach(IRenderable0 dev) {
        dlist.add(dev);
    }

    public final boolean isFadeEnd() {
        return (fadePos >= fadeEnd);
    }

    public final boolean isFading() {
        return (fadePos > 0);
    }

    public void fadeStart(double rate, int fadeInMs) {
        if (fadeInMs != 0) {
            double samples = (double) fadeInMs * rate / 1000.0;
            if (samples < Integer.MAX_VALUE) {
                fadeEnd = (int) (samples);
            } else {
                fadeEnd = Integer.MAX_VALUE;
            }
        } else {
            fadeEnd = 1;
        }
        fadePos = 1; // begin fade
    }

    public final void skip(int length) {
        if (fadePos > 0) {
            if (fadePos < fadeEnd)
                ++fadePos;
            else
                fadePos = fadeEnd;
        }
    }

    @Override
    public void tick(int clocks) {
        for (IRenderable0 r : dlist) {
            r.tick(clocks);
        }
    }

    @Override
    public int render(int[] bs) {
        int[] tmp = new int[2];
        bs[1] = bs[0] = 0;

        for (IRenderable0 r : dlist) {
            r.render(tmp);
            bs[0] += tmp[0];
            bs[1] += tmp[1];
        }

        if (fadePos > 0) {
            double fadeAmount = (double) (fadeEnd - fadePos) / (double) (fadeEnd);
            bs[0] = (int) (fadeAmount * bs[0]);
            bs[1] = (int) (fadeAmount * bs[1]);

            if (fadePos < fadeEnd)
                ++fadePos;
            else
                fadePos = fadeEnd;
        }
        return 2;
    }

}
