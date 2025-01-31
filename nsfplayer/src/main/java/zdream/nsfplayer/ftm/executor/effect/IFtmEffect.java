package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * Ftm Effect
 *
 * @author Zdream
 * @version 0.2.2
 * Starting from this version, effects can be sorted by priority
 * @since 0.2.1
 */
public interface IFtmEffect extends Comparable<IFtmEffect> {

    /**
     * <p>Effect type.
     * <p>Allows different effect classes to have the same kind,
     * such as {@link NoteEffect} and {@link NoiseEffect}.
     * They are used on different tracks.
     * </p>
     *
     * @return
     */
    FtmEffectType type();

    /**
     * Execution effect
     *
     * @param channelCode Current channel number
     * @param runtime
     */
    void execute(byte channelCode, FamiTrackerRuntime runtime);

    /**
     * Priority. The one with the higher priority will be executed first.
     *
     * @return
     * @since 0.2.2
     */
    default int priority() {
        return 0;
    }

    /**
     * The default is to sort in descending order.
     *
     * @since 0.2.2
     */
    @Override
    default int compareTo(IFtmEffect o) {
        return o.priority() - priority();
    }
}
