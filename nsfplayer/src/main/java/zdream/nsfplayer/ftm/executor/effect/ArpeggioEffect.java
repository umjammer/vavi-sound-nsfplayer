package zdream.nsfplayer.ftm.executor.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * Arpeggio effect, 0xy
 *
 * @author Zdream
 * @see ArpeggioState
 * @since 0.2.2
 */
public class ArpeggioEffect implements IFtmEffect {

    /**
     * Two arpeggios increase the original pitch by several semitones
     */
    public final int x, y;

    private ArpeggioEffect(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.ARPEGGIO;
    }

    /**
     * Create an arpeggio effect
     *
     * @param x range [0, 15]
     * @param y range [0, 15]
     * @return Effect Examples
     * @throws IllegalArgumentException When the timbre value <code>x</code> or <code>y</code> is not
     *                                  within the specified range
     */
    public static ArpeggioEffect of(int x, int y) throws IllegalArgumentException {
        if (x < 0 || x > 15) {
            throw new IllegalArgumentException("x must be an integer value between 0 and 15");
        }
        if (y < 0 || y > 15) {
            throw new IllegalArgumentException("y must be an integer value between 0 and 15");
        }
        return new ArpeggioEffect(x, y);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        // There is only one delay mute effect per track.
        HashSet<IFtmState> set = ch.filterStates(ArpeggioState.NAME);
        ArpeggioState s = null;

        if (!set.isEmpty()) {
            s = (ArpeggioState) set.iterator().next();

            if (x == 0 && y == 0) {
                ch.removeState(s);
            } else {
                s.x = x;
                s.y = y;
            }
        } else {
            if (x != 0 || y != 0) {
                s = new ArpeggioState(x, y);
                ch.addState(s);
            }
        }
    }

    @Override
    public String toString() {
        return "Arpeggio:" + x + "&" + y;
    }

    @Override
    public int priority() {
        return -3;
    }
}
