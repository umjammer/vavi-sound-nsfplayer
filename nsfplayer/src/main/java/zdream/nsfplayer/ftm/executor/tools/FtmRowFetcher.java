package zdream.nsfplayer.ftm.executor.tools;

import zdream.nsfplayer.ftm.audio.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.FamiTrackerParameter;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmTrack;


/**
 * Determines the position where {@link FtmAudio} has been playing, and can get the segment
 * and line that {@link FtmAudio} is currently playing.
 * <br>At the beginning of each frame, the class calculates the tempo and speed of {@link FtmAudio},
 * and then determines the paragraph and line to be played.
 *
 * @author Zdream
 * @since v0.2.1
 */
public class FtmRowFetcher {

    final FamiTrackerParameter param;

    /**
     * Query, encapsulates the currently playing track {@link FtmAudio}
     */
    FamiTrackerQuerier querier;

    /*
     * Playback parameters
     */

    /**
     * The tempo value currently being played, in beats per minute
     *
     * @see FtmTrack#tempo
     */
    int tempo;

    /**
     * The speed value of the current playback
     *
     * @see FtmTrack#speed
     */
    int speed;

    /**
     * <p>The next line number to play, starting from 0.
     * <p>When the player plays the note in row 0, row = 1, which means it is pointing to the next row.
     * This is how Famitracker is designed.
     * If you do this, there will be no continuous jump bug when you encounter line or segment skipping effects.
     * <p>Imagine this situation: Line x of paragraph 0 has a line skip effect (D00 to the first line of the next paragraph),
     * and then line 0 of paragraph 1, paragraph 2, and so on also has a line skip effect
     * (D00 to the first line of the next paragraph), and there will be a continuous skip situation.
     * This is not allowed in Famitracker.
     * </p>
     */
    int nextRow;

    /**
     * <p>When playing to the next line, the segment number (pattern)
     * </p>
     */
    int nextSection;

    /**
     * <p>Reset speed value
     * <p>The original method is in SoundGen.evaluateGlobalEffects(),
     * where the EF_SPEED part is processed.
     * </p>
     */
    public void setSpeed(int speed) {
        this.speed = speed;
        setupSpeed();
    }

    /**
     * <p>Reset tempo value
     * <p>The original method is in SoundGen.evaluateGlobalEffects(),
     * where the EF_SPEED part is processed.
     * </p>
     */
    public void setTempo(int tempo) {
        this.tempo = tempo;
        setupSpeed();
    }

    /**
     * @return Get the line number after the next playback position moves
     * @since v0.2.9
     */
    public int getNextRow() {
        if (skipRow == -1) {
            return nextRow;
        } else if (jumpSection == -1) {
            return 0;
        }

        return skipRow;
    }

    /**
     * @return Get the segment number after the next playback position moves
     * @since v0.2.9
     */
    public int getNextSection() {
        if (jumpSection == -1) {
            return nextSection;
        }

        return jumpSection;
    }

    /*
     * Jump parameter
     */

    /**
     * The segment number to jump to. Default -1 means invalid
     */
    int jumpSection = -1;

    /**
     * <p>The line number to jump to. Default is -1 which means it is invalid.
     * <p>Here is a convention. If {@link #jumpSection} is invalid,
     * it will jump to the skipRow line of the next section;
     * If {@link #jumpSection} is valid, jump to skipRow rows in the {@link #jumpSection} section.
     * </p>
     */
    int skipRow = -1;

    /**
     * Effect, jump to section.
     *
     * @param section Segment number
     */
    public void jumpToSection(int section) {
        jumpSection = section;
    }

    /**
     * <p>Effect, jump to the next row of the paragraph.
     * <p>This effect can be used in conjunction with {@link #jumpToSection(int)}. See {@link #skipRow}
     * </p>
     *
     * @param row Line number.
     */
    public void skipRows(int row) {
        skipRow = row;
    }

    /**
     * <p>Clear the data of the jump instruction.
     * </p>
     *
     * @since v0.3.1
     */
    public void clearJump() {
        jumpSection = skipRow = -1;
    }

    /*
     * Status Parameters
     */

    /**
     * Rhythm accumulator.
     * First add the tempo value required to play a row of notes,
     * then subtract one {@link #tempoDecrement} per frame
     * (accumulate)
     */
    int tempoAccum;

    /**
     * <p>Since the rhythm value and speed value are not integer multiples,
     * <code>rhythm / speed</code> will produce a remainder,
     * which means that the entire rhythm cannot be interpreted within an integer number of frames. Therefore,
     * this amount equivalent to the "remainder" is added.
     * <p>This value is determined each time the interpreter speed is determined.
     * This value does not change unless the interpreter speed ({@link #speed} or {@link #tempo}) is changed.
     * </p>
     */
    int tempoRemainder;

    /**
     * The number of beats to be deducted for each frame calculated
     */
    int tempoDecrement;

    /**
     * Whether the frame updates the row
     */
    boolean updateRow;

    /**
     * <p>Modified frame rate.
     * <p>The default value is 0, which means using the default audio frame rate;
     * If not 0, the frame rate indicated by this value is used
     * </p>
     *
     * @since v0.3.1
     */
    int frameRate;

    public int getFrameRate() {
        return frameRate == 0 ? querier.getFrameRate() : frameRate;
    }

    /**
     * Force frame rate
     *
     * @param frameRate Frame rate. 0 means using the default audio frame rate
     * @since v0.3.1
     */
    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    /*
     * Other methods
     */

    public FtmRowFetcher(FamiTrackerParameter param) {
        this.param = param;
    }

    public void ready(FamiTrackerQuerier querier, int track, int section, int row) {
        this.querier = querier;
        this.frameRate = querier.getFrameRate();
        ready(track, section, row);
    }

    /**
     * Ready without changing the track
     *
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     */
    public void ready(int track, int section, int row) {
        param.trackIdx = track;
        this.nextSection = section;
        this.nextRow = row;

        resetSpeed();
    }

    /**
     * <p>Reset speed and tempo values
     * <p>The speed defined in {@link FtmTrack} is used as the reference
     * </p>
     */
    public void resetSpeed() {
        speed = querier.audio.getTrack(param.trackIdx).speed;
        tempo = querier.audio.getTrack(param.trackIdx).tempo;

        setupSpeed();
        tempoAccum = 0;
        updateRow = false;
    }

    /**
     * Reset {@link #tempoDecrement} and {@link #tempoRemainder}
     */
    private void setupSpeed() {
        int i = tempo * 24;
        tempoDecrement = i / speed;
        tempoRemainder = i % speed;
    }

    /**
     * Ask if the current line has finished playing, and you need to jump to
     * the next line (not asking if the current frame has finished playing)
     *
     * @return true, If the current line has finished playing
     * @since v0.2.2
     */
    public final boolean needRowUpdate() {
        return tempoAccum <= 0;
    }

    /**
     * Asks whether the row has been updated in the current frame
     *
     * @return true, If the current frame updates the line
     * @since v0.3.1
     */
    public boolean isRowUpdated() {
        return updateRow;
    }

    /**
     * <p>Update playback status
     * <p>This call is made after the effects of {@link FtmNote} are processed.
     * This ensures that the effects of changing speed are taken into account.
     * </p>
     */
    public void updateState() {
        // (SoundGen.updatePlayer)
        if (tempoAccum <= 0) {
            int framePerSec = this.getFrameRate();
            // Change beats/second -> beats/minute
            tempoAccum += (60 * framePerSec) - tempoRemainder;
        }
        tempoAccum -= tempoDecrement;
    }

    /**
     * Execute, and let the tool run forward one frame.
     *
     * @return Whether the frame updates the row
     * @since v0.3.0
     */
    public boolean doFrameUpdate() {
        updateRow = false;

        if (this.needRowUpdate()) {
            this.updateRow = true;

            // Determine the part
            confirmJump();

            // Execution
            toNextRow();
        }

        return updateRow;
    }

    /**
     * <p>Determines whether to skip a line when going to the next line.
     * <p>If the previous frame has a Bxx Dxx or other effect that jumps to play,
     * then {@link #jumpSection} or {@link #skipRow} is not equal to -1.
     * Then the jump is performed directly;
     * </p>
     */
    public void confirmJump() {
        if (skipRow >= 0) {
            if (jumpSection >= 0) {
                nextSection = jumpSection;
                jumpSection = -1;
            } else {
                confirmNextSectionBegin();
            }
            nextRow = skipRow;
            skipRow = -1;
        } else if (jumpSection >= 0) {
            nextSection = jumpSection;
            nextRow = 0;
            jumpSection = -1;
        }

        // Check the section number again
        if (nextSection >= querier.trackCount(param.trackIdx)) {
            nextSection = 0;
        }
    }

    /**
     * Execute to the next line, and determine the position of the next line
     * to be played after reaching the next line
     * <p>Generally speaking, the next line played is the line after this one,
     * but this can vary in the following cases:
     * <p>When reaching the end of a paragraph,
     * it will jump to the first line of the next paragraph;
     * </p>
     */
    public void toNextRow() {
        param.curRow = nextRow;
        param.curSection = nextSection;

        nextRow++;

        // Is it to the end of the paragraph?
        int len = querier.maxRow(param.trackIdx); // Segment length
        if (nextRow >= len) {
            // Jump to line 0 of the next paragraph
            confirmNextSectionBegin();
        }
    }

    /**
     * <p>Go to the first line of the next paragraph
     * <p>This method determines the position of the next line instead of executing
     * </p>
     */
    private void confirmNextSectionBegin() {
        nextRow = 0;
        nextSection = param.curSection + 1;

        // Loop
        if (nextSection >= querier.trackCount(param.trackIdx)) {
            nextSection = 0;
        }
    }
}
