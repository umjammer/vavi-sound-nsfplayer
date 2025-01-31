package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * The effect of stopping the sound played by the key. Singleton
 *
 * @author Zdream
 * @since 0.2.1
 */
public class NoteHaltEffect implements IFtmEffect {

    private static final NoteHaltEffect instance = new NoteHaltEffect();

    private NoteHaltEffect() {
        // Singleton
    }

    public static NoteHaltEffect of() {
        return instance;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.HALT;
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        runtime.channels.get(channelCode).doHalt();
    }

    @Override
    public String toString() {
        return "Note:---";
    }

    /**
     * The priority must be greater than {@link NoteEffect} and {@link NoiseEffect},
     * So that after muting, the effect of playing sound can be rewritten
     */
    @Override
    public int priority() {
        return 1;
    }
}
