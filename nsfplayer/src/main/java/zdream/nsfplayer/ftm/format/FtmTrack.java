package zdream.nsfplayer.ftm.format;

import zdream.nsfplayer.ftm.audio.FtmAudio;


/**
 * <p>FTM Music
 * <p>{@link FtmAudio} It is a collection of FTM tracks.
 * </p>
 *
 * @author Zdream
 * @since v0.1
 */
public final class FtmTrack {

    public static final int
            DEFAULT_NTSC_TEMPO = 150,
            DEFAULT_PAL_TEMPO = 125;

    /**
     * Maximum number of lines per pattern/segment
     */
    public int length;

    /**
     * playback speed
     */
    public int speed;

    /**
     * tempo value
     */
    public int tempo;

    /**
     * name
     */
    public String name;

    //
    // mode
    //

    /*
     * Mode PATTERN (segment is FRAME)
     * [mode number] [track number]
     */
    public FtmPattern[][] patterns;

    /*
     * Order of repertoire
     */

    /*
     * ORDER
     * [number of paragraphs] [track number]
     */
    public int[][] orders;

    //
    // others
    //

    @Override
    public String toString() {
        return "Track" + ' ' + name;
    }
}
