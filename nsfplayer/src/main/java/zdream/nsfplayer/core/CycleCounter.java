package zdream.nsfplayer.core;

/**
 * <p>Periodic counters, tools
 * <p>A piece of mathematical model from this field.
 * One time step T Uniform land component N Partial time step T1, T2 ...
 * <br>Therefore, T, N, and T1, T2 are equal capital integers, so this is the sum(Tx) = T,
 * which is equal to T1, which is equal to T2.
 * <p>This model has been frequently used in this process, because of the tool packaging.
 * </p>
 *
 * @author Zdream
 * @since v0.2.5
 */
public final class CycleCounter implements IResetable {

    /**
     * Total number of cycles
     */
    private int cycle;

    /**
     * Total number of stages, divided into several stages
     */
    private int maxFrame;

    /**
     * Currently in cycle, completed number of steps
     */
    private int frameCount;

    /**
     * Currently in cycle, completed cycle number
     */
    private int cycleCount;

    /**
     * As a result, the number of cycles is one step.
     * As a result, there is no existence, For -1
     */
    private int last;

    public CycleCounter() {
        this(100, 10);
    }

    public CycleCounter(int cycle, int maxFrame) {
        setParam(cycle, maxFrame);
    }

    /**
     * The number of cycles added to the number of stages is
     * the same as the number of cycles that can be added.
     *
     * @param cycle    Total number of cycles
     * @param maxFrame Total number of stages
     */
    public void setParam(int cycle, int maxFrame) {
        this.cycle = cycle;
        this.maxFrame = maxFrame;
        reset();
    }

    /**
     * Time forward running one hour step
     *
     * @return The number of cycles per time. This is the number of cycles that are
     *         used when the number of cycles is used.
     *         Use {@link #getLast()} to get it.
     */
    public int tick() {
        if (frameCount == maxFrame) {
            // A cycle ends with zero
            frameCount = 0;
            cycleCount = 0;
        }

        frameCount++;
        int oldCycleCount = cycleCount;
        cycleCount = (int) ((double) cycle * frameCount / maxFrame);

        return last = cycleCount - oldCycleCount;
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

    public int getMaxFrame() {
        return maxFrame;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public int getLast() {
        return last;
    }
}
