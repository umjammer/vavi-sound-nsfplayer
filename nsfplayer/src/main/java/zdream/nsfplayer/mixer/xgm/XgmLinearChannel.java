package zdream.nsfplayer.mixer.xgm;

/**
 * <p>Xgm mixer linear read and write tracks
 * </p>
 *
 * @author Zdream
 * @since v0.2.10
 */
public final class XgmLinearChannel extends AbstractXgmAudioChannel {

    /**
     * Location.
     * Valid positions are [0, nextWritePtr)
     */
    int[] pos;
    /**
     * value.
     * Index equivalent to {@link #pos}
     */
    short[] values;

    /**
     * Total time span, unit: clock
     */
    int capacity;

    /*
     * Linear write part
     */

    /**
     * Points to the next index to fill in pos
     */
    int nextWritePtr;
    /**
     * Last written data
     */
    short lastWriteValue;

    /*
     * Linear read part
     */

    /**
     * The index of the last read data.
     */
    int lastReadPtr;

    /**
     * Input sampling rate / output sampling number of the current frame
     */
    float param;

    /*
     * Public Methods
     */

    public XgmLinearChannel() {
    }

    @Override
    public void reset() {
        nextWritePtr = 0;
        lastWriteValue = 0;
        lastReadPtr = 0;
    }

    @Override
    public void mix(int value, int time) {
        if (value == lastWriteValue) {
            return;
        }
        if (time < pos[lastReadPtr]) {
            return;
        }

        writeNext(time, (short) value);
        checkArraySize();
    }

    /*
     * XGM Mixer
     */

    @Override
    protected void beforeSubmit() {
        writeNext(Integer.MAX_VALUE, lastWriteValue);
    }

    @Override
    protected void checkCapacity(int size, int frame) {
        this.capacity = size;

        int len = size / 32 + 8;
        if (this.pos == null) {
            this.pos = new int[len];
            this.values = new short[len];
        } else {
            int delta = this.pos.length - len;
            if (delta > 8 || delta <= -8) {
                this.pos = new int[len];
                this.values = new short[len];
            }
        }

        nextWritePtr = 0;
        lastReadPtr = 0;
        writeNext(0, lastWriteValue);
        this.param = (float) size / frame;
    }

    @Override
    protected float read(int index) {
        float time = index * param + param / 2;
        return readValue((int) time);
    }

    int readValue(int time) {
        int beginTime = pos[lastReadPtr];
        if (time >= beginTime) {
            // Linear Read

            while (true) {
                int endTime = pos[lastReadPtr + 1];
                if (endTime > time) {
                    return values[lastReadPtr];
                }
                lastReadPtr++;
            }
        }

        lastReadPtr = 0;
        return readValue(time);
    }

    private void writeNext(int p, short v) {
        pos[nextWritePtr] = p;
        values[nextWritePtr] = v;
        nextWritePtr++;
        lastWriteValue = v;
    }

    private void checkArraySize() {
        if (nextWritePtr == pos.length) {
            int nlen = this.pos.length * 2 + 8;
            int[] npos = new int[nlen];
            short[] nvalues = new short[nlen];

            System.arraycopy(pos, 0, npos, 0, nextWritePtr);
            System.arraycopy(values, 0, nvalues, 0, nextWritePtr);
            pos = npos;
            values = nvalues;
        }
    }
}
