package zdream.nsfplayer.ftm.executor.tools;

import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_NTSC;
import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_PAL;


/**
 * Used for converting musical keys to wavelengths.
 *
 * @author Zdream
 * @since v0.2.1
 */
public class NoteLookupTable {

    private static final short[] palTable;
    private static final short[] ntscTable;
    private static final short[] sawTable;
    private static final short[] fds;
    private static final short[] n163;

    static {
        palTable = new short[96];
        ntscTable = new short[96];
        sawTable = new short[96];
        fds = new short[96];
        n163 = new short[96];

        double clock_ntsc = BASE_FREQ_NTSC / 16.0;
        double clock_pal = BASE_FREQ_PAL / 16.0;

        for (int i = 0; i < 96; ++i) {
            // Frequency (in Hz)
            double freq = 32.7032 * Math.pow(2.0, i / 12.0);
            double pitch;

            // 2A07
            pitch = (clock_pal / freq) - 0.5;
            palTable[i] = (short) pitch;

            // 2A03 / MMC5 / VRC6
            pitch = (clock_ntsc / freq) - 0.5;
            ntscTable[i] = (short) pitch;

            // VRC6 Saw
            pitch = ((clock_ntsc * 16.0) / (freq * 14.0)) - 0.5;
            sawTable[i] = (short) pitch;

            // FDS
            pitch = (freq * 65536.0) / (clock_ntsc / 1.0) + 0.5;
            fds[i] = (short) pitch;

            // N163
            pitch = (freq/* * namcoChannels*/ * 983040.0) / clock_ntsc;
            n163[i] = (short) (pitch / 4);
            // The value range of n163 is [71.84945516, 18393.46052]

//			pitch = (Freq * double(NamcoChannels) * 983040.0) / clock_ntsc;
//			m_iNoteLookupTableN163[i] = (int)(pitch) / 4;

//			if (m_iNoteLookupTableN163[i] > 0xFFFF)	// 0x3FFFF
//				m_iNoteLookupTableN163[i] = 0xFFFF;	// 0x3FFFF

            // Sunsoft 5B
//			pitch = (clock_ntsc / Freq) - 0.5;
//			m_iNoteLookupTableS5B[i] = (unsigned int)pitch;
        }
    }

    /**
     * In PAL format, the wavelength of a certain note. 2A07(Noise) uses this value
     *
     * @param note Key, [1, 96]
     * @return wavelength
     */
    public static short pal(int note) {
        return palTable[note - 1];
    }

    /**
     * The wavelength of a note in NTSC format. 2A03 (Pulse 1 and 2, Triangle),
     * MMC5, VRC6 (Pulse 1 and 2) use this value.
     *
     * @param note Key, [1, 96]
     * @return
     */
    public static short ntsc(int note) {
        return ntscTable[note - 1];
    }

    /**
     * The wavelength of the key used by the VRC6 Sawtooth track
     *
     * @param note Key, [1, 96]
     * @return
     */
    public static short saw(int note) {
        return sawTable[note - 1];
    }

    /**
     * The wavelength of the key used by the FDS track
     *
     * @param note Key, [1, 96]
     * @return
     */
    public static short fds(int note) {
        return fds[note - 1];
    }

    /**
     * <p>The wavelength of the key used by the N163 track.
     * Note that the wavelength value (period) needs to be converted when calculating:
     * <blockquote><pre>
     * period = n163(note) * ch * 4
     * </pre></blockquote>
     * Where, ch is the current orbit number of N163
     * </p>
     *
     * @param note Key, [1, 96]
     * @return
     * @since v0.2.6
     */
    public static short n163(int note) {
        return n163[note - 1];
    }
}
