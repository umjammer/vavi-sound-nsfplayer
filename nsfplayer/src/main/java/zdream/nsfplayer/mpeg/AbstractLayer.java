package zdream.nsfplayer.mpeg;

/**
 * Superclass for decoding audio data
 *
 * @author Zdream
 * @date 2018-01-17
 * @since v0.1
 */
public abstract class AbstractLayer {

    /**
     * Pass the frame header data to initialize decoding
     *
     * @param head
     */
    public abstract void ready(MpegFrameHead head);

    /**
     * decoding
     *
     * @param bs     data
     * @param offset Offset, excluding the frame header data, and hopefully also excluding the CRC check field
     * @param length Data length
     * @return
     */
    public abstract byte[] decode(byte[] bs, int offset, int length);

    /**
     * Number of channels
     */
    private int channels;

    /*
     * 5 variables for managing PCM buffers
     */
    protected byte[] pcmbuf;
    private int size;            // The length of the audio buffer pcmbuf to be written at one time
    private int[] writeCursor;    // The offsets used when writing data to pcmbuf for both channels

    public AbstractLayer() {

    }

    protected void init(MpegFrameHead head) {
        size = 2 * head.getPcmSize();    //#### Several numbers cannot be changed

        channels = head.getChannels();
        filter = new Synthesis(channels);
        writeCursor = new int[2];
        writeCursor[1] = 2;                //####
        pcmbuf = new byte[size * 4];    //####
    }

    /*
     * Layer1, Layer2, and Layer3 all have a filter.
     */
    private Synthesis filter;

    // test
    int count = 0;

    /**
     * A sub-band multiphase synthesis filter.
     *
     * @param samples 32 sample values of the input.
     * @param ch      Current channel. 0 represents the left channel, 1 represents the right channel.
     */
    protected final void synthesisSubBand(float[] samples, int ch) {
        writeCursor[ch] = filter.synthesisSubBand(samples, ch, pcmbuf, writeCursor[ch]);
    }

    /**
     * Try to output the decoded result.
     * <p>The PCM data output by the polyphase synthesis filter is written to the buffer.
     * The output is generated only when the buffer is filled with at least 4 frames of
     * PCM data obtained by decoding. However, the caller does not need to know whether
     * the current buffer has been filled with enough data. To prevent buffer overflow,
     * this method should be called once for every 4 frames decoded. Of course,
     * this method can also be called once for every frame decoded.
     * <p>If audio output is generated, the PCM data obtained by decoding 4 frames
     * will be taken from the buffer.
     * <p><b>Possible blocking:</b> If audio output has stopped, calling this method will block until
     * audio output starts. If the input stream is decoded, the block is automatically cleared.
     *
     * @see #startAudio()
     */
    protected byte[] outputPCM() {
        byte[] ret = new byte[writeCursor[0]];
        System.arraycopy(pcmbuf, 0, ret, 0, writeCursor[0]);

        // size = 0;
        writeCursor[0] = 0;
        writeCursor[1] = 2;

        return ret;
    }
}