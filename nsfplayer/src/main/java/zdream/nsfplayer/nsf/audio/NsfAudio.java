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
public class NsfAudio extends AbstractNsfAudio<byte[]> {

    private int version;
    private int total_songs;
    private int start;

    private int load_address;
    private int init_address;
    private int play_address;

    private int speed_ntsc;
    private final short[] bankswitch = new short[8];
    private int speed_pal;
    private byte pal_ntsc;
    private byte soundChip;

    private String title;

    private String artist;

    private String copyright;

    private byte[] body;

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

    @Override
    public byte[] getData() {
        return body;
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
    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public int getLength() {
        return body.length;
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

    NsfAudio() {
    }

    @Override
    public String toString() {
        return "NSF song" + title + " - " + artist + " Total tracks: " + total_songs;
    }
}
