package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * The effect of a key being released. Singleton
 *
 * @author Zdream
 * @since 0.2.1
 */
public class NoteReleaseEffect implements IFtmEffect {

    private static final NoteReleaseEffect instance = new NoteReleaseEffect();

    private NoteReleaseEffect() {
        // Singleton
    }

    public static NoteReleaseEffect of() {
        return instance;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.RELEASE;
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        runtime.channels.get(channelCode).doRelease();
    }

    @Override
    public String toString() {
        return "Note:===";
    }

    /**
     * The priority must be greater than {@link NoteEffect} and {@link NoiseEffect},
     * so that after release, the sound effect can be overwritten
     */
    @Override
    public int priority() {
        return 1;
    }

}
