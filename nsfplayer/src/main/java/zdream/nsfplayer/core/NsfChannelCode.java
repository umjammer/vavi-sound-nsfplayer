package zdream.nsfplayer.core;

/**
 * Data conversion related to static variables of channel number
 *
 * @author Zdream
 * @since 0.2.3
 */
public class NsfChannelCode implements INsfChannelCode {

    /**
     * Check the chip number corresponding to the channel number.
     * If there is no channel corresponding to the channelCode, return -1
     *
     * @param channelCode channel number / channel type number. Static variables CHANNEL_*
     * @return
     */
    public static byte chipOfChannel(byte channelCode) {
        return switch (channelCode) {
            case CHANNEL_2A03_PULSE1, CHANNEL_2A03_PULSE2 -> CHIP_2A03;
            case CHANNEL_2A03_TRIANGLE, CHANNEL_2A03_NOISE, CHANNEL_2A03_DPCM -> CHIP_2A07;
            case CHANNEL_VRC6_PULSE1, CHANNEL_VRC6_PULSE2, CHANNEL_VRC6_SAWTOOTH -> CHIP_VRC6;
            case CHANNEL_VRC7_FM1, CHANNEL_VRC7_FM2, CHANNEL_VRC7_FM3, CHANNEL_VRC7_FM4, CHANNEL_VRC7_FM5,
                 CHANNEL_VRC7_FM6 -> CHIP_VRC7;
            case CHANNEL_FDS -> CHIP_FDS;
            case CHANNEL_MMC5_PULSE1, CHANNEL_MMC5_PULSE2 -> CHIP_MMC5;
            case CHANNEL_N163_1, CHANNEL_N163_2, CHANNEL_N163_3, CHANNEL_N163_4, CHANNEL_N163_5, CHANNEL_N163_6,
                 CHANNEL_N163_7, CHANNEL_N163_8 -> CHIP_N163;
            case CHANNEL_S5B_SQUARE1, CHANNEL_S5B_SQUARE2, CHANNEL_S5B_SQUARE3 -> CHIP_S5B;
            default -> -1;
        };
    }

    /**
     * Check the channel type corresponding to the channel number.
     * If there is no channel corresponding to the channelCode, return -1
     *
     * @param channelCode channel number. Static variables CHANNEL_*
     * @return
     * @since v0.2.7
     */
    public static byte typeOfChannel(byte channelCode) {
        return switch (channelCode) {
            case CHANNEL_2A03_PULSE1, CHANNEL_2A03_PULSE2 -> CHANNEL_TYPE_PULSE;
            case CHANNEL_2A03_TRIANGLE -> CHANNEL_TYPE_TRIANGLE;
            case CHANNEL_2A03_NOISE -> CHANNEL_TYPE_NOISE;
            case CHANNEL_2A03_DPCM -> CHANNEL_TYPE_DPCM;
            case CHANNEL_VRC6_PULSE1, CHANNEL_VRC6_PULSE2 -> CHANNEL_TYPE_VRC6_PULSE;
            case CHANNEL_VRC6_SAWTOOTH -> CHANNEL_TYPE_SAWTOOTH;
            case CHANNEL_VRC7_FM1, CHANNEL_VRC7_FM2, CHANNEL_VRC7_FM3, CHANNEL_VRC7_FM4, CHANNEL_VRC7_FM5,
                 CHANNEL_VRC7_FM6 -> CHANNEL_TYPE_VRC7;
            case CHANNEL_FDS -> CHANNEL_TYPE_FDS;
            case CHANNEL_MMC5_PULSE1, CHANNEL_MMC5_PULSE2 -> CHANNEL_TYPE_MMC5_PULSE;
            case CHANNEL_N163_1, CHANNEL_N163_2, CHANNEL_N163_3, CHANNEL_N163_4, CHANNEL_N163_5, CHANNEL_N163_6,
                 CHANNEL_N163_7, CHANNEL_N163_8 -> CHANNEL_TYPE_N163;
            case CHANNEL_S5B_SQUARE1, CHANNEL_S5B_SQUARE2, CHANNEL_S5B_SQUARE3 -> CHANNEL_TYPE_S5B;
            default -> -1;
        };
    }
}
