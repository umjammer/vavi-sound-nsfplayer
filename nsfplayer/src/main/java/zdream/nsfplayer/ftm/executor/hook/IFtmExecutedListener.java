package zdream.nsfplayer.ftm.executor.hook;

import zdream.nsfplayer.ftm.executor.FamiTrackerExecutorHandler;


/**
 * <p>Effect execution end listener
 * <p>When the FamiTracker execution component completes a frame of work and has not yet written
 * data to the Sound, this type of listener is woken up. It will only be called once per frame.
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public interface IFtmExecutedListener {

    /**
     * The track effect is executed, but the data has not been written to the sound or other parts.
     * This is the last chance to modify the channel data before rendering.
     *
     * @param handler
     */
    void onExecuteFinished(FamiTrackerExecutorHandler handler);
}
