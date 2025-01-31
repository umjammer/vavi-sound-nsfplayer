package zdream.nsfplayer.ftm.executor.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * <p>Effects of changing volume
 * <p>This effect also resets volume-modifying-over-time effects (Axx) when it occurs,
 * but only clears the cumulative data.
 * </p>
 *
 * @author Zdream
 * @since 0.2.1
 */
public class VolumeEffect implements IFtmEffect {

    public final int volume;

    private VolumeEffect(int volume) {
        this.volume = volume;
    }

    @Override
    public final FtmEffectType type() {
        return FtmEffectType.VOLUME;
    }

    /**
     * Create an effect of modifying the volume
     *
     * @param volume Volume value. Volume value must be in the range [0, 15].
     * @return Effect Examples
     * @throws IllegalArgumentException When the volume value <code>volumn</code> is not within the specified range
     */
    public static VolumeEffect of(int volume) throws IllegalArgumentException {
        if (volume > 15 || volume < 0) {
            throw new IllegalArgumentException("The volume must be an integer value between 0 and 15");
        }
        return new VolumeEffect(volume);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        ch.setMasterVolume(volume);

        // If a Volume Accumulate effect exists, reset it
        HashSet<IFtmState> set = ch.filterStates(VolumeAccumulateState.NAME);
        if (!set.isEmpty()) {
            set.forEach((state) -> {
                if (state instanceof VolumeAccumulateState) {
                    ((VolumeAccumulateState) state).resetAccumulation();
                }
            });
        }
    }

    @Override
    public String toString() {
        return "Vol:" + Integer.toHexString(volume);
    }
}
