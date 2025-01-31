package zdream.nsfplayer.ftm.factory;

import zdream.nsfplayer.ftm.audio.FamiTrackerHandler;
import zdream.nsfplayer.ftm.audio.FtmAudio;


/**
 * <p>Abstract FamiTracker data creation tool,
 * used to create and improve {@link FtmAudio} information.
 * <p>Using the given vector T, read the data in the vector and fill it into {@link FtmAudio}.
 * <p>The subclass creation process is not thread-safe. To create a {@link FtmAudio},
 * the factory class must create a new instance of this class.
 * </p>
 *
 * @param <T> The data carrier required by the created {@link FtmAudio}
 * @author Zdream
 * @since v0.1
 */
public abstract class AbstractFamiTrackerCreater<T> {

    //
    // create
    //

    /**
     * Use the data carrier reader to complete the information of {@link FtmAudio}
     *
     * @param reader  Data carrier, data source
     * @param handler FamiTracker audio manipulator.
     *                This operator can be used to write data to a specific {@link FtmAudio}
     * @throws FamiTrackerFormatException When the data provided by the <code>reader</code> is incorrect
     * @since v0.2.5
     */
    public abstract void doCreate(T reader, FamiTrackerHandler handler)
            throws FamiTrackerFormatException;

    /*
     * Error handling
     */

    /*
     * When it is found that the data obtained from the data source (T mentioned above)
     * is abnormal and exceeds the reasonable range, a means of error handling is needed.
     */

    /**
     * Error handling. As long as you enter this function, an error will be thrown.
     *
     * @param t
     * @throws FamiTrackerFormatException
     * @since v0.2.5
     */
    protected abstract void handleException(T t, String msg) throws FamiTrackerFormatException;
}
