package zdream.nsfplayer.nsf.device;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.nsf.renderer.INsfRuntimeHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;


/**
 * <p>Virtual audio device (equivalent to a virtual sound card)
 *
 * @author Zdream
 * @since v0.2.4
 */
public abstract class AbstractSoundChip implements IDevice, INsfChannelCode, INsfRuntimeHolder {

    public AbstractSoundChip(NsfRuntime runtime) {
        this.runtime = runtime;
    }

    final NsfRuntime runtime;

    /**
     * Indicates if rendering has started.
     * VRC7 Six-track merge track
     */
    boolean startRender;

    @Override
    public NsfRuntime getRuntime() {
        return runtime;
    }

    /**
     * <p></p>The entire rendering process is divided into three parts:
     * initialization, execution, and rendering. This value can be used to determine whether
     * the rendering phase has been entered.
     * </p>
     *
     * @return A flag indicating whether to start rendering.
     * @see #startRender
     * @since v0.2.9
     */
    public boolean isStartRender() {
        return startRender;
    }

    /**
     * Get the audio generator of the specified track
     *
     * @param channelCode Track number
     * @return
     */
    public abstract AbstractNsfSound getSound(byte channelCode);

    /**
     * Get a list of track numbers for all supported tracks
     */
    public abstract byte[] getAllChannelCodes();
}
