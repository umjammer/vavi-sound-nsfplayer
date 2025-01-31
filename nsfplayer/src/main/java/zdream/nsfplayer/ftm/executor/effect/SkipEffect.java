package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * <p>Jump to the specified line of the next paragraph, Dxx
 * <p>Global effect
 * </p>
 *
 * @author Zdream
 * @since 0.2.1
 */
public class SkipEffect implements IFtmEffect {

    /**
     * Jump to segment number
     */
    final int row;

    private SkipEffect(int row) {
        this.row = row;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.SKIP;
    }

    /**
     * Create an effect of jumping to the next specified line and playing
     *
     * @param row Line number. Must be non-negative
     * @return Effect Examples
     * @throws IllegalArgumentException When <code>row</code> When not in the specified range
     */
    public static SkipEffect of(int row) throws IllegalArgumentException {
        if (row < 0) {
            throw new IllegalArgumentException("The row number must be a non-negative numeric value.");
        }
        return new SkipEffect(row);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        runtime.fetcher.skipRows(row);
    }

    @Override
    public String toString() {
        return "SkipTo:" + row;
    }
}
