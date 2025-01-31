package zdream.nsfplayer.ftm.executor;

/**
 * Holders of the Famitracker runtime environment
 *
 * @author Zdream
 * @since 0.2.1
 */
public interface IFtmRuntimeHolder {

    /**
     * Get an example of the runtime environment
     *
     * @return
     */
    FamiTrackerRuntime getRuntime();

}
