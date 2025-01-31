package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * <p>Pitch accumulation status
 * <p>When a channel has this state, the pitch will change over time.
 * Whether it goes higher or lower is determined by whether {@link #delta} is negative or positive.
 * Because delta refers to the change in wavelength, if delta is positive,
 * the wavelength will continue to increase and the pitch will continue to decrease.;
 * If delta is negative, the wavelength will continue to decrease and the pitch will continue to increase.
 * <p>If delta = 0, it means the pitch will not change.
 * But if the accumulation is not 0, it means the actual pitch of the whole track
 * will still be different from the original pitch.
 * The difference is determined by the accum value.
 * <p>Whenever a pitch-modifying effect {@link NoteEffect}
 * ({@link NoiseEffect} for Noise tracks) is triggered in a track,
 * the accumulated value is reset to zero.
 * </p>
 *
 * <br>
 * <p><b>Supplementary Rules</b>
 * <p>If Qxy and Rxy have an effect in this frame, delta is reset to 0
 * </p>
 *
 * @author Zdream
 * @see PortamentoEffect
 * @since 0.2.2
 */
public class PitchAccumulateState implements IFtmState {

    public static final String NAME = "Pitch Accumulated";

    /**
     * The wavelength change per frame
     */
    public int delta;

    /**
     * Cumulative volume. The first frame of the state trigger affects the volume of 0,
     * the second frame affects one delta, the third frame affects two deltas, and so on.
     */
    private int accum = 0;

    public PitchAccumulateState(int slide) {
        delta = slide;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        // Whenever a note effect or a sound effect that modifies the pitch or scale is triggered in the track,
        // the accumulated value is reset to zero.
        if (runtime.effects.get(channelCode).get(FtmEffectType.NOTE) != null) {
            resetAccumulation();
        }

        // Supplementary rule, if Qxy and Rxy have an effect in this frame, delta is reset to 0
        if (!ch.filterStates(NoteSlideState.NAME).isEmpty()) {
            delta = 0;
        }

        if (delta == 0 && accum == 0) {
            ch.removeState(this);
            return;
        }

        accum += delta;

        ch.addCurrentPeriod(accum);
    }

    /**
     * When the external pitch is reset, call this method to reset the cumulative amount.
     * Here the {@link #trigger(byte, FamiTrackerRuntime)} method will call
     */
    public void resetAccumulation() {
        accum = 0;
    }

    @Override
    public String toString() {
        return NAME + ":" + delta;
    }

    @Override
    public int priority() {
        return -3;
    }
}
