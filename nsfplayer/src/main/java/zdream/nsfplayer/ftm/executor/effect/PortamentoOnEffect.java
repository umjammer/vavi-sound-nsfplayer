package zdream.nsfplayer.ftm.executor.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * <p>Continuous portamento effect, 3xx
 * </p>
 *
 * @author Zdream
 * @see PortamentoOnState
 * @since v0.2.2
 */
public class PortamentoOnEffect implements IFtmEffect {

    /**
     * Sliding speed
     */
    public final int speed;

    private PortamentoOnEffect(int speed) {
        this.speed = speed;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.PORTAMENTO;
    }

    /**
     * Create a continuous glissando effect
     *
     * @param speed The speed at which the note slides. The number of cycles per frame that the note changes. This value must be non-negative.
     * @return Effect Examples
     * @throws IllegalArgumentException When the sliding speed <code>speed</code> is not within the specified range
     */
    public static PortamentoOnEffect of(int speed) throws IllegalArgumentException {
        if (speed < 0) {
            throw new IllegalArgumentException("The speed of the note slide must be a positive number");
        }
        return new PortamentoOnEffect(speed);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        /*
         * Here we need to ensure that there is at most one NoteSlideState instance per track.
         */
        HashSet<IFtmState> set = ch.filterStates(PortamentoOnState.NAME);
        NoteSlideState s = null;
        int baseNote = ch.getMasterNote();

        if (!set.isEmpty()) {
            s = (NoteSlideState) set.iterator().next();

            if (s instanceof PortamentoOnState) {
                // If the current frame, Note is modified directly,
                if (runtime.effects.get(channelCode).containsKey(FtmEffectType.NOTE) && speed == 0) {
                    ch.removeState(s);
                } else {
                    // Just modify the speed, regardless of whether the speed is equal to 0
                    s.speed = this.speed;
                }
            } else {
                int delta = s.delta;
                ch.removeState(s);

                s = new PortamentoOnState(speed, delta, baseNote);
                ch.addState(s);
            }
        } else {
            if (speed != 0) {
                s = new PortamentoOnState(speed, baseNote);
                ch.addState(s);
            }
        }
    }

    @Override
    public String toString() {
        return "PortamenOn:" + speed;
    }

    /**
     * Executed earlier than {@link NoteEffect} and {@link NoiseEffect}
     */
    @Override
    public int priority() {
        return 5;
    }
}
