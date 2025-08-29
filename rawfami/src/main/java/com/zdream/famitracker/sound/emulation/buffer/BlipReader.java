package com.zdream.famitracker.sound.emulation.buffer;

import static com.zdream.famitracker.sound.emulation.buffer.BufferContext.blip_sample_bits;


/**
 * <p>Optimize the way to read sample data in a custom format.
 * <p>Optimized inline sample reader for custom sample formats and mixing of Blip_Buffer samples
 *
 * @author Zdream
 */
public class BlipReader {

    /**
     * <p>Start reading sample data from the buffer.
     * <p>Begin reading samples from buffer.<br>
     * Returns value to pass to next() (can be ignored if default bass_freq is acceptable).
     *
     * @return
     */
    public int begin(BlipBuffer _buf) {
        buf = _buf;
        accum = _buf.reader_accum;
        return _buf.bass_shift;
    }

    /**
     * <p>Current sample value
     * <p>Current sample
     *
     * @return
     */
    public final long read() {
        return accum >> (blip_sample_bits - 16);
    }

    /**
     * <p>The original sample value that has not been modified. This value is directly obtained after calculation, and has not been optimized by high-pass, etc.
     * <p>Current raw sample in full internal resolution
     *
     * @return
     */
    public final long readRaw() {
        return accum;
    }

    /**
     * <p>Next sample value
     * <p>Advance to next sample
     *
     * @param bass_shift defaults to 9
     */
    public void next(int bass_shift) {
        accum += buf.buffer_[ptr++] - (accum >> bass_shift);
    }

    /**
     * <p>End reading sample data from the buffer.<br>
     * After reading the sample data, you need to call {@link BlipBuffer#removeSamples(int)}, pass the number of samples read as a parameter,
     * to delete these data from the buffer.
     * <p>End reading samples from buffer.<br>
     * The number of samples read must now be removed using {@link BlipBuffer#removeSamples(int)}.
     */
    public void end() {
        buf.reader_accum = accum;
    }

    private BlipBuffer buf;
    /**
     * This is the index pointer to buf.buffer_
     */
    private int ptr;
    private long accum;

}
