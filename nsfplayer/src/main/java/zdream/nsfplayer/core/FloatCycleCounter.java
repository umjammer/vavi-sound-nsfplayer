package zdream.nsfplayer.core;

/**
 * <p>Floating Point Cycle Counter, Tools
 * <p>Here is a mathematical model.
 * A time period T is evenly divided into N parts. Find the time periods T1, T2, ... of each part.
 * <br>Since T, N, T1, T2, etc. must be integers, sum(Tx) = T must be satisfied,
 * and we can conclude that T1 may not be equal to T2.
 * <p>The difference between this class and {@link CycleCounter} is that the total number of
 * segments here allows floating point numbers
 * </p>
 *
 * @author Zdream
 * @since v0.2.9
 */
public final class FloatCycleCounter implements IResetable {

    /**
     * Total number of cycles
     */
    private int cycle;

    /**
     * Total number of segments, i.e. how many segments are divided into
     */
    private float maxFrame;

    /**
     * Now in this cycle, the number of segments that have been completed
     */
    private float frameCount;

    /**
     * Now in this cycle, the number of cycles that have been completed
     */
    private int cycleCount;

    /**
     * The number of cycles to cache the previous segment. If not present, -1
     */
    private int last;

    public FloatCycleCounter() {
        this(100, 10);
    }

    public FloatCycleCounter(int cycle, float maxFrame) {
        setParam(cycle, maxFrame);
    }

    /**
     * Set the total number of cycles and segments, and reset all other parameters to default values
     *
     * @param cycle    Total number of cycles
     * @param maxFrame Total number of segments
     */
    public void setParam(int cycle, float maxFrame) {
        this.cycle = cycle;
        this.maxFrame = maxFrame;
        reset();
    }

    /**
     * Go forward one period of time
     *
     * @return The number of cycles in this time period. This number of cycles will be temporarily
     * stored and can be retrieved using {@link #getLast()} until the next call to the
     * {@link #tick()} method or reset.
     */
    public int tick() {
        float step = cycle / maxFrame;
        float start = frameCount * step;
        float end = start + step;

        cycleCount = (int) end;
        last = cycleCount - (int) (start);

        frameCount += 1;
        if (frameCount > maxFrame) {
            // Complete a cycle
            frameCount -= maxFrame;
            cycleCount -= cycle;
        }

        return last;
    }

    @Override
    public void reset() {
        frameCount = 0;
        cycleCount = 0;
        last = -1;
    }

    public int getCycle() {
        return cycle;
    }

    public float getMaxFrame() {
        return maxFrame;
    }

    public float getFrameCount() {
        return frameCount;
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public int getLast() {
        return last;
    }
}
