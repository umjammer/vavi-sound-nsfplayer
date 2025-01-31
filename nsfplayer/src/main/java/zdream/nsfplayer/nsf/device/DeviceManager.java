package zdream.nsfplayer.nsf.device;

import java.util.ArrayList;

import zdream.nsfplayer.core.ERegion;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.chip.NesAPU;
import zdream.nsfplayer.nsf.device.chip.NesDMC;
import zdream.nsfplayer.nsf.device.chip.NesFDS;
import zdream.nsfplayer.nsf.device.chip.NesMMC5;
import zdream.nsfplayer.nsf.device.chip.NesN163;
import zdream.nsfplayer.nsf.device.chip.NesS5B;
import zdream.nsfplayer.nsf.device.chip.NesVRC6;
import zdream.nsfplayer.nsf.device.chip.NesVRC7;
import zdream.nsfplayer.nsf.executor.IN163ReattachListener;
import zdream.nsfplayer.nsf.renderer.INsfRuntimeHolder;
import zdream.nsfplayer.nsf.renderer.NsfRendererConfig;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.SoundN163;

import static zdream.nsfplayer.core.ERegion.DENDY;
import static zdream.nsfplayer.core.ERegion.NTSC;
import static zdream.nsfplayer.core.ERegion.PAL;
import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_DENDY;
import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_NTSC;
import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_PAL;


/**
 * Manager of all hardware devices used to manage the runtime state of Nsf
 *
 * @author Zdream
 * @since v0.2.4
 */
public class DeviceManager implements INsfRuntimeHolder, IResetable {

    final NsfRuntime runtime;

    public DeviceManager(NsfRuntime runtime) {
        this.runtime = runtime;

        apu = new NesAPU(runtime);
        dmc = new NesDMC(runtime);
        vrc6 = new NesVRC6(runtime);
        mmc5 = new NesMMC5(runtime);
        fds = new NesFDS(runtime);
        n163 = new NesN163(runtime);
        vrc7 = new NesVRC7(runtime);
        s5b = new NesS5B(runtime);
    }

    @Override
    public NsfRuntime getRuntime() {
        return runtime;
    }

    public void init() {
        initSoundChip();
    }

    //
    // parameters
    //

    /**
     * Actual system used
     */
    ERegion region = NTSC;
    /**
     * Track number being played
     */
    int song;

    public void setSong(int song) {
        if (song >= runtime.audio.total_songs) {
            this.song = runtime.audio.total_songs - 1;
        } else if (song < 0) {
            this.song = 0;
        } else {
            this.song = song;
        }
    }

    /**
     * @return Track number being played
     * @since v0.2.8
     */
    public int getSong() {
        return song;
    }

    /**
     * Get the format of the current playback
     *
     * @since v0.2.8
     */
    public ERegion getRegion() {
        return region;
    }

    //
    // connected device
    //

    // computer bus
    public final Bus apu_bus = new Bus();
    public final Layer stack = new Layer();
    public final Layer layer = new Layer();

    /**
     * Disassemble all connected equipment.
     */
    private void detachAll() {
        stack.detachAll();
        layer.detachAll();
        apu_bus.detachAll();
    }

    // audio chip
    public final NesAPU apu;
    public final NesDMC dmc;
    public final NesVRC6 vrc6;
    public final NesMMC5 mmc5;
    public final NesFDS fds;
    public final NesN163 n163;
    public final NesVRC7 vrc7;
    public final NesS5B s5b;

    private void initSoundChip() {
        // Initialize some of the sound card's data.
        // Now you can get the sample rate from runtime.param.sampleRate.
        // Originally, the following was done in NsfPlayer.setPlayFreq(double)
        // but it's all mixer related, so all the mixer related work is no longer here.
        // so the following code is empty.
    }

    /**
     * Put the sound card into the runtime.chips mapping.
     *
     * @param chip
     */
    private void putSoundChipToRuntime(AbstractSoundChip chip) {
        byte[] channelCodes = chip.getAllChannelCodes();
        for (byte channelCode : channelCodes) {
            runtime.chips.put(channelCode, chip);
        }
    }

    //
    // reset
    //

    @Override
    public void reset() {
        // The original project was NsfPlayer.reset()

        // Determine the system
        region = confirmRegion();
        switch (region) {
            case NTSC:
                runtime.cpu.NES_BASECYCLES = BASE_FREQ_NTSC;
                break;
            case PAL:
                runtime.cpu.NES_BASECYCLES = BASE_FREQ_PAL;
                break;
            default:
                runtime.cpu.NES_BASECYCLES = BASE_FREQ_DENDY;
                break;
        }
        runtime.param.freqPerSec = runtime.cpu.NES_BASECYCLES;
        runtime.cpuCounter.setParam(runtime.param.freqPerSec, runtime.param.sampleRate);

        // Since the RAM space may have been modified after playback, it needs to be reloaded.
        reload();
        // Apply all configurations
        // config.notify(-1);
        stack.reset();
        // After the bus is reset, the CPU needs to be reset as well.
        runtime.cpu.reset();
        resetCPUCounter();

        // The frame rate used by the NSF internal virtual CPU is double precision floating point.
        // Default, NTSC: 60.0988, PAL/DENDY: 50.0070
        double speed;
        speed = 1000000.0 / ((region == NTSC) ? runtime.audio.speed_ntsc : runtime.audio.speed_pal);

        runtime.cpu.start(runtime.audio.init_address, runtime.audio.play_address,
                speed, this.song, (region == PAL) ? 1 : 0, 0);
    }

    /**
     * Determination of type.
     * NsfPlayer.getRegion(int)
     *
     * @return
     */
    public ERegion confirmRegion() {
        // Format specified in NSF
        ERegion flags = runtime.audio.getRegion();

        // Look at the contents of the flags data to determine
        // single-mode NSF, if only one mode is supported, then render in that mode.
        if (flags == NTSC || flags == PAL) {
            return flags;
        }

        // Rendering in a user-specified format
        int pref = runtime.param.region;

        switch (pref) {
            case NsfRendererConfig.REGION_FORCE_NTSC:
                return NTSC;
            case NsfRendererConfig.REGION_FORCE_PAL:
                return PAL;
            case NsfRendererConfig.REGION_FORCE_DENDY:
                return DENDY;
        }

        if (pref == 1)
            return NTSC;
        if (pref == 2)
            return PAL;

        return NTSC;
    }

    /**
     * <p>Perform a reassembly of the virtual device according to the information in the Nsf.
     * </p>
     * <p>
     * Referring to the NsfPlayer class, see NsfPlayer.reload().
     */
    public void reload() {

        int maxBankswitch = reloadMemory();

        // Perform a complete dismantling and reinstallation
        detachAll();
        runtime.chips.clear();

        // Here's how to start connecting devices

        // LoopDetector is ignored here.
        // Ignore CPULogger here.

        if (maxBankswitch != 0) {
            layer.attach(runtime.bank);
        }
        layer.attach(runtime.mem);

        // Connecting Audio Chips
        apu_bus.attach(apu);
        apu_bus.attach(dmc);
        // Hook up the sound card to the runtime first.
        putSoundChipToRuntime(apu);
        putSoundChipToRuntime(dmc);

        stack.attach(apu_bus);

        if (runtime.audio.useVrc6()) {
            stack.attach(vrc6);
            putSoundChipToRuntime(vrc6);
        }
        if (runtime.audio.useMmc5()) {
            stack.attach(mmc5);
            putSoundChipToRuntime(mmc5);
        }
        if (runtime.audio.useFds()) {
            stack.attach(fds);
            putSoundChipToRuntime(fds);
        }
        if (runtime.audio.useN163()) {
            n163.forceChannelCount(1);
            stack.attach(n163);
            putSoundChipToRuntime(n163);
        }
        if (runtime.audio.useVrc7()) {
            stack.attach(vrc7);
            putSoundChipToRuntime(vrc7);
        }
        if (runtime.audio.useS5b()) {
            stack.attach(s5b);
            putSoundChipToRuntime(s5b);
        }

        // And finally, layer
        stack.attach(layer);

        // NOTE: each layer in the stack is given a chance to take a read or write
        // exclusively. The stack is structured like this:
        // loop detector > APU > expansions > main memory
        // Note that the LoopDetector is ignored, so there is no loop detector.

        // main memory comes after other expansions because
        // when the FDS mode is enabled, VRC6/VRC7/5B have writable registers
        // in RAM areas of main memory. To prevent these from overwriting RAM
        // I allow the expansions above it in the stack to prevent them.

        // MMC5 comes high in the stack so that its PCM read behaviour
        // can reread from the stack below and does not get blocked by any
        // stack above.

        runtime.cpu.setMemory(stack);
    }

    /**
     * Based on the number of tracks in N163 after reset,
     * reconnect the tracks associated with N163 to the mixer.
     *
     * @param n163ChannelCount
     */
    public void reattachN163(int n163ChannelCount) {
        for (int i = 0; i < 8; i++) {
            byte channelCode = (byte) (NesN163.CHANNEL_N163_1 + i);
            SoundN163 sound = n163.getSound(channelCode);
            boolean on = sound != null;

            if (on) {
                if (!runtime.chips.containsKey(channelCode)) {
                    runtime.chips.put(channelCode, n163);
                }
            } else {
                if (runtime.chips.containsKey(channelCode)) {
                    runtime.chips.remove(channelCode);
                }
            }
        }

        ArrayList<IN163ReattachListener> ls = runtime.n163Lsners;
        for (IN163ReattachListener l : ls) {
            l.onReattach(n163ChannelCount);
        }
    }

    /**
     * Reset and overwrite the data in the memory with the data in the audio.
     *
     * @return Maximum value of bank-switch
     */
    private int reloadMemory() {
        NsfAudio audio = runtime.audio;

        int i, bmax = 0;

        for (i = 0; i < 8; i++)
            if (bmax < audio.bankswitch[i])
                bmax = audio.bankswitch[i];

        runtime.mem.setImage(audio.body, audio.load_address, audio.body.length);

        if (bmax != 0) {
            runtime.bank.setImage(audio.body, audio.load_address, audio.body.length);
            for (i = 0; i < 8; i++)
                runtime.bank.setBankDefault(i + 8, audio.bankswitch[i]);
        }

        return bmax;
    }

    //
    // execute
    //

    /**
     * The number of remaining unused CPU clocks.
     * NsfPlayer.cpu_clock_rest
     */
    int cpuFreqRemain;

    private void resetCPUCounter() {
        cpuFreqRemain = 0;
    }

    /**
     * Let the CPU go down one frame.
     * (Although it is said to be one frame, it actually depends on
     * the number of samples in the current frame)
     */
    public void tickCPU() {
        int freqInCurSample = runtime.cpuCounter.tick();

        cpuFreqRemain += freqInCurSample;
        if (cpuFreqRemain > 0) {
            int realCpuFreq = runtime.cpu.exec(cpuFreqRemain);
            cpuFreqRemain -= realCpuFreq;

            // tick APU frame sequencer
//            fsc.tickFrameSequence(real_cpu_clocks);
//            if (nsf.useMmc5)
//                mmc5.tickFrameSequence(real_cpu_clocks);
        }
    }
}
