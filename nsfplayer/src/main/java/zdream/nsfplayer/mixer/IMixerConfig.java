package zdream.nsfplayer.mixer;

/**
 * The configuration item interface of the mixer. Accepts the clone protocol by default
 *
 * @author Zdream
 * @since v0.2.5
 */
public interface IMixerConfig extends Cloneable {

    // nothing

    IMixerConfig clone();
}
