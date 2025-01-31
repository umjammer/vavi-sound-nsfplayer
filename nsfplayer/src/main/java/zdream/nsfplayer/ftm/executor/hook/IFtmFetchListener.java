package zdream.nsfplayer.ftm.executor.hook;

import zdream.nsfplayer.ftm.executor.FamiTrackerExecutorHandler;
import zdream.nsfplayer.ftm.format.FtmNote;


/**
 * <p>When the FamiTracker execution component obtains the current frame {@link FtmNote},
 * this type of listener will be awakened.
 * <p>In fact, this type of listener will be awakened multiple times per frame,
 * regardless of whether a new {@link FtmNote} is obtained,
 * the number of times is equal to the current number of tracks of FamiTracker.
 * This listener has the ability to modify the contents of a {@link FtmNote}.
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public interface IFtmFetchListener {

    /**
     * Called when the executing component gets a new {@link FtmNote} or fails to get a Note.
     *
     * @param note        FTM Keys
     * @param channelCode The track
     * @param handler
     */
    FtmNote onFetch(
            FtmNote note,
            byte channelCode,
            FamiTrackerExecutorHandler handler);
}
