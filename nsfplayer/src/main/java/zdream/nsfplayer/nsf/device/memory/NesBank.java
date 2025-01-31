package zdream.nsfplayer.nsf.device.memory;

import java.util.Arrays;

import zdream.nsfplayer.nsf.device.IDevice;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;


/**
 * 4 KB * 16 space
 *
 * @author Zdream
 */
public class NesBank implements IDevice {

    /**
     * Note that each element in banks points to an address (index) of the image.
     * The meaning of pointers in cpp files is different
     */
    final int[] bank = new int[256];
    byte[] image;
    final byte[] nullBank = new byte[0x1000];

    final int[] bankswitch = new int[16];
    final int[] bankdefault = new int[16];

    boolean fdsEnable = false;
    int bankMax;

    /**
     * <p>The size of each bank.</p>
     * A bank is a storage unit, there are multiple of them in the NES machine,
     * used to store the image data of the cartridge.
     * Because each bank has a fixed size, and the image data on the cartridge is usually larger than the bank size,
     * multiple banks are needed to store it.<br>
     * <p>
     * The formula for obtaining mirror data using bank is:<br>
     * The yth data on the xth bank: bank[x][y] = image[BANK_BYTES_IN_IMAGE * x + y];
     */
    public static final int BANK_BYTES_IN_IMAGE = 0x1000;

    public NesBank() {
    }

    public void setBankDefault(int bank, int value) {
        bankdefault[bank] = value;
    }

    public boolean setImage(byte[] data, int offset, int size) {
        // The initial bankDefault value is invalid -1
        Arrays.fill(bankdefault, -1);

        int totalSize = ((offset & 0xfff) + data.length);
        bankMax = (totalSize >> 12); // count of full banks

        if ((totalSize & 0xfff) != 0) {
            bankMax += 1; // include last partial bank
        }
        if (bankMax > 256) {
            return false;
        }

        image = new byte[0x1000 * bankMax];
        int idx = offset & 0xfff;
        System.arraycopy(data, 0, image, idx, size);

        for (int i = 0; i < bankMax; i++) {
            bank[i] = (BANK_BYTES_IN_IMAGE * i);
        }
        for (int i = bankMax; i < bank.length; i++) {
            bank[i] = -1; // Set all to invalid values
        }

        return true;
    }

    @Override
    public void reset() {
        Arrays.fill(nullBank, (byte) 0);
        for (int i = 0; i < 16; i++) {
            bankswitch[i] = bankdefault[i];
        }
    }

    @Override
    public boolean write(int addr, int value, int id) {
        if (0x5ff8 <= addr && addr < 0x6000) {
            bankswitch[(addr & 7) + 8] = value & 0xff;
            return true;
        }

        if (fdsEnable) {
            if (0x5ff6 <= addr && addr < 0x5ff8) {
                bankswitch[addr & 7] = value & 0xff;
                return true;
            }

            if (0 <= bankswitch[addr >> 12] && 0x6000 <= addr && addr < 0xe000) {
                int idx = bank[bankswitch[addr >> 12]];
                if (idx == -1) {
                    return false;
                }
                image[idx + (addr & 0x0fff)] = (byte) (value & 0xff);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean read(int addr, IntHolder val, int id) {
        if (0x5ff8 <= addr && addr < 0x5fff) {
            val.val = bankswitch[(addr & 7) + 8];
            return true;
        }

        if (0 <= bankswitch[addr >> 12] && 0x8000 <= addr && addr < 0x10000) {
            int idx = bank[bankswitch[addr >> 12]];
            if (idx == -1) {
                val.val = 0;
            } else {
                val.val = (image[idx + (addr & 0xfff)] & 0xff); // Convert the extracted number into a positive number
            }
            return true;
        }

        if (fdsEnable) {
            if (0x5ff6 <= addr && addr < 0x5ff8) {
                val.val = bankswitch[addr & 7];
                return true;
            }

            if (0 <= bankswitch[addr >> 12] && 0x6000 <= addr && addr < 0x8000) {
                int idx = bank[bankswitch[addr >> 12]];
                if (idx == -1) {
                    val.val = 0;
                } else {
                    val.val = (image[idx + addr & 0xfff] & 0xff); // Convert the extracted number into a positive number
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void setOption(int id, int value) {
        // do nothing
    }

    public final void setFDSMode(boolean t) {
        fdsEnable = t;
    }
}
