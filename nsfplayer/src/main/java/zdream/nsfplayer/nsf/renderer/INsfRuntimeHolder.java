package zdream.nsfplayer.nsf.renderer;

/**
 * Holders of the Nsf runtime environment
 *
 * @author Zdream
 * @since v0.2.4
 */
public interface INsfRuntimeHolder {

    /**
     * Get an example of the runtime environment
     *
     * @return
     */
    NsfRuntime getRuntime();
}
