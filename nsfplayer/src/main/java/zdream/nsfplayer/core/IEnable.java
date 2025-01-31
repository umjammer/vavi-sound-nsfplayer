package zdream.nsfplayer.core;

/**
 * Possible opening, opening / Forbidden access
 *
 * @author Zdream
 * @since v0.2.3
 */
public interface IEnable {

    /**
     * Can I set it up?
     *
     * @param enable
     */
    void setEnable(boolean enable);

    /**
     * Is the inquiry open?
     *
     * @return
     */
    boolean isEnable();
}
