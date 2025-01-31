package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * <p>Volume accumulation status
 * <p>When a channel has this state, the volume will change over time.
 * It will get quieter or louder depending on whether delta is negative or positive.
 * If delta = 0, it means the volume will not change.
 * But if the accumulated volume is not 0, it means the actual volume of the whole track
 * will still be different from the original volume.
 * The difference is determined by the accum value.
 * <p>Whenever a volume reset effect {@link VolumeEffect} is triggered in a track,
 * the accumulated volume is reset to zero.
 * </p>
 *
 * @author Zdream
 * @version 0.2.2
 * <br>Originally, the positioning of this state was just to modify the state of the volume effect over time,
 * and only completed the effect of Axx.
 * From this version, all volume accumulation effects and accumulated value storage are completed by it.
 * @see VolumeSlideEffect
 * @since 0.2.1
 */
public class VolumeAccumulateState implements IFtmState {

    public static final String NAME = "Volume Accumulated";

    /**
     * The amount of change per frame.
     * Except for VRC7 tracks, if the delta is greater than zero, the channel volume will increase.
     * VRC7 tracks have the opposite effect.
     */
    public int delta;

    /**
     * Cumulative volume. The first frame of the state trigger affects the volume of 0,
     * the second frame affects one delta, the third frame affects two deltas, and so on.
     */
    private int accum = 0;

    public VolumeAccumulateState(int slide) {
        delta = slide;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
        if (delta == 0 && accum == 0) {
            runtime.channels.get(channelCode).removeState(this);
            return;
        }

        runtime.channels.get(channelCode).addCurrentVolume(accum);

        accum += delta;
    }

    /**
     * When the external volume is reset, call this method to reset the cumulative volume.
     */
    public void resetAccumulation() {
        accum = 0;
    }

    @Override
    public String toString() {
        return NAME + ":" + delta;
    }
}
