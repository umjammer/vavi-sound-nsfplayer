package zdream.nsfplayer.nsf.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import zdream.nsfplayer.core.AbstractNsfAudio;
import zdream.nsfplayer.core.ERegion;


/**
 * <p>Stream Data in NSF audio files
 * <p>No playback related data will be stored here
 *
 * @author Zdream
 * @author Umjammer
 * @version 2025-09-13
 * @since 0.2.12
 */
public class StreamNsfAudio extends AbstractNsfAudio<InputStream> {

    /**
     * Version number of the current NSF file<br>
     * Address 0x000005, single byte
     */
    private int version;
    /**
     * Number of songs in NSF<br>
     * Address 0x000006, single byte
     */
    private int total_songs;
    /**
     * The number of the starting song to be played<br>
     * Address 0x000007, single byte
     */
    private int start;

    /**
     * Memory address where data is loaded, range ($8000-$FFFF)<br>
     * Address 0x000008-0x000009, double byte<br><br>
     * <p>
     * This specifies the address in the console's RAM. If the game is run from RAM,
     * then the NSF will be placed in RAM.
     * Excluding the file header (address 0x000000 to 0x00007F),
     * the remaining data will be placed at the address corresponding to lenA
     */
    private int load_address;
    /**
     * Initialization data start address, range ($8000-$FFFF)<br>
     * Address 0x00000A-0x00000B, double byte
     */
    private int init_address;
    /**
     * Music playback address, range ($8000-$FFFF)<br>
     * Address 0x00000C-0x00000D, double byte
     */
    private int play_address;

    /*
     * The game or music title, composer or artist name, copyright part,
     * additional instructions, etc. are omitted
     */

    /**
     * The music loop playback speed under NTSC standard, usually [16666]
     */
    private int speed_ntsc;
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
    private final short[] bankswitch = new short[8];
    /**
     * The music loop playback speed in PAL format, usually [20000]
     */
    private int speed_pal;
    /**
     * PAL/NTSC format selection<br>
     *
     * <p>Bit switch, data from left (high) to right (low), the first 6 bits are forced to be 0
     * <p>If the 7th bit is 1, NTSC/PAL, then the 8th bit must be 0; (= 2)<br>
     * Otherwise, the 7th bit is 0, the 8th bit is 0, which is NTSC format; if it is 1, it is PAL format.
     */
    private byte pal_ntsc;
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
    private byte soundChip;

    /**
     * title
     */
    private String title;

    /**
     * artist
     */
    private String artist;

    /**
     * Copyright
     */
    private String copyright;

    /**
     * Remaining data
     */
    private InputStream body;

    @Override
    public boolean useVrc6() {
        return (soundChip & 1) != 0;
    }

    @Override
    public boolean useVrc7() {
        return (soundChip & 2) != 0;
    }

    @Override
    public boolean useFds() {
        return (soundChip & 4) != 0;
    }

    @Override
    public boolean useMmc5() {
        return (soundChip & 8) != 0;
    }

    /**
     * <p>Whether the N163 chip is used.
     * <p>N163 (Namco 163) is also known as N106.
     *
     * @return
     */
    @Override
    public boolean useN163() {
        return (soundChip & 16) != 0;
    }

    /**
     * <p>Whether the S5B chip is used.
     * <p>S5B is also known as FME7.
     *
     * @return
     */
    @Override
    public boolean useS5b() {
        return (soundChip & 32) != 0;
    }

    @Override
    public int getBankSwitch(int index) {
        return bankswitch[index];
    }

    private int length;

    @Override
    public byte[] getData() {
        try {
            byte[] b = body.readAllBytes();
            length = b.length;
            return b;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int getLoadAddress() {
        return load_address;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getTrackCount() {
        return total_songs;
    }

    @Override
    public double getSpeedNtsc() {
        return speed_ntsc;
    }

    @Override
    public double getSpeedPal() {
        return speed_pal;
    }

    @Override
    public int getInitAddress() {
        return init_address;
    }

    @Override
    public int getPlayAddress() {
        return play_address;
    }

    @Override
    public void setTitle(String str) {
        title = str;
    }

    @Override
    public void setAuthor(String str) {
        artist = str;
    }

    @Override
    public void setCopyright(String str) {
        copyright = str;
    }

    @Override
    public void setVersion(short i) {
        version = i;
    }

    @Override
    public void setTrackCount(short i) {
        total_songs = i;
    }

    @Override
    public void setStart(short i) {
        start = i;
    }

    @Override
    public void setLoadAddress(int i) {
        load_address = i;
    }

    @Override
    public void setInitAddress(int i) {
        init_address = i;
    }

    @Override
    public void setPlayAddress(int i) {
        play_address = i;
    }

    @Override
    public void setSpeedNtsc(int i) {
        speed_ntsc = i;
    }

    @Override
    public void setBankSwitch(int index, short value) {
        bankswitch[index] = value;
    }

    @Override
    public void setSpeedPal(int i) {
        speed_pal = i;
    }

    @Override
    public void setPalNtsc(byte b) {
        pal_ntsc = b;
    }

    @Override
    public void setSoundChip(byte b) {
        soundChip = b;
    }

    @Override
    public void setBody(InputStream body) {
        this.body = body;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getAuthor() {
        return artist;
    }

    @Override
    public String getCopyright() {
        return copyright;
    }

    /**
     * Return to format
     *
     * @return Standard
     * @since v0.2.10
     */
    @Override
    public ERegion getRegion() {
        int v = pal_ntsc & 3;
        return switch (v) {
            case 0 -> ERegion.NTSC;
            case 1 -> ERegion.PAL;
            default -> ERegion.UNKNOWED;
        };
    }

    StreamNsfAudio() {
    }

    @Override
    public String toString() {
        return "NSF song" + title + " - " + artist + " Total tracks: " + total_songs;
    }
}
