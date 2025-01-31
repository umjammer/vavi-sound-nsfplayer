package zdream.nsfplayer.ftm.executor.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * Delayed mute effect, Sxx
 *
 * @author Zdream
 * @see DelayCutState
 * @since 0.2.2
 */
public class CutEffect implements IFtmEffect {

    /**
     * Trigger a mute effect after a few frames
     */
    public final int frames;

    private CutEffect(int frames) {
        this.frames = frames;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.NOTE_CUT;
    }

    /**
     * Trigger a mute effect after a few frames
     *
     * @param frames Number of frames after which the mute effect is triggered. Must be in the range [0, 255].
     * @return Effect Examples
     * @throws IllegalArgumentException When <code>frames</code> is not within the specified range
     */
    public static CutEffect of(int frames) throws IllegalArgumentException {
        if (frames > 255 || frames < 0) {
            throw new IllegalArgumentException("frames must be an integer value between 0 and 255");
        }
        return new CutEffect(frames);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        // There is only one delay mute effect per track.
        HashSet<IFtmState> set = ch.filterStates(DelayCutState.NAME);
        DelayCutState s = null;

        if (!set.isEmpty()) {
            s = (DelayCutState) set.iterator().next();

            if (frames == 0) {
                ch.removeState(s);
                ch.doHalt();
            } else {
                s.frames = frames;
            }
        } else {
            if (frames == 0) {
                ch.doHalt();
            } else {
                s = new DelayCutState(frames);
                ch.addState(s);
            }
        }
    }

    @Override
    public String toString() {
        return "Cut:" + frames;
    }

    @Override
    public int priority() {
        return -4;
    }
}
