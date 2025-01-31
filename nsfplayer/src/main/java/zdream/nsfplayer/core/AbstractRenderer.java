package zdream.nsfplayer.core;

import java.util.Arrays;


/**
 * <p>An abstract sound source renderer that outputs PCM audio data organized in byte/short arrays
 * <p>Further abstraction based on {@link AbstractNsfRenderer}
 * </p>
 *
 * @author Zdream
 * @since v0.3.1
 */
public abstract class AbstractRenderer<T> {

    /*
     * Preparation
     */

    /**
     * <p>Let the renderer read the corresponding audio data.
     * <p>Set the playback pause position to the beginning of the default track.
     * </p>
     *
     * @param audio Audio Data
     */
    public abstract void ready(T audio);

    /**
     * Asks if the entire song has been rendered
     *
     * @return
     */
    public abstract boolean isFinished();

    /*
     * Rendering
     */

    /**
     * Rendering
     * <br>Thread-unsafe methods
     *
     * @param bs
     * @param offset bs The starting location for storing data
     * @param length bs The total amount of data stored, in bytes.
     *               <br>This is mono, 16-bit depth, the data needs to be a multiple of 2.
     * @return The number of array elements actually filled
     */
    public int render(byte[] bs, int offset, int length) {
        length = length / 2 * 2; // this value start Sat 0
        int bOffset = offset; // bs offset
        int bLength = length;
        int ret = 0; // The number of samples completed

        // The remaining samples from the previous rendering that have not yet been returned
        int v = fillSample(bs, bOffset, bLength) * 2;
        ret += v;
        bOffset += v;
        bLength -= v;

        while (ret < length) {
            renderFrame();
            // data data is ready

            v = fillSample(bs, bOffset, bLength) * 2;
            ret += v;
            bOffset += v;
            bLength -= v;

            if (isFinished()) {
                break;
            }
        }

        return ret; // (Current unit: byte)
    }

    /**
     * Rendering, get sampling data as a short array
     * <br>Thread-unsafe methods
     *
     * @param bs    Get the array of sampled data, short format
     * @param offset bs The starting position of the data storage
     * @param length The total amount of data stored in bs.
     * @return The number of array elements actually filled
     * @since v0.2.9
     */
    public int render(short[] bs, int offset, int length) {
        int bOffset = offset; // bs offset
        int bLength = length; // bs The number of samples that can be obtained
        int ret = 0; // The number of samples completed

        // The remaining samples from the previous rendering that have not yet been returned
        int v = fillSample(bs, bOffset, bLength);
        ret += v;
        bOffset += v;
        bLength -= v;

        while (ret < length) {
            renderFrame();
            // data data is ready

            v = fillSample(bs, bOffset, bLength);
            ret += v;
            bOffset += v;
            bLength -= v;

            if (isFinished()) {
                break;
            }
        }

        return ret;
    }

    /**
     * <p>Render only one frame, and get the sample data as a short array.
     * If there is no sampling data from the previous frame that has been rendered,
     * only the remaining sampling data from the previous frame will be written into the array.
     * <br>Thread-unsafe methods
     * </p>
     *
     * @param bs
     * @param offset bs The starting position of the data storage
     * @param length bs The total amount of data stored, in bytes.
     *               <br>This is mono, 16-bit depth, the data needs to be a multiple of 2.
     * @return The number of array elements actually filled
     * @since v0.2.2
     */
    public int renderOneFrame(byte[] bs, int offset, int length) {
        int bLength = length / 2 * 2; // Convert to multiples of 2

        // The remaining samples from the previous rendering that have not yet been returned
        int ret = fillSample(bs, offset, bLength) * 2;
        if (ret == 0) {
            renderFrame();
            // data data is ready
            ret = fillSample(bs, offset, bLength) * 2;
        }

        return ret; // (Current unit: byte)
    }

    /**
     * <p>Only render one frame. If there is sampling data from the previous frame
     * that has not been rendered, only the remaining sampling data from the previous
     * frame will be written into the array.
     * <br>Thread-unsafe methods
     * </p>
     *
     * @param bs     Get the array of sampled data, short format
     * @param offset bs The starting position of the data storage
     * @param length The total amount of data stored in bs, in short units.
     * @return The number of array elements actually filled
     * @since v0.2.9
     */
    public int renderOneFrame(short[] bs, int offset, int length) {
        // The remaining samples from the previous rendering that have not yet been returned
        int ret = fillSample(bs, offset, length);
        if (ret == 0) {
            renderFrame();
            // data is ready
            ret = fillSample(bs, offset, length);
        }

        return ret;
    }

    /**
     * <p>Skip the specified number of frames.
     * <p>If there is sampling data of the previous frame that has not been rendered, the frame data
     * will be discarded and will not be counted in the number of skipped frames;
     * <p><code>skip(0)</code> means discarding the sampling data that has not been rendered in the previous frame
     * and starting rendering from the beginning of the next frame.
     * </p>
     *
     * @param frame The number of frames. Must be a positive number
     * @since v0.2.9
     */
    public void skip(int frame) {
        for (int i = 0; i < frame; i++) {
            this.skipFrame();
        }

        // If the remaining samples from the previous rendering have not been returned, clear them directly
        this.offset = this.length = 0;
    }

    /**
     * <p>The number of samples remaining.
     * <p>Returns the sampling data of the previous frame that has not been rendered. If there is none, returns 0.
     * </p>
     *
     * @return The number of samples that were not rendered in the previous frame
     * @since v0.2.9
     */
    public int remain() {
        return this.length - this.offset;
    }

    /*
     * Rendering parameters
     */
    /**
     * Sample rate counter.
     *
     * @since v0.2.5
     */
    protected final CycleCounter counter = new CycleCounter();

    /**
     * Audio data.
     * <br>The sample data that has not been returned is in this block: [offset, length)
     */
    protected short[] data;
    protected int offset = 0;
    protected int length = 0;

    /**
     * <p>Fill the sample data. byte[] array.
     * <p>Filled with 48000 Hz, 16 bit signed | little-endian, mono (mono)
     * </p>
     *
     * @param bs
     * @param bOffset
     * @param bLength
     * @return The number of samples actually filled
     */
    protected int fillSample(byte[] bs, int bOffset, int bLength) {
        int bRemain = bLength / 2;
        int dRemain = this.length - this.offset; // The rest of data (in samples)
        int ret = 0;

        if (dRemain != 0) {
            if (bRemain <= dRemain) {
                // Fill the data of data into bs
                fillSample(bs, bOffset, bLength, bRemain);
                // bs filled

                ret += bRemain;
            } else {
                // Fill the data of data into bs
                fillSample(bs, bOffset, bLength, dRemain);
                // data is used up

                ret += dRemain;
            }
        }

        return ret;
    }

    /**
     * Fill the sample data. short[] array
     *
     * @param bs      short array
     * @param bOffset
     * @param bLength
     * @return The number of samples actually filled
     * @since v0.2.9
     */
    protected int fillSample(short[] bs, int bOffset, int bLength) {
        int bRemain = bLength;
        int dRemain = this.length - this.offset; // The rest of data (in samples)
        int ret = 0;

        if (dRemain != 0) {
            if (bRemain <= dRemain) {
                // Fill the data of data into bs
                System.arraycopy(this.data, this.offset, bs, bOffset, bRemain);
                this.offset += bRemain;
                // bs filled
                ret = bRemain;
            } else {
                // Fill the data of data into bs
                System.arraycopy(this.data, this.offset, bs, bOffset, dRemain);
                // data is used up
                ret = dRemain;
                this.offset += dRemain;
            }
        }

        return ret;
    }

    protected void fillSample(byte[] bs, int bOffset, int bLength, int dLength) {
        int bptr = bOffset;
        int dptr = this.offset;
        for (int i = 0; i < dLength; i++) {
            short sample = this.data[dptr++];
            bs[bptr++] = (byte) sample; // Low
            bs[bptr++] = (byte) ((sample & 0xff00) >> 8); // High
        }

        this.offset += dLength;
    }

    /**
     * Calculate the number of samples (per channel) needed for the next frame,
     * taking into account the effect of playback speed
     *
     * @return The number of samples required for the next frame
     */
    protected int countNextFrame() {
        int ret = counter.tick();

        if (data == null || data.length < ret || data.length - ret > 16) {
            data = new short[ret + 8];
        } else {
            Arrays.fill(data, (byte) 0);
        }
        length = ret;
        offset = 0;

        return ret;
    }

    /**
     * Reset frame rate and sample rate. If the renderer needs to replace the audio,
     * you need to call this parameter to reset the counter
     *
     * @param maxFrameCount  Frame rate, usually 60
     * @param maxSampleCount Sampling rate, usually 48000. This value is not affected by the playback speed
     * @since v0.2.5
     */
    protected void resetCounterParam(int maxFrameCount, int maxSampleCount) {
        float speed = getSpeed();
        int cycle = maxSampleCount;
        if (speed != 1) {
            cycle = (int) (cycle / speed);
        }

        // Reset Counter
        counter.setParam(cycle, maxFrameCount);
    }

    /**
     * Reset the audio part, including the buffer array
     *
     * @since v0.2.9
     */
    protected void clearBuffer() {
        // Reset the audio part, including the buffer array
        offset = 0;
        length = 0;
        if (data != null) {
            Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * Render a frame
     *
     * @return The number of samples rendered by this function (calculated as mono)
     */
    protected abstract int renderFrame();

    /*
     * Instrument panel area
     */

    /**
     * Skip a frame
     *
     * @return The number of samples skipped by this function (calculated as mono)
     * @since v0.2.9
     */
    protected abstract int skipFrame();

    /**
     * <p>Set the playback speed.
     * <p>If the audio data of the current frame has not been completely retrieved,
     * this part of the audio data will no longer be speed-changed, and the speed-changed
     * effect will start from the next frame.
     * </p>
     *
     * @param speed Playback speed. Valid value range: [0.1f, 10f]
     * @since v0.2.9
     */
    public abstract void setSpeed(float speed);

    /**
     * Get the current playback speed.
     *
     * @return Playback speed. Valid value range: [0.1f, 10f]
     * @since v0.2.9
     */
    public abstract float getSpeed();

    /**
     * Reset playback speed
     *
     * @since v0.2.9
     */
    public void resetSpeed() {
        setSpeed(1.0f);
    }
}
