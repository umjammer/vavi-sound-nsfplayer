package zdream.nsfplayer.ftm.format;

import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_EFFECT_COLUMNS;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_INSTRUMENTS;


/**
 * <p>FTM per valid note data.
 * <p>It holds the data for a key, including tune, volume, instrument, effect, etc.
 *
 * @author Zdream
 * @version 0.2.1
 * Implements the {@link Cloneable} interface.
 * @date 2018-05-03
 * @since 0.1
 */
public class FtmNote implements Cloneable {

    /**
     * The parameter is the {@link NOTE_NONE} class.
     */
    public byte note;

    /**
     * octave scale
     */
    public byte octave;

    /**
     * Note that {@link MAX_VOLUMN} is empty.
     */
    public byte vol;

    /**
     * Note that {@link MAX_INSTRUMENTS} is empty.
     */
    public int instrument;

    public final byte[] effNumber = new byte[MAX_EFFECT_COLUMNS];

    public final short[] effParam = new short[MAX_EFFECT_COLUMNS];

    /**
     * Note type. NOTE_HALT is the stop character, NOTE_RELEASE is the rest character.
     */
    public static final byte
            NOTE_NONE = 0,
            NOTE_C = 1,
            NOTE_CS = 2,
            NOTE_D = 3,
            NOTE_DS = 4,
            NOTE_E = 5,
            NOTE_F = 6,
            NOTE_FS = 7,
            NOTE_G = 8,
            NOTE_GS = 9,
            NOTE_A = 10,
            NOTE_AS = 11,
            NOTE_B = 12,
            NOTE_RELEASE = 13,
            NOTE_HALT = 14;

    /**
     * <p>Music effects in the track
     * <p>Channel effects
     * <p>In the original C++ documentation it was recorded in the enumeration effect_t.
     * <p>EF_PORTAOFF (= 7) It's useless!<br>
     * EF_DELAYED_VOLUME (= 25) Marked as Unimplemented
     */
    public static final byte
            EF_NONE = 0,
            EF_SPEED = 1,
            EF_JUMP = 2,
            EF_SKIP = 3,
            EF_HALT = 4, // Cxx cancel
            EF_VOLUME = 5, // as an effect is outdated, but here I need to use the
            EF_PORTAMENTO = 6,
            EF_PORTAOFF = 7,
            EF_SWEEPUP = 8,
            EF_SWEEPDOWN = 9,
            EF_ARPEGGIO = 10,
            EF_VIBRATO = 11, // 4xy
            EF_TREMOLO = 12, // 7xy
            EF_PITCH = 13, // Pxx
            EF_DELAY = 14,
            EF_DAC = 15,
            EF_PORTA_UP = 16, // 1xx
            EF_PORTA_DOWN = 17, // 2xx
            EF_DUTY_CYCLE = 18,
            EF_SAMPLE_OFFSET = 19,
            EF_SLIDE_UP = 20,
            EF_SLIDE_DOWN = 21,
            EF_VOLUME_SLIDE = 22,
            EF_NOTE_CUT = 23, // Sxx
            EF_RETRIGGER = 24,
            EF_DELAYED_VOLUME = 25, // Unimplemented
            EF_FDS_MOD_DEPTH = 26,
            EF_FDS_MOD_SPEED_HI = 27,
            EF_FDS_MOD_SPEED_LO = 28,
            EF_DPCM_PITCH = 29,
            EF_SUNSOFT_ENV_LO = 30,
            EF_SUNSOFT_ENV_HI = 31,
            EF_SUNSOFT_ENV_TYPE = 32,
            EF_COUNT = 33;

    // Channel effect letters
    @Deprecated
    public static final char[] EFF_CHAR = {
            '.',    // None I added this one, to match the elements of the effects array above.
            'F',    // Speed
            'B',    // Jump
            'D',    // Skip
            'C',    // Halt
            'E',    // Volume
            '3',    // Porta on
            0,        // Porta off		// unused
            'H',    // Sweep up
            'I',    // Sweep down
            '0',    // Arpeggio
            '4',    // Vibrato
            '7',    // Tremolo
            'P',    // Pitch
            'G',    // Note delay
            'Z',    // DAC setting
            '1',    // Portamento up
            '2',    // Portamento down
            'V',    // Duty cycle
            'Y',    // Sample offset
            'Q',    // Slide up
            'R',    // Slide down
            'A',    // Volume slide
            'S',    // Note cut
            'X',    // DPCM retrigger
            0,        // deprecated
            'H',    // FDS modulation depth
            'I',    // FDS modulation speed hi
            'J',    // FDS modulation speed lo
            'W',    // DPCM Pitch
            'H',    // Sunsoft envelope low
            'I',    // Sunsoft envelope high
            'J',    // Sunsoft envelope type
            //'9'	// Targeted volume slide
            /*
            'H',	// VRC7 modulator
            'I',	// VRC7 carrier
            'J',	// VRC7 modulator/feedback level
            */
    };

    /**
     * Volume range [0, 15], 16 is empty
     */
    public static final byte MAX_VOLUME = 16;

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(30);
        String hex = "0123456789ABCDEF";

        if (note == NOTE_NONE) {
            b.append("...");
        } else if (note == NOTE_RELEASE) {
            b.append("===");
        } else if (note == NOTE_HALT) {
            b.append("---");
        } else {
            switch (note) {
                case NOTE_C:
                    b.append("C-");
                    break;
                case NOTE_CS:
                    b.append("C#");
                    break;
                case NOTE_D:
                    b.append("D-");
                    break;
                case NOTE_DS:
                    b.append("D#");
                    break;
                case NOTE_E:
                    b.append("E-");
                    break;
                case NOTE_F:
                    b.append("F-");
                    break;
                case NOTE_FS:
                    b.append("F#");
                    break;
                case NOTE_G:
                    b.append("G-");
                    break;
                case NOTE_GS:
                    b.append("G#");
                    break;
                case NOTE_A:
                    b.append("A-");
                    break;
                case NOTE_AS:
                    b.append("A#");
                    break;
                case NOTE_B:
                    b.append("B-");
                    break;
            }
            b.append(octave);
        }

        b.append(' ');
        if (instrument == MAX_INSTRUMENTS) {
            b.append("..");
        } else {
            b.append(hex.charAt(instrument / 16));
            b.append(hex.charAt(instrument % 16));
        }
        b.append(' ');

        if (vol == MAX_VOLUME) {
            b.append('.');
        } else {
            b.append(hex.charAt(vol));
        }

        for (int i = 0; i < MAX_EFFECT_COLUMNS; i++) {
            if (effNumber[i] != 0) {
                b.append(' ').append(EFF_CHAR[effNumber[i] - 1]).append('-');
                String paramStr = Integer.toHexString(effParam[i] & 0xFF);
                if (paramStr.length() == 1) {
                    b.append('0');
                }
                b.append(paramStr);
            }
        }

        return b.toString();
    }

    @Override
    public FtmNote clone() {
        FtmNote n = new FtmNote();
        n.note = this.note;
        n.vol = this.vol;
        n.instrument = this.instrument;
        n.octave = this.octave;

        System.arraycopy(this.effNumber, 0, n.effNumber, 0, MAX_EFFECT_COLUMNS);
        System.arraycopy(this.effParam, 0, n.effParam, 0, MAX_EFFECT_COLUMNS);

        return n;
    }
}
