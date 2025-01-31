package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * <p>Modifying the effect of a tone, Vxx
 * </p>
 *
 * @author Zdream
 * @since 0.2.1
 */
public class DutyEffect implements IFtmEffect {

    public final int duty;

    private DutyEffect(int duty) {
        this.duty = duty;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.DUTY_CYCLE;
    }

    /**
     * Creates a modified timbre effect
     *
     * @param duty Tone value. Only positive numbers or 0 are allowed.
     * @return Examples of effects
     * @throws IllegalArgumentException When the timbre value <code>duty</code> is not in the specified range
     */
    public static DutyEffect of(int duty) throws IllegalArgumentException {
        if (duty < 0) {
            throw new IllegalArgumentException("Tone must be a non-negative value");
        }
        return new DutyEffect(duty);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        runtime.channels.get(channelCode).setMasterDuty(duty);
    }

    @Override
    public String toString() {
        return "Duty:" + duty;
    }
}
