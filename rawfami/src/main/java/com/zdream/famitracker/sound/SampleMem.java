package com.zdream.famitracker.sound;

import com.zdream.famitracker.test.FamitrackerLogger;


/**
 * <p>Used to simulate CPU memory, this is only used on the DPCM track
 * <p>class for simulating CPU memory, used by the DPCM channel
 *
 * @author Zdream
 */
public class SampleMem {

    public SampleMem() {
    }

    public final byte read(int address) {
        if (m_pMemory == null)
            return 0;
        int addr = (address - 0xC000) & 0xFFFF; // % m_iMemSize;
        if (addr >= m_pMemory.length)
            return 0;
        return m_pMemory[addr];
    }

    public void setMem(byte[] pPtr) {
        m_pMemory = pPtr;
    }

    void clear() {
        m_pMemory = null;
    }

    private byte[] m_pMemory;

    public void setMem(int i, int j) {
        FamitrackerLogger.instance.logToDo("A method that has not been completed is called");
    }

}
