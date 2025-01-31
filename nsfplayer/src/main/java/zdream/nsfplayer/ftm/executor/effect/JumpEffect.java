package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * <p>Jump to the specified segment, Bxx
 * <p>Global effect
 * </p>
 *
 * @author Zdream
 * @since 0.2.1
 */
public class JumpEffect implements IFtmEffect {

    /**
     * Jump to segment number
     */
    public final int section;

    private JumpEffect(int section) {
        this.section = section;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.JUMP;
    }

    /**
     * Create an effect of jumping to the beginning of a specified segment and playing it
     *
     * @param section Segment number. Must be a non-negative number
     * @return Effect Examples
     * @throws IllegalArgumentException When <code>section</code> is not within the specified range
     */
    public static JumpEffect of(int section) throws IllegalArgumentException {
        if (section < 0) {
            throw new IllegalArgumentException("The segment number must be a positive integer value.");
        }
        return new JumpEffect(section);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime rumtime) {
        rumtime.fetcher.jumpToSection(section);
    }

    @Override
    public String toString() {
        return "JumpTo:" + section;
    }
}
