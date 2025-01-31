package zdream.nsfplayer.mpeg;

/**
 * <p>Audio frame header in Mpeg format.
 * <p>Mpeg format audio is a stream structure consisting of frames connected side by side.
 * Each frame begins with 32 bits of data, which is used to identify the relevant information
 * of the frame.
 * <br>This class is used to save frame header information.
 *
 * <p>Supplement: The variables and meanings used to parse the 32-bit frame header in the source code:
 * <table border="2" bordercolor="#000000" cellpadding="8" style="border-collapse:collapse">
 * <tr><th>Offset</th><th>Length</th><th>Variable Name</th><th>Meaning</th></tr>
 * <tr><td>0</td><td>11</td><td>Direct analysis during frame synchronization</td><td>11 bits of frame synchronization word all set to '1'</td></tr>
 * <tr><td>11</td><td>2</td><td>verID</td><td>MPEG version</td></tr>
 * <tr><td>13</td><td>2</td><td>layer</td><td>MPEG Compression Layer</td></tr>
 * <tr><td>15</td><td>1</td><td>protection_bit</td><td>Is CRC</td></tr>
 * <tr><td>16</td><td>4</td><td>bitrate_index</td><td>Bit rate index</td></tr>
 * <tr><td>20</td><td>2</td><td>sampling_frequency</td><td>Sampling Rate Index</td></tr>
 * <tr><td>22</td><td>1</td><td>padding</td><td>Whether the current frame should add a slot of data to fill</td></tr>
 * <tr><td>23</td><td>1</td><td>Unresolved</td><td>Tell if it is private</td></tr>
 * <tr><td>24</td><td>2</td><td>mode</td><td>Channel Mode</td></tr>
 * <tr><td>26</td><td>2</td><td>mode_extension</td><td>Channel Expansion Mode</td></tr>
 * <tr><td>28</td><td>1</td><td>Unresolved</td><td>Tell me if there is copyright</td></tr>
 * <tr><td>29</td><td>1</td><td>Unresolved</td><td>Tell whether it is the original version</td></tr>
 * <tr><td>30</td><td>2</td><td>Uncommon, unresolved</td><td>Pre-emphasis</td></tr>
 * </table>
 *
 * @author Zdream
 * @date 2018-01-16
 * @since v0.1
 */
public class MpegFrameHead {

    /**
     * MPEG version MPEG-1
     */
    public static final byte MPEG1 = 3;

    /**
     * MPEG version MPEG-2
     */
    public static final byte MPEG2 = 2;

    /**
     * MPEG version MPEG-2.5（Unofficial version）
     */
    public static final byte MPEG25 = 0;

    /**
     * <p>Only 2 bits are valid for characters.
     * <p>
     * 0 : MPEG-2.5 (Unofficial version); see {@link #MPEG25}<br>
     * 1 : Invalid<br>
     * 2 : MPEG-2 (ISO/IEC 13818-3); see {@link #MPEG2}<br>
     * 3 : MPEG-1 (ISO/IEC 11172-3). See {@link #MPEG1}
     */
    private byte verID;

    /**
     * Audio Data Layer 1
     */
    public static final byte LAYER1 = 3;

    /**
     * Audio Data Layer 2
     */
    public static final byte LAYER2 = 2;

    /**
     * Audio Data Layer 3
     */
    public static final byte LAYER3 = 1;

    /**
     * <p>Audio data stored at layer level, 2-bit
     * <p>
     * 3 : Layer I; see {@link #LAYER1}<br>
     * 2 : Layer II; see {@link #LAYER2}<br>
     * 1 : Layer III; see {@link #LAYER3}<br>
     * 0 : invalid
     */
    private byte layer;

    /**
     * <p>Whether to use CRC for verification
     * <p>In the original data, 0 is to use CRC check (true), 1 is not to use (false)
     */
    private boolean protection_bit;

    /**
     * Audio bit rate comparison table
     */
    private static final int[][][] BITRATE = {
            {
                    //MPEG-1
                    //Layer I
                    {0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448},
                    //Layer II
                    {0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384},
                    //Layer III
                    {0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320}
            }, {
            //MPEG-2/2.5
            //Layer I
            {0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256},
            //Layer II
            {0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160},
            //Layer III = Layer II
            null
    }
    };

    static {
        BITRATE[1][2] = BITRATE[1][1];
    }

    /**
     * samplingRate[verID][sampling_frequency]
     */
    private static final int[][] SAMPLING_RATE = {
            {11025, 12000, 8000, 0}, // MPEG-2.5
            null, // none
            {22050, 24000, 16000, 0}, // MPEG-2 (ISO/IEC 13818-3)
            {44100, 48000, 32000, 0} // MPEG-1 (ISO/IEC 11172-3)
    };

    private int padding;

    /**
     * <p>channel Mode
     * <p>MODE_STEREO: stereo<br>
     * MODE_JOINT_STEREO: Joint Stereo<br>
     * MODE_DUAL_CHANNEL: Two separate mono channels<br>
     * MODE_MONO: Mono
     */
    public static final byte
            MODE_STEREO = 0,
            MODE_JOINT_STEREO = 1,
            MODE_DUAL_CHANNEL = 2,
            MODE_MONO = 3;

    /**
     * channel, see {@link #MODE_STEREO}, {@link #MODE_JOINT_STEREO},
     * {@link #MODE_DUAL_CHANNEL}, {@link #MODE_MONO}
     */
    private byte mode;

    /**
     * Bit rate
     */
    private int bitrate;

    private int samplingRate;

    /**
     * Index value of the PCM sample rate
     */
    private int samplingFrequency;

    private int framesize;

    /**
     * Master data length
     */
    private int maindatasize;

    /**
     * Frame edge information length
     */
    private int sideinfosize;

    private int lsf;
    private boolean isMS, isIntensity;

    /**
     * initialization.
     */
    protected void reset() {
        layer = 0;
        sideinfosize = framesize = 0;
        verID = 1;
    }

    /**
     * Frame header decoding. The decoded data will be stored directly in this class
     *
     * @param i Frame header, 4-byte (32-bit) integer.
     */
    protected void decode(int i) {
        verID = (byte) ((i >> 19) & 3);
        layer = (byte) ((i >> 17) & 3);
        protection_bit = ((i >> 16) & 0x1) == 0;
        int bitrate_index = (i >> 12) & 0xF;

        samplingFrequency = (i >> 10) & 3;
        padding = (i >> 9) & 0x1;
        mode = (byte) ((i >> 6) & 3);
        int mode_extension = (i >> 4) & 3;

        isMS = mode == 1 && (mode_extension & 2) != 0;
        isIntensity = mode == 1 && (mode_extension & 0x1) != 0;
        lsf = (verID == MPEG1) ? 0 : 1;

        samplingRate = SAMPLING_RATE[verID][samplingFrequency];

        switch (layer) {
            case LAYER1:
                bitrate = BITRATE[lsf][0][bitrate_index];
                framesize = bitrate * 12000;
                framesize /= samplingRate;
                framesize += padding;
                framesize <<= 2; // 1-slot = 4-byte
                break;
            case LAYER2:
                bitrate = BITRATE[lsf][1][bitrate_index];
                framesize = bitrate * 144000;
                framesize /= samplingRate;
                framesize += padding;
                break;
            case LAYER3:
                bitrate = BITRATE[lsf][2][bitrate_index];
                framesize = bitrate * 144000;
                framesize /= samplingRate << lsf;
                framesize += padding;

                // Calculate the frame edge information length
                if (verID == MPEG1)
                    sideinfosize = (mode == 3) ? 17 : 32;
                else
                    sideinfosize = (mode == 3) ? 9 : 17;
                break;
        }

        // Calculate the master data length
        maindatasize = framesize - 4 - sideinfosize;

        if (protection_bit)
            maindatasize -= 2;    //CRC-word
    }

    /**
     * Is there a cyclic redundancy check code?
     *
     * @return Returning true indicates that there is a cyclic redundancy check code, and there are 2 bytes
     *         of data following the frame header for CRC.
     */
    public boolean isProtected() {
        return protection_bit;
    }

    /**
     * Gets whether the channel mode is Mid/Side stereo mode.
     *
     * @return True indicates mid/side stereo mode.
     */
    public boolean isMS() {
        return isMS;
    }

    /**
     * Gets whether the channel mode is intensity stereo mode.
     *
     * @return True indicates intensity stereo mode.
     */
    public boolean isIntensityStereo() {
        return isIntensity;
    }

    /**
     * Get the bit rate of the current frame.
     *
     * @return The bit rate of the current frame, in kilobits per second (Kbps).
     */
    public int getBitrate() {
        return bitrate;
    }

    /**
     * Get the number of channels.
     *
     * @return Number of channels: 1 or 2.
     */
    public int getChannels() {
        return (mode == 3) ? 1 : 2;
    }

    /**
     * Get the channel mode.
     *
     * @return Channel mode, its value means:
     * <table border="1" bordercolor="#000000" cellpadding="8" style="border-collapse:collapse">
     * <tr><th>Return value: Channel mode</th></tr>
     * <tr><td>0</td><td>stereo</td></tr>
     * <tr><td>1</td><td>joint stereo</td></tr>
     * <tr><td>2</td><td>dual channel</td></tr>
     * <tr><td>3</td><td>mono channel</td></tr>
     * </table>
     * @see #getModeExtension()
     */
    public int getMode() {
        return mode;
    }

    /**
     * @return MPEG version: {@link #MPEG1}, {@link #MPEG2}, or {@link #MPEG25}.
     */
    public byte getVersion() {
        return verID;
    }

    public boolean isMPEG1() {
        return verID == MPEG1;
    }

    public boolean isMPEG2() {
        return verID == MPEG2;
    }

    public boolean isMPEG25() {
        return verID == MPEG25;
    }

    /**
     * Get the MPEG encoding layer.
     *
     * @return See {@link #LAYER1}, {@link #LAYER2}, {@link #LAYER3}
     */
    public byte getLayer() {
        return layer;
    }

    public boolean isLayer1() {
        return layer == LAYER1;
    }

    public boolean isLayer2() {
        return layer == LAYER2;
    }

    public boolean isLayer3() {
        return layer == LAYER3;
    }

    /**
     * Get the main data length.
     *
     * @return The main data length of the current frame, in bytes.
     */
    public int getMainDataSize() {
        return maindatasize;
    }

    /**
     * Get the length of the edge information.
     *
     * @return The current frame edge information length, in bytes.
     */
    public int getSideInfoSize() {
        return sideinfosize;
    }

    /**
     * Get the frame length. <p>Frame length = 4-byte frame header + CRC (if any, 2 bytes) + music data length.
     * <br>The music data length = side information length + main data length.
     * <p>Whether it is a file encoded with a variable bit rate (VBR) or a file encoded with a constant bit rate (CBR),
     * the length of each frame is not necessarily the same.
     *
     * @return The length of the current frame, in bytes.
     */
    public int getFrameSize() {
        return framesize;
    }

    /**
     * Get the length of the PCM sample obtained after decoding the current frame. Usually,
     * the length of the PCM sample obtained after decoding each frame of the same file is the same.
     *
     * @return The PCM sample length obtained after decoding the current frame, in bytes.
     */
    public int getPcmSize() {
        int pcmsize = (verID == MPEG1) ? 4608 : 2304;
        if (mode == 3) // if channels == 1
            pcmsize >>= 1;
        return pcmsize;
    }

    /**
     * @return The playback time of one frame of the current file, in seconds
     */
    public float getFrameDuration() {
        return 1152f / (samplingRate << lsf);
    }

    public int getSamplingFrequency() {
        return samplingFrequency;
    }

    /**
     * Get the brief information of the frame header.
     *
     * @return Brief information of the frame header.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();

        if (verID == MPEG25) buf.append("MPEG-2.5");
        else if (verID == MPEG2) buf.append("MPEG-2");
        else if (verID == MPEG1) buf.append("MPEG-1");
        else return "Let me tell you gently\nThe header is unavailable";

        buf.append(", Layer ");
        buf.append(layer);
        buf.append(", ");
        buf.append(samplingRate);
        buf.append("Hz, ");

        if (mode == 0) buf.append("Stereo");
        else if (mode == 1) buf.append("Joint Stereo");
        else if (mode == 2) buf.append("Dual channel");
        else if (mode == 3) buf.append("Mono");

        if (isMS) {
            buf.append((isIntensity) ? "(I/S & M/S)" : "(M/S)");
        } else if (isIntensity) {
            buf.append("(I/S)");
        }

        return buf.toString();
    }

    MpegFrameHead() {
    }
}
