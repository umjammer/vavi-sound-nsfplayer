package com.zdream.famitracker.sound.emulation;

import com.zdream.famitracker.test.FamitrackerLogger;


public class Square extends Channel {

    private static final byte[][] DUTY_TABLE = {
            {0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    public Square(Mixer pMixer, byte id, byte chip) {
        super(pMixer, id, chip);
    }

    public void reset() {
        m_iEnabled = m_iControlReg = 0;
        m_iCounter = 0;

        m_iSweepCounter = 1;
        m_iSweepPeriod = 1;

        m_iEnvelopeCounter = 1;
        m_iEnvelopeSpeed = 1;

        write(0, 0);
        write(1, 0);
        write(2, 0);
        write(3, 0);

        sweepUpdate(0);

        endFrame();
    }

    public void write(int address, int value) {
        value &= 0xFF;
        FamitrackerLogger.instance.logWriteAddress("Square", address, value);
        switch (address) {
            case 0x00:
                m_iDutyLength = value >> 6;
                m_iFixedVolume = value & 0x0F;
                m_iLooping = (value & 0x20) != 0;
                m_iEnvelopeFix = (value & 0x10) != 0;
                m_iEnvelopeSpeed = m_iFixedVolume + 1;
                break;
            case 0x01:
                m_iSweepEnabled = (value & 0x80) != 0;
                m_iSweepPeriod = ((value >> 4) & 0x07) + 1;
                m_iSweepMode = (value & 0x08) != 0;
                m_iSweepShift = value & 0x07;
                m_bSweepWritten = true;
                break;
            case 0x02:
                m_iPeriod = value | (m_iPeriod & 0x0700);
                break;
            case 0x03:
                m_iPeriod = ((value & 0x07) << 8) | (m_iPeriod & 0xFF);
                m_iLengthCounter = (short) (APU.LENGTH_TABLE[(value & 0xF8) >> 3] & 0xFF);
                m_iDutyCycle = 0;
                m_iEnvelopeVolume = 0x0F;
                if (m_iControlReg != 0)
                    m_iEnabled = 1;
                break;
        }
    }

    public void writeControl(int value) {
        m_iControlReg = (byte) (value & 0x01);

        if (m_iControlReg == 0)
            m_iEnabled = 0;
    }

    public int readControl() {
        return ((m_iLengthCounter > 0) && (m_iEnabled == 1)) ? 1 : 0;
    }

    public void process(int time) {
        if (m_iPeriod == 0) {
            m_iTime += time;
            return;
        }

        boolean valid = (m_iPeriod > 7) && (m_iEnabled != 0) && (m_iLengthCounter > 0) && (m_iSweepResult < 0x800);

        while (time >= m_iCounter) {
            time -= m_iCounter;
            m_iTime += m_iCounter;
            m_iCounter = m_iPeriod + 1;
            int volume = m_iEnvelopeFix ? m_iFixedVolume : m_iEnvelopeVolume;

            // test
            if (!m_iEnvelopeFix) {
                FamitrackerLogger.instance.logValue("Enable EnvelopeFix");
            }
            // test end

            // mix (valid && DUTY_TABLE[m_iDutyLength][m_iDutyCycle] != 0 ? volume : 0);
            if (valid) {
                if (DUTY_TABLE[m_iDutyLength][m_iDutyCycle] != 0) {
                    mix(volume);
                } else {
                    mix(0);
                }
            }

            m_iDutyCycle = (m_iDutyCycle + 1) & 0x0F;
        }

        m_iCounter -= time;
        m_iTime += time;
    }

    /**
     * APU.clock_120Hz() call
     */
    public void lengthCounterUpdate() {
        if (!m_iLooping && (m_iLengthCounter > 0))
            --m_iLengthCounter;
    }

    /**
     * APU.clock_120Hz() call
     */
    public void sweepUpdate(int diff) {
        m_iSweepResult = (m_iPeriod >> m_iSweepShift);

        if (m_iSweepMode)
            m_iSweepResult = m_iPeriod - m_iSweepResult - diff;
        else
            m_iSweepResult = m_iPeriod + m_iSweepResult;

        if (--m_iSweepCounter == 0) {
            m_iSweepCounter = m_iSweepPeriod;
            if (m_iSweepEnabled && (m_iPeriod > 0x07) && (m_iSweepResult < 0x800) && (m_iSweepShift > 0))
                m_iPeriod = m_iSweepResult;
        }

        if (m_bSweepWritten) {
            m_bSweepWritten = false;
            m_iSweepCounter = m_iSweepPeriod;
        }
    }

    /**
     * APU.clock_240Hz() call
     */
    public void envelopeUpdate() {
        if (--m_iEnvelopeCounter == 0) {
            m_iEnvelopeCounter = m_iEnvelopeSpeed;
            if (!m_iEnvelopeFix) {
                if (m_iLooping)
                    m_iEnvelopeVolume = (m_iEnvelopeVolume - 1) & 0x0F;
                else if (m_iEnvelopeVolume > 0)
                    m_iEnvelopeVolume--;
            }
        }
    }


    /**
     * <p>Position 0: xx000000
     * <p>unsigned, value range [0, 3]. Indicates the value of the timbre, pointing to the first-level index of DUTY_TABLE
     */
    int m_iDutyLength;

    /**
     * Value range [0, 15], pointing to the second-level index of DUTY_TABLE
     */
    int m_iDutyCycle;

    /**
     * <p>Position 0: 00x00000
     * <p>True when not 0, false when 0
     *
     * <p>According to the system judgment, this value is always true during the operation of FamiTracker
     * </p>
     */
    boolean m_iLooping;

    /**
     * <p>Position 0: 000x0000
     * <p>True when not 0, false when 0
     *
     * <p>According to the system judgment, this value is always true during the operation of FamiTracker
     * </p>
     */
    boolean m_iEnvelopeFix;
    int m_iEnvelopeSpeed;

    /**
     * unsigned
     */
    int m_iEnvelopeVolume;

    /**
     * <p>Position 0: 0000xxxx
     * <p>unsigned, value range [0, 15]
     */
    int m_iFixedVolume;
    int m_iEnvelopeCounter;


    /**
     * <p>Position 1: x0000000
     * <p>True when not 0, false when 0
     */
    boolean m_iSweepEnabled;

    /**
     * <p>Position 1: 0xxx0000, add 1 after getting the value
     * <p>unsigned, value range [1, 8]
     */
    int m_iSweepPeriod;

    /**
     * <p>Position 1: 0000x000
     * <p>True when not 0, false when 0
     */
    boolean m_iSweepMode;

    /**
     * <p>Position 1: 00000xxx
     * <p>unsigned, value range [0, 7]
     */
    int m_iSweepShift;

    int m_iSweepCounter, m_iSweepResult;
    boolean m_bSweepWritten;

    /*
     * In addition:
     * m_iPeriod
     * Position 2: xxxxxxxx (lower eight bits), Position 3: 00000xxx (upper three bits), a total of 11 bits
     * is the wavelength value;
     *
     * m_iLengthCounter lookup index
     * Position 3: xxxxx000
     */
}
