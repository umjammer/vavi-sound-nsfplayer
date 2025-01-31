package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * <p>Slide up or down over time to the state of the specified note effect, Qxy, Rxy
 * </p>
 *
 * <br>
 * <p><b>Supplementary Rules</b>
 * <p>If the frame has a {@link NoteEffect} or {@link NoiseEffect} effect triggered,
 * and this is not the first frame where the state is established, delete the state
 * </p>
 *
 * @author Zdream
 * @see NoteSlideEffect
 * @since 0.2.2
 */
public class NoteSlideState implements IFtmState {

    public static final String NAME = "Note Slide";

    /**
     * The wavelength value of the note sliding up or down in each frame.
     * This value must be a positive number.
     */
    public int speed;

    /**
     * <p>The difference from the target wavelength value.
     * When the state is not triggered, the final sliding up or down target is set,
     * and the wavelength value of the speed unit is slid towards the target each frame.
     * In a certain frame, the value of delta indicates the wavelength value of the difference
     * from the target. It can be positive or negative.
     *
     * <p>Note that this is the wavelength value,
     * so if delta > 0, it means the following note slides upward;
     * If delta < 0, it means the following note slides down;
     */
    public int delta;

    /**
     * Record whether this is the first frame established in this state.
     */
    private boolean startFrame = true;

    public NoteSlideState(int speed, int delta) {
        super();
        this.speed = speed;
        this.delta = delta;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        // If the tonic key of the frame changes and it is not the first frame
        if (!startFrame && ch.isNoteUpdated()) {
            // Delete this status
            ch.removeState(this);
            return;
        }

        if (delta > 0) {
            delta -= speed;
            if (delta < 0) {
                delta = 0;
            }
        } else {
            delta += speed;
            if (delta > 0) {
                delta = 0;
            }
        }

        if (delta == 0) {
            // Delete this status
            ch.removeState(this);
            return;
        }

        ch.addCurrentPeriod(delta);
        startFrame = false;
    }

    @Override
    public String toString() {
        return NAME + ":" + speed;
    }
}
