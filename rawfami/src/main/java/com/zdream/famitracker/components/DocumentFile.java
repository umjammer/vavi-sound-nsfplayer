package com.zdream.famitracker.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.zdream.famitracker.FamiTrackerApp;


public class DocumentFile {

    /*
     * Java IO part
     */
    File file;

    public DocumentFile() {

    }

    @Override
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        super.finalize();
    }

    public final boolean finished() {
        return m_bFileDone;
    }

    // Write functions
    public boolean beginDocument() {
        // TODO
        return false;
    }

    public boolean endDocument() {
        // TODO
        return false;
    }

    public void createBlock(String id, int version) {
        // TODO
    }

    public void writeBlock(String pData, int size) {
        // TODO
    }

    public void writeBlockInt(int value) {
        // TODO
    }

    public void writeBlockChar(byte value) {
        // TODO
    }

    public void writeString(String String) {
        // TODO
    }

    public boolean flushBlock() {
        // TODO
        return false;
    }

    //
    // Read file part
    //

    byte[] bs; // All data read from the file is here
    int offset;

    private int read(byte[] bs, int offset, int len) {
        int len0 = (len > this.bs.length - this.offset) ? this.bs.length - this.offset : len;
        System.arraycopy(this.bs, this.offset, bs, offset, len0);
        this.offset += len0;
        return len0;
    }

    private int read(byte[] bs) {
        int len = (bs.length > this.bs.length - this.offset) ? this.bs.length - this.offset : bs.length;
        System.arraycopy(this.bs, this.offset, bs, 0, len);
        offset += len;
        return len;
    }

    /**
     * C++ data is a data structure with the low bit in the front and the high bit in the back
     *
     * @return
     */
    private int readIntForCppData() {
        if (offset + 4 > bs.length) {
            throw new ArrayIndexOutOfBoundsException("There are still " + (bs.length - offset) + " data, unable to read 4 values");
        }
        int value = (bs[offset] & 0xFF) | ((bs[offset + 1] & 0xFF) << 8)
                | ((bs[offset + 2] & 0xFF) << 16) | ((bs[offset + 3] & 0xFF) << 24);
        offset += 4;
        return value;
    }

    @SuppressWarnings("unused")
    private boolean isEnd() {
        return bs.length == offset;
    }

    public void open(String fileName) throws IOException {
        file = new File(fileName);
        FileInputStream reader = new FileInputStream(file);
        bs = new byte[(int) file.length()];
        reader.read(bs);
        offset = 0;

        reader.close();
    }

    // Read functions
    public boolean validateFile() {

        // Check header ID
        int len = FILE_HEADER_ID.length();
        byte[] bs_head = new byte[len];
        int i = read(bs_head);

        if (i != len) {
            return false;
        }

        byte[] id_head = FILE_HEADER_ID.getBytes();
        for (int j = 0; j < bs_head.length; j++) {
            if (id_head[j] != bs_head[j]) {
                return false;
            }
        }

        // Get file version number
        read(bs_head, 0, 4);
        m_iFileVersion = (bs_head[3] << 24) | (bs_head[2] << 16) | (bs_head[1] << 8) | bs_head[0];

        m_bFileDone = false;
        m_bIncomplete = false;

        return true;
    }

    public final int getFileVersion() {
        return m_iFileVersion;
    }

    /**
     * Read the next block
     *
     * @return
     */
    public boolean readBlock() {
        m_iBlockPointer = 0;
        m_cBlockID = null;

        byte[] bs = new byte[16];
        int bytesRead = this.read(bs);

        if (bytesRead == 0) {
            // After the data cannot be read, it is also considered that the reading is complete
            m_bFileDone = true;
            return false;
        }

        blockId(bs);

        if (FILE_END_ID.equals(m_cBlockID)) {
            m_bFileDone = true;
            return false;
        }

        m_iBlockVersion = readIntForCppData();
        m_iBlockSize = readIntForCppData();

        // The original program judges the legitimacy of m_iBlockSize, which is skipped here

        m_pBlockData = new byte[m_iBlockSize];
        read(m_pBlockData);

        return false;
    }

    private void blockId(byte[] array) {
        int end = 0;
        for (end = 0; end < array.length; end++) {
            if (array[end] == 0) {
                break;
            }
        }

        m_cBlockID = new String(array, 0, end, FamiTrackerApp.defCharset);
    }

    public void getBlock(byte[] buffer) {
        assert (buffer.length < MAX_BLOCK_SIZE);

        int length = Math.min(buffer.length, m_pBlockData.length - m_iBlockPointer);
        System.arraycopy(m_pBlockData, m_iBlockPointer, buffer, 0, length);
        m_iBlockPointer += buffer.length;
    }

    public final int getBlockVersion() {
        return m_iBlockVersion;
    }

    public boolean blockDone() {
        return m_iBlockPointer >= m_iBlockSize;
    }

    public String getBlockHeaderID() {
        return m_cBlockID;
    }

    public int getBlockInt() {
        int value = (m_pBlockData[m_iBlockPointer] & 0xFF)
                | ((m_pBlockData[m_iBlockPointer + 1] & 0xFF) << 8)
                | ((m_pBlockData[m_iBlockPointer + 2] & 0xFF) << 16)
                | ((m_pBlockData[m_iBlockPointer + 3] & 0xFF) << 24);
        m_iBlockPointer += 4;
        return value;
    }

    public byte getBlockChar() {
        return m_pBlockData[m_iBlockPointer++];
    }

    public final int getBlockPos() {
        return m_iBlockPointer;
    }

    public final int getBlockSize() {
        return m_iBlockSize;
    }

    public String readString() {
        byte c;
        StringBuilder b = new StringBuilder();

        while ((c = getBlockChar()) != 0) {
            b.append((char) c);
        }

        return b.toString();
    }

    /**
     * avoid this
     *
     * @param count
     */
    public void rollbackPointer(int count) {
        m_iBlockPointer -= count;
    }

    public final boolean isFileIncomplete() {
        return m_bIncomplete;
    }

    // Constants
    /**
     * Current file version (4.40), 4.6
     */
    public static final int FILE_VER = 0x0440;
    /**
     * Compatible file version (1.0)
     */
    public static final int COMPATIBLE_VER = 0x0100;

    public static final String FILE_HEADER_ID = "FamiTracker Module";
    public static final String FILE_END_ID = "END";

    public static final int MAX_BLOCK_SIZE = 0x80000;
    public static final int BLOCK_SIZE = 0x10000;

    @SuppressWarnings("unused")
    private void writeBlockData(IWriteBlock value) {
        // TODO
    }

    protected void reallocateBlock() {
        // TODO
    }

    protected int m_iFileVersion;
    protected boolean m_bFileDone;
    protected boolean m_bIncomplete;

    protected String m_cBlockID;
    protected int m_iBlockSize;
    protected int m_iBlockVersion;
    protected byte[] m_pBlockData;

    protected int m_iMaxBlockSize;

    protected int m_iBlockPointer;

    public int getLength() {
        return (int) file.length();
    }

    public void close() {

    }

}
