package zdream.nsfplayer.core;


/**
 * Abstract NSF music number installation category
 *
 * TODO this class should be interface, and should make base abstract cass
 *
 * @author Zdream
 * @since v0.2.4
 */
public abstract class AbstractNsfAudio<T> {

    /**
     * @return Total number of songs
     * @since 0.2.8
     */
    public abstract int getTrackCount();

    public abstract double getSpeedNtsc();

    public abstract double getSpeedPal();

    public abstract int getInitAddress();

    public abstract int getPlayAddress();

    public abstract ERegion getRegion();

    public abstract boolean useVrc6();

    public abstract boolean useMmc5();

    public abstract boolean useFds();

    public abstract boolean useN163();

    public abstract boolean useVrc7();

    public abstract boolean useS5b();

    public abstract int getBankSwitch(int index);

    public abstract byte[] getData();

    public abstract int getLoadAddress();

    public abstract int getStart();

    /**
     * title
     */
    public abstract String getTitle();

    /**
     * artist
     */
    public abstract String getAuthor();

    /**
     * Copyright
     */
    public abstract String getCopyright();

    /**
     * Version number of the current NSF file<br>
     * Address 0x000005, single byte
     */
    public abstract void setVersion(short i);

    /**
     * Number of songs in NSF<br>
     * Address 0x000006, single byte
     */
    public abstract void setTrackCount(short i);

    /**
     * The number of the starting song to be played<br>
     * Address 0x000007, single byte
     */
    public abstract void setStart(short i);

    /**
     * Memory address where data is loaded, range ($8000-$FFFF)<br>
     * Address 0x000008-0x000009, double byte<br><br>
     * <p>
     * This specifies the address in the console's RAM. If the game is run from RAM,
     * then the NSF will be placed in RAM.
     * Excluding the file header (address 0x000000 to 0x00007F),
     * the remaining data will be placed at the address corresponding to lenA
     */
    public abstract void setLoadAddress(int i);

    /**
     * Initialization data start address, range ($8000-$FFFF)<br>
     * Address 0x00000A-0x00000B, double byte
     */
    public abstract void setInitAddress(int i);

    /**
     * Music playback address, range ($8000-$FFFF)<br>
     * Address 0x00000C-0x00000D, double byte
     */
    public abstract void setPlayAddress(int i);

    /*
     * The game or music title, composer or artist name, copyright part,
     * additional instructions, etc. are omitted
     */

    public abstract void setTitle(String str);

    public abstract void setAuthor(String str);

    public abstract void setCopyright(String str);

    /**
     * The music loop playback speed under NTSC standard, usually [16666]
     */
    public abstract void setSpeedNtsc(int i);

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
    public abstract void setBankSwitch(int index, short value);

    /**
     * The music loop playback speed in PAL format, usually [20000]
     */
    public abstract void setSpeedPal(int i);

    /**
     * PAL/NTSC format selection<br>
     *
     * <p>Bit switch, data from left (high) to right (low), the first 6 bits are forced to be 0
     * <p>If the 7th bit is 1, NTSC/PAL, then the 8th bit must be 0; (= 2)<br>
     * Otherwise, the 7th bit is 0, the 8th bit is 0, which is NTSC format; if it is 1, it is PAL format.
     */
    public abstract void setPalNtsc(byte b);

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
    public abstract void setSoundChip(byte b);

    /**
     * Remaining data
     */
    public abstract void setBody(T body);

    /** @return body length */
    public abstract int getLength();
}
