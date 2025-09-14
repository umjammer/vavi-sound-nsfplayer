package zdream.nsfplayer.core;

/**
 * NSF Abstract Audio Implementation Component
 *
 * @param <T> Audio Data
 * @author Zdream
 * @since v0.3.0
 */
public abstract class AbstractNsfExecutor<T extends AbstractNsfAudio> implements INsfExecutor<T>, INsfChannelCode {

    public AbstractNsfExecutor() {

    }

    /*
     * Public Methods
     */

    private boolean enable;

    @Override
    public final boolean isEnable() {
        return enable;
    }

    @Override
    public final void setEnable(boolean enable) {
        this.enable = enable;
    }
}
