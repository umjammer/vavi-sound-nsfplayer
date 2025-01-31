package zdream.nsfplayer.nsf.device;

import zdream.nsfplayer.nsf.device.cpu.IntHolder;


/**
 * Layer.
 * <p>Similar to Bus, but does not propagate reads and writes across all devices.<br>
 * The propagation stops when a device confirms the read or write.</p>
 *
 * @author Zdream
 */
public class Layer extends Bus {

    /**
     * data writing
     * <p>Performs a <code>write()</code> operation on a device installed on the bus.
     * The call order is the order in which the devices are installed.
     * If one of the devices writes successfully directly <code>return</code></p>
     *
     * @param id will be ignored in this method
     */
    @Override
    public boolean write(int addr, int value, int id) {
        boolean ret;
        for (IDevice d : vd) {
            ret = d.write(addr, value, 0);
            if (ret) {
                return true;
            }
        }
        return false;
    }

    /**
     * data retrieval
     * <p>Perform a <code>read()</code> operation on a device installed on the bus.
     * The call order is the order in which the devices are installed.
     * If one of the devices reads successfully then directly <code>return</code></p>
     *
     * @param id will be ignored in this method
     */
    @Override
    public boolean read(int adr, IntHolder val, int id) {
        val.val = 0;
        for (IDevice d : vd) {
            if (d.read(adr, val, 0))
                return true;
        }
        return false;
    }
}
