package zdream.nsfplayer.ftm.executor.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * <p>Portamento effect, 1xx up, 2xx down
 * </p>
 *
 * @author Zdream
 * @see PitchAccumulateState
 * @since v0.2.2
 */
public class PortamentoEffect implements IFtmEffect {

    public final int delta;

    private PortamentoEffect(int slide) {
        this.delta = slide;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.PORTA;
    }

    /**
     * Create a glissando effect that modifies the pitch over time
     *
     * @param delta Amount of change. The number of wavelengths of pitch change per frame (usually 1/60 s), no range
     *              <br>If it is a positive number, the wavelength increases over time and the sound becomes lower.
     *              <br>If it is a negative number, the wavelength will decrease over time and the sound will become higher.
     *              <br>0, the pitch does not change over time, and the portamento effect originally applied to the channel can also be disabled
     *              (the 3xx destination portamento effect cannot be disabled);
     * @return Effect Examples
     * @throws IllegalArgumentException When the change <code>delta</code> is not within the specified range
     */
    public static PortamentoEffect of(int delta) throws IllegalArgumentException {
        return new PortamentoEffect(delta);
    }

    /**
     * @return Is it the effect of the wavelength increasing and the sound getting lower?
     */
    public boolean slideUp() {
        return delta > 0;
    }

    /**
     * @return Is it the effect of the wavelength getting smaller and the sound getting higher?
     */
    public boolean slideDown() {
        return delta < 0;
    }

    /**
     * @return Is there a glissando effect?
     */
    public boolean slide() {
        return delta != 0;
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        /*
         * Here we need to ensure that a channel has at most one state that changes the volume over time.
         */
        HashSet<IFtmState> set = ch.filterStates(PitchAccumulateState.NAME);
        PitchAccumulateState s = null;

        if (!set.isEmpty()) {
            s = (PitchAccumulateState) set.iterator().next();
            s.delta = delta; // But it does not reset the cumulative amount
        } else if (delta != 0) {
            s = new PitchAccumulateState(delta);
            ch.addState(s);
        }
    }

    @Override
    public String toString() {
        return "Portamento:" + delta;
    }

    /**
     * Lower priority than {@link PitchEffect} and {@link VibratoEffect}
     */
    @Override
    public final int priority() {
        return -3;
    }
}
