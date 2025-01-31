package zdream.nsfplayer.nsf.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import zdream.nsfplayer.nsf.device.cpu.IntHolder;


/**
 * Multiple device buses can be installed, providing reset, write, and read actions.
 *
 * @author Zdream
 */
public class Bus implements IDevice, Iterable<IDevice> {

    protected final List<IDevice> vd = new ArrayList<>();

    /**
     * The <code>reset()</code> operation is performed on all devices installed on the bus.
     * The call order is the order in which the devices are installed.
     */
    @Override
    public void reset() {
        for (IDevice d : vd) {
            d.reset();
        }
    }

    /**
     * Uninstall all devices installed on the bus
     */
    public void detachAll() {
        vd.clear();
    }

    /**
     * Installation of equipment
     * <p>Install the device on this bus. </p>
     *
     * @param d Devices to be installed on the bus
     */
    public void attach(IDevice d) {
        if (d != null) {
            vd.add(d);
        }
    }

    /**
     * data writing
     * <p>The <code>write()</code> operation is performed on all devices installed on the bus.
     * The call order is the order in which the devices are installed. </p>
     *
     * @param id will be ignored in this method
     */
    @Override
    public boolean write(int addr, int value, int id) {
        boolean ret = false;
        for (IDevice d : vd) {
            d.write(addr, value, 0);
        }
        return ret;
    }

    /**
     * data retrieval
     * <p>The <code>write()</code> operation is performed on all devices installed on the bus.
     * The call order is the order in which the devices are installed. </p>
     *
     * @return Returns true if all devices installed on the bus are read correctly.
     */
    @Override
    public boolean read(int adr, IntHolder val, int id) {
        boolean ret = false;
        IntHolder vtmp = new IntHolder(0);

        val.val = 0;
        for (IDevice d : vd) {
            if (d.read(adr, vtmp, 0)) {
                val.val |= vtmp.val;
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public Iterator<IDevice> iterator() {
        return vd.iterator();
    }

    /**
     * Copy the data from memory to the bs array.
     *
     * @param bs      Arrays of data
     * @param offset  offset of bs
     * @param length  Number of data copied
     * @param address Replication starting position of the mirror image
     * @since v0.2.8
     */
    public void read(byte[] bs, int offset, int length, int address) {
        IntHolder holder = new IntHolder();

        int bsPtr = offset;
        int imgPtr = address;
        for (int i = 0; i < length; i++, bsPtr++, imgPtr++) {
            if (read(imgPtr, holder, 0)) {
                bs[bsPtr] = (byte) holder.val;
            }
        }
    }
}
