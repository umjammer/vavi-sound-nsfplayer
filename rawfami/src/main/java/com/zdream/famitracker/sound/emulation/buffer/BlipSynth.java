package com.zdream.famitracker.sound.emulation.buffer;

import static com.zdream.famitracker.sound.emulation.buffer.BufferContext.blip_res;
import static com.zdream.famitracker.sound.emulation.buffer.BufferContext.blip_widest_impulse_;


/**
 * <p>
 * <p>Range specifies the greatest expected change in amplitude.
 * Calculate it by finding the difference between the maximum and minimum expected amplitudes (max - min).
 *
 * @author Zdream
 */
public class BlipSynth {

    /**
     * @param quality
     * @param range   specifies the maximum expected change in amplitude
     */
    public BlipSynth(int quality, int range) {
        this.quality = quality;
        this.range = range;
        impulses = new short[blip_res * (quality / 2) + 1];
        impl = new BlipSynth_(impulses, quality);
    }

    /**
     * <p>Set the total volume of the waveform
     * <p>Set overall volume of waveform
     *
     * @param v
     */
    public void volume(double v) {
        impl.volumeUnit(v * (1.0 / (range < 0 ? -range : range)));
    }

    /**
     * <p>Set low-pass filter
     * <p>Configure low-pass filter
     */
    public void trebleEq(BlipEQ eq) {
        impl.trebleEq(eq);
    }

    /**
     * <p>Get BlipBuffer
     * <p>Get BlipBuffer used for output
     *
     * @return
     */
    public final BlipBuffer output() {
        return impl.buf;
    }

    /**
     * <p>Set BlipBuffer
     * <p>Set BlipBuffer used for output
     *
     * @param b
     */
    public void output(BlipBuffer b) {
        impl.buf = b;
        impl.last_amp = 0;
    }

    /**
     * <p>Update the waveform amplitude within the specified time range.<br>
     * Using this requires a separate BlipSynth for each waveform.
     * <p>Update amplitude of waveform at given time.<br>
     * Using this requires a separate BlipSynth for each waveform.
     *
     * @param time
     * @param amplitude
     */
    public void update(int time, int amplitude) {
        int delta = amplitude - impl.last_amp;
        impl.last_amp = amplitude;
        offsetResampled(time * impl.buf.factor_ + impl.buf.offset_, delta, impl.buf);
    }

// Low-level interface

    /**
     * <p>Add an increment to the sample value at a certain point in time.
     * <p>Add an amplitude transition of specified delta,
     * optionally into specified buffer rather than the one set with output().<br>
     * Delta can be positive or negative.
     * The actual change in amplitude is delta * (volume / range)
     *
     * @param time  time point
     * @param delta The amount of change between this frame and the previous frame, which can be positive or negative
     * @param buf
     */
    public final void offset(int time, int delta, BlipBuffer buf) {
        offsetResampled(time * buf.factor_ + buf.offset_, delta, buf);
    }
	
	/*public final void offset(int t, int delta) {
		offset(t, delta, impl.buf);
	}*/

    /*
     * public final void offset_inline( int t, int delta, BlipBuffer buf )
     * public final void offset_inline( int t, int delta )
     * Just to use the inline writing method to improve operating efficiency. In fact, it is not used
     */

    /**
     * <p>Directly use fractions to output samples.
     * <p>Works directly in terms of fractional output samples.
     *
     * @param time  clock
     * @param delta
     * @param buf
     */
    private void offsetResampled(int time, int delta, BlipBuffer buf) {
        // Fails if time is beyond end of Blip_Buffer, due to a bug in caller code or the
        // need for a longer buffer as set by set_sample_rate().
        assert ((long) (time >> 16) < buf.buffer_size_);

        delta *= impl.delta_factor;
        int phase = time >> (16 - 6) & (blip_res - 1);
        int impptr = (short) (blip_res - phase); // points to impulses
        int bufptr = time >> 16; // points to buf.buffer_
        long i0 = impulses[impptr];

        int fwd = (blip_widest_impulse_ - quality) / 2;
        int rev = fwd + quality - 2;

        {
            if (bufptr + fwd < 0) {
                System.out.println();
            }

            long t0 = i0 * delta + buf.buffer_[bufptr + fwd];
            long t1 = (long) impulses[impptr + blip_res] * delta + buf.buffer_[bufptr + fwd + 1];
            i0 = impulses[impptr + blip_res * 2];
            buf.buffer_[bufptr + fwd] = t0;
            buf.buffer_[bufptr + fwd + 1] = t1;
        }

        if (quality > 8) {
            long t0 = i0 * delta + buf.buffer_[bufptr + fwd + 2];
            long t1 = (long) impulses[impptr + blip_res * (3)] * delta + buf.buffer_[bufptr + fwd + 3];
            i0 = impulses[impptr + blip_res * (4)];
            buf.buffer_[bufptr + fwd + 2] = t0;
            buf.buffer_[bufptr + fwd + 3] = t1;
        }
        if (quality > 12) {
            long t0 = i0 * delta + buf.buffer_[bufptr + fwd + 4];
            long t1 = (long) impulses[impptr + blip_res * (4 + 1)] * delta + buf.buffer_[bufptr + fwd + 1 + 4];
            i0 = impulses[impptr + blip_res * (4 + 2)];
            buf.buffer_[bufptr + fwd + 4] = t0;
            buf.buffer_[bufptr + fwd + 5] = t1;
        }
        {
            int mid = quality / 2 - 1;
            long t0 = i0 * delta + buf.buffer_[bufptr + fwd + mid - 1];
            long t1 = (long) impulses[impptr + blip_res * mid] * delta + buf.buffer_[bufptr + fwd + mid];
            impptr = phase;
            i0 = impulses[impptr + blip_res * mid];
            buf.buffer_[bufptr + fwd + mid - 1] = t0;
            buf.buffer_[bufptr + fwd + mid] = t1;
        }
        if (quality > 12) { // r = 6
            long t0 = i0 * delta + buf.buffer_[bufptr + rev - 6];
            long t1 = (long) impulses[impptr + blip_res * 6] * delta + buf.buffer_[bufptr + rev - 5];
            i0 = impulses[impptr + blip_res * 5];
            buf.buffer_[bufptr + rev - 6] = t0;
            buf.buffer_[bufptr + rev - 5] = t1;
        }
        if (quality > 8) { // r = 4
            long t0 = i0 * delta + buf.buffer_[bufptr + rev - 4];
            long t1 = (long) impulses[impptr + blip_res * 4] * delta + buf.buffer_[bufptr + rev - 3];
            i0 = impulses[impptr + blip_res * 3];
            buf.buffer_[bufptr + rev - 4] = t0;
            buf.buffer_[bufptr + rev - 3] = t1;
        }
        { // r = 2
            long t0 = i0 * delta + buf.buffer_[bufptr + rev - 2];
            long t1 = (long) impulses[impptr + blip_res * 2] * delta + buf.buffer_[bufptr + rev - 1];
            i0 = impulses[impptr + blip_res];
            buf.buffer_[bufptr + rev - 2] = t0;
            buf.buffer_[bufptr + rev - 1] = t1;
        }

        long t0 = i0 * delta + buf.buffer_[bufptr + rev];
        long t1 = (long) impulses[impptr] * delta + buf.buffer_[bufptr + rev + 1];
        buf.buffer_[bufptr + rev] = t0;
        buf.buffer_[bufptr + rev + 1] = t1;
    }

    public final int quality, range;

    // typedef short imp_t;
    private final short[] impulses;
    private final BlipSynth_ impl;

}
