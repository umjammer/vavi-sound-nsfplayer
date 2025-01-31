package zdream.nsfplayer.ftm.executor.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * <p>Effect that modifies volume over time, Axx
 * </p>
 *
 * @author Zdream
 * @see VolumeAccumulateState
 * @since 0.2.1
 */
public class VolumeSlideEffect implements IFtmEffect {

    public final int delta;

    protected VolumeSlideEffect(int slide) {
        this.delta = slide;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.VOLUME_SLIDE;
    }

    /**
     * Create an effect that modifies the volume over time
     *
     * @param delta Change amount. The volume change per frame (usually 1/60 s), range [-30, 30]
     *              <br>If it is a positive number, the volume will increase over time;
     *              <br>If it is a negative number, the volume will decrease over time;
     *              <br>0, the volume does not change with time, and the effect of modifying the volume over time
     *              that originally acts on the channel can also be disabled;
     * @return Effect Examples
     * @throws IllegalArgumentException When the change <code>delta</code> is not within the specified range
     */
    public static VolumeSlideEffect of(int delta) throws IllegalArgumentException {
        if (delta < -30 || delta > 30) {
            throw new IllegalArgumentException("The volume change must be between -30 and 30");
        }
        return new VolumeSlideEffect(delta);
    }

    /**
     * @return Is it the effect of increasing the volume?
     */
    public boolean slideUp() {
        return delta > 0;
    }

    /**
     * @return Is it the effect of volume reduction?
     */
    public boolean slideDown() {
        return delta < 0;
    }

    /**
     * @return Is it the effect of volume change?
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
        HashSet<IFtmState> set = ch.filterStates(VolumeAccumulateState.NAME);
        VolumeAccumulateState s = null;

        if (!set.isEmpty()) {
            s = (VolumeAccumulateState) set.iterator().next();
            s.delta = delta; // But it does not reset the cumulative amount
        } else if (delta != 0) {
            s = new VolumeAccumulateState(delta);
            ch.addState(s);
        }
    }

    @Override
    public String toString() {
        return "VolumeSlide:" + delta;
    }

    /**
     * Priority is lower than the effect {@link VolumeEffect} that changes the volume
     */
    @Override
    public final int priority() {
        return -1;
    }
}
