package zdream.nsfplayer.nsf.device;

import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;


/**
 * Abstraction of devices for use with instruments and equipment
 *
 * @author Zdream
 */
public interface IDevice extends IResetable {

    /**
     * data writing
     *
     * @param adr address
     * @param val Fill in the value
     * @param id  Device identification information.
     *            IO support for the plural of a device, etc.
     *            This value is usually 0
     * @return true on success, false on failure
     */
    boolean write(int adr, int val, int id);

    /**
     * Reading data from a device
     *
     * @param adr address
     * @param val Parameter. Rewrite this parameter if data is read.
     * @param id  Device identification information.
     *            IO support for the plural of a device, etc.
     *            This value is usually 0
     * @return The value received. Returns null if the value could not be read or failed.
     * true on success, false on failure
     */
    boolean read(int adr, IntHolder val, int id);

    /**
     * Option parameter setting
     *
     * @param id
     * @param value
     */
    default void setOption(int id, int value) {
        // do nothing
    }
}
