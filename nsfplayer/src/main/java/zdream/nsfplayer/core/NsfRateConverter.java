package zdream.nsfplayer.core;

/**
 * <p>Rate Converter
 * <p>Used to calculate clock-related data, such as how many clocks are required per frame.
 * At the beginning of each frame, it will calculate the total number of clocks in the frame
 * through the number of clocks per second, the playback rate, etc., and write it to
 * {@link NsfCommonParameter}.
 * <p>It is a tool developed in the context of the NSF and FamiTracker work.
 * </p>
 *
 * @author Zdream
 * @since v0.2.9
 */
public class NsfRateConverter implements IResetable {

    final NsfCommonParameter param;

    public NsfRateConverter(NsfCommonParameter param) {
        this.param = param;
        this.counter = new CycleCounter();
    }

    /*
     * Clock parameters
     */
    private final CycleCounter counter;

    /*
     * Public Methods
     */

    @Override
    public void reset() {
        counter.reset();
    }

    /**
     * <p>If the parameters change, this method needs to be called to reset the class.
     * <p>The following parameters have changed and this method needs to be called:
     * <li>Frame rate
     * <li>NES CPU clock count (FTM constant)
     * <li>Playback Speed
     * </li>
     * </p>
     */
    public void onParamUpdate() {
        int cycle = countCycle();
        counter.setParam(cycle, param.frameRate);
    }

    /**
     * Pass the frame rate directly to reset the class.
     *
     * @param frameRate Frame rate, default 60
     */
    public void onParamUpdate(int frameRate) {
        param.frameRate = frameRate;

        int cycle = countCycle();
        counter.setParam(cycle, frameRate);
    }

    /**
     * Passing the frame rate and ticks per second directly causes the class to reset.
     *
     * @param frameRate  Frame rate, default 60
     * @param freqPerSec Clocks per second
     */
    public void onParamUpdate(int frameRate, int freqPerSec) {
        param.frameRate = frameRate;
        param.freqPerSec = freqPerSec;

        int cycle = countCycle();
        counter.setParam(cycle, frameRate);
    }

    /**
     * Calculate the clock-related data of this frame and write it to {@link NsfCommonParameter}.
     */
    public void doConvert() {
        int freqPerFrame = counter.tick();
        param.freqPerFrame = freqPerFrame;
    }

    /*
     * Private methods
     */

    private int countCycle() {
        int cycle = param.freqPerSec;
        if (param.speed != 1 && param.speed > 0) {
            cycle = (int) (cycle / param.speed);
        }
        return cycle;
    }
}
