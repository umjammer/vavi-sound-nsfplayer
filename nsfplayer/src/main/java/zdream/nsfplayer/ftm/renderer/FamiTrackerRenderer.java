package zdream.nsfplayer.ftm.renderer;

import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfPlayerApplication;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.core.NsfRateConverter;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.FamiTrackerExecutor;
import zdream.nsfplayer.ftm.executor.FamiTrackerParameter;
import zdream.nsfplayer.ftm.executor.hook.IFtmExecutedListener;
import zdream.nsfplayer.ftm.executor.hook.IFtmFetchListener;
import zdream.nsfplayer.ftm.process.base.FtmPosition;
import zdream.nsfplayer.mixer.IMixerChannel;
import zdream.nsfplayer.mixer.IMixerConfig;
import zdream.nsfplayer.mixer.IMixerHandler;
import zdream.nsfplayer.mixer.ISoundMixer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.nsfplayer.sound.AbstractNsfSound;

import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_NTSC;


/**
 * <p>Default FamiTracker audio renderer.
 * <p>SoundGen class from the original C++ project.
 * <p>This renderer is not thread-safe, please be careful not to set parameters during rendering.
 * </p>
 *
 * @author Zdream
 * @version <b>v0.3.0</b>
 * <br>Move the original execution-related components to {@link FamiTrackerExecutor}.
 * @since v0.2.1
 */
public class FamiTrackerRenderer extends AbstractNsfRenderer<FtmAudio> {

    /**
     * Actuator
     */
    private final FamiTrackerExecutor executor = new FamiTrackerExecutor();

    /**
     * Rate Converter
     */
    private final NsfRateConverter rate;

    /**
     * Audio Mixer
     */
    private ISoundMixer mixer;

    private final FamiTrackerConfig config;

    private final NsfCommonParameter param = new NsfCommonParameter();

    /**
     * Generate an audio renderer using the default configuration
     */
    public FamiTrackerRenderer() {
        this(null);
    }

    public FamiTrackerRenderer(FamiTrackerConfig config) {
        if (config == null) {
            this.config = new FamiTrackerConfig();
        } else {
            this.config = config.clone();
        }

        // Sampling rate data is only needed for rendering builds
        param.sampleRate = this.config.sampleRate;

        // The volume parameter is only needed for rendering builds
        param.levels.copyFrom(this.config.channelLevels);

        rate = new NsfRateConverter(param);
        initMixer();
    }

    private void initMixer() {
        IMixerConfig mixerConfig = config.mixerConfig;
        if (mixerConfig == null) {
            mixerConfig = new XgmMixerConfig();
        }

        this.mixer = NsfPlayerApplication.app.mixerFactory.create(mixerConfig, param);
    }

    /*
     * Preparation
     */

    /**
     * <p>Let the renderer read the corresponding audio data.
     * <p>Set the playback pause position to the first segment (segment 0) of the first track (track 0)
     * </p>
     *
     * @param audio
     */
    @Override
    public void ready(FtmAudio audio) throws NsfPlayerException {
        ready(audio, 0, 0, 0);
    }

    /**
     * <p>Let the renderer read the corresponding audio data.
     * <p>Set the playback pause position to the first segment of the specified track (segment 0)
     * </p>
     *
     * @param audio
     * @param track number, starting from 0
     */
    @Override
    public void ready(FtmAudio audio, int track) throws NsfPlayerException {
        ready(audio, track, 0, 0);
    }

    /**
     * <p>Let the renderer read the corresponding audio data.
     * <p>Set the playback pause position to the specified segment of the specified track
     * </p>
     *
     * @param audio
     * @param track   Track number, starting from 0
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
     * <p>Let the renderer read the corresponding audio data.
     * <p>Set the playback pause position to the specified line of the specified track
     * </p>
     *
     * @param audio
     * @param track   Track number, starting from 0
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
        executor.ready(audio, track, section, row);

        // Reset playback related data
        int frameRate = executor.getFrameRate();
        resetCounterParam(frameRate, param.sampleRate);
        clearBuffer();
        rate.onParamUpdate(frameRate, BASE_FREQ_NTSC);

        reloadMixer();
        connectChannels();
    }

    /**
     * <p>Reset the current track without changing the Ftm audio,
     * so that the playback position is reset to the beginning of the track
     * <p>The Ftm audio data needs to be specified when playing for the first time.
     * Therefore, the first time you need to call the overloaded method with the {@link FtmAudio} parameter
     * </p>
     *
     * @throws NullPointerException When {@link FtmAudio} audio is not specified before calling this method
     */
    public void ready() throws NsfPlayerException {
        executor.ready();
        resetMixer();
    }

    /**
     * <p>Switch to the beginning of the specified track without changing the Ftm file.
     * <p>The Ftm audio data needs to be specified when playing for the first time.
     * Therefore, the first time you need to call the overloaded method with the {@link FtmAudio} parameter
     * </p>
     *
     * @param track Track number, starting from 0
     * @throws NullPointerException When {@link FtmAudio} audio is not specified before calling this method
     */
    @Override
    public void ready(int track) throws NsfPlayerException {
        ready(track, 0, 0);
    }

    /**
     * <p>Switch tracks and segment numbers without changing the Ftm file
     * <p>The Ftm audio data needs to be specified when playing for the first time.
     * Therefore, the first time you need to call the overloaded method with the {@link FtmAudio} parameter
     * </p>
     *
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @throws NullPointerException When {@link FtmAudio} audio is not specified before calling this method
     */
    public void ready(int track, int section) throws NsfPlayerException {
        ready(track, section, 0);
    }

    /**
     * <p>Switch tracks and segment numbers without changing the Ftm file, line number
     * <p>The Ftm audio data needs to be specified when playing for the first time.
     * Therefore, the first time you need to call the overloaded method with the {@link FtmAudio} parameter
     * </p>
     *
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     * @throws NullPointerException When {@link FtmAudio} audio is not specified before calling this method
     * @since v0.3.1
     */
    public void ready(int track, int section, int row) throws NsfPlayerException {
        executor.ready(track, section, row);
        resetMixer();
    }

    /**
     * <p>Switch the segment number and line number without changing the Ftm file or track number
     * <p>The Ftm audio data needs to be specified when playing for the first time.
     *
     * @param pos the playback position, not null
     * @throws NullPointerException When {@link FtmAudio} audio is not specified before calling this method,or pos == null
     * @since v0.3.1
     */
    public void ready(FtmPosition pos) {
        executor.ready(pos);
        resetMixer();
    }

    /**
     * <p>Switch to the specified playback position without changing the parameters of each track.
     * When switching, the playback pitch, volume, effects, etc. of each track will not change,
     * including the delay effect Gxx.
     * The mixer is not reset, which means that the sound played in the previous frame may continue
     * to play for an extended period of time.
     * The playback speed of the FTM document (not the playback speed) will be reset according
     * to the tempo and other values.
     * <p>Please use this method with caution. If you used tremolo 4xy or other effects before and
     * did not eliminate them, these effects will still remain after switching positions,
     * causing strange playback later.
     * If you want to use a more robust way to switch the playback position without
     * causing major changes in the playback effect,
     * please use the {@link #ready(int, int)} or {@link #skip(int)} method.
     * <p>You need to make sure that the renderer has successfully loaded the {@link FtmAudio} audio before calling it.
     * </p>
     *
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @throws NullPointerException When the {@link FtmAudio} audio is not loaded successfully before calling this method
     * @see #ready(int, int)
     * @see #skip(int)
     * @since v0.2.9
     */
    public void switchTo(int track, int section) {
        executor.switchTo(track, section);
    }

    /**
     * <p>Switch to the specified playback position without changing the parameters of each track.
     * When switching, the playback pitch, volume, effects, etc. of each track will not change,
     * including the delay effect Gxx.
     * The mixer is not reset, which means that the sound played in the previous frame may continue
     * to play for an extended period of time.
     * The playback speed of the FTM document (not the playback speed) will be reset according
     * to the tempo and other values.
     * <p>Please use this method with caution. If you used tremolo 4xy or other effects before
     * and did not eliminate them, these effects will still remain after switching positions,
     * causing strange playback later.
     * If you want to use a more robust way to switch the playback position without
     * causing major changes in the playback effect,
     * please use the {@link #ready(int, int)} or {@link #skip(int)} method.
     * <p>You need to make sure that the renderer has successfully loaded the {@link FtmAudio} audio before calling it.
     * </p>
     *
     * @param track   Track number, starting from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     * @throws NullPointerException When the {@link FtmAudio} audio is not loaded successfully before calling this method
     * @see #ready(int, int)
     * @see #skip(int)
     * @since v0.3.1
     */
    public void switchTo(int track, int section, int row) {
        executor.switchTo(track, section, row);
    }

    /*
     * Rendering part
     */

    /**
     * <p>Render a frame.
     * <p>This method has a new interpretation in version v0.3.0, namely: the execution component executes one frame,
     * and the rendering component also executes one frame.
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
        executor.tick();
        triggerSounds();

        // Reading data from the mixer
        readMixer();

        return ret;
    }

    /**
     * <p>Skip a frame.
     * <p>This method has a new interpretation in version v0.3.0, namely: the execution component executes one frame,
     * and the rendering component does not execute.
     * </p>
     *
     * @return Number of samples skipped by this function (based on mono)
     */
    @Override
    protected int skipFrame() {
        int ret = countNextFrame();
        param.sampleInCurFrame = ret;
        rate.doConvert();

        executor.tick();

        return ret;
    }

    /**
     * <p>Ask if playback has finished
     * <p>If the Ftm audio that has already finished playing tries to call {@link #render(byte[], int, int)}
     * or {@link #renderOneFrame(byte[], int, int)} again, the stop symbol will be ignored,
     * and the audio will be forced to play again.
     * </p>
     *
     * @return
     */
    @Override
    public boolean isFinished() {
        return executor.isFinished();
    }

    /*
     * Instrument panel area
     */

    /*
     * The part used to control the actual playback data.
     * Among them are: control volume, control whether to play
     */

    /**
     * <p>Asks whether the row was updated in the current frame.
     * <p>Whether the execution position is switched manually or the actuator automatically switches to the next line,
     * it is considered that the line is updated.
     * </p>
     *
     * @return true, if the current frame updates the row
     * @since v0.3.1
     */
    public boolean isRowUpdated() {
        return executor.isRowUpdated();
    }

    /**
     * @return Get the number of the track currently playing
     */
    @Override
    public int getCurrentTrack() {
        return executor.getCurrentTrack();
    }

    /**
     * @return Get the segment number being played
     */
    public int getCurrentSection() {
        return executor.getCurrentSection();
    }

    /**
     * @return Get the line number currently playing
     */
    public int getCurrentRow() {
        return executor.getCurrentRow();
    }

    /**
     * @return Get the location of execution
     * @since v0.3.1
     */
    public FtmPosition currentPosition() {
        return executor.currentPosition();
    }

    /**
     * Ask if the current line has finished playing, and you need to jump to the next line
     * (not asking if the current frame has finished playing)
     *
     * @return true, if the current line has finished playing
     * @since v0.2.2
     */
    public boolean currentRowRunOut() {
        return executor.currentRowRunOut();
    }

    /**
     * <p>Get the segment number corresponding to the position jumped to if jumping to the next line (not the next frame).
     * <p>If it is detected that a jump effect is being triggered, return the result after the trigger.
     * </p>
     *
     * @return The segment number corresponding to the next line
     * @since v0.2.9
     */
    public int getNextSection() {
        return executor.getNextSection();
    }

    /**
     * <p>Get the position to jump to if jumping to the next line (not the next frame).
     * <p>If it is detected that a jump effect is being triggered, return the result after the trigger.
     * </p>
     *
     * @return Get the position of the next line to be executed
     * @since v0.3.1
     */
    public FtmPosition nextPosition() {
        return executor.nextPosition();
    }

    /**
     * <p>Get the line number corresponding to the position jumped to if jumping to the next line (not the next frame).
     * <p>If it is detected that a jump effect is being triggered, return the result after the trigger.
     * </p>
     *
     * @return The segment number corresponding to the next line
     * @since v0.2.9
     */
    public int getNextRow() {
        return executor.getNextRow();
    }

    /**
     * Returns a collection of all track numbers. The track number parameters are written in {@link INsfChannelCode}
     *
     * @return A collection of all track numbers. If the ready(...) method is not called, an empty collection is returned.
     * @since v0.2.2
     */
    @Override
    public Set<Byte> allChannelSet() {
        return executor.allChannelSet();
    }

    /**
     * Set the volume of a channel
     *
     * @param channelCode channel number
     * @param level       Volume. range [0, 1]
     * @since v0.2.2
     */
    @Override
    public void setLevel(byte channelCode, float level) {
        if (level < 0) {
            level = 0;
        } else if (level > 1) {
            level = 1;
        }

        int id = findMixerChannelByCode(channelCode);
        if (id != -1) {
            mixer.setLevel(id, level);
        }
    }

    /**
     * Get the volume of a channel
     *
     * @param channelCode channel number
     * @return volume. range [0, 1]
     * @throws NullPointerException When there is no track corresponding to <code>channelCode</code>
     * @since v0.2.3
     */
    @Override
    public float getLevel(byte channelCode) throws NullPointerException {
        int id = findMixerChannelByCode(channelCode);
        if (id != -1) {
            return mixer.getLevel(id);
        }
        throw new NullPointerException("Does not exist " + channelCode + " corresponding track");
    }

    /**
     * Sets whether the track should emit sound
     *
     * @param channelCode channel number
     * @param muted       false, makes the track sound; true, mutes it
     * @since v0.2.2
     */
    @Override
    public void setChannelMuted(byte channelCode, boolean muted) {
        AbstractNsfSound sound = executor.getSound(channelCode);
        if (sound != null) {
            sound.setMuted(muted);
        }
    }

    /**
     * Check if the track can produce sound
     *
     * @param channelCode channel number
     * @return false, Indicates that the track is not blocked; true, it has been blocked
     * @throws NullPointerException When there is no track corresponding to <code>channelCode</code>
     * @since v0.2.3
     */
    @Override
    public boolean isChannelMuted(byte channelCode) throws NullPointerException {
        return executor.getSound(channelCode).isMuted();
    }

    @Override
    public void setSpeed(float speed) {
        if (speed > 10) {
            speed = 10;
        } else if (speed < 0.1f) {
            speed = 0.1f;
        }

        param.speed = speed;

        int frameRate = executor.getFrameRate();
        resetCounterParam(frameRate, param.sampleRate);
        rate.onParamUpdate();
    }

    @Override
    public float getSpeed() {
        return param.speed;
    }

    /**
     * Get the operator (tool class) of the mixer.
     * It can be used to perform simple operations on the mixer used.
     *
     * @return Mixer operator
     * @since v0.2.10
     */
    public IMixerHandler getMixerHandler() {
        return mixer.getHandler();
    }

    /*
     * Initialization
     */

    /**
     * Initialize/reset the audio synthesizer (mixer)
     */
    private void reloadMixer() {
        mixer.detachAll();
        mixer.reset();
    }

    /**
     * <p>Take out the sound generator from the executive component and connect it to the mixer.
     * </p>
     */
    private void connectChannels() {
        Set<Byte> channels = executor.allChannelSet();
        this.channels = new ChannelParam[channels.size()];

        int index = 0;
        int mixerChannel = -1;
        for (byte channelCode : channels) {
            AbstractNsfSound sound = executor.getSound(channelCode);
            if (sound != null) {
                mixerChannel = mixer.allocateChannel(channelCode);
                IMixerChannel mix = mixer.getMixerChannel(mixerChannel);
                sound.setOut(mix);

                // volume
                mix.setLevel(getInitLevel(channelCode));

                // TODO Tells the mixer more information, including the output sample rate of the sound generator
                //  (1.77 million for NSF, 44100 or 48000 for mpeg, etc.)
            }

            // channel param
            ChannelParam p = new ChannelParam();
            this.channels[index] = p;
            p.channelCode = channelCode;
            p.delay = index * 100;
            p.mixerChannel = mixerChannel;

            index++;
        }
    }

    /**
     * Get the original volume of a track. This value is taken from {@link FamiTrackerParameter}
     *
     * @param channelCode channel number
     * @return range [0, 1]
     * @since v0.2.4
     */
    private float getInitLevel(byte channelCode) {
        float level = switch (channelCode) {
            case CHANNEL_2A03_PULSE1 -> config.channelLevels.level2A03Pules1;
            case CHANNEL_2A03_PULSE2 -> config.channelLevels.level2A03Pules2;
            case CHANNEL_2A03_TRIANGLE -> config.channelLevels.level2A03Triangle;
            case CHANNEL_2A03_NOISE -> config.channelLevels.level2A03Noise;
            case CHANNEL_2A03_DPCM -> config.channelLevels.level2A03DPCM;
            case CHANNEL_VRC6_PULSE1 -> config.channelLevels.levelVRC6Pules1;
            case CHANNEL_VRC6_PULSE2 -> config.channelLevels.levelVRC6Pules2;
            case CHANNEL_VRC6_SAWTOOTH -> config.channelLevels.levelVRC6Sawtooth;
            case CHANNEL_MMC5_PULSE1 -> config.channelLevels.levelMMC5Pules1;
            case CHANNEL_MMC5_PULSE2 -> config.channelLevels.levelMMC5Pules2;
            case CHANNEL_FDS -> config.channelLevels.levelFDS;
            case CHANNEL_N163_1 -> config.channelLevels.levelN163Namco1;
            case CHANNEL_N163_2 -> config.channelLevels.levelN163Namco2;
            case CHANNEL_N163_3 -> config.channelLevels.levelN163Namco3;
            case CHANNEL_N163_4 -> config.channelLevels.levelN163Namco4;
            case CHANNEL_N163_5 -> config.channelLevels.levelN163Namco5;
            case CHANNEL_N163_6 -> config.channelLevels.levelN163Namco6;
            case CHANNEL_N163_7 -> config.channelLevels.levelN163Namco7;
            case CHANNEL_N163_8 -> config.channelLevels.levelN163Namco8;
            case CHANNEL_VRC7_FM1 -> config.channelLevels.levelVRC7FM1;
            case CHANNEL_VRC7_FM2 -> config.channelLevels.levelVRC7FM2;
            case CHANNEL_VRC7_FM3 -> config.channelLevels.levelVRC7FM3;
            case CHANNEL_VRC7_FM4 -> config.channelLevels.levelVRC7FM4;
            case CHANNEL_VRC7_FM5 -> config.channelLevels.levelVRC7FM5;
            case CHANNEL_VRC7_FM6 -> config.channelLevels.levelVRC7FM6;
            case CHANNEL_S5B_SQUARE1 -> config.channelLevels.levelS5BSquare1;
            case CHANNEL_S5B_SQUARE2 -> config.channelLevels.levelS5BSquare2;
            case CHANNEL_S5B_SQUARE3 -> config.channelLevels.levelS5BSquare3;
            default -> 1.0f;
        };

        if (level > 1) {
            level = 1.0f;
        } else if (level < 0) {
            level = 0;
        }

        return level;
    }

    /**
     * <p>Handle delayed write. The latter track writes data 100 clocks later than the former track.
     * <p>Since the triggering time of each track is different, the resonance between tracks can be
     * effectively avoided. Therefore, the method of writing data in the tracks one by one is needed.
     * <p>In versions v0.2.9 and v0.2.10, lazy writing is done by AbstractFtmChannel.
     * Now due to the separation of execution components, the task of lazy writing is now borne by the renderer.
     * </p>
     *
     * @see #triggerSounds()
     * @since v0.3.0
     */
    private void handleDelay() {
        for (ChannelParam p : channels) {
            byte channelCode = p.channelCode;
            AbstractNsfSound s = executor.getSound(channelCode);
            s.process(p.delay);
        }
    }

    /**
     * <p>Let the sounders work one by one.
     * <p>The number of working clocks is the number of clocks required to work for this frame, minus the number of delayed clocks.
     * </p>
     *
     * @see #handleDelay()
     * @since v0.3.0
     */
    private void triggerSounds() {
        int clock = param.freqPerFrame;
        for (ChannelParam p : channels) {
            byte channelCode = p.channelCode;
            AbstractNsfSound s = executor.getSound(channelCode);
            s.process(clock - p.delay);
            s.endFrame();
        }
    }

    /**
     * Reset Mixer
     */
    private void resetMixer() {
        mixer.reset();
    }

    /**
     * Reading audio data from Mixer
     */
    private void readMixer() {
        mixer.finishBuffer();
        mixer.readBuffer(data, 0, data.length);
    }

    static class ChannelParam {

        /**
         * channel number
         */
        byte channelCode;
        /**
         * Delay write clock count
         */
        int delay;
        /**
         * Mixer track ID
         */
        int mixerChannel;
    }

    private ChannelParam[] channels;

    /**
     * According to the channel number, find the track identification number in Mixer
     *
     * @param channelCode NSF defined channel number
     * @return Mixer track ID
     * @since v0.3.0
     */
    private int findMixerChannelByCode(byte channelCode) {
        for (ChannelParam p : channels) {
            if (p == null) {
                continue;
            }
            if (p.channelCode == channelCode) {
                return p.mixerChannel;
            }
        }
        return -1;
    }

    /*
     * Listeners
     */

    /**
     * Add a listener to get the key
     *
     * @param l Get the listener for the key
     * @throws NullPointerException When the listener <code>l == null</code>
     * @since v0.3.0
     */
    public void addFetchListener(IFtmFetchListener l) {
        executor.addFetchListener(l);
    }

    /**
     * Remove the listener for getting the key
     *
     * @param l Remove the key listener
     * @since v0.3.0
     */
    public void removeFetchListener(IFtmFetchListener l) {
        executor.removeFetchListener(l);
    }

    /**
     * Clear all listeners for getting key sounds
     *
     * @since v0.3.0
     */
    public void clearFetchListener() {
        executor.clearFetchListener();
    }

    /**
     * Add a listener for execution completion.
     * This listener will be woken up when the effect finishes executing but before writing the sound.
     *
     * @param l Execution end listener
     * @throws NullPointerException When the listener <code>l == null</code>
     * @since v0.3.0
     */
    public void addExecuteFinishedListener(IFtmExecutedListener l) {
        executor.addExecuteFinishedListener(l);
    }

    /**
     * Remove the execution end listener
     *
     * @param l Execution end listener
     * @since v0.3.0
     */
    public void removeExecuteFinishedListener(IFtmExecutedListener l) {
        executor.removeExecuteFinishedListener(l);
    }

    /**
     * Clear all listeners that have completed execution
     *
     * @since v0.3.0
     */
    public void clearExecuteFinishedListener() {
        executor.clearExecuteFinishedListener();
    }
}
