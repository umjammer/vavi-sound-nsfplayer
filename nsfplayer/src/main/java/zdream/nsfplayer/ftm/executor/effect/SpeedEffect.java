package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * <p>Effect of modifying speed, Fxx
 * <p>Global effect
 * </p>
 *
 * @author Zdream
 * @since v0.2.1
 */
public class SpeedEffect implements IFtmEffect {

    public final int speed;

    private SpeedEffect(int speed) {
        this.speed = speed;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.SPEED;
    }

    /**
     * Creates a speed modification effect
     *
     * @param speed Speed value. Speed value must be a positive number.
     * @return Effect Examples
     * @throws IllegalArgumentException When the speed value <code>speed</code> is not within the specified range
     */
    public static SpeedEffect of(int speed) throws IllegalArgumentException {
        if (speed <= 0) {
            throw new IllegalArgumentException("Speed \u200B\u200Bmust be a non-negative value");
        }
        return new SpeedEffect(speed);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        runtime.fetcher.setSpeed(speed);
    }

    @Override
    public String toString() {
        return "Speed:" + speed;
    }
}
