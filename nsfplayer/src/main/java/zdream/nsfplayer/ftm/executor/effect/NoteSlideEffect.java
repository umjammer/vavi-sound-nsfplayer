package zdream.nsfplayer.ftm.executor.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;

import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_2A03_NOISE;


/**
 * <p>The effect of sliding up or down to a specific note over time, Qxy, Rxy
 * <p>Only one Qxy Rxy 3xx effect is allowed to exist on a channel at the same time,
 * and the later one takes priority.
 * If the three effects appear in the same frame, 3xx is triggered first,
 * and then the Qxy Rxy effect is overwritten.
 * <br>Once the Qxy Rxy effect appears, if there is a 3xx state,
 * the 3xx state is directly replaced.
 * </p>
 *
 * @author Zdream
 * @see NoteSlideState
 * @since v0.2.2
 */
public class NoteSlideEffect implements IFtmEffect {

    /**
     * The difference between the target note and the current note
     */
    public final int delta;

    /**
     * Sliding speed
     */
    public final int speed;

    private NoteSlideEffect(int slide, int speed) {
        this.delta = slide;
        this.speed = speed;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.SLIDE;
    }

    /**
     * Create an effect that slides up or down to a specified note over time
     *
     * @param delta Amount of change. The number of notes to slide when the slide effect stops.
     *              <br>A positive number indicates that the note slides upward;
     *              <br>A negative number means the note slides downward;
     *              <br>0, indicating that the sliding speed is modified for the last unfinished note sliding effect;
     * @param speed The speed at which the note slides. The number of cycles per frame that the note changes. This value must be a positive number
     * @return Effect Examples
     * @throws IllegalArgumentException When the sliding speed <code>speed</code> is not within the specified range
     */
    public static NoteSlideEffect of(int delta, int speed) throws IllegalArgumentException {
        if (speed <= 0) {
            throw new IllegalArgumentException("The speed of the note slide must be a positive number");
        }
        return new NoteSlideEffect(delta, speed);
    }

    /**
     * @return Is it the effect of the note sliding upwards?
     */
    public boolean slideUp() {
        return delta > 0;
    }

    /**
     * @return Is it the effect of the note sliding down?
     */
    public boolean slideDown() {
        return delta < 0;
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);
        int snode = ch.getMasterNote();
        int dnode = snode + this.delta;

        // The range of note is [1, 96]
        if (channelCode == CHANNEL_2A03_NOISE) {
            if (dnode > 16) {
                dnode = 16;
            } else if (dnode < 1) {
                dnode = 1;
            }
        } else {
            if (dnode > 96) {
                dnode = 96;
            } else if (dnode < 1) {
                dnode = 1;
            }
        }

        int pitchDelta;

        if (snode == dnode) {
            // No change
            pitchDelta = 0;
        } else {
            ch.setMasterNoteWithoutUpdate(dnode);
            pitchDelta = (channelCode == CHANNEL_2A03_NOISE) ?
                    snode - dnode : ch.periodTable(snode) - ch.periodTable(dnode);
        }

        // If there is an unfinished note slide effect, modify its speed here
        HashSet<IFtmState> set = ch.filterStates(NoteSlideState.NAME);
        NoteSlideState s = null;

        if (!set.isEmpty()) {
            s = (NoteSlideState) set.iterator().next();
            if (s instanceof PortamentoOnState || ch.isNoteUpdated()) {
                ch.removeState(s);
                s = new NoteSlideState(speed, pitchDelta);
                ch.addState(s);
            } else {
                s.delta += pitchDelta;
                s.speed = speed;
            }
        } else {
            s = new NoteSlideState(speed, pitchDelta);
            ch.addState(s);
        }
    }

    @Override
    public String toString() {
        return "NoteSlide:" + delta + "#" + speed;
    }

    /**
     * Takes precedence over note-modifying effects {@link NoteEffect} and {@link NoiseEffect}
     */
    @Override
    public final int priority() {
        return -1;
    }
}
