package com.zdream.famitracker.sound.emulation;

import com.zdream.famitracker.sound.SampleMem;

import static com.zdream.famitracker.sound.emulation.Types.SNDCHIP_NONE;


public class DPCM extends Channel {

    public static final short[] DMC_PERIODS_NTSC = {
            428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 84, 72, 54
    };
    public static final short[] DMC_PERIODS_PAL = {
            398, 354, 316, 298, 276, 236, 210, 198, 176, 148, 132, 118, 98, 78, 66, 50
    };

    public short[] PERIOD_TABLE = DMC_PERIODS_NTSC;

    public DPCM(Mixer pMixer, SampleMem pSampleMem, byte id) {
        super(pMixer, id, SNDCHIP_NONE);
        this.m_pSampleMem = pSampleMem;
    }

    public void reset() {
        m_iCounter = m_iPeriod = DMC_PERIODS_NTSC[0];

        m_iBitDivider = m_iShiftReg = 0;
        m_iDMA_LoadReg = 0;
        m_iDMA_LengthReg = 0;
        m_iDMA_Address = 0;
        m_iDMA_BytesRemaining = 0;

        m_bTriggeredIRQ = m_bSampleFilled = false;

        // loaded with 0 on power-up.
        m_iDeltaCounter = 0;

        endFrame();
    }

    public void write(int address, int value) {
        value &= 0xFF;
        switch (address) {
            case 0x00:
                m_iPlayMode = value & 0xC0;
                m_iPeriod = PERIOD_TABLE[value & 0x0F];
                if ((value & 0x80) == 0x00)
                    m_bTriggeredIRQ = false;
                break;
            case 0x01:
                m_iDeltaCounter = value & 0x7F;
                mix(m_iDeltaCounter);
                break;
            case 0x02:
                m_iDMA_LoadReg = value;
                break;
            case 0x03:
                m_iDMA_LengthReg = value;
                break;
        }
    }

    /**
     * APU.write4015 call
     *
     * @param value
     */
    public void writeControl(int value) {
        if ((value & 1) == 1) {
            if (m_iDMA_BytesRemaining == 0)
                reload();
        } else {
            m_iDMA_BytesRemaining = 0;
        }

        m_bTriggeredIRQ = false;
    }

    @SuppressWarnings("unused")
    private int readControl() {
        return (m_iDMA_BytesRemaining > 0) ? 1 : 0;
    }

    @SuppressWarnings("unused")
    private int didIRQ() {
        return m_bTriggeredIRQ ? 1 : 0;
    }

    public void process(int time) {
        while (time >= m_iCounter) {
            time -= m_iCounter;
            m_iTime += m_iCounter;
            m_iCounter = m_iPeriod;

            // DMA reader
            // Check if a new byte should be fetched
            if (!m_bSampleFilled && (m_iDMA_BytesRemaining > 0)) {

                m_iSampleBuffer = m_pSampleMem.read(m_iDMA_Address | 0x8000)
                        & 0xFF; // convert to non-negative integer
                m_iDMA_Address = (m_iDMA_Address + 1) & 0xFFFF; // convert to non-negative integer
                --m_iDMA_BytesRemaining;
                m_bSampleFilled = true;

                if (m_iDMA_BytesRemaining == 0) {
                    switch (m_iPlayMode) {
                        case 0x00:    // Stop
                            break;
                        case 0x40:    // Loop
                        case 0xC0:
                            reload();
                            break;
                        case 0x80:    // Stop and do IRQ (not when an NSF is playing)
                            m_bTriggeredIRQ = true;
                            break;
                    }
                }
            }

            // Output unit
            if (m_iBitDivider == 0) {
                // Begin new output cycle
                m_iBitDivider = 8;
                if (m_bSampleFilled) {
                    m_iShiftReg = m_iSampleBuffer;
                    m_bSampleFilled = false;
                    m_bSilenceFlag = false;
                } else {
                    m_bSilenceFlag = true;
                }
            }

            if (!m_bSilenceFlag) {
                if ((m_iShiftReg & 1) == 1) {
                    if (m_iDeltaCounter < 126)
                        m_iDeltaCounter += 2;
                } else {
                    if (m_iDeltaCounter > 1)
                        m_iDeltaCounter -= 2;
                }
            }

            m_iShiftReg >>= 1;
            --m_iBitDivider;

            mix(m_iDeltaCounter);
        }

        m_iCounter -= time;
        m_iTime += time;
    }

    private void reload() {
        m_iDMA_Address = (m_iDMA_LoadReg << 6) | 0x4000;
        m_iDMA_BytesRemaining = (m_iDMA_LengthReg << 4) + 1;
    }

    /**
     * Not called
     *
     * @return
     */
    public final int getSamplePos() {
        return (m_iDMA_Address - (m_iDMA_LoadReg << 6 | 0x4000)) >> 6;
    }

    /**
     * Not called
     *
     * @return
     */
    public final int getDeltaCounter() {
        return m_iDeltaCounter;
    }

    /**
     * Not called
     *
     * @return
     */
    public boolean isPlaying() {
        return (m_iDMA_BytesRemaining > 0);
    }

    /*
     * The following 9 types of data are all unsigned
     */

    /**
     * The number of bits remaining to be read in the currently read byte. [0, 8]
     */
    private int m_iBitDivider;
    private int m_iShiftReg;

    /**
     * Indicates whether to loop or play only once
     */
    private int m_iPlayMode;

    /**
     * It is likely related to the volume.
     * It looks like the position of the audio and video
     */
    private int m_iDeltaCounter;
    /**
     * The value of the currently taken byte.
     * Because it needs to be parsed bit by bit during parsing, it is used for caching.
     */
    private int m_iSampleBuffer;

    /**
     * Start reading bit offset, unit 64 bytes
     */
    private int m_iDMA_LoadReg;

    /**
     * Sampling length, unit 16 bytes
     */
    private int m_iDMA_LengthReg;
    /**
     * Virtual read position. Used to store which address number in m_pSampleMem the currently read position is at
     */
    private int m_iDMA_Address;
    /**
     * Number of remaining bytes
     */
    private int m_iDMA_BytesRemaining;

    /**
     * No practical effect
     */
    private boolean m_bTriggeredIRQ;
    private boolean m_bSampleFilled;
    private boolean m_bSilenceFlag;

    // Needed by FamiTracker
    private final SampleMem m_pSampleMem;

}
