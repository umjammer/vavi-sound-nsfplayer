package zdream.nsfplayer.ftm.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import zdream.nsfplayer.core.AbstractRenderer;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfPlayerApplication;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.core.NsfRateConverter;
import zdream.nsfplayer.core.NsfStatic;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.FamiTrackerExecutor;
import zdream.nsfplayer.ftm.executor.hook.IFtmExecutedListener;
import zdream.nsfplayer.ftm.executor.hook.IFtmFetchListener;
import zdream.nsfplayer.ftm.process.SyncProcessManager;
import zdream.nsfplayer.ftm.process.agreement.WaitingAgreement;
import zdream.nsfplayer.ftm.process.base.FtmPosition;
import zdream.nsfplayer.mixer.EmptyMixerChannel;
import zdream.nsfplayer.mixer.IMixerConfig;
import zdream.nsfplayer.mixer.ISoundMixer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.nsfplayer.sound.AbstractNsfSound;

import static java.util.Objects.requireNonNull;
import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_NTSC;


/**
 * <p>FamiTracker synchronized audio renderer.
 * <p>Support multiple {@link FtmAudio} to play simultaneously, and add some stop-and-wait protocols.
 * <p>This renderer is not thread-safe, please be careful not to set parameters during rendering.
 * </p>
 *
 * @author Zdream
 * @since v0.3.1
 */
public class FamiTrackerSyncRenderer extends AbstractRenderer<FtmAudio>
        implements INsfChannelCode {

    /**
     * Rate converter, also used by the main actuator
     */
    private final NsfRateConverter rate;

    /**
     * Audio Mixer
     */
    private ISoundMixer mixer;

    /**
     * Execution, waiting management
     */
    private final SyncProcessManager process = new SyncProcessManager();

    private final FamiTrackerConfig config;

    /**
     * Parameters, also used by the main actuator
     */
    private final NsfCommonParameter param = new NsfCommonParameter();

    public FamiTrackerSyncRenderer() {
        this(null);
    }

    public FamiTrackerSyncRenderer(FamiTrackerConfig config) {
        if (config == null) {
            this.config = new FamiTrackerConfig();
        } else {
            this.config = config.clone();
        }

        // Sampling rate data is only needed for rendering builds
        param.sampleRate = this.config.sampleRate;
        // Uses the actual frameRate.
        // The actual frame rate is based on the frame rate of the main executor, and other executors will be forced to use this frame rate.
        param.frameRate = NsfStatic.FRAME_RATE_NTSC;
        param.freqPerSec = NsfStatic.BASE_FREQ_NTSC;

        // The volume parameter is only needed for rendering builds
        param.levels.copyFrom(this.config.channelLevels);

        rate = new NsfRateConverter(param);
        initMixer();
        initExecutors();
    }

    private void initMixer() {
        IMixerConfig mixerConfig = config.mixerConfig;
        if (mixerConfig == null) {
            mixerConfig = new XgmMixerConfig();
        }

        this.mixer = NsfPlayerApplication.app.mixerFactory.create(mixerConfig, param);
        this.mixer.reset();
    }

    /*
     * Preparation
     */

    /**
     * <p>Let the main executor of the renderer read the corresponding audio data and set the playback pause position to the beginning of track 0.
     * <p>This rendering has one and only one main executor. If this executor is rendering other audio,
     * it will be interrupted and directly jump to the beginning of the new audio. For other executors,
     * there is no impact.
     * </p>
     *
     * @param audio The audio data to be rendered
     * @see #updateAudio(int, FtmAudio, int, int, int, boolean)
     */
    @Override
    public void ready(FtmAudio audio) {
        updateAudio(masterExecutorId, audio, 0, 0, 0, true);
        eParams[masterExecutorId].enable = true;
    }

    /**
     * <p>Let the main executor of the renderer read the corresponding audio data and set the playback pause position to the beginning of the specified track.
     * <p>This rendering has one and only one main executor. If this executor is rendering other audio,
     * it will be interrupted and directly jump to the beginning of the new audio. For other executors,
     * there is no impact.
     * </p>
     *
     * @param audio The audio data to be rendered
     * @param track Track number, starting from 0
     * @see #updateAudio(int, FtmAudio, int, int, int, boolean)
     */
    public void ready(FtmAudio audio, int track) {
        updateAudio(masterExecutorId, audio, track, 0, 0, true);
        eParams[masterExecutorId].enable = true;
    }

    /**
     * <p>Let the main executor of the renderer read the corresponding audio data and set the playback pause position to the specified position of the specified track.
     * <p>This rendering has only one main executor. If this executor is rendering other audio,
     * it will be interrupted and directly jump to the beginning of the new audio. For other executors,
     * it will not be affected.
     * </p>
     *
     * @param audio   The audio data to be rendered
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @see #updateAudio(int, FtmAudio, int, int, int, boolean)
     */
    public void ready(
            FtmAudio audio,
            int track,
            int section) {
        updateAudio(masterExecutorId, audio, track, section, 0, true);
        eParams[masterExecutorId].enable = true;
    }

    /**
     * <p>Let the main executor of the renderer read the corresponding audio data and set the playback pause position to the specified position of the specified track.
     * <p>This rendering has one and only one main executor. If this executor is rendering other audio,
     * it will be interrupted and directly jump to the beginning of the new audio. For other executors,
     * there is no impact.
     * </p>
     *
     * @param audio   The audio data to be rendered
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     * @see #updateAudio(int, FtmAudio, int, int, int, boolean)
     */
    public void ready(
            FtmAudio audio,
            int track,
            int section,
            int row) {
        updateAudio(masterExecutorId, audio, track, section, row, true);
        eParams[masterExecutorId].enable = true;
    }

    /**
     * <p>Let the main executor of the renderer read the corresponding audio data and set the playback pause position to the beginning of the specified track.
     * <p>This rendering has one and only one main executor. If this executor is rendering other audio,
     * it will be interrupted and directly jump to the beginning of the new audio. For other executors,
     * there is no impact.
     * </p>
     *
     * @param track Track number, starting from 0
     * @see #updateAudio(int, int, int, int)
     */
    public void ready(int track) {
        updateAudio(masterExecutorId, track, 0, 0);
    }

    /**
     * <p>Let the main executor of the renderer read the corresponding audio data and set the playback pause position to the specified position of the specified track.
     * <p>This rendering has one and only one main executor. If this executor is rendering other audio,
     * it will be interrupted and directly jump to the beginning of the new audio. For other executors,
     * there is no impact.
     * </p>
     *
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @see #updateAudio(int, int, int, int)
     */
    public void ready(
            int track,
            int section) {
        updateAudio(masterExecutorId, track, section, 0);
    }

    /**
     * <p>Let the main executor of the renderer read the corresponding audio data and set the playback pause position to the specified position of the specified track.
     * <p>This rendering has only one main executor. If this executor is rendering other audio,
     * it will be interrupted and directly jump to the beginning of the new audio. For other executors,
     * it will not be affected.
     * </p>
     *
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     * @see #updateAudio(int, int, int, int)
     */
    public void ready(
            int track,
            int section,
            int row) {
        updateAudio(masterExecutorId, track, section, row);
    }

    /**
     * When all renderers are stopped, the track rendering is complete.
     */
    @Override
    public boolean isFinished() {
        for (ExecutorParam ep : eParams) {
            if (ep == null || !ep.enable) {
                continue;
            }
            if (!ep.stop) {
                return false;
            }
        }
        return true;
    }

    /*
     * Rendering part
     */

    /**
     * <p>All non-stopped executors render one frame.
     * A stopped executor will enter the stopped state until reset, woken up, or deleted by the user.
     * </p>
     *
     * @return The number of samples rendered by this function (calculated as mono)
     */
    @Override
    protected int renderFrame() {
        int ret = countNextFrame();
        param.sampleInCurFrame = ret;
        rate.doConvert();
        mixer.readyBuffer();

        handleDelay();
        tickExeutors();
        triggerSounds();

        // Reading data from the mixer
        readMixer();

        return ret;
    }

    @Override
    protected int skipFrame() {
        int ret = countNextFrame();
        param.sampleInCurFrame = ret;
        rate.doConvert();

        tickExeutors();

        return ret;
    }

    /**
     * Let all non-stopped executors execute one frame and determine whether they are stopped.
     * If so, update the stop status
     */
    private void tickExeutors() {
        for (ExecutorParam ep : eParams) {
            if (ep == null || !ep.enable || ep.stop) {
                continue;
            }
            if (process.isWaiting(ep.id)) {
                // Protocol: Pending
                continue;
            }
            ep.executor.tick();
            if (ep.executor.isFinished()) {
                ep.stop = true;
            }

            // Location Updates
            if (ep.executor.isRowUpdated()) {
                process.updatePosition(ep.id, ep.executor.currentPosition());
            }
        }

        process.updateStates();

        // Update the started value. This value can only be determined after the first frame
        // has run completely. So put it here
        for (ExecutorParam ep : eParams) {
            if (process.isWaiting(ep.id)) {
                // Protocol: Pending
                continue;
            }
            ep.started = true;
        }
    }

    /**
     * <p>Handle delayed write. The latter track writes data 100 clocks later than the former track.
     * <p>Since the triggering time of each track is different, the resonance between tracks
     * can be effectively avoided. Therefore, the method of writing data in the tracks one by one
     * is needed.
     * </p>
     *
     * @see #triggerSounds()
     */
    private void handleDelay() {
        int delay = 0;
        int clock = param.freqPerFrame;
        for (ChannelParam cp : this.cParams) {
            if (cp == null) {
                continue;
            }

            ExecutorParam ep = eParams[cp.executorId];
            if (!ep.enable || ep.stop) {
                continue;
            }

            byte channelCode = cp.channelCode;
            AbstractNsfSound s = ep.executor.getSound(channelCode);
            if (ep.started) {
                s.process(cp.delay = delay);
            }

            delay += 100;
            if (delay >= clock) {
                delay = clock - 1;
            }
        }
    }

    /**
     * <p>Let the sounders work one by one.
     * <p>The number of working clocks is the number of clocks required to work for this frame,
     * minus the number of delayed clocks.
     * </p>
     *
     * @see #handleDelay()
     */
    private void triggerSounds() {
        int clock = param.freqPerFrame;
        for (ChannelParam cp : this.cParams) {
            if (cp == null) {
                continue;
            }

            ExecutorParam ep = eParams[cp.executorId];
            if (!ep.enable || ep.stop) {
                continue;
            }

            byte channelCode = cp.channelCode;
            AbstractNsfSound s = ep.executor.getSound(channelCode);

            if (ep.started) {
                s.process(clock - cp.delay);
            }
            s.endFrame();
            cp.delay = 0;
        }
    }

    /**
     * Reading audio data from Mixer
     */
    private void readMixer() {
        mixer.finishBuffer();
        mixer.readBuffer(data, 0, data.length);
    }

    /*
     * Assign Track
     */

    /**
     * This renderer needs to manage multiple {@link FamiTrackerExecutor},
     * which stores the corresponding information.
     *
     * @author Zdream
     */
    static class ExecutorParam {

        final int id;

        FamiTrackerExecutor executor;
        FtmAudio audio;

        /**
         * Has it stopped?
         */
        boolean stop;
        /**
         * Is it enabled?
         */
        boolean enable;
        /**
         * Whether to start.
         * Starts out as false. If the executor is waiting on the first frame of execution due to
         * waiting for the protocol, this value will remain false until it is released.
         * When this value is false, the audio for this executor will not be rendered.
         */
        boolean started;

        public ExecutorParam(int id) {
            this.id = id;
        }
    }

    /**
     * Information about each track
     *
     * @author Zdream
     */
    static class ChannelParam {

        /**
         * Track ID
         */
        final int id;
        /**
         * Which executor executes this track?
         */
        int executorId;
        /**
         * Track number.
         * In this renderer, there is at most one combination of the same executorId and channelCode
         */
        byte channelCode;
        /**
         * The track number corresponding to the mixer
         */
        int mixerId = -1;
        /**
         * Track delay. Unit: clock
         */
        int delay;

        public ChannelParam(int id) {
            this.id = id;
        }
    }

    /**
     * <p>Identification number of the master actuator.
     * <p>The renderer has at least one executor, which is the main executor. The default is the executor corresponding to number 0.
     * An executor set as the master executor is not allowed to be deleted unless the master executor changes.
     * </p>
     */
    int masterExecutorId;
    ExecutorParam[] eParams;
    ChannelParam[] cParams;

    void initExecutors() {
        masterExecutorId = 0;

        eParams = new ExecutorParam[1];
        eParams[0] = new ExecutorParam(0);
        eParams[0].executor = new FamiTrackerExecutor();

        cParams = new ChannelParam[0];
    }

    /**
     * <p>Allocate a new executor for a new audio.
     * The original track being rendered will not be terminated or paused, but will be executed simultaneously with the new executor.
     * <p>If the master actuator is not started, priority is given to the master actuator.
     * </p>
     *
     * @param audio Audio Data
     * @return
     */
    public int allocateAll(FtmAudio audio) {
        return allocateAll(audio, 0, 0, 0);
    }

    /**
     * <p>Allocate a new executor for a new audio file, specifying the track to start rendering.
     * The original track being rendered will not be terminated or paused, and will be executed simultaneously with the new executor.
     * <p>If the main executor is not started, it will be assigned priority to the main executor.
     * </p>
     *
     * @param audio Audio Data
     * @param track Track number, starting from 0
     * @return
     */
    public int allocateAll(FtmAudio audio, int track) {
        return allocateAll(audio, track, 0, 0);
    }

    /**
     * <p>Allocate a new executor for a new audio file, specifying the track to start rendering and the initial segment.
     * The original track being rendered will not be terminated or paused, and will be executed simultaneously with the new executor.
     * <p>If the main executor is not started, it will be assigned priority to the main executor.
     * </p>
     *
     * @param audio   Audio Data
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @return
     */
    public int allocateAll(FtmAudio audio, int track, int section) {
        return allocateAll(audio, track, section, 0);
    }

    /**
     * <p>Allocate a new executor for a new audio file, specifying the track to start rendering and the initial line.
     * The original track being rendered will not be terminated or paused, and will be executed simultaneously with the new executor.
     * <p>If the main executor is not started, it will be assigned priority to the main executor.
     * </p>
     *
     * @param audio   Audio Data
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     * @return
     */
    public int allocateAll(FtmAudio audio, int track, int section, int row) {
        // Main actuator
        ExecutorParam p = eParams[masterExecutorId];
        if (!p.enable) {
            try {
                process.addExecutor(masterExecutorId, new FtmPosition(section, row));
                updateAudio(masterExecutorId, audio, track, section, row, true);
            } catch (RuntimeException e) {
                process.removeExecutor(masterExecutorId);
                throw e;
            }
            p.enable = true;
            return masterExecutorId;
        }

        // New actuator
        p = createExecutorParam();
        p.audio = audio;
        try {
            process.addExecutor(p.id, p.executor.currentPosition());
            updateAudio(p.id, audio, track, section, row, true);
        } catch (RuntimeException e) {
            process.removeExecutor(masterExecutorId);
            throw e;
        }

        // Lock frequency
        p.executor.lockFrameRate(param.frameRate);
        // Reset the incoming sample counter

        return p.id;
    }

    /**
     * <p>Assign a new executor to a new audio and specify which tracks should be rendered. All other tracks will be set not to be rendered.
     * The original track being rendered will not be terminated or paused, and will be executed simultaneously with the new executor.
     * <p>If the main executor is not started, it will be assigned priority to the main executor.
     * </p>
     *
     * @param audio        Audio Data
     * @param channelCodes All NSF track numbers that need to be rendered
     * @return
     */
    public int allocate(FtmAudio audio, byte... channelCodes) {
        return allocate(audio, 0, 0, 0, channelCodes);
    }

    /**
     * <p>Allocate a new executor for a new audio file, specifying the track to start rendering.
     * Additionally, specify which tracks need to be rendered, all other tracks will be set not to be rendered.
     * The original track being rendered will not be terminated or paused, and will be executed simultaneously with the new executor.
     * <p>If the main executor is not started, it will be assigned priority to the main executor.
     * </p>
     *
     * @param audio        Audio Data
     * @param channelCodes All NSF track numbers that need to be rendered
     * @return
     */
    public int allocate(FtmAudio audio, int track, byte... channelCodes) {
        return allocate(audio, track, 0, 0, channelCodes);
    }

    /**
     * <p>Allocate a new executor for a new audio file, specifying the track to start rendering and the initial segment.
     * Additionally, specify which tracks need to be rendered, all other tracks will be set not to be rendered.
     * The original track being rendered will not be terminated or paused, and will be executed simultaneously with the new executor.
     * <p>If the main executor is not started, it will be assigned priority to the main executor.
     * </p>
     *
     * @param audio        Audio Data
     * @param channelCodes All NSF track numbers that need to be rendered
     * @return
     */
    public int allocate(FtmAudio audio, int track, int section, byte... channelCodes) {
        return allocate(audio, track, section, 0, channelCodes);
    }

    /**
     * <p>Allocate a new executor for a new audio file, specifying the track to start rendering and the initial line.
     * Additionally, specify which tracks need to be rendered, all other tracks will be set not to be rendered.
     * The original track being rendered will not be terminated or paused, and will be executed simultaneously with the new executor.
     * <p>If the main executor is not started, it will be assigned priority to the main executor.
     * </p>
     *
     * @param audio        Audio Data
     * @param channelCodes All NSF track numbers that need to be rendered
     * @return
     */
    public int allocate(
            FtmAudio audio,
            int track,
            int section,
            int row,
            byte... channelCodes) {
        // Main actuator
        ExecutorParam p = eParams[masterExecutorId];
        if (!p.enable) {
            process.addExecutor(masterExecutorId, p.executor.currentPosition());
            updateAudio(masterExecutorId, audio, track, section, row, channelCodes);
            p.enable = true;
            return masterExecutorId;
        }

        // New actuator
        p = createExecutorParam();
        p.audio = audio;
        process.addExecutor(p.id, p.executor.currentPosition());
        updateAudio(p.id, audio, track, section, row, channelCodes);

        // Need to lock frequency
        p.executor.lockFrameRate(param.frameRate);

        return p.id;
    }

    /**
     * <p>For an already running executor, specify which track needs to be rendered additionally.
     * If this track is already being rendered, returns false.
     * Otherwise create a new track and return true
     * </p>
     *
     * @param exeId       Actuator identification number
     * @param channelCode NSF track number
     * @return Whether to generate a new track. True means a new track is generated, false means the track already exists
     * @throws NsfPlayerException When there is no executor corresponding to exeId
     */
    public boolean allocate(int exeId, byte channelCode) {
        getExecutorParam(exeId);

        // Query section
        for (ChannelParam cp : this.cParams) {
            if (cp == null) {
                continue;
            }
            if (cp.executorId == exeId && cp.channelCode == channelCode) {
                return false;
            }
        }

        // Create one
        ChannelParam cp = createChannelParam(1)[0];
        cp.executorId = exeId;
        cp.channelCode = channelCode;
        mixerChannelConnect(cp.id);

        return true;
    }

    /**
     * <p>For an already running executor, specifies which track to recycle.
     * If this track is currently being rendered, remove it and return true.
     * Otherwise return false
     * </p>
     *
     * @param exeId       Actuator identification number
     * @param channelCode NSF track number
     * @return Whether to delete an existing track
     * @throws NsfPlayerException When there is no executor corresponding to exeId
     */
    public boolean free(int exeId, byte channelCode) {
        getExecutorParam(exeId);

        // Query section
        for (ChannelParam cp : this.cParams) {
            if (cp == null) {
                continue;
            }
            if (cp.executorId == exeId && cp.channelCode == channelCode) {
                mixerChannelDisconnect(cp.id);
                removeChannelParam(cp.id);
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Delete a working executor, and all tracks of this executor will be recycled and no longer rendered.
     * </p>
     *
     * @param exeId Actuator identification number
     * @throws NsfPlayerException When there is no executor corresponding to exeId
     */
    public void remove(int exeId) {
        getExecutorParam(exeId);

        // If other rules involve it, they need to be changed
        process.removeExecutor(exeId);

        // Deleting an executor
        removeExecutorParam(exeId);
    }

    /**
     * @return List of all executorId
     */
    public int[] getAllExeutorId() {
        int[] ret = new int[eParams.length];
        int nextIdx = 0;

        for (ExecutorParam ep : eParams) {
            if (ep != null) {
                ret[nextIdx++] = ep.id;
            }
        }

        if (nextIdx != ret.length) {
            ret = Arrays.copyOf(ret, nextIdx);
        }
        return ret;
    }

    /**
     * Get the track identification number corresponding to the track of the specified executor and track number
     *
     * @param exeId       Actuator identification number
     * @param channelCode NSF track number
     * @return Track ID. If not available, returns -1
     * @throws NsfPlayerException When there is no executor corresponding to exeId
     */
    public int getChannelId(int exeId, byte channelCode) {
        getExecutorParam(exeId);

        // Query section
        for (ChannelParam cp : this.cParams) {
            if (cp == null) {
                continue;
            }
            if (cp.executorId == exeId && cp.channelCode == channelCode) {
                return cp.id;
            }
        }

        return -1;
    }

    /**
     * Asks if the track with the specified executor and track number is already being rendered
     *
     * @param exeId       Actuator identification number
     * @param channelCode NSF track number
     * @return Specifies whether the track is currently being rendered. If it is, returns true
     * @throws NsfPlayerException When there is no executor corresponding to exeId
     */
    public boolean isAllocated(int exeId, byte channelCode) {
        return getChannelId(exeId, channelCode) != -1;
    }

    /**
     * <p>Update the playback audio position for the specified executor, starting from the specified track,
     * and does not modify the existing track configuration
     * <p>Ftm Audio Data needs to be specified when playing for the first time.
     * Therefore, the first time you need to call the overloaded method with the {@link FtmAudio} parameter
     * </p>
     *
     * @param exeId Actuator identification number
     * @param track Track number, starting from 0
     * @param track Line number, starting from 0
     * @throws NullPointerException When Audio Data has not been previously specified
     */
    public void updateAudio(int exeId, int track) {
        ExecutorParam ep = this.eParams[exeId];
        ep.executor.ready(track);

        // Process Management
        process.updatePosition(exeId, ep.executor.currentPosition());

        // started parameter
        ep.started = false;
    }

    /**
     * <p>Update the playback audio position for the specified executor,
     * start playing from the specified segment, and do not modify the existing track configuration
     * <p>Ftm Audio Data needs to be specified when playing for the first time.
     * Therefore, the first time you need to call the overloaded method with the {@link FtmAudio} parameter
     * </p>
     *
     * @param exeId   Actuator identification number
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @throws NullPointerException When Audio Data has not been previously specified
     */
    public void updateAudio(
            int exeId,
            int track,
            int section) {
        ExecutorParam ep = this.eParams[exeId];
        ep.executor.ready(track, section);

        // Process Management
        process.updatePosition(exeId, ep.executor.currentPosition());

        // started parameter
        ep.started = false;
    }

    /**
     * <p>Update the audio playback position for the specified executor,
     * start playing from the specified position, and do not modify the existing track configuration
     * <p>Ftm Audio Data needs to be specified when playing for the first time.
     * Therefore, the first time you need to call the overloaded method with the {@link FtmAudio} parameter
     * </p>
     *
     * @param exeId   Actuator identification number
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     * @throws NullPointerException When Audio Data has not been previously specified
     */
    public void updateAudio(
            int exeId,
            int track,
            int section,
            int row) {
        ExecutorParam ep = this.eParams[exeId];
        ep.executor.ready(track, section, row);

        // Process Management
        process.updatePosition(exeId, ep.executor.currentPosition());

        // started parameter
        ep.started = false;
    }

    /**
     * Updates the audio for the specified executor and starts playing from the specified position
     *
     * @param exeId           Actuator identification number
     * @param audio           the Audio Data, not null
     * @param track           Track number, starting from 0
     * @param section         Segment number, starting from 0
     * @param row             Line number, starting from 0
     * @param remapperChannel Do you need to reallocate the tracks? If you do, reallocate them according to the tracks required by audio.
     * @throws NullPointerException When <tt>audio == null</tt>
     */
    public void updateAudio(
            int exeId,
            FtmAudio audio,
            int track,
            int section,
            int row,
            boolean remapperChannel) {
        requireNonNull(audio, "audio = null");

        ExecutorParam ep = this.eParams[exeId];

        // All channelIds
        ChannelParam[] cps = channelsOfExecutor(exeId);
        // Disconnect them from the mixer
        for (ChannelParam cp : cps) {
            mixerChannelDisconnect(cp.id);
        }

        if (remapperChannel) {
            // Recycle them
            for (ChannelParam cp : cps) {
                removeChannelParam(cp.id);
            }

            ep.executor.ready(audio, track, section);
            ep.audio = audio;

            // Assign all audio tracks
            Set<Byte> channels = ep.executor.allChannelSet();
            cps = this.createChannelParam(channels.size());

            int index = 0;
            for (byte channelCode : channels) {
                ChannelParam cp = cps[index++];
                cp.executorId = exeId;
                cp.channelCode = channelCode;
            }
        } else {
            ep.executor.ready(audio);
            ep.audio = audio;
        }

        // Reestablish their connection to the mixer
        for (ChannelParam cp : cps) {
            mixerChannelConnect(cp.id);
        }

        // Frame rate
        if (exeId == masterExecutorId) {
            param.frameRate = ep.executor.getFrameRate();
            onFrameRateUpdated();
        }

        // Process Management
        process.updatePosition(exeId, ep.executor.currentPosition());

        // started parameter
        ep.started = false;
    }

    /**
     * Updates the audio for the specified executor and sets which tracks need to be rendered
     *
     * @param exeId        Actuator identification number
     * @param audio        Audio Data, Not null
     * @param track        Track number, starting from 0
     * @param section      Segment number, starting from 0
     * @param row          Line number, starting from 0
     * @param channelCodes NSF track number, indicating which tracks need to be re-rendered
     * @throws NullPointerException When <tt>audio == null</tt>
     */
    public void updateAudio(
            int exeId,
            FtmAudio audio,
            int track,
            int section,
            int row,
            byte... channelCodes) {
        requireNonNull(audio, "audio = null");

        ExecutorParam ep = this.eParams[exeId];

        // All channelIds
        ChannelParam[] cps = channelsOfExecutor(exeId);
        // Disconnect them from the mixer
        for (ChannelParam cp : cps) {
            mixerChannelDisconnect(cp.id);
        }

        // Recycle them
        for (ChannelParam cp : cps) {
            removeChannelParam(cp.id);
        }

        ep.executor.ready(audio, track, section);
        ep.audio = audio;

        // Assign all audio tracks
        Set<Byte> channels = new HashSet<>();
        for (byte channelCode : channelCodes) {
            channels.add(channelCode);
        }
        cps = this.createChannelParam(channels.size());

        int index = 0;
        for (byte channelCode : channels) {
            ChannelParam cp = cps[index++];
            cp.executorId = exeId;
            cp.channelCode = channelCode;
        }

        // Reestablish their connection to the mixer
        for (ChannelParam cp : cps) {
            mixerChannelConnect(cp.id);
        }

        // Frame rate
        if (exeId == masterExecutorId) {
            param.frameRate = ep.executor.getFrameRate();
            onFrameRateUpdated();
        }

        // Process Management
        process.updatePosition(exeId, ep.executor.currentPosition());

        // started parameter
        ep.started = false;
    }

    /**
     * Get executor parameters. If there is no corresponding executor, throw NsfPlayerException.
     * This method is also used to check whether the exeId is reasonable.
     */
    private ExecutorParam getExecutorParam(int exeId) {
        if (exeId >= eParams.length || exeId < 0 || eParams[exeId] == null) {
            throw new NsfPlayerException("There is no executor corresponding to " + exeId);
        }
        return eParams[exeId];
    }

    /**
     * Get track parameters. If there is no corresponding track, returns null.
     */
    private ChannelParam getChannelParam(int exeId, byte channelCode) {
        for (ChannelParam cp : this.cParams) {
            if (cp == null) {
                continue;
            }
            if (cp.executorId == exeId && cp.channelCode == channelCode) {
                return cp;
            }
        }
        return null;
    }

    /**
     * Returns all tracks for the specified executor.
     *
     * @param exeId Actuator identification number
     * @return
     */
    private ChannelParam[] channelsOfExecutor(int exeId) {
        ChannelParam cp;
        ArrayList<ChannelParam> list = new ArrayList<>();
        for (ChannelParam cParam : cParams) {
            cp = cParam;
            if (cp == null) {
                continue;
            }
            if (cp.executorId == exeId) {
                list.add(cp);
            }
        }
        return list.toArray(new ChannelParam[list.size()]);
    }

    /**
     * Create one new execution parameter class
     *
     * @return Execution parameter entity
     */
    private ExecutorParam createExecutorParam() {
        int i;
        for (i = 0; i < eParams.length; i++) {
            ExecutorParam ep = eParams[i];
            if (ep == null) {
                ep = eParams[i] = new ExecutorParam(i);
                ep.enable = true;
                ep.executor = new FamiTrackerExecutor();
                return ep;
            }
        }

        eParams = Arrays.copyOf(eParams, i + 1);
        ExecutorParam ep = eParams[i] = new ExecutorParam(i);
        ep.enable = true;
        ep.executor = new FamiTrackerExecutor();
        return ep;
    }

    /**
     * <p>Deletes an existing execution parameter class and all its associated tracks.
     * <p>If it is a master actuator, only the track is cleared and the actuator is set to [Unused] state,
     * but it is not deleted.
     * </p>
     *
     * @param exeId Actuator identification number
     */
    private void removeExecutorParam(int exeId) {
        // Clear Track
        for (ChannelParam cp : cParams) {
            if (cp.executorId == exeId) {
                mixerChannelDisconnect(cp.id);
                this.removeChannelParam(cp.id);
            }
        }

        // Processing execution parameter class
        if (exeId == masterExecutorId) {
            this.eParams[masterExecutorId].enable = false;
            this.eParams[masterExecutorId].audio = null;
        } else {
            this.eParams[exeId] = null;
            if (exeId + 1 == eParams.length) {
                // cParams array size needs to be changed
                int newLen = 0;
                for (int i = exeId - 1; i >= 0; i--) {
                    if (eParams[i] != null) {
                        newLen = i + 1;
                        break;
                    }
                }

                eParams = Arrays.copyOf(eParams, newLen);
            }
        }
    }

    /**
     * Assigning Track Parameters Class
     *
     * @param count The number, greater than 0.
     *              It is also the length of the returned ChannelParam array
     */
    private ChannelParam[] createChannelParam(int count) {
        ChannelParam[] cps = new ChannelParam[count];
        int[] idxs = new int[count];
        int nextIdx = 0;

        // Query section
        int len = cParams.length;
SERCHING:
        {
            for (int i = 0; i < len; i++) {
                if (cParams[i] == null) {
                    idxs[nextIdx++] = i;
                    if (nextIdx == count) {
                        break SERCHING;
                    }
                }
            }

            for (int i = len; ; i++) {
                idxs[nextIdx++] = i;
                if (nextIdx == count) {
                    break SERCHING;
                }
            }
        }

        // Resize the cParams array
        int maxLen = idxs[count - 1] + 1;
        if (maxLen > len) {
            cParams = Arrays.copyOf(cParams, maxLen);
        }

        // Create Section
        for (int i = 0; i < idxs.length; i++) {
            int id = idxs[i];
            cps[i] = cParams[id] = new ChannelParam(id);
        }

        return cps;
    }

    /**
     * Delete the mixer connection
     *
     * @param channelId
     */
    private void mixerChannelDisconnect(int channelId) {
        ChannelParam cp = this.cParams[channelId];

        // Recycling mixer tracks
        mixer.detach(cp.mixerId);
        cp.mixerId = -1;

        ExecutorParam ep = eParams[cp.executorId];
        ep.executor.getSound(cp.channelCode).setOut(EmptyMixerChannel.INSTANCE);
    }

    /**
     * Create a mixer connection
     *
     * @param channelId
     */
    private void mixerChannelConnect(int channelId) {
        ChannelParam cp = this.cParams[channelId];

        // Creating a Mixer Track
        int mixerId = mixer.allocateChannel(cp.channelCode);
        cp.mixerId = mixerId;

        ExecutorParam ep = eParams[cp.executorId];
        ep.executor.getSound(cp.channelCode).setOut(mixer.getMixerChannel(mixerId));
    }

    /**
     * ChannelParam that retrieves the track id
     *
     * @param channelId
     */
    private void removeChannelParam(int channelId) {
        this.cParams[channelId] = null;

        if (channelId + 1 == cParams.length) {
            // cParams array size needs to be changed
            int newLen = 0;
            for (int i = channelId - 1; i >= 0; i--) {
                if (cParams[i] != null) {
                    newLen = i + 1;
                    break;
                }
            }

            cParams = Arrays.copyOf(cParams, newLen);
        }
    }

    //
    // Reset
    //

    /**
     * <p>Called when the frame rate changes.
     * <p>The frame rate will change when the following events occur:
     * When the main renderer changes audio.
     * <p>The working content of this method is that all non-master actuators will be forced
     * to lock the frame rate, reset the frame rate and sampling related calculations
     * </p>
     */
    private void onFrameRateUpdated() {
        int frameRate = param.frameRate;
        for (ExecutorParam ep : this.eParams) {
            if (ep.id != masterExecutorId) {
                ep.executor.lockFrameRate(frameRate);
            }
        }

        resetCounterParam(frameRate, param.sampleRate);
        clearBuffer();
        rate.onParamUpdate(frameRate, BASE_FREQ_NTSC);
    }

    /*
     * Dashboard
     */

    /**
     * <p>Asks the specified executor whether any rows were updated in the current frame.
     * <p>Whether the execution position is switched manually or the actuator automatically switches
     * to the next line, it is considered that the line is updated.
     * </p>
     *
     * @param exeId Actuator identification number
     * @return true, if the specified executor updates the row in the current frame
     */
    public boolean isRowUpdated(int exeId) {
        ExecutorParam ep = getExecutorParam(exeId);
        return ep.executor.isRowUpdated();
    }

    /**
     * <p>For an already working executor, ask the executor for the track number it is currently rendering.
     * </p>
     *
     * @param exeId Actuator identification number
     * @return The track number corresponding to the executor rendering, starting from 0
     */
    public int getCurrentTrack(int exeId) {
        ExecutorParam ep = getExecutorParam(exeId);
        return ep.executor.getCurrentTrack();
    }

    /**
     * <p>For an already working executor, ask the executor for the segment number it is currently rendering.
     * </p>
     *
     * @param exeId Actuator identification number
     * @return The segment number corresponding to the executor rendering, starting from 0
     */
    public int getCurrentSection(int exeId) {
        ExecutorParam ep = getExecutorParam(exeId);
        return ep.executor.getCurrentSection();
    }

    /**
     * <p>For an already working executor, ask the executor for the line number it is currently rendering.
     * </p>
     *
     * @param exeId Actuator identification number
     * @return Corresponding to the Line number rendered by the executor, starting from 0
     */
    public int getCurrentRow(int exeId) {
        ExecutorParam ep = getExecutorParam(exeId);
        return ep.executor.getCurrentRow();
    }

    /**
     * Ask the specified executor whether the current line has been played and whether to jump to the next line (not whether the current frame has been played)
     *
     * @param exeId Actuator identification number
     * @return true, if the current line has finished playing
     */
    public boolean currentRowRunOut(int exeId) {
        ExecutorParam ep = getExecutorParam(exeId);
        return ep.executor.currentRowRunOut();
    }

    /**
     * <p>Get the segment number corresponding to the position to jump to if the specified executor jumps to the next line (not the next frame).
     * <p>If it is detected that a jump effect is being triggered, return the result after the trigger.
     * </p>
     *
     * @param exeId Actuator identification number
     * @return Specify the segment number position after the next jump of the executor
     */
    public int getNextSection(int exeId) {
        ExecutorParam ep = getExecutorParam(exeId);
        return ep.executor.getNextSection();
    }

    /**
     * <p>Get the line number corresponding to the position jumped to if the specified executor jumps to the next line (not the next frame).
     * <p>If it is detected that a jump effect is being triggered, return the result after the trigger.
     * </p>
     *
     * @param exeId Actuator identification number
     * @return Specifies the line number position after the next line jump of the executor
     */
    public int getNextRow(int exeId) {
        ExecutorParam ep = getExecutorParam(exeId);
        return ep.executor.getNextRow();
    }

    /**
     * Returns the set of all channel numbers for the specified executor. The channel number parameter is written in {@link INsfChannelCode}
     *
     * @param exeId Actuator identification number
     * @return A collection of all track numbers of the specified executor. If the ready(...) method has not been called, an empty collection is returned.
     */
    public Set<Byte> allChannelSet(int exeId) {
        ExecutorParam ep = getExecutorParam(exeId);
        return ep.executor.allChannelSet();
    }

    /**
     * Set the volume of a track
     *
     * @param exeId       Actuator identification number
     * @param channelCode Channel numberTrack number
     * @param level       Volume. range [0, 1]
     */
    public void setLevel(int exeId, byte channelCode, float level) {
        ChannelParam cp = getChannelParam(exeId, channelCode);
        if (cp == null) {
            return;
        }
        int mixerId = cp.mixerId;
        if (mixerId == -1) {
            return;
        }

        if (level < 0) {
            level = 0;
        } else if (level > 1) {
            level = 1;
        }
        mixer.setLevel(mixerId, level);
    }

    /**
     * Get the volume of a track
     *
     * @param exeId       Actuator identification number
     * @param channelCode Channel number
     * @return Volume. range [0, 1]
     * @throws NullPointerException When there is no track corresponding to <code>channelCode</code>
     */
    public float getLevel(int exeId, byte channelCode) {
        ChannelParam cp = getChannelParam(exeId, channelCode);
        if (cp == null) {
            throw new NullPointerException(
                    "There is no corresponding track for exeId: " + exeId + ", channelCode: " + channelCode + "");
        }
        int mixerId = cp.mixerId;
        if (mixerId == -1) {
            throw new NullPointerException(
                    "There is no corresponding track for exeId: " + exeId + ", channelCode: " + channelCode + "");
        }
        return mixer.getLevel(mixerId);
    }

    /**
     * Sets whether the track should emit sound
     *
     * @param exeId       Actuator identification number
     * @param channelCode Channel number
     * @param muted       false, makes the track sound; true, mutes it
     */
    public void setChannelMuted(int exeId, byte channelCode, boolean muted) {
        ExecutorParam ep = getExecutorParam(exeId);
        AbstractNsfSound sound = ep.executor.getSound(channelCode);
        if (sound != null) {
            sound.setMuted(muted);
        }
    }

    /**
     * Check if the track can produce sound
     *
     * @param exeId       Actuator identification number
     * @param channelCode Channel number
     * @return false, indicating that the track is not blocked; true, indicating that it has been blocked
     * @throws NullPointerException When there is no track corresponding to <code>channelCode</code>
     */
    public boolean isChannelMuted(int exeId, byte channelCode) throws NullPointerException {
        ExecutorParam ep = getExecutorParam(exeId);
        return ep.executor.getSound(channelCode).isMuted();
    }

    @Override
    public float getSpeed() {
        return param.speed;
    }

    @Override
    public void setSpeed(float speed) {
        if (speed > 10) {
            speed = 10;
        } else if (speed < 0.1f) {
            speed = 0.1f;
        }

        param.speed = speed;

        int frameRate = param.frameRate;
        resetCounterParam(frameRate, param.sampleRate);
        rate.onParamUpdate();
    }

    /*
     * Listeners
     */

    /**
     * <p>Add a listener to get the key for the specified executor.
     * <p>If the specified executor is deleted, the listener will be deleted as well.
     * </p>
     *
     * @param exeId Actuator identification number
     * @param l     Get the listener for the key
     * @throws NullPointerException When the listener <code>l == null</code>
     * @throws NsfPlayerException   When there is no executor corresponding to exeId
     */
    public void addFetchListener(int exeId, IFtmFetchListener l) {
        ExecutorParam ep = getExecutorParam(exeId);
        ep.executor.addFetchListener(l);
    }

    /**
     * Remove the listener for getting the key for the specified executor
     *
     * @param exeId Actuator identification number
     * @param l     Remove the key listener
     * @throws NsfPlayerException When there is no executor corresponding to exeId
     */
    public void removeFetchListener(int exeId, IFtmFetchListener l) {
        ExecutorParam ep = getExecutorParam(exeId);
        ep.executor.removeFetchListener(l);
    }

    /**
     * <p>Add an execution end listener for the specified executor.
     * This listener will be woken up when the effect finishes executing but before writing the sound.
     * <p>If the specified executor is deleted, the listener will be deleted as well.
     * </p>
     *
     * @param exeId Actuator identification number
     * @param l     Execution end listener
     * @throws NullPointerException When the listener <code>l == null</code>
     * @throws NsfPlayerException   When there is no executor corresponding to exeId
     */
    public void addExecuteFinishedListener(int exeId, IFtmExecutedListener l) {
        ExecutorParam ep = getExecutorParam(exeId);
        ep.executor.addExecuteFinishedListener(l);
    }

    /**
     * Removes the execution end listener for the specified executor.
     *
     * @param exeId Actuator identification number
     * @param l     Execution end listener
     * @throws NsfPlayerException When there is no executor corresponding to exeId
     */
    public void removeExecuteFinishedListener(int exeId, IFtmExecutedListener l) {
        ExecutorParam ep = getExecutorParam(exeId);
        ep.executor.removeExecuteFinishedListener(l);
    }

    /*
     * Waiting for agreement
     */

    /**
     * Add Wait Protocol
     *
     * @param a Protocol Data
     */
    public void addWaitingAgreement(WaitingAgreement a) {
        process.addWaitingAgreement(a);
    }

    /**
     * Delete waiting protocol
     *
     * @param a Protocol Data
     */
    public void removeWaitingAgreement(WaitingAgreement a) {
        process.removeWaitingAgreement(a);
    }
}
