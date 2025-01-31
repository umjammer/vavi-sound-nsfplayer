package zdream.nsfplayer.ftm.executor.effect;

import java.util.Map;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * <p>delayed mute state
 * <p>If a frame produces a mute effect {@link NoteHaltEffect}, the state is removed.
 * </p>
 *
 * <br>
 * <p><b>Supplementary rules</b>
 * <p>If there is a {@link NoteEffect} or {@link NoiseEffect} effect triggered in this frame,
 * and this is not the first frame in which the state was created, delete the state.
 * </p>
 *
 * @author Zdream
 * @see CutEffect
 * @since 0.2.2
 */
public class DelayCutState implements IFtmState {

    public static final String NAME = "Delay Cut";

    /**
     * Trigger the mute effect after a few frames
     */
    public int frames;

    /**
     * Record whether this is the first frame in which the state is established.
     */
    private boolean startFrame = true;

    public DelayCutState(int frames) {
        this.frames = frames;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        // Whenever a NoteHaltEffect is triggered in a track, the effect is removed.
        Map<FtmEffectType, IFtmEffect> map = runtime.effects.get(channelCode);
        if (map.get(FtmEffectType.HALT) != null) {
            ch.removeState(this);
            return;
        }
        if (!startFrame && map.get(FtmEffectType.NOTE) != null) {
            ch.removeState(this);
            return;
        }

        if (frames <= 0) {
            ch.doHalt();
            ch.removeState(this);
            return;
        }

        frames--;
        startFrame = false;
    }

    @Override
    public String toString() {
        return NAME + ":" + frames;
    }

    @Override
    public int priority() {
        return -1;
    }
}
