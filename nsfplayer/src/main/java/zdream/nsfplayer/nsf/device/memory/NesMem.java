package zdream.nsfplayer.nsf.device.memory;

import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.IDevice;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;


/**
 * Emulates NES virtual memory.
 * <p>Initially set up 0x10000 bytes of memory.
 *
 * @author Zdream
 */
public class NesMem implements IDevice {

    protected final byte[] image;

    /**
     * Only relevant for FDS chips
     */
    protected boolean fdsEnable = false;

    public NesMem() {
        this.image = new byte[0x10000];
    }

    @Override
    public void reset() {
        for (int i = 0; i < 0x800; i++) {
            image[i] = (byte) 0;
        }
        fdsEnable = false;
    }

    /**
     * Put data into virtual memory
     *
     * @param data   NSF removes all data after the header
     * @param offset That is {@link NsfAudio#load_address}
     */
    public final boolean setImage(byte[] data, int offset, int size) {
        if (offset + size < 0x10000) {
            System.arraycopy(data, 0, image, offset, size);
        } else {
            int length = 0x10000 - offset;
            System.arraycopy(data, 0, image, offset, length);
        }
        return true;
    }

    @Override
    public boolean write(int addr, int value, int id) {
        if (0x0000 <= addr && addr < 0x2000) {
            image[addr & 0x7ff] = (byte) (value & 0xff);
            return true;
        }
        if (0x6000 <= addr && addr < 0x8000) {
            image[addr] = (byte) (value & 0xff);
            return true;
        }
        if (0x4100 <= addr && addr < 0x4110) {
            image[addr] = (byte) (value & 0xff);
            return true;
        }
        if (fdsEnable && 0x8000 <= addr && addr < 0xe000) {
            image[addr] = (byte) (value & 0xff);
        }
        return false;
    }

    @Override
    public boolean read(int addr, IntHolder val, int id) {
        if (0x0000 <= addr && addr < 0x2000) {
            val.val = image[addr & 0x7ff] & 0xff;
            return true;
        }
        if (0x4100 <= addr && addr < 0x4110) {
            val.val = image[addr] & 0xff;
            return true;
        }
        if (0x6000 <= addr && addr < 0x10000) {
            val.val = image[addr] & 0xff;
            return true;
        }
        return false;
    }

    /**
     * Copy the memory data to the bs array
     *
     * @param bs      Arrays that hold data
     * @param offset  bs offset
     * @param length  Number of data copied
     * @param address The memory copy starting point
     * @since v0.2.4
     */
    public void read(byte[] bs, int offset, int length, int address) {
        System.arraycopy(image, address, bs, offset, length);
    }

    public final void setFDSMode(boolean t) {
        fdsEnable = t;
    }
}
