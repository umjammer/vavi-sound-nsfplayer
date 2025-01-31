package zdream.nsfplayer.ftm.executor.effect;

import java.util.HashSet;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;

import static zdream.nsfplayer.core.NsfChannelCode.chipOfChannel;


/**
 * <p>Effect that modifies volume over time, VRC7 dedicated channel effect, Axx
 * </p>
 *
 * @author Zdream
 * @since 0.2.7
 */
public class VRC7VolumeSlideEffect extends VolumeSlideEffect {

    protected VRC7VolumeSlideEffect(int slide) {
        super(slide);
    }

    /**
     * Creates an effect that modifies the volume over time, a VRC7 dedicated channel effect
     *
     * @param delta Change amount. The volume change per frame (usually 1/60 s), range [-30, 30]
     *              <br>If it is a positive number, the volume will increase over time;
     *              <br>If it is a negative number, the volume will decrease over time;
     *              <br>0, the volume does not change with time, and the effect of modifying the volume over time
     *              that originally acts on the channel can also be disabled;
     * @return Effect Examples
     * @throws IllegalArgumentException When the change <code>delta</code> is not within the specified range
     */
    public static VRC7VolumeSlideEffect of(int delta) throws IllegalArgumentException {
        if (delta < -30 || delta > 30) {
            throw new IllegalArgumentException("The volume change must be between -30 and 30");
        }
        return new VRC7VolumeSlideEffect(delta);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        if (chipOfChannel(channelCode) != INsfChannelCode.CHIP_VRC7) {
            throw new IllegalStateException("The effect of modifying the VRC7 volume over time can only be triggered on the VRC7 track, not on the "
                    + channelCode + " track.");
        }
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        /*
         * Here we need to ensure that a channel has at most one state that changes the volume over time.
         */
        HashSet<IFtmState> set = ch.filterStates(VolumeAccumulateState.NAME);
        VolumeAccumulateState s = null;

        if (!set.isEmpty()) {
            s = (VolumeAccumulateState) set.iterator().next();
            s.delta = -delta; // But it does not reset the cumulative amount
        } else if (delta != 0) {
            s = new VolumeAccumulateState(-delta);
            ch.addState(s);
        }
    }

    @Override
    public String toString() {
        return "VRC7VolumeSlide:" + delta;
    }
}
