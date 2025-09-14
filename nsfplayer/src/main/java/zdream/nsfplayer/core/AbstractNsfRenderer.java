package zdream.nsfplayer.core;

/**
 * Abstract NSF sound source renderer, used to output PCM audio data organized as byte / short array
 *
 * @author Zdream
 * @version 0.3.2
 * Related methods are independently extracted from the interface,
 * @since v0.2.4
 */
public abstract class AbstractNsfRenderer<T extends AbstractNsfAudio<?>> extends AbstractRenderer<T>
        implements INsfRendererHandler<T> {

}
