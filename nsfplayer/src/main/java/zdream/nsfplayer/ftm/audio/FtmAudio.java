package zdream.nsfplayer.ftm.audio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zdream.nsfplayer.core.AbstractNsfAudio;
import zdream.nsfplayer.core.ERegion;
import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.ftm.format.AbstractFtmInstrument;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.format.FtmTrack;

import static zdream.nsfplayer.core.NsfStatic.FRAME_RATE_NTSC;
import static zdream.nsfplayer.core.NsfStatic.FRAME_RATE_PAL;


/**
 * FamiTracker's audio data, an abstract.
 * ftm file or a wrapper object for this data format.
 *
 * @author Zdream
 * @since v0.1
 */
public class FtmAudio extends AbstractNsfAudio {

    public final FamiTrackerHandler handler;

    {
        handler = new FamiTrackerHandler(this);
    }

    /**
     * TITLE
     */
    public String title;

    /**
     * AUTHOR
     */
    public String author;

    /**
     * COPYRIGHT
     */
    public String copyright;

    /**
     * Format.
     * However, the rendering is forced to be in NTSC, which is only recorded as a parameter.
     */
    ERegion region = ERegion.NTSC;

    /**
     * fps, The effective value is [0, 800].<br>
     * If set to 0, specifies the default value for the format.
     */
    int frameRate;

    /**
     * Use of various chips
     */
    boolean useVrc6, useVrc7, useFds, useMmc5, useN163, useS5b;

    /**
     * <p>mode
     * <p>0 for old style vibrato, 1 for new style (Here's what the Famitracker documentation says)
     * <br>This refers to the pattern of vibrato. The old mode only trills upwards,
     * the new mode trills sinusoidally (up and down).
     * <p>Forces the new mode to be followed when rendering, which is simply documented here.
     * </p>
     */
    byte vibrato;

    public static final int DEFAULT_SPEED_SPLIT = 21;
    /**
     * <p>Split values for rhythm and tempo
     * <p>split point where Fxx effect sets tempo instead of speed
     * <br>'Fxx' This function affects the speed at which the music is played.
     * <p>When xx represents a value greater than or equal to {@code split},
     * it will be parsed as {@link FtmTrack#tempo}; the value will be parsed as {@link FtmTrack#tempo}.
     * <br>Otherwise it resolves to {@link FtmTrack#speed}
     */
    int split;

    /**
     * <p>Number of orbits of N163.
     * <p>Valid when {@link #useN163} is true.
     */
    int namcoChannels;

    /**
     * Standard, including {@link ERegion#NTSC} and {@link ERegion#PAL}.
     *
     * @return The format of the track
     */
    public ERegion getRegion() {
        return region;
    }

    /**
     * fps
     *
     * @return frame rate
     */
    public int getFrameRate() {
        if (frameRate == 0) {
            if (region == ERegion.NTSC) {
                return FRAME_RATE_NTSC;
            } else if (region == ERegion.PAL) {
                return FRAME_RATE_PAL;
            }
        }
        return frameRate;
    }

    /**
     * Whether it is the default frame rate for the standard
     *
     * @return
     */
    public boolean isDefaultFrameRate() {
        return frameRate == 0;
    }

    public boolean isUseVrc6() {
        return useVrc6;
    }

    public boolean isUseVrc7() {
        return useVrc7;
    }

    public boolean isUseFds() {
        return useFds;
    }

    public boolean isUseMmc5() {
        return useMmc5;
    }

    public boolean isUseN163() {
        return useN163;
    }

    public boolean isUseS5b() {
        return useS5b;
    }

    /**
     * @return {@link #vibrato}
     */
    public byte getVibrato() {
        return vibrato;
    }

    /**
     * @return {@link #split}
     */
    public int getSplit() {
        return split;
    }

    /**
     * Total number of tracks
     *
     * @return
     */
    @Override
    public int getTrackCount() {
        return tracks.size();
    }

    /**
     * @return {@link #namcoChannels}
     */
    public int getNamcoChannels() {
        return namcoChannels;
    }

    @Override
    public String toString() {

        String builder = "FTM Audio" + '\n' +
                "title" + ':' + ' ' + title + '\n' +
                "author" + ':' + ' ' + author + '\n' +
                "copyright" + ':' + ' ' + copyright + '\n';

        return builder;
    }

    //
    // instruments
    //

    /**
     * Instruments
     */
    final List<AbstractFtmInstrument> insts = new ArrayList<>();

    /**
     * sequences
     * int (chip * seqtype.length + seqtype) - seq
     */
    final Map<Integer, ArrayList<FtmSequence>> seqs = new HashMap<>();

    /**
     * sample list
     */
    final List<FtmDPCMSample> samples = new ArrayList<>();

    /**
     * Acquisition of musical instruments
     *
     * @param index
     * @return
     */
    public AbstractFtmInstrument getInstrument(int index) {
        return insts.get(index);
    }

    /**
     * @return Total number of instruments
     */
    public int instrumentCount() {
        return insts.size();
    }

    public FtmSequence getSequence(FtmChipType chip, FtmSequenceType type, int index) {
        List<FtmSequence> list = seqs.get(chip.ordinal() * FtmSequenceType.values().length + type.ordinal());
        if (list == null || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /**
     * Get the number of sequences
     */
    public int sequenceCount(FtmChipType chip, FtmSequenceType type) {
        List<FtmSequence> list = seqs.get(chip.ordinal() * FtmSequenceType.values().length + type.ordinal());
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    /*
     * track
     */

    /*
     * Tracks
     */

    final List<FtmTrack> tracks = new ArrayList<>();

    public FtmTrack getTrack(int index) {
        return tracks.get(index);
    }
}
