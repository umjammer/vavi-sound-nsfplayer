package com.zdream.famitracker.sound.emulation;

/**
 * <p>This class is used to derive audio channels
 * <p>This class is used to derive the audio channels
 *
 * @author Zdream
 */
public abstract class Channel {

    public Channel(Mixer pMixer, byte id, byte chip) {
        m_pMixer = pMixer;
        m_iChanId = id;
        m_iChip = chip;
    }

    /**
     * Begin a new audio frame
     */
    public void endFrame() {
        m_iTime = 0;
    }

    public final int getPeriod() {
        return m_iPeriod;
    }

    protected void mix(int value) {
        if (m_iLastValue != value) {
            m_pMixer.addValue(m_iChanId, m_iChip, value, value, m_iTime);
            m_iLastValue = value;
        }
    }

    protected final Mixer m_pMixer; // The mixer

    /**
     * Cycle counter, resets every new frame, unsigned
     */
    protected int m_iTime;

    /**
     * Last value sent to mixer
     */
    protected int m_iLastValue;

    /**
     * This channels unique ID, unsigned, this value is in {@link Types}
     */
    protected final byte m_iChanId;

    /**
     * Chip, unsigned, this value is in {@link Types}
     */
    protected final byte m_iChip;

    // Variables used by channels
    /**
     * unsigned 4015 value
     */
    protected byte m_iControlReg;
    /**
     * unsigned
     */
    protected byte m_iEnabled;
    /**
     * unsigned
     */
    protected int m_iPeriod;
    /**
     * unsigned
     */
    protected short m_iLengthCounter;
    /**
     * unsigned
     */
    protected int m_iCounter;

}
