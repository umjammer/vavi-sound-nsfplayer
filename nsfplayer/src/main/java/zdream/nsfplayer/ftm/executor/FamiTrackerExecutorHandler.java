package zdream.nsfplayer.ftm.executor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.effect.IFtmEffect;
import zdream.nsfplayer.sound.AbstractNsfSound;

import static java.util.Objects.requireNonNull;


/**
 * <p>FamiTracker's control bar, which is used to view the execution of FamiTracker's
 * actuators and allows modification of the mode of operation and data of FamiTracker's actuators.
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public final class FamiTrackerExecutorHandler implements INsfChannelCode {

    private FamiTrackerRuntime runtime;

    FamiTrackerExecutorHandler(FamiTrackerRuntime runtime) {
        requireNonNull(runtime);
        this.runtime = runtime;
    }

    void destroy() {
        this.runtime = null;
    }

    /*
     * Parametric indicators
     */

    /**
     * <p>Ask if it has finished playing
     * <p>If a finished Ftm audio tries to call {@link #render(byte[], int, int)} or
     * {@link #renderOneFrame(byte[], int, int)} again, the stop sign is ignored and
     * it is forced to play down again.
     * </p>
     *
     * @return
     */
    public boolean isFinished() {
        return runtime.param.finished;
    }

    /**
     * @return Get the track number of the track being played
     */
    public int getCurrentTrack() {
        return runtime.param.trackIdx;
    }

    /**
     * @return Get the number of the segment being played
     */
    public int getCurrentSection() {
        return runtime.param.curSection;
    }

    /**
     * @return Get the line number being played
     */
    public int getCurrentRow() {
        return runtime.param.curRow;
    }

    /**
     * Ask if the current line is finished, need to jump to the next line (not ask if the current frame is finished).
     *
     * @return true, If the current line has already been played
     */
    public boolean currentRowRunOut() {
        return runtime.fetcher.needRowUpdate();
    }

    /**
     * <p>Gets the segment number of the next line (not the next frame), if jumped to.
     * <p>If a jump effect is detected to be triggered, the result is returned as triggered.
     * </p>
     *
     * @return Paragraph number corresponding to the next line
     */
    public int getNextSection() {
        return runtime.fetcher.getNextSection();
    }

    /**
     * <p>Get the line number of the position to jump to if jumping to the next line (not the next frame).
     * <p>If a jump effect is detected to be triggered, the result is returned as triggered.
     * </p>
     *
     * @return Paragraph number corresponding to the next line
     */
    public int getNextRow() {
        return runtime.fetcher.getNextRow();
    }

    /**
     * Get the frame rate of the execution, how many frames per second.
     * The frame rate will vary depending on the track being played.
     *
     * @return frame rate
     * @throws NullPointerException An error is thrown when a method such as {@link #ready(FtmAudio)}
     *                              is not called for initialization.
     *                              The frame rate is calculated when the execution artifact senses {@link FtmAudio}.
     */
    public int getFrameRate() {
        return runtime.querier.getFrameRate();
    }

    /**
     * Returns the set of all track numbers. The parameter for the track number is specified in {@link INsfChannelCode}.
     *
     * @return The set of all track numbers. If the ready(...) method is not called, the empty set is returned.
     */
    public Set<Byte> allChannelSet() {
        return new HashSet<>(runtime.effects.keySet());
    }

    /**
     * <p>Acquisition of a loudspeaker corresponding to the track number.
     * <p>The speaker is the final output of the actuator, and all execution results
     * are written directly to the speaker.
     * </p>
     *
     * @param channelCode channel number
     * @return An instance of the sound generator for the corresponding track.
     *         If there is no corresponding track, returns null.
     */
    public AbstractNsfSound getSound(byte channelCode) {
        AbstractFtmChannel ch = getChannel(channelCode);
        if (ch == null) {
            return null;
        }
        return ch.getSound();
    }

    /*
     * collection of effects
     */

    /**
     * Get the effect iterator for the specified track
     *
     * @param channelCode channel number
     */
    public Iterator<IFtmEffect> channelEffects(byte channelCode) {
        return runtime.effects.get(channelCode).values().iterator();
    }

    /**
     * Get the effect iterator for the global track
     */
    public Iterator<IFtmEffect> globalEffects() {
        return runtime.geffect.values().iterator();
    }

    /*
     * Execution track
     */

    /**
     * Get the master volume of the specified track
     *
     * @param channelCode channel number
     */
    public int masterVolume(byte channelCode) {
        return getChannel(channelCode).getMasterVolume();
    }

    /**
     * Get the current volume of the specified track
     *
     * @param channelCode channel number
     */
    public int currentVolume(byte channelCode) {
        return getChannel(channelCode).getCurrentVolume();
    }

    /**
     * Gets the master key of the specified track
     *
     * @param channelCode channel number
     */
    public int masterNote(byte channelCode) {
        return getChannel(channelCode).getMasterNote();
    }

    /**
     * Get the current key of the specified track
     *
     * @param channelCode channel number
     */
    public int currentNote(byte channelCode) {
        return getChannel(channelCode).getCurrentNote();
    }

    /**
     * Gets the master pitch of the specified track. This is the value of the effect Pxx.
     *
     * @param channelCode channel number
     */
    public int masterPitch(byte channelCode) {
        return getChannel(channelCode).getMasterPitch();
    }

    /**
     * Gets the current pitch calculation value for the specified track.
     * Pitch and pitch value are not necessarily positively correlated and are only indicative.
     *
     * @param channelCode channel number
     */
    public int currentPeriod(byte channelCode) {
        return getChannel(channelCode).getCurrentPeriod();
    }

    /**
     * Get the master tone of the specified track
     *
     * @param channelCode channel number
     */
    public int masterDuty(byte channelCode) {
        return getChannel(channelCode).getMasterDuty();
    }

    /**
     * Get the current tone of the specified track
     *
     * @param channelCode channel number
     */
    public int currentDuty(byte channelCode) {
        return getChannel(channelCode).getCurrentDuty();
    }

    /**
     * Gets the currently used instrument for the given track
     *
     * @param channelCode channel number
     */
    public int currentInstrument(byte channelCode) {
        return getChannel(channelCode).getInstrument();
    }

    /**
     * Ask if a track is playing
     *
     * @param channelCode channel number
     */
    public boolean isChannelPlaying(byte channelCode) {
        return getChannel(channelCode).isPlaying();
    }

    private AbstractFtmChannel getChannel(byte channelCode) {
        return runtime.channels.get(channelCode);
    }
}
