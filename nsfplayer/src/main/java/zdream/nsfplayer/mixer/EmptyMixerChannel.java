package zdream.nsfplayer.mixer;

/**
 * <p>Unimplemented empty receive track
 * </p>
 *
 * @author Zdream
 * @since v0.3.1
 */
public class EmptyMixerChannel implements IMixerChannel {

    /**
     * Unique instance
     */
    public static final EmptyMixerChannel INSTANCE = new EmptyMixerChannel();

    private EmptyMixerChannel() {
    }

    @Override
    public void reset() {
    }

    @Override
    public void setLevel(float level) {
    }

    @Override
    public float getLevel() {
        return 1.0f;
    }

    @Override
    public void mix(int value, int time) {
    }
}
