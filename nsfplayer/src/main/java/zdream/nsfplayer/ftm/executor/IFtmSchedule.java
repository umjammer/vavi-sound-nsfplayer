package zdream.nsfplayer.ftm.executor;

/**
 * <p>A task that is triggered before the start of each frame. It is executed only once
 * before the effect of the first frame is triggered, and then deleted.
 * </p>
 *
 * @author Zdream
 * @since 0.2.2
 */
public interface IFtmSchedule {

    /**
     * Method body triggered per frame
     *
     * @param channelCode Current channel number
     * @param runtime     operating environment
     */
    void trigger(byte channelCode, FamiTrackerRuntime runtime);

}
