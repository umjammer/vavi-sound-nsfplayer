package zdream.nsfplayer.ftm.executor.context;

import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;

import static zdream.nsfplayer.ftm.format.FtmSequence.SEQUENCE_COUNT;


/**
 * <p>Default sequence handler
 * <p>Applicable to 2A03's Pulse, Triangle, Noise tracks,
 * and VRC6's Pulse, Sawtooth tracks
 * </p>
 *
 * @author Zdream
 * @since v0.2.1
 */
public class DefaultSequenceHandler implements IResetable {

    /**
     * <li>STATE_DISABLED: Not played
     * <li>STATE_RUNNING: Now Playing
     * <li>STATE_END: Play to the end of the sequence, pointing outside the sequence
     */
    public static final byte
            STATE_DISABLED = 0,
            STATE_RUNNING = 1,
            STATE_END = 2;

    private final FtmSequence[] sequence = new FtmSequence[SEQUENCE_COUNT];

    /**
     * Record the playback status of the current sequence
     */
    private final byte[] seqState = new byte[SEQUENCE_COUNT];

    /**
     * Record the position of the current sequence. Greater than zero
     */
    private final int[] seqPtr = new int[SEQUENCE_COUNT];

    /**
     * The volume level indicated by the sequence. Valid values are [0, 15]
     */
    public int volume = 15;
    /**
     * The wavelength increment indicated by the sequence. Positive, negative, and 0 are allowed.
     * The higher the value, the lower the final pitch and the longer the wavelength.
     */
    public int period;
    /**
     * The increment of the key indicated by the sequence. Positive, negative, and 0 are allowed.
     */
    public int deltaNote;
    /**
     * Timbre. 2A03 Valid values: [0, 3]. Invalid values / Default value: -1
     */
    public int duty = -1;
    /**
     * Configuration for the sequence {@link FtmSequenceType#ARPEGGIO}.
     *
     * @see FtmSequence#ARP_SETTING_ABSOLUTE
     * @see FtmSequence#ARP_SETTING_FIXED
     * @see FtmSequence#ARP_SETTING_RELATIVE
     */
    public byte arpSetting;
    /**
     * The value of the sequence {@link FtmSequenceType#ARPEGGIO}
     */
    public int arp;
    /**
     * Is the sequence in the released state?
     */
    private boolean release;

    public DefaultSequenceHandler() {
    }

    /**
     * Install the sequence and immediately set it to run
     *
     * @param seq
     */
    public void setupSequence(FtmSequence seq) {
        int index = seq.type.ordinal();

        seqState[index] = STATE_RUNNING;
        seqPtr[index] = 0;
        sequence[index] = seq;

        resetValue();
        setRelease(false);
    }

    /**
     * Clear Sequence
     *
     * @param type
     */
    public void clearSequence(FtmSequenceType type) {
        int index = type.ordinal();

        seqState[index] = STATE_DISABLED;
        seqPtr[index] = 0;
        sequence[index] = null;

        resetValue();
        setRelease(false);
    }

    /**
     * Clear all sequences and reset to their original state
     */
    @Override
    public void reset() {
        for (int i = 0; i < sequence.length; i++) {
            seqState[i] = STATE_DISABLED;
            seqPtr[i] = 0;
            sequence[i] = null;
        }
        volume = 15;
        period = deltaNote = arp = 0;
        arpSetting = 0;
        duty = -1;
    }

    /**
     * Only reset the value, not the sequence
     */
    public void resetValue() {
        volume = 15;
        period = deltaNote = arp = 0;
        arpSetting = 0;
        duty = -1;
    }

    /**
     * Get the state of the specified sequence. Defaults to {@link #STATE_DISABLED}
     *
     * @param type
     * @return
     */
    public byte getSequenceState(FtmSequenceType type) {
        int index = type.ordinal();
        return this.seqState[index];
    }

    /**
     * Get Sequence
     *
     * @param type
     * @return
     */
    public FtmSequence getSequence(FtmSequenceType type) {
        int index = type.ordinal();
        return this.sequence[index];
    }

    /**
     * Get the current playback position of the specified sequence
     *
     * @param type
     * @return
     */
    public int getSequencePointer(FtmSequenceType type) {
        int index = type.ordinal();
        return this.seqPtr[index];
    }

    /**
     * @param release {@link #release}
     */
    public void setRelease(boolean release) {
        this.release = release;
    }

    /**
     * @return {@link #release}
     */
    public boolean isReleasing() {
        return release;
    }

    //
    // run
    //

    /**
     * <p>Run Sequence
     * <p>This method is run once per frame, and it will reset the data affected by the sequence,
     * including {@link #volume} and {@link #deltaNote},
     * but not {@link #period}, which can be accumulated in each loop.
     * It will overwrite the data and move the sequence pointer forward one space.
     * </p>
     */
    public void update() {
        volume = 15;
        /*period = */
        deltaNote = arp = 0;
        arpSetting = 0;
        duty = -1;

        for (int i = 0; i < sequence.length; i++) {
            update(i);
        }
    }

    /**
     * @param index The first few sequences
     */
    private void update(int index) {
        FtmSequence seq = this.sequence[index];

        if (seq == null || seq.length() == 0) {
            return;
        }

        switch (seqState[index]) {
            case STATE_RUNNING:
                updateRunning(index, seq);
                break;
            case STATE_END:
                updateEnd(index, seq);
                break;
            default:
                // Do nothing
                break;
        }
    }

    private void updateRunning(int index, FtmSequence seq) {
        int ptr = this.seqPtr[index]++;
        int value = seq.data[ptr];

        updateValue(seq, value, false);

        int release = seq.releasePoint;
        int loop = seq.loopPoint;
        int length = seq.length(); // length is guaranteed to be greater than 0

        // The following is the value of the next frame
        ptr++;
        if (isReleasing() && release != -1) {
            // Enter the release section

            if (ptr < release) {
                ptr = release + 1;
            }
            if (ptr >= length) {
                // At the end of the sequence
                seqState[index] = STATE_END;
            }
        } else {
            if (loop != -1) {
                if (release != -1 && ptr > release) {
                    ptr = loop;
                } else if (ptr >= length) {
                    ptr = loop;
                }
            } else {
                if (release != -1 && ptr > release) {
                    ptr = release;
                } else if (ptr >= length) {
                    // At the end of the sequence
                    seqState[index] = STATE_END;
                }
            }
        }

        this.seqPtr[index] = ptr;
    }

    private void updateEnd(int index, FtmSequence seq) {
        int length = seq.data.length;
        int value = seq.data[length - 1];

        updateValue(seq, value, true);
    }

    private void updateValue(FtmSequence seq, int value, boolean isEnd) {
        switch (seq.type) {
            case VOLUME:
                this.volume = value;
                break;
            case ARPEGGIO:
                if (!isEnd || seq.settings != FtmSequence.ARP_SETTING_FIXED) {
                    this.arpSetting = seq.settings;
                    this.arp = value;
                }
                break;
            case PITCH:
                if (!isEnd)
                    this.period += value;
                break;
            case HI_PITCH:
                if (!isEnd)
                    this.period += (value << 4);
                break;
            case DUTY:
                this.duty = value;
                break;
        }
    }
}
