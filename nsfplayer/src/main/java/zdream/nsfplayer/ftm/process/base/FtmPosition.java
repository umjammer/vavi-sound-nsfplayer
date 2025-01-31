package zdream.nsfplayer.ftm.process.base;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FamiTrackerFormatException;
import zdream.nsfplayer.ftm.format.FtmStatic;

import static java.lang.System.getLogger;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_PATTERN_LENGTH;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_SECTIONS;


/**
 * <p>Used to store FamiTracker execution location data structure.
 * <p>Facts Immutable Instances
 * </p>
 *
 * @author Zdream
 * @since v0.3.1
 */
public class FtmPosition implements Cloneable, Comparable<FtmPosition> {

    private static final Logger logger = getLogger(FtmPosition.class.getName());

    /**
     * The segment number indicated in {@link FtmAudio} starts from 0.
     */
    public final int section;
    /**
     * The line number indicated in {@link FtmAudio}, starting from 0.
     */
    public final int row;

    /**
     * Creates an Ftm position instance at the beginning of the specified segment.
     *
     * @param section Segment number, valid range is [0, MAX_SECTIONS).
     * @throws FamiTrackerFormatException When the segment number is not within the valid range
     */
    public FtmPosition(int section) throws NsfPlayerException {
        this(section, 0);
    }

    /**
     * Generates an Ftm position instance.
     *
     * @param section Segment number, valid range is [0, MAX_SECTIONS).
     * @param row     Line number, valid range is [0, MAX_PATTERN_LENGTH).
     * @throws FamiTrackerFormatException When the line number or segment number is not within the valid range
     * @see FtmStatic#MAX_SECTIONS
     * @see FtmStatic#MAX_PATTERN_LENGTH
     */
    public FtmPosition(int section, int row) {
        if (section < 0 || section >= MAX_SECTIONS) {
            throw new FamiTrackerFormatException(
                    "Segment number: " + section + " needs to be in the valid range [0, " + MAX_SECTIONS + "]");
        }
        if (row < 0 || row >= MAX_PATTERN_LENGTH) {
            throw new FamiTrackerFormatException(
                    "Row number: " + row + " needs to be in the valid range [0, " + MAX_PATTERN_LENGTH + "]");
        }

        this.section = section;
        this.row = row;
    }

    /**
     * Duplicate an Ftm location instance.
     *
     * @param o Copy Source
     */
    public FtmPosition(FtmPosition o) {
        this.section = o.section;
        this.row = o.row;
    }

    @Override
    public String toString() {
        return "FtmPosition [section=" + section + ", row=" + row + "]";
    }

    private int hash = -1;

    @Override
    public int hashCode() {
        if (hash == -1) {
            return hash = section + row * 131;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FtmPosition other = (FtmPosition) obj;
        if (row != other.row)
            return false;
        if (section != other.section)
            return false;
        return true;
    }

    @Override
    public FtmPosition clone() {
        try {
            return (FtmPosition) super.clone();
        } catch (CloneNotSupportedException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public int compareTo(FtmPosition o) {
        if (this.section != o.section) {
            return this.section - o.section;
        } else {
            return this.row - o.row;
        }
    }
}
