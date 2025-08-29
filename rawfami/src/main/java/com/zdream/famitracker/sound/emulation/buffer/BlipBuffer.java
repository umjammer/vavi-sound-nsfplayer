package com.zdream.famitracker.sound.emulation.buffer;

import java.util.Arrays;

import static com.zdream.famitracker.sound.emulation.buffer.BufferContext.blip_max_length;
import static com.zdream.famitracker.sound.emulation.buffer.BufferContext.blip_sample_bits;
import static com.zdream.famitracker.sound.emulation.buffer.BufferContext.blip_widest_impulse_;
import static com.zdream.famitracker.sound.emulation.buffer.BufferContext.buffer_extra;


/**
 * <p>Source file Blip_Buffer 0.4.0
 * <p>Band-limited sound synthesis and buffering
 *
 * @author Zdream
 */
public class BlipBuffer {

    /**
     * @see #setSampleRate(int, int)
     */
    public void setSampleRate(int new_rate) {
        setSampleRate(new_rate, 250);
    }

    /**
     * <p>Set the output sample rate and buffer length (need to be converted to milliseconds, the default is 1/4 second, that is, 250 milliseconds), and then clear the buffer.<br>
     * If successful, return null, if there is not enough memory, an error is thrown, but it does not affect the current buffer settings.
     * <p>Set output sample rate and buffer length in milliseconds (1/1000 sec, defaults
     * to 1/4 second), then clear buffer.<br>
     * If there isn't enough memory, throw error without affecting current buffer setup.
     * <p>The same method in the original project sampleRate(1, 2)
     *
     * @param new_rate output sample rate, how many sample points per second
     * @param msec     buffer length, in milliseconds, the default value of the call is 1000 / 4 = 250
     * @throws IllegalStateException The required buffer length has exceeded the maximum limit, possibly due to insufficient memory
     */
    public void setSampleRate(int new_rate, int msec) throws IllegalStateException {
        // Originally (0xFFFFFFFFUL >> 16) - buffer_extra - 64;
        int new_size = 65535 - buffer_extra - 64;
        if (msec != blip_max_length) {
            int s = (new_rate * (msec + 1) + 999) / 1000;
            if (s < new_size)
                new_size = s;
            else
                throw new IllegalStateException("The required buffer length has exceeded the maximum limit");
        }

        if (buffer_size_ != new_size) {
            buffer_ = new long[new_size + buffer_extra];
        }

        buffer_size_ = new_size;

        // update things based on the sample rate
        sample_rate_ = new_rate;
        length_ = new_size * 1000 / new_rate - 1;

        if (msec != 0)
            assert (length_ == msec); // ensure length is same as that passed in
        if (clock_rate_ != 0)
            clockRate(clock_rate_);
        bassFreq(bass_freq_);

        clear(true);

        // success and return
    }

    /**
     * <p>Set the number of clocks per second
     * <p>Set number of source time units per second
     *
     * @param rate
     */
    public void clockRate(int cps) {
        factor_ = clockRateFactor(clock_rate_ = cps);
    }

    /**
     * <p>End the current time frame according to the specified duration,
     * so that the sample data can be obtained when the {@link #readSamples()} method is called (along with any unread samples).
     * Start a new frame at the end of the current frame.
     * <p>End current time frame of specified duration and make its samples available
     * (along with any still-unread samples) for reading with read_samples(). Begins
     * a new time frame at the end of the current frame.
     *
     * @param time The specified duration, unit TODO
     * @throws IllegalStateException when the number of sample data will exceed the buffer length, that is, the calculated {@link #offset_} will exceed {@link #buffer_size_}
     */
    public void endFrame(int time) {
        offset_ += time * factor_;
        // time outside buffer length
        if (samplesAvail() > buffer_size_) {
            throw new IllegalStateException("The number of sample data will exceed the buffer length");
        }
    }

    /**
     * <p>Read {@code max_samples} sample points of data from the buffer to {@code dest}, and these data will be removed from the buffer after reading.<br>
     * Returns the number of sample points of data actually read (not the length of the array).<br>
     * If it is set to stereo ({@code stereo = true}), then after writing the data of one sample point,
     * skip the position of the data of one sample point on {@code dest}, and write the next data to the position of the data of the third sample point.<br>
     * This makes it easier to write the data of the two channels in one array.
     * <p>Read at most {@code max_samples} out of buffer into {@code dest}, removing them from
     * the buffer. Returns number of samples actually read and removed. If stereo is
     * true, increments 'dest' one extra time after writing each sample, to allow
     * easy interleving of two channels into a stereo output buffer.
     * <p>In addition, note that because the 16-bit sample data is to be written to a byte array of 8 bits per unit, one sample data occupies two byte bits.
     *
     * @param dest        has been converted to a byte array
     * @param offset      defaults to 0, which means where to start writing data from the dest array.<br>
     *                    In mono (stereo = false), it is generally 0<br>
     *                    In stereo (stereo = true), channel 1 is generally 0, and channel 2 is 2
     * @param max_samples This is a 16-bit sample, which should be less than or equal to dest.length / 2
     * @param stereo      defaults to false. If true, it means stereo
     * @return
     */
    public int readSamples(byte[] dest, int offset, int max_samples, boolean stereo) {
        int count = samplesAvail();
        if (count > max_samples)
            count = max_samples;

        if (count != 0) {
            final int sample_shift = blip_sample_bits - 16;
            long accum = reader_accum;
            int ptr = 0;
            int outptr = offset;

            if (!stereo) {
                for (int n = count; (--n) >= 0; ) {
                    long s = (accum >> sample_shift);
                    accum -= accum >> bass_shift;
                    accum += buffer_[ptr++];

                    short out;
                    if (s > Short.MAX_VALUE) {
                        out = Short.MAX_VALUE;
                    } else if (s < Short.MIN_VALUE) {
                        out = Short.MIN_VALUE;
                    } else {
                        out = (short) s;
                    }

                    // clamp sample
                    if (out != s)
                        out = (short) (0x7FFF - (s >> 24));

                    dest[outptr++] = (byte) out; // low bit
                    dest[outptr++] = (byte) ((out & 0xFF00) >> 8); // high bit
                }
            } else {
                for (int n = count; (--n) >= 0; ) {
                    long s = (accum >> sample_shift);
                    accum -= accum >> bass_shift;
                    accum += buffer_[ptr++];

                    short out;
                    if (s > Short.MAX_VALUE) {
                        out = Short.MAX_VALUE;
                    } else if (s < Short.MIN_VALUE) {
                        out = Short.MIN_VALUE;
                    } else {
                        out = (short) s;
                    }

                    // clamp sample
                    if (out != s)
                        out = (short) (0x7FFF - (s >> 24));

                    dest[outptr++] = (byte) out; // low bit
                    dest[outptr++] = (byte) ((out & 0xFF00) >> 8); // high bit

                    outptr += 2;
                }
            }

            reader_accum = accum;
            removeSamples(count);
        }
        return count;
    }

// Additional optional features

    /**
     * <p>Sample rate
     * <p>Current output sample rate, const function
     *
     * @return
     */
    public final int sampleRate() {
        return sample_rate_;
    }

	/*public void sampleRate(int r, int msec) {
		setSampleRate(r, msec);
	}*/

    /**
     * <p>The length of the buffer, in milliseconds
     * <p>Length of buffer, in milliseconds
     *
     * @return
     */
    public final int length() {
        return length_;
    }

    /**
     * <p>Get the number of clocks per second
     * <p>Number of source time units per second
     *
     * @return
     */
    public final int clockRate() {
        return clock_rate_;
    }

    /**
     * <p>Set the frequency high-pass filter frequency, where higher values reduce the bass more.
     * <p>Set frequency high-pass filter frequency, where higher values reduce bass more
     *
     * @param freq
     */
    public void bassFreq(int freq) {
        bass_freq_ = freq;
        int shift = 31;
        if (freq > 0) {
            shift = 13;
            long f = ((long) freq << 16) / sample_rate_;
            while ((f >>= 1) > 0 && (--shift > 0)) {
            }
        }
        bass_shift = shift;
    }

    /**
     * <p>Number of sample delays from synthesis to readout
     * <p>Number of samples delay from synthesis to samples read out
     *
     * @return
     */
    public int outputLatency() {
        return blip_widest_impulse_ / 2;
    }

    /**
     * <p>Clear all sample data and clear the buffer.<br>
     * If {@code entire = false}, only clear the sample data waiting, not the entire buffer.
     * <p>Remove all available samples and clear buffer to silence.<br>
     * If {@code entire} is false, just clears out any samples waiting rather than the entire buffer.
     *
     * @param entire entire_buffer, whether to choose to clear the data of the entire buffer. Default true
     */
    public void clear(boolean entire) {
        offset_ = 0;
        reader_accum = 0;
        if (buffer_ != null) {
            int count = (entire ? buffer_size_ : samplesAvail());
            Arrays.fill(buffer_, 0, count + buffer_extra, 0);
        }
    }

    /**
     * <p>Returns how much sample data can be read by {@link #readSamples(byte[], int, int, boolean)}.
     * <p>Number of samples available for reading with read_samples()
     *
     * @return
     */
    public final int samplesAvail() {
        return offset_ >> 16;
    }

    /**
     * <p>Remove 'count' samples from those waiting to be read
     *
     * @param count
     */
    public void removeSamples(int count) {
        removeSilence(count);

        // copy remaining samples to beginning and clear old samples
        int remain = samplesAvail() + buffer_extra;
        long[] buffer2 = new long[buffer_.length];
        System.arraycopy(buffer_, count, buffer2, 0, remain);

        buffer_ = buffer2;
    }

// Experimental features

    /**
     * <p>Number of raw samples that can be mixed within frame of specified duration.
     *
     * @param duration
     * @return
     */
    public final int countSamples(int duration) {
        int last_sample = resampledTime(duration) >> 16;
        int first_sample = offset_ >> 16;
        return last_sample - first_sample;
    }

    /**
     * <p>Mix 'count' samples from 'buf' into buffer.
     */
    public void mixSamples(short[] buf, int offset, int count) {
        int outptr = (offset_ >> 16) + blip_widest_impulse_ / 2;
        int inptr = offset;

        final int sample_shift = blip_sample_bits - 16;
        long prev = 0;
        while (count-- > 0) {
            long s = buf[inptr++] << sample_shift;
            buffer_[outptr] += s - prev;
            prev = s;
            ++outptr;
        }
        buffer_[outptr] -= prev;
    }

    /**
     * <p>Count number of clocks needed until 'count' samples will be available.
     * If buffer can't even hold 'count' samples, returns number of clocks until
     * buffer becomes full.
     *
     * @param count
     * @return
     */
    public final int countClocks(int count) {
        if (count > buffer_size_)
            count = buffer_size_;
        int time = count << 16;
        return ((time - offset_ + factor_ - 1) / factor_);
    }

    // not documented yet
    public void removeSilence(int count) throws IllegalArgumentException {
        if (count > samplesAvail()) {
            // tried to remove more samples than available
            throw new IllegalArgumentException("count > samplesAvail");
        }
        offset_ -= count << 16;
    }

    public final int resampledDuration(int t) {
        return t * factor_;
    }

    public final int resampledTime(int t) {
        return t * factor_ + offset_;
    }

    private int clockRateFactor(int clock_rate) {
        double ratio = (double) sample_rate_ / clock_rate;
        int factor = (int) Math.floor(ratio * (1L << 16) + 0.5);
        assert (factor > 0 || sample_rate_ == 0); // fails if clock/output ratio is too large
        return factor;
    }

    public BlipBuffer() {
        factor_ = Integer.MAX_VALUE;
        bass_freq_ = 16;
    }

    /**
     * I guess, milliseconds * factor_ = number of samples
     */
    public int factor_;
    public int offset_;

    /**
     * This is the storage location of the sample data, which is the calculated sample data
     */
    long[] buffer_;
    int buffer_size_;

    /**
     * I guess that the value of each calculated sample data is different from the value of the actually played sample data,
     * The data played at the next sampling point is actually determined by the calculated value of the previous sampling point and the calculated value of the next sampling point.
     * Therefore, it is necessary to store the calculated value of the previous sampling point
     */
    long reader_accum; // determined to be long
    int bass_shift;
    int sample_rate_;
    int clock_rate_;
    private int bass_freq_;
    int length_;
}
