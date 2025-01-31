package zdream.utils.common;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import static vavi.sound.SoundUtil.volume;


public class BytesPlayer {

    // additions

    /**
     * This one focuses on whether dateline.start() has been called or not
     */
    boolean started = false;

    // javax

    private SourceDataLine dateline;

    public BytesPlayer() {
        this(48000);
    }

    /**
     * Modifying the sample rate during playback
     *
     * @param s Sample rate, default 48000
     * @since v0.2.10
     */
    public BytesPlayer(int s) {
        AudioFormat af = new AudioFormat(s, 16, 1, true, false); // mono
        try {
            dateline = AudioSystem.getSourceDataLine(af);
            dateline.open(af, s);
        } catch (LineUnavailableException e) {
            System.err.println("Failed to initialize audio output.");
        }
    }

    public void setVolume(double volume) {
        volume(dateline, volume);
    }

    public int writeSamples(byte[] bs, int off, int len) {
        if (!started) {
            dateline.start();
            started = true;
        }
        return dateline.write(bs, off, len);
    }

    private byte[] bytes;

    /**
     * Write to short sampling array
     *
     * @param bs
     * @param off
     * @param len
     * @return Number of samples actually read
     * @since v0.2.9
     */
    public int writeSamples(short[] bs, int off, int len) {
        if (bytes == null) {
            bytes = new byte[len * 2];
        } else {
            if (len > bytes.length * 2) {
                bytes = new byte[len * 2];
            }
        }

        convert(bs, off, len);
        return writeSamples(bytes, 0, len * 2) / 2;
    }

    private void convert(short[] src, int off, int len) {
        int sptr = off;
        int dptr = 0;
        for (int i = 0; i < len; i++) {
            short sample = src[sptr++];
            bytes[dptr++] = (byte) sample; // lows
            bytes[dptr++] = (byte) ((sample & 0xff00) >> 8); // highs
        }
    }
}
