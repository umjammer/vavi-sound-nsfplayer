package com.zdream.famitracker.sound;

/**
 * <p>When the buffer is full, let the system play this piece of data
 * <p>Used to play the audio when the buffer is full
 *
 * @author Zdream
 */
public interface IAudioCallback {

    /**
     * @param buffer audio sample array
     * @param offset
     * @param length
     */
    void flushBuffer(byte[] buffer, int offset, int length);

}
