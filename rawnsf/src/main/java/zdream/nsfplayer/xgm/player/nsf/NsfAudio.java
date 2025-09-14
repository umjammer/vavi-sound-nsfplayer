package zdream.nsfplayer.xgm.player.nsf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import zdream.nsfplayer.xgm.player.SoundDataMSP;
import zdream.utils.common.FileUtils;


/**
 * Data in NSF file
 *
 * @author Zdream
 */
public class NsfAudio extends SoundDataMSP {

    /*
     * In the NSF file
     * Address 0x000000 to 0x00007F is the frame header data
     *
     * Note that the low bit of the data is in the front and the high bit is in the back
     */
    public final byte[] magic = new byte[4]; // The cpp file is 5 because the last bit is \0

    /**
     * The version number of the current NSF file<br>
     * Address 0x000005, single byte
     */
    public int version;
    /**
     * Number of songs in NSF<br>
     * Address 0x000006, single byte
     */
    public int songs;
    public int total_songs;
    /**
     * The number of the starting song to play<br>
     * Address 0x000007, single byte
     */
    public int start;

    /**
     * The memory address where the data is loaded, range ($8000-$FFFF)<br>
     * Address 0x000008-0x000009, double byte<br><br>
     * <p>
     * This explains the address in the game console's RAM. If the game is run in memory, the NSF will be placed in memory.
     * Except for the file header (address 0x000000 to 0x00007F), other data will be placed at the address corresponding to lenA
     */
    public int load_address;
    /**
     * The starting address of the initialization data, range ($8000-$FFFF)<br>
     * Address 0x00000A-0x00000B, double byte
     */
    public int init_address;
    /**
     * Song playback address, range ($8000-$FFFF)<br>
     * Address 0x00000C-0x00000D, double byte
     */
    public int play_address;

    public String filename;

    public byte[] title_nsf;
    public byte[] artist_nsf;
    public byte[] copyright_nsf;

    public String title;
    public String artist;
    public String copyright;

    /**
     * NSFe only
     */
    public String ripper;
    /**
     * NSFe only
     */
    public String text;

    /*
     * The title of the game or music, the name of the composer or artist, the copyright part, additional instructions, etc. are omitted
     */
    /**
     * The loop playback speed of the music in NTSC format is often [16666]
     */
    public int speed_ntsc;
    /**
     * Bank switch, initial 8-bit value<br>
     * <p>
     * The addressing space of 6502 assembly is 64K, but NES only uses $8000-$FFFF, a total of 32K. For small games like Super Mario 1, there is no need
     * to consider bank switching, but for games like Contra 1 and 2, which exceed 32K, bank switching is required.
     * The size may be different, some are 16K, some are 32K, some are 8K, etc.
     * The address is also different, $8000, $A000, $C000 are all possible.
     * NSF will also encounter the situation of insufficient space. At this time, it is necessary to use bank switching. The NSF bank switching size is 4K.
     */
    public final int[] bankswitch = new int[8];
    /**
     * Loop playback speed of music in NTSC format
     */
    public int speed_pal;
    /**
     * PAL/NTSC format selection<br>
     * <p>
     * Bit switch, data from left (high) to right (low), the first 6 bits are forced to 0
     * If the 7th bit is 1, NTSC/PAL, the 8th bit must be 0; (= 2)
     * Otherwise, the 7th bit is 0, the 8th bit is 0, which is NTSC format; if it is 1, it is PAL format
     */
    public int pal_ntsc;
    /**
     * <b>Special sound chip</b><br>
     * <p>
     * Bit switch, data from left (high) to right (low), the first 2 bits are forced to 0<br>
     * If the 3rd bit is 1, use Sunsoft (FME7) chip;<br>
     * If the 4th bit is 1, use Namcot (106) chip;<br>
     * If the 5th bit is 1, use Nintendo (MMC5) chip;<br>
     * If the 6th bit is 1, use Nintendo (FDS) chip;<br>
     * If the 7th bit is 1, use Konami (VRC7) chip;<br>
     * If the 8th bit is 1, use Konami (VRC6) chip;<br>
     * <p>
     * If f2 == 0, no chip is used
     */
    public int soundchip;
    /**
     * @see #soundchip
     */
    public boolean useVrc6, useVrc7, useFds, useMmc5, useN106, useFme7;

    public byte[] extra = new byte[4];
    public byte[] body;
    public byte[] nsfe_image;
    public byte[] nsfe_plst;
    public final NsfeEntry[] nsfe_entry = new NsfeEntry[256];

    {
        for (int i = 0; i < 256; i++) {
            nsfe_entry[i] = new NsfeEntry();
        }
    }

    /*
     * The following is playback related data
     */

    /** The currently selected song number, starting from 0 */
    public int song;
    /** Data from the game list is true */
    public boolean playlist_mode;
    /** Singing time | seconds */
    public int time_in_ms;
    /** Default playback time */
    public int default_playtime;
    /** Loop time */
    public int loop_in_ms;
    /** Fade-out time */
    public int fade_in_ms, default_fadetime;
    /** Number of loops */
    public int loop_num, default_loopnum;
    /** Enable when the performance time is unknown (default performance time) */
    public boolean playtime_unknown;
    public boolean title_unknown;

    public String print_title;

    /**
     * This is not in the original C++ file. It caches the last opened PLSItem.<br>
     * Because the original C++ file still needs to re-read the file even if it uses the same file to switch songs up and down during operation.
     * Such IO operations are actually meaningless. Therefore, this cache variable is added.<br>
     * If you want to use it, please use the <code>reload()</code> method.<br>
     * Of course, if you open another different file, this will naturally become invalid.
     * The system will still perform IO operations to read the file.
     */
    PLSItem last_item;

    public void setDefaults(int p, int f, int l) {
        default_playtime = p;
        default_fadetime = f;
        default_loopnum = l;
    }

    public boolean loadFile(String fn) throws IOException {
        return loadBytes(fn, FileUtils.readFile(fn));
    }

    public boolean loadAssets(String str) throws IOException {
        InputStream in = getClass().getResourceAsStream(str);
        byte[] bs, buf = new byte[8192];
        List<byte[]> list = new ArrayList<>();
        int lastLen = 0, len = 0, i;

        while ((i = in.read(buf)) > 0) {
            lastLen = i;
            len += i;
            list.add(buf);
            buf = new byte[8192];
        }

        bs = new byte[len];
        int limit = list.size() - 1;
        for (i = 0; i < limit; i++) {
            System.arraycopy(list.get(i), 0, bs, 8192 * i, 8192);
        }
        System.arraycopy(list.get(limit), 0, bs, 8192 * limit, lastLen);

        return loadBytes(str, bs);
    }

    /**
     * To read an NSF file, the caller needs to convert it into a byte array first.
     * loads file (playlist or NSF or NSFe)
     */
    public boolean loadBytes(String str, byte[] bs) throws IOException {
A:
        {
            PLSItem pls = new PLSItem(str.toCharArray());

            if (pls.type == 3) {
                filename = pls.filename;
            } else if (str.endsWith(".nsf") || str.endsWith(".NSF") || str.endsWith(".nsfe") || str.endsWith(".NSFE")) {
                filename = pls.filename;
            } else {
                break A;
            }

            if (load(bs) == false) {
                break A;
            }

            if (pls.type == 3) {
                setTitle(pls.title);
                song = pls.song;
                playlist_mode = true;
                title_unknown = false;
                enable_multi_tracks = false;
            } else {
                playlist_mode = false;
                title_unknown = true;
                enable_multi_tracks = true;
            }

            time_in_ms = pls.time_in_ms;
            loop_in_ms = pls.loop_in_ms;
            fade_in_ms = pls.fade_in_ms;
            loop_num = pls.loop_num;

            if (time_in_ms < 0)
                playtime_unknown = true;
            else
                playtime_unknown = false;

            // This step is used to cache files
            this.last_item = pls;

            return true;
        }

        last_item = null;
        return false;
    }

    public void reload() {
        time_in_ms = this.last_item.time_in_ms;
        loop_in_ms = this.last_item.loop_in_ms;
        fade_in_ms = this.last_item.fade_in_ms;
        loop_num = this.last_item.loop_num;
    }

    @Override
    public void setLength(int t) {
        time_in_ms = t;
        playtime_unknown = false;
    }

    public int getPlayTime() {
        int s = song;
        if (nsfe_plst != null)
            s = nsfe_plst[song];
        if (nsfe_entry[s].time >= 0) {
            return nsfe_entry[s].time;
        }

        return time_in_ms < 0 ? default_playtime : time_in_ms;
    }

    public int getLoopTime() {
        return loop_in_ms < 0 ? 0 : loop_in_ms;
    }

    public int getFadeTime() {
        int s = song;
        if (nsfe_plst != null)
            s = nsfe_plst[song];

        if (s == -1) {
            s = song = 0;
        }

        if (nsfe_entry[s].fade >= 0) {
            return nsfe_entry[s].fade;
        }

        if (fade_in_ms < 0)
            return default_fadetime;
        else if (fade_in_ms == 0)
            return 50;
        else
            return fade_in_ms;
    }

    public int getLoopNum() {
        return loop_num > 0 ? loop_num - 1 : default_loopnum - 1;
    }

    @Override
    public int getLength() {
        return getPlayTime() + getLoopTime() * getLoopNum() + getFadeTime();
    }

    @Override
    public int getSong() {
        return song;
    }

    @Override
    public int getSongNum() {
        return songs;
    }

    public boolean useNSFePlaytime() {
        if (nsfe_plst == null)
            return false;
        return nsfe_entry[nsfe_plst[song]].time >= 0;
    }

    @Override
    public void setSong(int s) {
        song = s % songs;
    }

    /**
     * Read from binary image
     *
     * @param image byte array composed of binary image data
     * @return true on success, false on failure
     */
    public boolean load(byte[] image) {
        if (image.length < 4) // no FourCC
            return false;

        // fill NSFe values with defaults

        // 'plst'
        nsfe_plst = null;

        // entries 'tlbl', 'time', 'fade'
        for (int i = 0; i < 256; ++i) {
            nsfe_entry[i].tlbl = "";
            nsfe_entry[i].time = -1;
            nsfe_entry[i].fade = -1;
        }

        // load the NSF or NSFe
        System.arraycopy(image, 0, magic, 0, 4);
        if (magic[0] == 'M' && magic[1] == 'E' && magic[2] == 'S' && magic[3] == 'M') {
            return loadNSFe(image, false);
        }
        if (image.length < 0x80) { // no header?
            return false;
        }

        version = image[0x05] & 0xFF;
        total_songs = songs = image[0x06] & 0xFF;
        start = image[0x07] & 0xFF;

        load_address = (image[0x08] & 0xFF) | ((image[0x09] & 0xFF) << 8);
        init_address = (image[0x0a] & 0xFF) | ((image[0x0B] & 0xFF) << 8);
        play_address = (image[0x0c] & 0xFF) | ((image[0x0D] & 0xFF) << 8);

        int end = 0;

        title_nsf = new byte[32];
        System.arraycopy(image, 0x0e, title_nsf, 0, 32);
        for (end = 0; end < title_nsf.length; end++) {
            if (title_nsf[end] == 0)
                break;
        }
        title = new String(title_nsf, 0, end);

        artist_nsf = new byte[32];
        System.arraycopy(image, 0x2e, artist_nsf, 0, 32);
        for (end = 0; end < artist_nsf.length; end++) {
            if (artist_nsf[end] == 0)
                break;
        }
        artist = new String(artist_nsf, 0, end);

        copyright_nsf = new byte[32];
        System.arraycopy(image, 0x4e, copyright_nsf, 0, 32);
        for (end = 0; end < copyright_nsf.length; end++) {
            if (copyright_nsf[end] == 0)
                break;
        }
        copyright = new String(copyright_nsf, 0, end);

        ripper = ""; // NSFe only
        text = null; // NSFe only

        speed_ntsc = (image[0x6e] & 0xFF) | ((image[0x6f] & 0xFF) << 8);
        for (int i = 0; i < 8; i++) {
            bankswitch[i] = image[0x70 + i] & 0xFF;
        }
        speed_pal = (image[0x78] & 0xFF) | ((image[0x79] & 0xFF) << 8);
        pal_ntsc = image[0x7a] & 0xFF;
        if (speed_pal == 0)
            speed_pal = 19997;
        if (speed_ntsc == 0)
            speed_ntsc = 16639;
        soundchip = image[0x7b] & 0xFF;

        useVrc6 = (soundchip & 1) != 0 ? true : false;
        useVrc7 = (soundchip & 2) != 0 ? true : false;
        useFds = (soundchip & 4) != 0 ? true : false;
        useMmc5 = (soundchip & 8) != 0 ? true : false;
        useN106 = (soundchip & 16) != 0 ? true : false;
        useFme7 = (soundchip & 32) != 0 ? true : false;

        extra = new byte[4];
        System.arraycopy(image, 0x7c, extra, 0, 4);

        // The body here is the data excluding the first 128 bytes of the header
        body = new byte[image.length - 0x80];
        System.arraycopy(image, 0x80, body, 0, body.length);

        song = start - 1;
        return true;
    }

    public boolean loadNSFe(byte[] image, boolean info) {
        // store entire file for string references, etc.
        nsfe_image = new byte[image.length + 1];

        System.arraycopy(image, 0, nsfe_image, 0, image.length);
        nsfe_image[image.length] = 0; // null terminator for safety
        image = nsfe_image;

        if (image.length < 4) // no FourCC
            return false;

        System.arraycopy(image, 0, magic, 0, 4);
        if (magic[0] != 'M' || magic[1] != 'E' || magic[2] != 'F' || magic[3] != 'E') {
            return false;
        }

        int chunk_offset = 4; // skip 'NSFE'
        while (true) {
            if ((image.length - chunk_offset) < 8) // not enough data for chunk size + FourCC
                return false;

            // UINT8* chunk = image + chunk_offset;
            int chunkp = chunk_offset;

            int chunk_size = ((image[chunkp] & 0xFF)) + ((image[chunkp + 1] & 0xFF) << 8)
                    + ((image[chunkp + 2] & 0xFF) << 16) + ((image[chunkp + 3] & 0xFF) << 24);

            if ((image.length - chunk_offset) < (chunk_size + 8)) // not enough data for chunk
                return false;

            byte[] cid = new byte[4];
            cid[0] = image[chunkp + 4];
            cid[1] = image[chunkp + 5];
            cid[2] = image[chunkp + 6];
            cid[3] = image[chunkp + 7];

            chunk_offset += 8;
            chunkp += 8;

            if (cid[0] == 'N' && cid[1] == 'E' && cid[2] == 'N' && cid[3] == 'D') { // end of chunks
                break;
            }

            if (cid[0] == 'I' && cid[1] == 'N' && cid[2] == 'F' && cid[3] == 'O') {
                if (chunk_size < 0x0A)
                    return false;

                version = 1;
                load_address = (image[chunkp + 0x00] & 0xFF) | ((image[chunkp + 0x01] & 0xFF) << 8);
                init_address = (image[chunkp + 0x02] & 0xFF) | ((image[chunkp + 0x03] & 0xFF) << 8);
                play_address = (image[chunkp + 0x04] & 0xFF) | ((image[chunkp + 0x05] & 0xFF) << 8);
                pal_ntsc = (image[chunkp + 0x06] & 0xFF);
                soundchip = (image[chunkp + 0x07] & 0xFF);
                songs = (image[chunkp + 0x08] & 0xFF);
                start = (image[chunkp + 0x09] & 0xFF) + 1; // note NSFe is 0 based, unlike NSF
                total_songs = songs;

                // NSFe doesn't allow custom speeds
                speed_ntsc = 16639; // 60.09Hz
                speed_pal = 19997; // 50.00Hz

                // other variables contained in other banks
                for (int i = 0; i < 8; i++) {
                    bankswitch[i] = 0;
                }
                for (int i = 0; i < 4; i++) {
                    extra[i] = 0;
                }

                // setup derived variables
                useVrc6 = (soundchip & 1) != 0 ? true : false;
                useVrc7 = (soundchip & 2) != 0 ? true : false;
                useFds = (soundchip & 4) != 0 ? true : false;
                useMmc5 = (soundchip & 8) != 0 ? true : false;
                useN106 = (soundchip & 16) != 0 ? true : false;
                useFme7 = (soundchip & 32) != 0 ? true : false;
                song = start - 1;

                // body should follow in 'DATA' chunk
                body = null;

                // description strings should follow in 'auth' chunk
                title_nsf[0] = 0;
                artist_nsf[0] = 0;
                copyright_nsf[0] = 0;
                title = new String(title_nsf);
                artist = new String(artist_nsf);
                copyright = new String(copyright_nsf);
                ripper = "";
                text = null;

                // INFO chunk read
                info = true;
            } else if (cid[0] == 'D' && cid[1] == 'A' && cid[2] == 'T' && cid[3] == 'A') // DATA
            {
                if (!info)
                    return false;

                body = new byte[chunk_size];
                System.arraycopy(image, chunkp, body, 0, chunk_size);
            } else if (cid[0] == 'B' && cid[1] == 'A' && cid[2] == 'N' && cid[3] == 'K') // BANK
            {
                if (!info)
                    return false;

                for (int i = 0; i < 8 && i < chunk_size; ++i) {
                    bankswitch[i] = image[chunkp + i];
                }
            } else if (cid[0] == 'a' && cid[1] == 'u' && cid[2] == 't' && cid[3] == 'h') // auth lowercase
            {
                /*
                 * #define NSFE_STRING(p) \ if (n >= chunk_size) break; \ p =
                 * reinterpret_cast<char*>(chunk+n); \ while (n < chunk_size && chunk[n] != 0)
                 * ++n; \ if(chunk[n] == 0) ++n;
                 */

                int n = 0;
                while (true) {
                    // title
                    if (n >= chunk_size)
                        break;
                    int begin = chunkp + n;
                    while (n < chunk_size && image[chunkp + n] != 0)
                        ++n;
                    byte[] bytes = new byte[n - begin];
                    System.arraycopy(image, begin, bytes, 0, bytes.length);
                    title = new String(bytes);
                    if (image[chunkp + n] == 0)
                        ++n;

                    // artist
                    if (n >= chunk_size)
                        break;
                    begin = chunkp + n;
                    while (n < chunk_size && image[chunkp + n] != 0)
                        ++n;
                    bytes = new byte[n - begin];
                    System.arraycopy(image, begin, bytes, 0, bytes.length);
                    artist = new String(bytes);
                    if (image[chunkp + n] == 0)
                        ++n;

                    // copyright
                    if (n >= chunk_size)
                        break;
                    begin = chunkp + n;
                    while (n < chunk_size && image[chunkp + n] != 0)
                        ++n;
                    bytes = new byte[n - begin];
                    System.arraycopy(image, begin, bytes, 0, bytes.length);
                    copyright = new String(bytes);
                    if (image[chunkp + n] == 0)
                        ++n;

                    // ripper
                    if (n >= chunk_size)
                        break;
                    begin = chunkp + n;
                    while (n < chunk_size && image[chunkp + n] != 0)
                        ++n;
                    bytes = new byte[n - begin];
                    System.arraycopy(image, begin, bytes, 0, bytes.length);
                    ripper = new String(bytes);
                    if (image[chunkp + n] == 0)
                        ++n;

                    break;
                }
            } else if (cid[0] == 'p' && cid[1] == 'l' && cid[2] == 's' && cid[3] == 't') // plst lowercase
            {
                nsfe_plst = new byte[chunk_size];
                System.arraycopy(image, chunkp, nsfe_plst, 0, chunk_size);
            } else if (cid[0] == 't' && cid[1] == 'i' && cid[2] == 'm' && cid[3] == 'e') // time lowercase
            {
                int i = 0;
                int n = 0;
                while (i < 256 && (n + 3) < chunk_size) {
                    int value = ((image[chunkp + n] & 0xFF)) + ((image[chunkp + n + 1] & 0xFF) << 8)
                            + ((image[chunkp + n + 2] & 0xFF) << 16) + ((image[chunkp + n + 3] & 0xFF) << 24);
                    nsfe_entry[i].time = value;
                    ++i;
                    n += 4;
                }
            } else if (cid[0] == 'f' && cid[1] == 'a' && cid[2] == 'd' && cid[3] == 'e') // fade lowercase
            {
                int i = 0;
                int n = 0;
                while (i < 256 && (n + 3) < chunk_size) {
                    int value = ((image[chunkp + n] & 0xFF)) + ((image[chunkp + n + 1] & 0xFF) << 8)
                            + ((image[chunkp + n + 2] & 0xFF) << 16) + ((image[chunkp + n + 3] & 0xFF) << 24);
                    nsfe_entry[i].fade = value;
                    ++i;
                    n += 4;
                }
            } else if (cid[0] == 't' && cid[1] == 'l' && cid[2] == 'b' && cid[3] == 'l') // tlbl lowercase
            {
                int n = 0;
                for (int i = 0; i < 256; ++i) {
                    if (n >= chunk_size)
                        break;
                    int begin = chunkp + n;
                    while (n < chunk_size && image[chunkp + n] != 0)
                        ++n;
                    byte[] bytes = new byte[n - begin];
                    System.arraycopy(image, begin, bytes, 0, bytes.length);
                    nsfe_entry[i].tlbl = new String(bytes);
                    if (image[chunkp + n] == 0)
                        ++n;
                }
            } else if (cid[0] == 't' && cid[1] == 'e' && cid[2] == 'x' && cid[3] == 't') // text lowercase
            {
                text = new String(image, chunkp, chunk_size);
            } else { // unknown chunk
                if (cid[0] >= 'A' && cid[0] <= 'Z') {
                    return false;
                }
            }

            // next chunk
            chunk_offset += chunk_size;
        }

        return true;
    }

    public String debugOut() {
        StringBuilder b = new StringBuilder(256);

        int i;
        // char buf[256] = "";

        b.append(String.format("Magic:    %4s\n", new String(magic)));
        b.append(String.format("Version:  %d\n", version));

        b.append(String.format("Songs:    %d\n", songs));
        b.append(String.format("Load:     %04x\n", load_address));
        b.append(String.format("Init:     %04x\n", init_address));
        b.append(String.format("Play:     %04x\n", play_address));
        b.append(String.format("Title:    %s\n", title));
        b.append(String.format("Artist:   %s\n", artist));
        b.append(String.format("Copyright:%s\n", copyright));
        b.append(String.format("Speed(N): %d\n", speed_ntsc));
        b.append(String.format("Speed(P): %d\n", speed_pal));

        b.append("Bank :");
        for (i = 0; i < 8; i++) {
            b.append(String.format("[%02x]", bankswitch[i]));
        }
        b.append("\n");

        if ((pal_ntsc & 1) != 0)
            b.append("PAL mode.\n");
        else
            b.append("NTSC mode.\n");
        if ((pal_ntsc & 2) != 0)
            b.append("Dual PAL and NTSC mode.\n");

        if ((soundchip & 1) != 0)
            b.append("VRC6 ");
        if ((soundchip & 2) != 0)
            b.append("VRC7 ");
        if ((soundchip & 4) != 0)
            b.append("FDS ");
        if ((soundchip & 8) != 0)
            b.append("MMC5 ");
        if ((soundchip & 16) != 0)
            b.append("Namco 106 ");
        if ((soundchip & 32) != 0)
            b.append("FME-07 ");

        b.append("\n");

        b.append("Extra:     ");
        for (i = 0; i < 4; i++) {
            b.append(String.format("[%02x]", extra[i]));
        }
        b.append("\n");
        b.append(String.format("DataSize: %d\n", body.length));

        return b.toString();
    }

    public String getTitle(String format, int song) {

        String fn;
        int i;
        if ((i = this.filename.lastIndexOf('\\')) >= 0) {
            fn = this.filename.substring(i + 1);
        } else if ((i = this.filename.lastIndexOf('/')) >= 0) {
            fn = this.filename.substring(i + 1);
        } else {
            fn = this.filename;
        }

        if (song < 0)
            song = this.song;

        if (!title_unknown) {
            return print_title = "<unknown>";
        }

        if (format == null)
            format = "%L (%n/%e) %T - %A";
        int ptr = 0; // ptr is the index pointer to format
        int len = format.length();

        StringBuilder b = new StringBuilder(len * 4 + 32); // Generate print_title

        while (ptr < len) {
            char ch = format.charAt(ptr);
            if (SST.is_sjis_prefix(ch)) {
                b.append(ch);
                b.append(format.charAt(ptr++));
                ptr++;
                continue;
            } else if (ch == '%') {
                ch = format.charAt(++ptr);
                switch (ch) {
                    case 'F':
                    case 'f':
                        b.append(fn);
                        ptr++;
                        break;
                    case 'P':
                    case 'p':
                        b.append(this.filename);
                        ptr++;
                        break;
                    case 'T':
                    case 't':
                        b.append(this.title);
                        ptr++;
                        break;
                    case 'A':
                    case 'a':
                        b.append(this.artist);
                        ptr++;
                        break;
                    case 'C':
                    case 'c':
                        b.append(this.copyright);
                        ptr++;
                        break;
                    case 'L':
                    case 'l':
                        b.append(nsfe_entry[nsfe_plst != null ? nsfe_plst[song] : song].tlbl);
                        ptr++;
                        break;
                    case 'N':
                        b.append(String.format("$%02x", song + 1));
                        ptr++;
                        break;
                    case 'n':
                        b.append(String.format("%03d", song + 1));
                        ptr++;
                        break;
                    case 'S':
                        b.append(String.format("$%02x", start));
                        ptr++;
                        break;
                    case 's':
                        b.append(String.format("%03d", start));
                        ptr++;
                        break;
                    case 'E':
                        b.append(String.format("$%02x", songs));
                        ptr++;
                        break;
                    case 'e':
                        b.append(String.format("%03d", songs));
                        ptr++;
                        break;
                    default:
                        break;
                }
            } else {
                b.append(format.charAt(ptr++));
            }
        }

        // strip leading whitespace
        int wp;
        for (wp = 0; b.charAt(wp) == ' '; ++wp) {
        }
        if (wp > 0) {
            b.delete(0, wp);
        }

        title_unknown = false;
        return print_title = b.toString();
    }

}
