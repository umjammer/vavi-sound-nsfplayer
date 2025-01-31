package zdream.nsfplayer.core;

import static zdream.nsfplayer.core.INsfChannelCode.CHIP_2A03;
import static zdream.nsfplayer.core.INsfChannelCode.CHIP_2A07;
import static zdream.nsfplayer.core.INsfChannelCode.CHIP_FDS;
import static zdream.nsfplayer.core.INsfChannelCode.CHIP_MMC5;
import static zdream.nsfplayer.core.INsfChannelCode.CHIP_N163;
import static zdream.nsfplayer.core.INsfChannelCode.CHIP_S5B;
import static zdream.nsfplayer.core.INsfChannelCode.CHIP_VRC6;
import static zdream.nsfplayer.core.INsfChannelCode.CHIP_VRC7;


/**
 * Chip enumeration used by FamiTracker and NsfPlayer.
 *
 * @author Zdream
 * @version v0.2.5
 * Supplementary S5B chips and implementation of related methods
 * @since v0.2.3
 */
public enum FtmChipType {

    _2A03(CHIP_2A03),

    _2A07(CHIP_2A07),

    VRC6(CHIP_VRC6),

    VRC7(CHIP_VRC7),

    FDS(CHIP_FDS),

    MMC5(CHIP_MMC5),

    N163(CHIP_N163),

    S5B(CHIP_S5B);

    public final byte chipCode;

    FtmChipType(byte chipCode) {
        this.chipCode = chipCode;
    }

    public static FtmChipType get(int index) {
        return values()[index];
    }

    /**
     * Converting between code and enumerations
     *
     * @param code View {@link INsfChannelCode} constant amount
     * @return
     * @since v0.2.4
     */
    public static FtmChipType ofChipCode(byte code) {
        return switch (code) {
            case CHIP_2A03 -> _2A03;
            case CHIP_2A07 -> _2A07;
            case CHIP_VRC6 -> VRC6;
            case CHIP_VRC7 -> VRC7;
            case CHIP_FDS -> FDS;
            case CHIP_MMC5 -> MMC5;
            case CHIP_N163 -> N163;
            case CHIP_S5B -> S5B;
            default -> null;
        };
    }
}
