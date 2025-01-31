package zdream.nsfplayer.ftm.executor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfExecutor;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.ftm.audio.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.effect.IFtmEffect;
import zdream.nsfplayer.ftm.executor.hook.IFtmExecutedListener;
import zdream.nsfplayer.ftm.executor.hook.IFtmFetchListener;
import zdream.nsfplayer.ftm.executor.tools.FtmRowFetcher;
import zdream.nsfplayer.ftm.process.base.FtmPosition;
import zdream.nsfplayer.sound.AbstractNsfSound;

import static java.util.Objects.requireNonNull;


/**
 * <p>Execution artifacts of FamiTracker.
 * <p>In version 0.2.x, the execution of the FamiTracker is written directly in the FamiTrackerRenderer.
 * As of version 0.3.0, the execution artifacts are separated from the renderer and form a separate class.
 * It takes over execution-related tasks that would otherwise be done by the FamiTrackerRuntime or FamiTrackerRenderer.
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public class FamiTrackerExecutor extends AbstractNsfExecutor<FtmAudio> {

    /**
     * execution context
     */
    private final FamiTrackerRuntime runtime;

    public FamiTrackerExecutor() {
        runtime = new FamiTrackerRuntime();
        this.runtime.init();
    }

    /*
     * preliminary
     */

    /**
     * <p>Let the actuator read the corresponding audio data.
     * <p>Set the playback pause position to the first segment (segment 0) of the first track (track 0).
     * </p>
     *
     * @param audio FamiTracker's Packaged Tracks
     */
    @Override
    public void ready(FtmAudio audio) throws NsfPlayerException {
        ready(audio, 0, 0, 0);
    }

    /**
     * <p>Let the actuator read the corresponding audio data.
     * <p>Sets the playback pause position to the first segment (segment 0) of the specified track.
     * </p>
     *
     * @param audio FamiTracker's Packaged Tracks
         * @param track Track number, from 0
     */
    public void ready(FtmAudio audio, int track) throws NsfPlayerException {
        ready(audio, track, 0, 0);
    }

    /**
     * <p>Let the actuator read the corresponding audio data.
     * <p>Setting the playback pause position to a specified section of a specified track
     * </p>
     *
     * @param audio   FamiTracker's Packaged Tracks
     * @param track   Track number, from 0
     * @param section Segment number, starting from 0
     */
    public void ready(
            FtmAudio audio,
            int track,
            int section)
            throws NsfPlayerException {
        ready(audio, track, section, 0);
    }

    /**
     * <p>Let the actuator read the corresponding audio data.
     * <p>Set the playback pause position to the specified line of the specified track.
     * </p>
     *
     * @param audio   FamiTracker's Packaged Tracks
     * @param track   Track number, from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     * @since v0.3.1
     */
    public void ready(
            FtmAudio audio,
            int track,
            int section,
            int row)
            throws NsfPlayerException {
        requireNonNull(audio, "FamiTracker track audio = null");

        runtime.ready(audio, track, section, row);
        readyChannels();
        runtime.switchFlag = true;
    }

    /**
     * <p>Reset the current track without changing the Ftm audio,
     * so that the execution position is reset to the beginning of track 0.
     * <p>Ftm audio data needs to be specified in the first call.
     * So the first time you need to call the overloaded method with the {@link FtmAudio} argument
     * </p>
     *
     * @throws NullPointerException When {@link FtmAudio} audio is not specified before calling this method
     */
    public void ready() throws NsfPlayerException {
        ready(0, 0, 0);
    }

    /**
     * <p>Switch to the beginning of the specified track without changing the Ftm file.
     * <p>Ftm audio data needs to be specified in the first call.
     * So the first time you need to call the overloaded method with the {@link FtmAudio} argument
     * </p>
     *
     * @param track Track number, from 0
     * @throws NullPointerException When {@link FtmAudio} audio is not specified before calling this method
     */
    public void ready(int track) throws NsfPlayerException {
        ready(track, 0, 0);
    }

    /**
     * <p>So the first time you need to call the overloaded method with the {@link FtmAudio} argument
     * <p>Ftm audio data needs to be specified in the first call.
     * So the first time you need to call the overloaded method with the {@link FtmAudio} argument
     * </p>Switching Tracks and Segments without Changing Ftm Files
     *
     * @param track   Track number, from 0
     * @param section Segment number, starting from 0
     * @throws NullPointerException When {@link FtmAudio} audio is not specified before calling this method
     */
    public void ready(int track, int section) throws NsfPlayerException {
        ready(track, section, 0);
    }

    /**
     * <p>So the first time you need to call the overloaded method with the {@link FtmAudio} argument
     * <p>Ftm audio data needs to be specified in the first call.
     * So the first time you need to call the overloaded method with the {@link FtmAudio} argument
     * </p>
     *
     * @param track   Track number, from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     * @throws NullPointerException When {@link FtmAudio} audio is not specified before calling this method
     * @since v0.3.1
     */
    public void ready(int track, int section, int row) throws NsfPlayerException {
        requireNonNull(runtime.querier, "FamiTracker track audio = null");

        runtime.ready(track, section, row);
        runtime.resetAllChannels();
        runtime.switchFlag = true;
    }

    /**
     * <p>Switch paragraph and line numbers without changing the Ftm file or track number.
     * <p>Ftm audio data needs to be specified in the first call.
     * So the first time you need to call the overloaded method with the {@link FtmAudio} argument
     *
     * @param pos Playback position, not null
     * @throws NullPointerException When {@link FtmAudio} audio is not specified before calling this method, or pos == null
     * @since v0.3.1
     */
    public void ready(FtmPosition pos) {
        requireNonNull(pos, "FamiTracker position pos = null");
        ready(getCurrentTrack(), pos.section, pos.row);
    }

    /**
     * <p>Without changing the parameters of each track, switch to the specified position and execute downwards.
     * When switching, the playback pitch, volume, and effects of each track remain unchanged, including
     * the delay effect Gxx.
     * The mixer is not reset, which means that the sound played in the previous frame may continue to be played
     * for an extended period of time.
     * The playback speed of the FTM document (not the playback speed speed) is reset according to the value of tempo.
     * <p>Please use this method with caution. If vibrato 4xy or other effects are used in the front and not canceled,
     * they will remain after switching positions, resulting in strange playback in the back.
     * For a more robust way of switching playback position without changing the playback effect significantly,
     * use the {@link #ready(int, int)} or {@link #skip(int)} methods.
     * <p>You need to make sure that the renderer has successfully loaded {@link FtmAudio} audio before calling it.
     * </p>
     *
     * @param track   Track number, from 0
     * @param section Segment number, starting from 0
     * @throws NullPointerException When {@link FtmAudio} audio is not successfully loaded before calling this method
     * @see #ready(int, int)
     * @see #skip(int)
     * @since v0.2.9
     */
    public void switchTo(int track, int section) {
        switchTo(track, section, 0);
    }

    /**
     * <p>Without changing the parameters of each track, switch to the specified position and execute downwards.
     * When switching, the playback pitch, volume, and effects of each track remain unchanged, including the
     * delay effect Gxx.
     * The mixer is not reset, which means that the sound played in the previous frame may continue to be played
     * for an extended period of time.
     * The playback speed of the FTM document (not the playback speed speed) will be reset according to the tempo value.
     * <p>Please use this method with caution. If a tremolo 4xy or other effect is used in front of it, and it is
     * not removed, the
     * After switching positions, these effects will remain, resulting in strange playback later on.
     * For a more robust way of switching playback position without changing the playback effect significantly,
     * use the {@link #ready(int, int)} or {@link #skip(int)} methods.
     * <p>You need to make sure that the renderer has successfully loaded {@link FtmAudio} audio before calling it.
     * </p>
     *
     * @param track   Track number, from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     * @throws NullPointerException When {@link FtmAudio} audio is not successfully loaded before calling this method
     * @see #ready(int, int)
     * @see #skip(int)
     * @since v0.3.1
     */
    public void switchTo(int track, int section, int row) {
        requireNonNull(runtime.querier, "FamiTracker Tracks audio = null");

        runtime.ready(track, section, row);
        runtime.switchFlag = true;
    }

    /**
     * <p>Without changing the parameters of each track, switch to the specified position and execute downwards.
     * <p>You need to make sure that the renderer has successfully loaded {@link FtmAudio} audio before calling it.
     * </p>
     *
     * @param pos Playback position, not null
     * @throws NullPointerException When {@link FtmAudio} audio is not specified before calling this method,
     *                              or when pos == null
     * @see #ready(FtmPosition)
     * @see #switchTo(int, int)
     * @see #switchTo(int, int, int)
     * @since v0.3.1
     */
    public void switchTo(FtmPosition pos) {
        requireNonNull(pos, "FamiTracker location pos = null");
        switchTo(getCurrentTrack(), pos.section, pos.row);
    }

    private void readyChannels() {
        runtime.channels.clear();
        runtime.effects.clear();
        runtime.selector.reset();

        FamiTrackerQuerier querier = runtime.querier;

        int len = querier.channelCount();
        for (int i = 0; i < len; i++) {
            byte code = querier.channelCode(i);

            AbstractFtmChannel ch = runtime.selector.selectFtmChannel(code);
            ch.setRuntime(runtime);
            runtime.channels.put(code, ch);
            runtime.effects.put(code, new HashMap<>());
        }
    }

    /*
     * operative part
     */

    /**
     * Execute a frame
     */
    @Override
    public void tick() {
        runtime.runFrame();
        updateChannels();

        runtime.fetcher.updateState();
        runtime.updateFlag();
    }

    /**
     * <p>Blocking execution of a frame.
     * <p>In this frame, all previously triggered effects continue to be executed.
     * However, all line skips and playback position changes will not be performed,
     * and the calculation of time will be suspended.
     * </p>
     *
     * @since v0.3.1
     */
    public void tickBlock() {
        updateChannels();
        runtime.fetcher.updateState();
        runtime.updateFlag();
    }

    /**
     * <p>Let each channel do the playback. But this process only writes data to the speaker,
     * it doesn't make the speaker work.
     * <p>So the length of the job and how it works will be left to the caller to realize.
     * </p>
     */
    private void updateChannels() {
        FamiTrackerQuerier querier = runtime.querier;

        // global effect
        for (IFtmEffect eff : runtime.geffect.values()) {
            eff.execute((byte) 0, runtime);
        }

        // local effect
        if (runtime.elners.isEmpty()) {
            // Here's the process that doesn't need to call the listener (actually,
            // it's just trying to be faster, so the two processes aren't merged)
            int len = querier.channelCount();
            for (int i = 0; i < len; i++) {
                byte code = querier.channelCode(i);
                AbstractFtmChannel channel = runtime.channels.get(code);

                channel.playNote();
                channel.writeToSound();
            }
        } else {
            // Here's the flow of the listener that needs to be called
            int len = querier.channelCount();
            for (int i = 0; i < len; i++) {
                byte code = querier.channelCode(i);
                runtime.channels.get(code).playNote();
            }
            runtime.onExecuteFinished();
            for (int i = 0; i < len; i++) {
                byte code = querier.channelCode(i);
                runtime.channels.get(code).writeToSound();
            }
        }
    }

    @Override
    public void reset() {
        ready();
    }

    /*
     * Parameter indicators
     */

    /**
     * <p>Asks if the current frame has updated the rows.
     * <p>Whether you switch the execution position manually,
     * or {@link FtmRowFetcher} automatically switches to the next row, the row is considered updated.
     * </p>
     *
     * @return true, If the current frame updates the line
     * @since v0.3.1
     */
    public boolean isRowUpdated() {
        return runtime.updateFlag;
    }

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
     * @return Get the number of the paragraph being executed
     */
    public int getCurrentSection() {
        return runtime.param.curSection;
    }

    /**
     * @return Get the line number being executed
     */
    public int getCurrentRow() {
        return runtime.param.curRow;
    }

    /**
     * @return Get the location of the ongoing execution
     * @since v0.3.1
     */
    public FtmPosition currentPosition() {
        return new FtmPosition(runtime.param.curSection, runtime.param.curRow);
    }

    /**
     * Ask if the current line is finished, need to jump to the next line
     * (not ask if the current frame is finished).
     *
     * @return true, if the current line has been played. The next frame will be a line skip
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
     * <p>Playback position, not null
     * </p>
     *
     * @return The segment number corresponding to the next line
     */
    public int getNextRow() {
        return runtime.fetcher.getNextRow();
    }

    /**
     * <p>Gets the position to jump to if jumping to the next line (not the next frame).
     * <p>Playback position, not null
     * </p>
     *
     * @return Get the position of the next line to be executed
     * @since v0.3.1
     */
    public FtmPosition nextPosition() {
        return new FtmPosition(runtime.fetcher.getNextSection(), runtime.fetcher.getNextRow());
    }

    /**
     * Get the frame rate of the execution, how many frames per second. The frame rate will
     * vary depending on the track being played.
     *
     * @return Actual execution frame rate.
     * <br>If the frame rate is locked, the actual execution frame rate is the locked frame rate;
     * otherwise it is the default frame rate of the audio.
     * @throws NullPointerException An error will be thrown if the frame rate is not locked beforehand
     *                              if the method {@link #ready(FtmAudio)} is not called
     *                              for initialization.
     *                              <br>The frame rate is prioritized by using the locked frame rate,
     *                              when the frame rate is not locked beforehand,
     *                              it needs to be obtained via {@link FtmAudio}.
     * @see #lockFrameRate(int)
     */
    public int getFrameRate() {
        return runtime.fetcher.getFrameRate();
    }

    /**
     * Forced frame rate lock. Subsequent songs are rendered at this frame rate until
     * the next call to {@link #lockFrameRate(int)} or {@link #unlockFrameRate()}.
     *
     * @param frameRate Frame rate. Must be in the range [50, 300].
     * @throws NsfPlayerException Frame rate. Must be in the range [50, 300].
     * @see #unlockFrameRate()
     * @since v0.3.1
     */
    public void lockFrameRate(int frameRate) {
        if (frameRate < 50 || frameRate > 300) {
            throw new NsfPlayerException("Lock frame rate : " + frameRate + " needs to be in range [50, 300].");
        }
        runtime.fetcher.setFrameRate(frameRate);
    }

    /**
     * Unlock frame rate. Subsequent songs are rendered at the default frame rate used by the song,
     * until the next call to {@link #lockFrameRate(int)} or {@link #unlockFrameRate()}.
     *
     * @see #lockFrameRate(int)
     * @since v0.3.1
     */
    public void unlockFrameRate() {
        runtime.fetcher.setFrameRate(0);
    }

    /**
     * Returns the set of all track numbers. The parameter for the track number
     * is specified in {@link INsfChannelCode}.
     *
     * @return The set of all track numbers. If the ready(...) method is not called,
     *         the empty set is returned.
     */
    public Set<Byte> allChannelSet() {
        return new HashSet<>(runtime.effects.keySet());
    }

    /**
     * <p>Acquisition of a loudspeaker corresponding to the track number.
     * <p>Gets the speaker corresponding to the track number.
     * </p>
     *
     * @param channelCode channel number
     * @return An instance of the sound generator for the corresponding track.
     *         If there is no corresponding track, returns null.
     */
    public AbstractNsfSound getSound(byte channelCode) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);
        if (ch == null) {
            return null;
        }
        return ch.getSound();
    }

    /*
     * listener
     */

    /**
     * Add a listener to get the tone key
     *
     * @param l Get the listener for the key
     * @throws NullPointerException When the listener <code>l == null</code
     */
    public void addFetchListener(IFtmFetchListener l) {
        requireNonNull(l, "listener = null");
        runtime.flners.add(l);
    }

    /**
     * Remove the listener that gets the tone key
     *
     * @param l Remove the listener for the tone key
     */
    public void removeFetchListener(IFtmFetchListener l) {
        runtime.flners.remove(l);
    }

    /**
     * Empty all the listeners that get the tone keys.
     */
    public void clearFetchListener() {
        runtime.flners.clear();
    }

    /**
     * Add an end-of-execution listener.
     * This listener wakes up when the effect has finished executing,
     * but before the sound has been written.
     *
     * @param l End-of-execution listener
     * @throws NullPointerException When the listener <code>l == null</code
     */
    public void addExecuteFinishedListener(IFtmExecutedListener l) {
        requireNonNull(l, "listener = null");
        runtime.elners.add(l);
    }

    /**
     * Remove the end-of-execution listener
     *
     * @param l End-of-execution listener
     */
    public void removeExecuteFinishedListener(IFtmExecutedListener l) {
        runtime.elners.remove(l);
    }

    /**
     * Empty all end-of-execution listeners
     */
    public void clearExecuteFinishedListener() {
        runtime.elners.clear();
    }
}
