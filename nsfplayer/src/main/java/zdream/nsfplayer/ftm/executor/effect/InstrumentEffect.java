package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * Modify the effect of the instrument
 *
 * @author Zdream
 * @since 0.2.1
 */
public class InstrumentEffect implements IFtmEffect {

    public final int inst;

    private InstrumentEffect(int inst) {
        this.inst = inst;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.INSTRUMENT;
    }

    /**
     * Creates an effect of modifying the instrument.
     * If the passed inst is an illegal value such as -1, it returns null.
     *
     * @param inst Instrument Number
     * @return Effect Examples
     */
    public static InstrumentEffect of(int inst) {
        if (inst == -1) {
            return null;
        }
        return new InstrumentEffect(inst);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        runtime.channels.get(channelCode).setInstrument(inst);
    }

    @Override
    public String toString() {
        return "Inst:" + Integer.toHexString(inst);
    }
}
