package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * <p>The effect of stopping playback (called Halt effect in FamiTracker), Cxx
 * <p>Global effect
 * </p>
 *
 * @author Zdream
 * @since v0.2.2
 */
public class StopEffect implements IFtmEffect {

    private static final StopEffect instance = new StopEffect();

    private StopEffect() {
        // Singleton
    }

    public static StopEffect of() {
        return instance;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.STOP;
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        runtime.param.finished = true;
    }

    @Override
    public String toString() {
        return "Stop";
    }

    @Override
    public int priority() {
        return 9;
    }
}
