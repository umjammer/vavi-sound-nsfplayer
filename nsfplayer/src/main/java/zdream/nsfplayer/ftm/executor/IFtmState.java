package zdream.nsfplayer.ftm.executor;

/**
 * <p>State. Each channel and global is triggered every frame during playback.
 * </p>
 *
 * @author Zdream
 * @version 0.2.2
 * Starting from this version, statuses can be sorted according to priority.
 * @since 0.2.1
 */
public interface IFtmState extends Comparable<IFtmState> {

    /**
     * logo name
     *
     * @return
     */
    String name();

    /**
     * Method body triggered per frame
     *
     * @param channelCode Current channel number
     * @param runtime
     */
    void trigger(byte channelCode, FamiTrackerRuntime runtime);

    /**
     * Triggered when the state is assembled,
     * i.e., assembled by the channel or global state machine (after adding it).
     *
     * @param channelCode Current channel number
     * @param runtime
     */
    default void onAttach(byte channelCode, FamiTrackerRuntime runtime) {
    }

    /**
     * Triggered when the state is dismantled,
     * i.e. by a channel or global state machine (before dismantling)
     *
     * @param channelCode Current channel number
     * @param runtime
     */
    default void onDetach(byte channelCode, FamiTrackerRuntime runtime) {
    }

    /**
     * Priority. The higher the priority, the first to be executed.
     *
     * @return
     * @since 0.2.2
     */
    default int priority() {
        return 0;
    }

    /**
     * By default, they are sorted in ascending order.
     *
     * @since 0.2.2
     */
    @Override
    default int compareTo(IFtmState o) {
        return o.priority() - priority();
    }
}
