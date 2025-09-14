package zdream.nsfplayer.nsf.renderer;

import java.util.ArrayList;
import java.util.HashMap;

import zdream.nsfplayer.core.AbstractNsfAudio;
import zdream.nsfplayer.core.CycleCounter;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.DeviceManager;
import zdream.nsfplayer.nsf.device.cpu.NesCPU;
import zdream.nsfplayer.nsf.device.memory.NesBank;
import zdream.nsfplayer.nsf.device.memory.NesMem;
import zdream.nsfplayer.nsf.executor.IN163ReattachListener;


/**
 * Nsf Runtime Status
 *
 * @author Zdream
 * @since v0.2.4
 */
public class NsfRuntime implements IResetable {

    //
    // members
    //

    public AbstractNsfAudio<?> audio;
    public final NsfParameter param = new NsfParameter();
    public final DeviceManager manager;
    public final CycleCounter cpuCounter = new CycleCounter();

    // Storage Components
    public final NesMem mem;
    public final NesBank bank;

    // actuator (computer)
    public final NesCPU cpu;

    // analog sound card

    /**
     * Mapping relationship: track number - virtual sound card.
     * <br>Possibly multiple track numbers will be mapped to a single sound card
     */
    public final HashMap<Byte, AbstractSoundChip> chips = new HashMap<>();

    /**
     * N163 Listeners for reconnection
     */
    public final ArrayList<IN163ReattachListener> n163Lsners = new ArrayList<>();

    //
    // initialization
    //

    public NsfRuntime() {
        manager = new DeviceManager(this);

        mem = new NesMem();
        bank = new NesBank();
        cpu = new NesCPU();
    }

    public void init() {
        // This method doesn't do anything.
        manager.init();
    }

    @Override
    public void reset() {
        manager.reset();
    }

    //
    // hardware part
    //

    /**
     * Rereading Nsf Audio
     */
    public void reload() {
        manager.reload();
    }
}
