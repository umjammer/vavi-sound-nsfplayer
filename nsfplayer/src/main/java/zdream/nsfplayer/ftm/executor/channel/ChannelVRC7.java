package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.format.FtmInstrumentVRC7;
import zdream.nsfplayer.sound.vrc7.OPLL;
import zdream.nsfplayer.sound.vrc7.OPLLPatch;
import zdream.nsfplayer.sound.vrc7.SoundVRC7;


/**
 * VRC7 Track
 *
 * @author Zdream
 * @since v0.2.7
 */
public class ChannelVRC7 extends AbstractFtmChannel {

    public final int index;

    /**
     * @param index VRC7 channel number. range [0, 5]
     * @param opll  OPLL Environment
     */
    public ChannelVRC7(int index, OPLL opll) {
        super((byte) (CHANNEL_VRC7_FM1 + index));
        this.index = index;
        this.opll = opll;
        this.sound = opll.getSound(index);
    }

    @Override
    public void playNote() {
        super.playNote();

        if (instrumentUpdated) {
            handleInstrument();
            state = STATE_NOTE_TRIGGER;
        }
        calculateVolume();
        calculatePeriod();
    }

    @Override
    public void reset() {
        hold = false;
        super.reset();
    }

    /*
     * Instrument Sequence
     */

    /**
     * Calculate the volume, combine the volumes from the sequence,
     * and finally limit the volume to the range [0, 120]
     */
    protected void calculateVolume() {
        int volume = masterVolume * 8 + curVolume; // Accuracy 120

        if (volume > 120) {
            curVolume = 120;
        } else if (volume < 1) {
            curVolume = 0;
        } else {
            curVolume = volume;
        }
    }

    /**
     * <p>Calculate the wavelength, combine the pitch and key values obtained from other effects
     * Finally combine the pitch and key values finally used by VRC7
     */
    protected void calculatePeriod() {
        if (masterNote == 0) {
            // Do not play
            curNote = 0;
            curPeriod = 0;
            return;
        }

        hold = true;
        int note = masterNote + curNote;
        lastOctave = (note - 1) / 12;
        int period = periodTable(note) - masterPitch + curPeriod;
        curPeriod = this.calcCurrentFnum(period);

        if (note <= 1) {
            note = 1;
        } else if (note > 96) {
            note = 96;
        }

        curNote = note;
    }

    @Override
    public void doHalt() {
        super.doHalt();
        state = STATE_NOTE_HALT;
    }

    @Override
    public void doRelease() {
        state = STATE_NOTE_RELEASE;
    }

    //
    // OPLL
    //

    /**
     * state
     */
    static final byte
            STATE_NONE = 0,
            STATE_NOTE_ON = 1,
            STATE_NOTE_TRIGGER = 2,
            STATE_NOTE_OFF = 3,
            STATE_NOTE_HALT = 4,
            STATE_NOTE_RELEASE = 5;

    /*
     * [172, 344)
     */
    final static int[] FREQ_TABLE = {172, 183, 194, 205, 217, 230, 244, 258, 274, 290, 307, 326};

    final OPLL opll;

    int patchNum;
    final short[] regs = new short[8];
    boolean hold;
    byte state;
    int lastOctave;

    private void handleInstrument() {
        FtmInstrumentVRC7 inst = getRuntime().querier.getVRC7Instrument(instrument);
        if (inst == null) {
            return;
        }

        this.patchNum = inst.patchNum;
        if (patchNum == 0) {
            System.arraycopy(inst.regs, 0, regs, 0, 8);
        }
    }

    private int calcFnum(int note) {
        return FREQ_TABLE[(note - 1) % 12] << 1;
    }

    @Override
    public int periodTable(int note) {
        int baseFreq = calcFnum(note);
        int octave = (note - 1) / 12;
        int span = calcFnum(1); // What is the difference in the table value of 12 semitones?

        int ret = baseFreq + (octave * span);
        if (ret < 2) {
            ret = 1;
        }
        return ret;
    }

    /**
     * According to the current curPeriod, calculate the fNum and octave currently in use.
     * octave The value will be written to {@link #lastOctave}
     *
     * @return fNum
     */
    private int calcCurrentFnum(int period) {
        int span = calcFnum(1); // What is the difference in the table value of 12 semitones?
        lastOctave = (period) / span - 1; // [0, 9]
        int fnum = period % span + span; // [344, 344*2=688]

        if (lastOctave < 0) {
            while (lastOctave < 0) {
                fnum /= 2;
                lastOctave++;
            }
        } else if (lastOctave > 7) {
            while (lastOctave > 7) {
                fnum *= 2;
                lastOctave--;
            }
            if (fnum > 686) {
                fnum = 686;
            }
        }

        return fnum >> 1;
    }

    /*
     * Sounder
     */
    private final SoundVRC7 sound;

    @Override
    public SoundVRC7 getSound() {
        return sound;
    }

    @Override
    public void writeToSound() {
        int block = lastOctave; // bnum
        int fnum = curPeriod;

        if (!playing) {
            state = STATE_NOTE_HALT;
        }

        boolean noteOn = false;
        boolean sustainOn = false;

        switch (state) {
            case STATE_NOTE_TRIGGER:
                sound.keyOff();
                noteOn = true;
                sustainOn = true;
                break;
            case STATE_NOTE_ON:
                if (hold) {
                    noteOn = true;
                } else {
                    sustainOn = true;
                }
                break;
            case STATE_NOTE_HALT:
                break;
            case STATE_NOTE_RELEASE:
                sustainOn = true;
                break;
        }

        // Write frequency
        sound.modulatorSlot.fNum = fnum;
        sound.carrierSlot.fNum = fnum;

        // Custom pitch
        if (patchNum == 0 && (state == STATE_NOTE_TRIGGER)) {
            writeCustomPitch();
        }

        sound.modulatorSlot.block = block;
        sound.carrierSlot.block = block;
        sound.carrierSlot.sustain = sustainOn;

        if (noteOn) {
            sound.keyOn();
        } else {
            sound.keyOff();
        }

        if (state != STATE_NOTE_HALT) {
            // Select volume & patch
            sound.carrierSlot.volume = 63 - curVolume / 2;
            sound.setPatch(patchNum);
        }

        sound.rebuildAll();
        if (state == STATE_NOTE_TRIGGER) {
            state = STATE_NOTE_ON;
        }
    }

    private void writeCustomPitch() {
        OPLLPatch p = opll.getCustomModPatch();

        p.AM = (regs[0] & 0x80) != 0;
        p.PM = (regs[0] & 0x40) != 0;
        p.EG = (regs[0] & 0x20) != 0;
        p.KR = (regs[0] & 0x10) != 0;
        p.ML = (regs[0]) & 15;
        p.KL = (regs[2] >> 6) & 3;
        p.TL = (regs[2]) & 63;
        p.WF = (regs[3] >> 3) & 1;
        p.FB = (regs[3]) & 7;
        p.AR = (regs[4] >> 4) & 15;
        p.DR = (regs[4]) & 15;
        p.SL = (regs[6] >> 4) & 15;
        p.RR = (regs[6]) & 15;

        p = opll.getCustomCarPatch();

        p.AM = (regs[1] & 0x80) != 0;
        p.PM = (regs[1] & 0x40) != 0;
        p.EG = (regs[1] & 0x20) != 0;
        p.KR = (regs[1] & 0x10) != 0;
        p.ML = (regs[1]) & 15;
        p.KL = (regs[3] >> 6) & 3;
        p.WF = (regs[3] >> 4) & 1;
        p.AR = (regs[5] >> 4) & 15;
        p.DR = (regs[5]) & 15;
        p.SL = (regs[7] >> 4) & 15;
        p.RR = (regs[7]) & 15;
    }
}
