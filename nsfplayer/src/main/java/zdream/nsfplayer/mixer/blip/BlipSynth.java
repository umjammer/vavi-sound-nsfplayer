package zdream.nsfplayer.mixer.blip;

import static zdream.nsfplayer.mixer.blip.BufferContext.blip_res;
import static zdream.nsfplayer.mixer.blip.BufferContext.blip_widest_impulse_;


/**
 * <p>Blip track input tool, used to fill {@link BlipBuffer} with audio.
 * </p>
 *
 * @author Zdream
 */
public class BlipSynth {

    /**
     * <p>Range specifies the greatest expected change in amplitude.
     * Calculate it by finding the difference between the maximum and minimum expected amplitudes (max - min).
     * </p>
     *
     * @param quality The quality of the input audio, i.e. the bit rate,
     *               how many bits each sample consists of. The default is 16
     * @param range   Maximum expected change in the specified amplitude
     */
    public BlipSynth(int quality, int range) {
        this.quality = quality;
        this.range = range;
        impulses = new short[blip_res * (quality / 2) + 1];
        impl = new BlipSynth_(impulses, quality);
    }

    /**
     * <p>Set overall volume of waveform
     *
     * @param v
     */
    public void volume(double v) {
        impl.volume_unit(v * (1.0 / (range < 0 ? -range : range)));
    }

    /**
     * <p>Configure low-pass filter
     */
    public void trebleEq(BlipEQ eq) {
        impl.trebleEq(eq);
    }

    /**
     * <p>Get BlipBuffer used for output
     *
     * @return
     */
    public final BlipBuffer output() {
        return impl.buf;
    }

    /**
     * <p>Set BlipBuffer used for output
     *
     * @param b
     */
    public void output(BlipBuffer b) {
        impl.buf = b;
        impl.last_amp = 0;
    }

    /**
     * <p>Update amplitude of waveform at given time.<br>
     * Using this requires a separate BlipSynth for each waveform.
     *
     * @param time
     * @param amplitude
     */
    public void update(int time, int amplitude) {
        int delta = amplitude - impl.last_amp;
        impl.last_amp = amplitude;
        offset_resampled(time * ((factor_ != 0) ? factor_ : impl.buf.factor_) + impl.buf.offset_,
                delta, impl.buf);
    }

// Low-level interface

    /**
     * <p>Add an amplitude transition of specified delta,
     * optionally into specified buffer rather than the one set with output().<br>
     * Delta can be positive or negative.
     * The actual change in amplitude is delta * (volume / range)
     *
     * @param time  The time is in units of the input sampling rate, which for NSF tracks is clocks per second.
     * @param delta Audio data, can be positive or negative
     * @param buf
     */
    public final void offset(int time, int delta, BlipBuffer buf) {
        offset_resampled(time * ((factor_ != 0) ? factor_ : buf.factor_) + buf.offset_, delta, buf);
    }

    public final void offset(int t, int delta) {
        offset(t, delta, impl.buf);
    }

    /**
     * <p>Use the score output samples directly.
     * <p>Works directly in terms of fractional output samples.
     *
     * @param time  clock
     * @param delta
     * @param buf
     */
    public final void offset_resampled(int time, int delta, BlipBuffer buf) {
        // Fails if time is beyond end of Blip_Buffer, due to a bug in caller code or the
        // need for a longer buffer as set by set_sample_rate().
        assert ((long) (time >> 16) < buf.buffer_size_);

        delta *= impl.delta_factor;
        int phase = time >> (16 - 6) & (blip_res - 1);
        int impptr = (short) (blip_res - phase); // Pointing to impulses
        int bufptr = time >> 16; // Pointer to buf.buffer_
        long i0 = impulses[impptr];

        int fwd = (blip_widest_impulse_ - quality) / 2;
        int rev = fwd + quality - 2;

        {
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

    private final short[] impulses;
    private final BlipSynth_ impl;

    /**
     * The default value is 0, which means it is not enabled;
     * If it is not 0, this value is used instead of the global factor_ provided by BlipBuffer
     */
    private int factor_;

    /**
     * If you have a custom input sampling rate, set it here.
     *
     * @param rate The sampling rate of the input.
     *             If it is 0, use the global input sampling rate
     * @since v0.3.0
     */
    public void in_sample_rate(int rate) {
        if (rate <= 0) {
            factor_ = 0;
        } else {
            this.factor_ = impl.buf.clockRateFactor(rate);
        }
    }

    /**
     * If you have a custom input sampling rate, set it here.
     *
     * @param rate Input sampling rate
     *             If it is 0, use the global input sampling rate
     * @param buf  The specified audio buffer
     * @since v0.3.0
     */
    public void in_sample_rate(int rate, BlipBuffer buf) {
        if (rate <= 0) {
            factor_ = 0;
        } else {
            this.factor_ = buf.clockRateFactor(rate);
        }
    }
}
