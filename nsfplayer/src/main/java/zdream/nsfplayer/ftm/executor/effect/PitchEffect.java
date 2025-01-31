package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.format.FtmNote;


/**
 * <p>Effects that modify pitch, Pxx
 * </p>
 *
 * @author Zdream
 * @since 0.2.1
 */
public class PitchEffect implements IFtmEffect {

    public final int pitch;

    private PitchEffect(int pitch) {
        this.pitch = pitch;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.PITCH;
    }

    /**
     * Creates a speed modification effect
     *
     * @param pitch Pitch value. Positive, negative, or 0 are allowed.<br>
     *              0x80 in {@link FtmNote} is the case where the pitch is 0.
     * @return Effect Examples
     */
    public static PitchEffect of(int pitch) {
        return new PitchEffect(pitch);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        runtime.channels.get(channelCode).setMasterPitch(pitch);
    }

    @Override
    public String toString() {
        return "Pitch:" + pitch;
    }
}
