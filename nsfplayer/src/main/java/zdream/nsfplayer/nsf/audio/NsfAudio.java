package zdream.nsfplayer.nsf.audio;

import zdream.nsfplayer.core.AbstractNsfAudio;
import zdream.nsfplayer.core.ERegion;


/**
 * <p>Data in NSF audio files
 * <p>No playback related data will be stored here
 *
 * @author Zdream
 * @version v0.1
 * @date 2018-01-16
 */
public class NsfAudio extends AbstractNsfAudio {

    /**
     * Version number of the current NSF file<br>
     * Address 0x000005, single byte
     */
    public int version;
    /**
     * Number of songs in NSF<br>
     * Address 0x000006, single byte
     */
    public int total_songs;
    /**
     * The number of the starting song to be played<br>
     * Address 0x000007, single byte
     */
    public int start;

    /**
     * Memory address where data is loaded, range ($8000-$FFFF)<br>
     * Address 0x000008-0x000009, double byte<br><br>
     * <p>
     * This specifies the address in the console's RAM. If the game is run from RAM,
     * then the NSF will be placed in RAM.
     * Excluding the file header (address 0x000000 to 0x00007F),
     * the remaining data will be placed at the address corresponding to lenA
     */
    public int load_address;
    /**
     * Initialization data start address, range ($8000-$FFFF)<br>
     * Address 0x00000A-0x00000B, double byte
     */
    public int init_address;
    /**
     * Music playback address, range ($8000-$FFFF)<br>
     * Address 0x00000C-0x00000D, double byte
     */
    public int play_address;

    /*
     * The game or music title, composer or artist name, copyright part,
     * additional instructions, etc. are omitted
     */

    /**
     * The music loop playback speed under NTSC standard, usually [16666]
     */
    public int speed_ntsc;
    /**
     * Bank switch, initial 8 bit value<br>
     * <p>
     * The addressing space of 6502 assembly is 64K, but NES only uses $8000-$FFFF,
     * a total of 32K. For small games like Super Mario 1, there is no need to consider bank switching,
     * but for games like Contra 1 and 2, if the addressing space exceeds 32K, bank switching must be
     * performed. The size may be different, some are 16K, some are 32K, some are 8K, etc.
     * The address is different, $8000, $A000, $C000 are all possible.
     * NSF may also encounter insufficient space, in which case bank switching is required.
     * The NSF bank switching size is 4K.
     */
    public final short[] bankswitch = new short[8];
    /**
     * The music loop playback speed in PAL format, usually [20000]
     */
    public int speed_pal;
    /**
     * PAL/NTSC format selection<br>
     *
     * <p>Bit switch, data from left (high) to right (low), the first 6 bits are forced to be 0
     * <p>If the 7th bit is 1, NTSC/PAL, then the 8th bit must be 0; (= 2)<br>
     * Otherwise, the 7th bit is 0, the 8th bit is 0, which is NTSC format; if it is 1, it is PAL format.
     */
    public byte pal_ntsc;
    /**
     * <b>Special sound chip</b><br>
     * <p>
     * Bit switch, data from left (high) to right (low), first 2 bits forced to 0<br>
     * If the 3rd bit is 1, Sunsoft (FME7) chip is used;<br>
     * If the 4th bit is 1, use the Namcot (106) chip;<br>
     * If the 5th bit is 1, the Nintendo (MMC5) chip is used;<br>
     * If the 6th bit is 1, the Nintendo (FDS) chip is used;<br>
     * If bit 7 is 1, Konami (VRC7) chip is used;<br>
     * If bit 8 is 1, Konami (VRC6) chip is used;<br>
     * <p>
     * If f2 == 0, no chip is used
     */
    public byte soundChip;

    /**
     * title
     */
    public String title;

    /**
     * artist
     */
    public String artist;

    /**
     * Copyright
     */
    public String copyright;

    /**
     * Remaining data
     */
    public byte[] body;

    public boolean useVrc6() {
        return (soundChip & 1) != 0;
    }

    public boolean useVrc7() {
        return (soundChip & 2) != 0;
    }

    public boolean useFds() {
        return (soundChip & 4) != 0;
    }

    public boolean useMmc5() {
        return (soundChip & 8) != 0;
    }

    /**
     * <p>Whether the N163 chip is used.
     * <p>N163 (Namco 163) is also known as N106.
     *
     * @return
     */
    public boolean useN163() {
        return (soundChip & 16) != 0;
    }

    /**
     * <p>Whether the S5B chip is used.
     * <p>S5B is also known as FME7.
     *
     * @return
     */
    public boolean useS5b() {
        return (soundChip & 32) != 0;
    }

    @Override
    public int getTrackCount() {
        return total_songs;
    }

    /**
     * Return to format
     *
     * @return Standard
     * @since v0.2.10
     */
    public ERegion getRegion() {
        int v = pal_ntsc & 3;
        return switch (v) {
            case 0 -> ERegion.NTSC;
            case 1 -> ERegion.PAL;
            default -> ERegion.UNKNOWED;
        };
    }

    NsfAudio() {
    }

    @Override
    public String toString() {
        return "NSF song" + title + " - " + artist + " Total tracks: " + total_songs;
    }
}
