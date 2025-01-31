package zdream.nsfplayer.mixer;

import java.util.ArrayList;

import static zdream.nsfplayer.core.NsfChannelCode.typeOfChannel;


/**
 * <p>Abstract NSF mixer.
 * <p>This layer is used to manage channel identification numbers,
 * while channel generation and other tasks are completed by subclasses
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public abstract class AbstractNsfSoundMixer<T extends IMixerChannel> implements ISoundMixer {

    protected final ArrayList<ChannelAttr> attrs = new ArrayList<>();

    /**
     * AbstractNsfSoundMixer for per-channel parameters. Subclasses may inherit as needed.
     *
     * @author Zdream
     */
    protected class ChannelAttr {

        public ChannelAttr(byte code, T t) {
            this.channel = t;
            this.code = code;
        }

        /**
         * channel Examples
         */
        public final T channel;
        /**
         * Input Sample Number
         */
        public int inSample;
        /**
         * channel type, or channel number
         */
        public final byte code;
    }

    /**
     * Create tracks and parameters based on channel type
     *
     * @param code channel Type
     * @return
     */
    protected abstract ChannelAttr createChannelAttr(byte code);

    /**
     * Look up the next used id in [0, attrs.length) (i.e. when the attrs element is null).
     *
     * @return
     */
    private int findNextId() {
        return attrs.indexOf(null);
    }

    @Override
    public int allocateChannel(byte code) {
        int nextId = findNextId();

        // Here, all codes are converted to channel types
        code = typeOfChannel(code);
        ChannelAttr attr = createChannelAttr(code);

        if (nextId != -1) {
            attrs.set(nextId, attr);
        } else {
            nextId = attrs.size();
            attrs.add(attr);
        }

        return nextId;
    }

    @Override
    public T getMixerChannel(int id) {
        if (id >= attrs.size()) {
            return null;
        }

        ChannelAttr attr = attrs.get(id);
        if (attr != null) {
            return attr.channel;
        }
        return null;
    }

    /*
     * This method is recommended to be inherited and the last line of the overridden method is super.detach(id)
     */
    @Override
    public void detach(int id) {
        if (id >= attrs.size()) {
            return;
        }

        ChannelAttr attr = attrs.get(id);
        if (attr != null) {
            attrs.set(id, null);
        }
    }

    /*
     * This method is recommended to be inherited and the last line of the override method is super.detachAll()
     */
    @Override
    public void detachAll() {
        attrs.clear();
    }

}
