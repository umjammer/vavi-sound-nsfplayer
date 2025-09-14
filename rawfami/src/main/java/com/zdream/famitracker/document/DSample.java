package com.zdream.famitracker.document;

/**
 * DPCM sample class
 *
 * @author Zdream
 */
public class DSample implements Cloneable {

    /**
     * Max size of a sample as supported by the NES, in bytes
     */
    public static final int MAX_SIZE = 0x0FF1;

    /**
     * Size of sample name
     public static final int MAX_NAME_SIZE = 256;*/

    /**
     * Empty constructor
     */
    public DSample() {
        this(0, null);
    }

    public DSample(int size) {
        this(size, null);
    }

    /**
     * Unnamed sample constructor
     *
     * @param size unsigned
     */
    public DSample(int size, byte[] pData) {
        this.m_pSampleData = pData;

        if (pData == null) {
            m_pSampleData = new byte[size];
        }

        m_Name = "";
    }

    @Override
    public DSample clone() {
        DSample sample = new DSample();
        clone(sample);
        return sample;
    }

    /**
     * <p>Translated from the copy constructor in the original C++ file
     * <p>Use itself as a template to copy data to the sample
     */
    protected void clone(DSample sample) {
        sample.m_pSampleData = new byte[this.m_pSampleData.length];

        assert (sample.m_pSampleData.length != 0);
        System.arraycopy(m_pSampleData, 0, sample.m_pSampleData, 0, m_pSampleData.length);
        sample.m_Name = m_Name;
    }

    /**
     * Copy from existing sample
     *
     * @param pDSample
     */
    public void copy(DSample pDSample) {
        assert (pDSample != null);

        m_pSampleData = new byte[pDSample.m_pSampleData.length];
        System.arraycopy(pDSample.m_pSampleData, 0, m_pSampleData, 0, pDSample.m_pSampleData.length);
    }

    /**
     * Allocate memory, optionally copy data
     *
     * @param pData cannot be null
     */
    public void allocate(byte[] pData) {
        m_pSampleData = new byte[pData.length];

        if (pData != null) {
            System.arraycopy(pData, 0, m_pSampleData, 0, pData.length);
        }
    }

    /**
     * Allocate memory, which is equivalent to reallocating the space of the sample array
     *
     * @param pData default null
     */
    public void allocate(int size) {
        m_pSampleData = new byte[size];
    }

    /**
     * Clear sample data
     */
    public void clear() {
        m_pSampleData = null;
    }

    /**
     * Set sample data and size, the object will own the memory area assigned
     *
     * @param pData data source. Because it will not be copied, just assign the reference to this class
     */
    public void setData(byte[] pData) {
        assert (pData != null);
        m_pSampleData = pData;
    }

    /**
     * Get sample size
     *
     * @return
     */
    public final int getSize() {
        return m_pSampleData.length;
    }

    /**
     * Get sample data
     *
     * @return
     */
    public final byte[] getData() {
        return m_pSampleData;
    }

    /**
     * Set sample name
     */
    public void setName(String pName) {
        this.m_Name = pName;
    }

    /**
     * Get sample name
     */
    public final String getName() {
        return m_Name;
    }

    /**
     * Data is stored here
     */
    private byte[] m_pSampleData;

    private String m_Name;
}
