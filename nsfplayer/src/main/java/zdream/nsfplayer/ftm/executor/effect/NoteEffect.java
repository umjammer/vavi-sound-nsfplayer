package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * Modify the effect of pitch and scale
 *
 * @author Zdream
 * @since 0.2.1
 */
public class NoteEffect implements IFtmEffect {

    /**
     * Scale * 12 + Notes
     */
    public final int note;

    private NoteEffect(int note) {
        this.note = note;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.NOTE;
    }

    /**
     * Creates a note-modifying effect
     *
     * @param octave The scale value. Must be in the range [0, 7]
     * @param note   Note value without scale. Must be in the range [1, 12]. 0 is an illegal value.
     * @return Effect Examples
     * @throws IllegalArgumentException When <code>octave</code> or <code>note</code> is not within the specified range
     */
    public static NoteEffect of(int octave, int note) throws IllegalArgumentException {
        if (octave > 7 || octave < 0) {
            throw new IllegalArgumentException("The scale must be an integer value between 0 and 7");
        }
        if (note > 12 || note < 1) {
            throw new IllegalArgumentException("Note must be an integer value between 1 - 12");
        }
        return new NoteEffect(octave * 12 + note);
    }

    /**
     * Creates a note-modifying effect
     *
     * @param note Note value of the musical scale. Must be in the range [1, 96]. 0 is an illegal value.
     * @return Effect Examples
     * @throws IllegalArgumentException When <code>octave</code> or <code>note</code> is not within the specified range
     */
    public static NoteEffect of(int note) throws IllegalArgumentException {
        if (note > 96 || note < 1) {
            throw new IllegalArgumentException("Note must be an integer value between 1 - 96");
        }
        return new NoteEffect(note);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        ch.setMasterNote(note);
        ch.turnOn();
    }

    @Override
    public String toString() {
        int n = note % 12;
        int octave = note / 12;

        StringBuilder b = new StringBuilder();
        b.append("Note:");

        switch (n) {
            case 1:
                b.append("C-");
                break;
            case 2:
                b.append("C#");
                break;
            case 3:
                b.append("D-");
                break;
            case 4:
                b.append("D#");
                break;
            case 5:
                b.append("E-");
                break;
            case 6:
                b.append("F-");
                break;
            case 7:
                b.append("F#");
                break;
            case 8:
                b.append("G-");
                break;
            case 9:
                b.append("G#");
                break;
            case 10:
                b.append("A-");
                break;
            case 11:
                b.append("A#");
                break;
            case 0:
                b.append("B-");
                octave--;
                break;
        }

        b.append(octave);
        return b.toString();
    }
}
