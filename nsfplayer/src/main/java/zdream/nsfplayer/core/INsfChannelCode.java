package zdream.nsfplayer.core;

/**
 * <p>Static variable to store channel number
 * </p>
 *
 * @author Zdream
 * @version v0.2.10
 * Added the statement of 2A07 chip number. Now the triangle,
 * noise and DPCM in the original 2A03 chip are moved to 2A07.
 * @since v0.2.1
 */
public interface INsfChannelCode {

    /**
     * The identification number of each track
     */
    byte
            CHANNEL_2A03_PULSE1 = 1,
            CHANNEL_2A03_PULSE2 = 2,
            CHANNEL_2A03_TRIANGLE = 3,
            CHANNEL_2A03_NOISE = 4,
            CHANNEL_2A03_DPCM = 5,

    CHANNEL_VRC6_PULSE1 = 0x11,
            CHANNEL_VRC6_PULSE2 = 0x12,
            CHANNEL_VRC6_SAWTOOTH = 0x13,

    CHANNEL_VRC7_FM1 = 0x21,
            CHANNEL_VRC7_FM2 = 0x22,
            CHANNEL_VRC7_FM3 = 0x23,
            CHANNEL_VRC7_FM4 = 0x24,
            CHANNEL_VRC7_FM5 = 0x25,
            CHANNEL_VRC7_FM6 = 0x26,

    CHANNEL_FDS = 0x31,

    CHANNEL_MMC5_PULSE1 = 0x41,
            CHANNEL_MMC5_PULSE2 = 0x42,

    CHANNEL_N163_1 = 0x51,
            CHANNEL_N163_2 = 0x52,
            CHANNEL_N163_3 = 0x53,
            CHANNEL_N163_4 = 0x54,
            CHANNEL_N163_5 = 0x55,
            CHANNEL_N163_6 = 0x56,
            CHANNEL_N163_7 = 0x57,
            CHANNEL_N163_8 = 0x58,

    CHANNEL_S5B_SQUARE1 = 0x61,
            CHANNEL_S5B_SQUARE2 = 0x62,
            CHANNEL_S5B_SQUARE3 = 0x63;

    /**
     * Identification number of each chip
     *
     * @since v0.2.3
     */
    byte
            CHIP_2A03 = 0,
            CHIP_2A07 = 3,
            CHIP_VRC6 = 0x10,
            CHIP_VRC7 = 0x20,
            CHIP_FDS = 0x30,
            CHIP_MMC5 = 0x40,
            CHIP_N163 = 0x50,
            CHIP_S5B = 0x60;

    /**
     * <p>channel Type Number
     * <p>For example, the two rectangular pulse tracks of 2A03 can be considered as one class, one type;
     * </p>
     *
     * @since v0.2.7
     */
    byte
            CHANNEL_TYPE_PULSE = CHANNEL_2A03_PULSE1,
            CHANNEL_TYPE_TRIANGLE = CHANNEL_2A03_TRIANGLE,
            CHANNEL_TYPE_NOISE = CHANNEL_2A03_NOISE,
            CHANNEL_TYPE_DPCM = CHANNEL_2A03_DPCM,
            CHANNEL_TYPE_VRC6_PULSE = CHANNEL_VRC6_PULSE1,
            CHANNEL_TYPE_SAWTOOTH = CHANNEL_VRC6_SAWTOOTH,
            CHANNEL_TYPE_VRC7 = CHANNEL_VRC7_FM1,
            CHANNEL_TYPE_FDS = CHANNEL_FDS,
            CHANNEL_TYPE_MMC5_PULSE = CHANNEL_MMC5_PULSE1,
            CHANNEL_TYPE_N163 = CHANNEL_N163_1,
            CHANNEL_TYPE_S5B = CHANNEL_S5B_SQUARE1,
            CHANNEL_TYPE_CUSTOM = 0x70;
}
