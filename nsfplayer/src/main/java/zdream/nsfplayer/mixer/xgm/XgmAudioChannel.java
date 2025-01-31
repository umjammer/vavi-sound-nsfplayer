package zdream.nsfplayer.mixer.xgm;

import java.util.Arrays;

import zdream.nsfplayer.sound.AbstractNsfSound;


/**
 * Xgm's mixer sample integration track.
 *
 * @author Zdream
 * @version v0.2.10
 * It has been greatly optimized. It has changed from storing all the clock audio data
 * to storing all the sampled audio data. In addition to greatly reducing the storage size,
 * the efficiency has also been improved.
 * @since v0.2.1
 */
public final class XgmAudioChannel extends AbstractXgmAudioChannel {

    /**
     * <p>Stores only audio data output from {@link AbstractNsfSound} via the {@link #mix(int, int)} method.
     * Store the audio value of each sample in the buffer, get the audio data of each clock,
     * and add it to the audio data of the corresponding sample according to its proportion
     * of the entire sample.
     * Therefore, we need to know the frame clock and total number of samples at the beginning of each frame.
     *
     * <p>The final processing situation is that each sampled data is obtained by averaging the audio data
     * corresponding to multiple clocks.
     * In previous versions, short was used as the storage format.
     * However, considering that very small numbers will lose a lot of precision after averaging,
     * we use float format starting from version v0.2.10.
     *
     * <p>The original setting is that the buffer length is the number of samples per frame + 4.
     * Because the number of samples in each frame will fluctuate,
     * we need to add 4 to ensure that the array is large enough.
     * </p>
     */
    float[] buffer;

    /**
     * The last stored value
     */
    short lastValue;

    /**
     * The time value of the previous mix.
     * The position to be mixed later is [lastTime, time]
     */
    int lastTime;

    /**
     * Total time, unit: clock
     */
    int maxTime;

    // New Section

    /**
     * Ratio: Number of samples / Number of clocks
     */
    float param;

    public XgmAudioChannel() {
    }

    @Override
    public void reset() {
        if (buffer != null) {
            Arrays.fill(buffer, (short) 0);
        }
        lastValue = 0;
        lastTime = 0;
    }

    @Override
    public void mix(int value, int time) {
        if (value == lastValue) {
            return;
        }

        if (buffer == null) {
            this.lastValue = (short) value;
            return;
        }

        if (time > this.maxTime) {
            time = maxTime;
        }

        mix0(value, time);
    }

    private void mix0(int value, int time) {
        // Write to buffer

        float pstart = lastTime * param;
        float pend = time * param;
        int istart = (int) (pstart) + 1;
        int iend = (int) (pend);

        if (istart <= iend) {
            // pstart <= istart <= iend <= pend
            // The beginning
            buffer[istart - 1] += this.lastValue * (istart - pstart);

            // Middle part
            Arrays.fill(buffer, istart, iend, lastValue);

            // Ending
            buffer[iend] = this.lastValue * (pend - iend);
        } else {
            // iend <= pstart <= pend <= istart
            buffer[iend] += this.lastValue * (pend - pstart);
        }

        this.lastTime = time;
        this.lastValue = (short) value;
    }

    @Override
    protected void beforeSubmit() {
        this.mix0(lastValue, maxTime);
        this.lastTime = 0;
    }

    @Override
    protected float read(int index) {
        return buffer[index];
    }

    @Override
    protected void checkCapacity(int size, int frame) {
        if (this.buffer != null) {
            int length = this.buffer.length;
            if (length < frame || length > frame + 8) {
                this.buffer = new float[frame + 4];
            } else {
                Arrays.fill(buffer, 0, buffer.length, 0);
            }
        } else {
            this.buffer = new float[frame + 4];
        }
        this.maxTime = size;
        this.param = (float) frame / size;
    }
}
