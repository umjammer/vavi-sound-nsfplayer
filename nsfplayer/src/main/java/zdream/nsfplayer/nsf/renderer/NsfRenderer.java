package zdream.nsfplayer.nsf.renderer;

import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfAudio;
import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.core.CycleCounter;
import zdream.nsfplayer.core.FloatCycleCounter;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfPlayerApplication;
import zdream.nsfplayer.core.NsfRateConverter;
import zdream.nsfplayer.core.NsfStatic;
import zdream.nsfplayer.mixer.IMixerChannel;
import zdream.nsfplayer.mixer.IMixerConfig;
import zdream.nsfplayer.mixer.IMixerHandler;
import zdream.nsfplayer.mixer.ISoundMixer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.chip.NesN163;
import zdream.nsfplayer.nsf.executor.IN163ReattachListener;
import zdream.nsfplayer.nsf.executor.NsfExecutor;
import zdream.nsfplayer.sound.AbstractNsfSound;


/**
 * <p>The array needs to be expanded
 * <p>This class was basically unusable before v0.2.3, until v0.2.4,
 * when it underwent a major overhaul.
 * <p>This renderer is not thread-safe, please be careful not to set parameters during rendering.
 * </p>
 *
 * @author Zdream
 * @since v0.1
 */
public class NsfRenderer<T> extends AbstractNsfRenderer<AbstractNsfAudio<T>> {

    private final NsfExecutor<T> executor = new NsfExecutor<>();

    /**
     * Calculate how many clocks per frame, counting the speed impact.
     */
    public final NsfRateConverter rate;

    /**
     * Calculate how many samples per frame, counting the speed impact.
     */
    private final FloatCycleCounter apuCounter = new FloatCycleCounter();

    private final NsfCommonParameter param = new NsfCommonParameter();

    /**
     * Manage the tick count of the executor
     * Calculate the number of samples per frame, regardless of playback speed.
     */
    private final CycleCounter exeCycle = new CycleCounter();

    /**
     * audio mixer
     */
    public ISoundMixer mixer;

    /**
     * <p>N163 Number of tracks .
     * <p>If not determined, the value is -1.
     * </p>
     *
     * @since v0.3.2
     */
    private int n163ChannelCount = -1;

    /**
     * <p>Whether track initialization is in progress.
     * <p>This value is only used when initializing N163 orbits. The number of N163 orbitals is
     * determined at reset() or runtime, if it is determined at reset() it will operate directly
     * on uninitialized orbitals and an exception will be thrown.
     * This value is needed to determine the state of the renderer.
     * </p>
     *
     * @since v0.3.2
     */
    private boolean channelInit;

    public NsfRenderer() {
        this(new NsfRendererConfig());
    }

    public NsfRenderer(NsfRendererConfig config) {
        param.sampleRate = config.sampleRate;
        param.frameRate = frameRate;
        param.levels.copyFrom(config.channelLevels);

        executor.setRegion(config.region);
        executor.setRate(config.sampleRate);
        executor.addN163ReattachListener(n163lsner);

        initMixer(config);
        rate = new NsfRateConverter(param);
        exeCycle.setParam(config.sampleRate, this.frameRate);
    }

    public void initMixer(NsfRendererConfig config) {
        IMixerConfig mixerConfig = config.mixerConfig;
        if (mixerConfig == null) {
            mixerConfig = new XgmMixerConfig();
        }

        this.mixer = NsfPlayerApplication.app.mixerFactory.create(mixerConfig, param);
    }

    //
    // preliminary
    //

    /**
     * It doesn't really matter if you choose 50 or 60,
     * because the Nsf player calculates in steps of one sample, not frames.
     */
    private final int frameRate = NsfStatic.FRAME_RATE_NTSC;

    /**
     * Reads Nsf audio and prepares it with default tracks
     *
     * @param audio
     * @throws NullPointerException When audio is null
     */
    @Override
    public void ready(AbstractNsfAudio<T> audio) {
        ready0(audio, audio.getStart());
    }

    /**
     * Reads Nsf audio, prepares it with a specified track
     *
     * @param audio Nsf Audio Examples
     * @param track track number (of a song)
     * @throws NullPointerException     When audio is null
     * @throws IllegalArgumentException When the track number track is outside the range [0, audio.total_songs).
     */
    @Override
    public void ready(AbstractNsfAudio<T> audio, int track) {
        this.ready0(audio, track);
    }

    /**
     * <p>Switch to the beginning of the specified track without changing the Nsf audio.
     * <p>Nsf audio data needs to be specified for the first playback.
     * So the first time you need to call the overloaded method with the {@link NsfAudio} parameter
     * </p>
     *
     * @param track Track number, from 0
     * @throws NullPointerException     When {@link NsfAudio} audio is not specified before calling this method
     * @throws IllegalArgumentException When the track number track is outside the range [0, audio.total_songs).
     */
    @Override
    public void ready(int track) throws NullPointerException {
        executor.ready(track);
    }

    private void ready0(AbstractNsfAudio<T> audio, int track) {
        n163ChannelCount = -1;
        channelInit = true;
        executor.ready(audio, track);

        super.resetCounterParam(frameRate, param.sampleRate);
        rate.onParamUpdate(frameRate, executor.cycleRate());
        apuCounter.setParam(countCycle(param.speed), param.sampleRate);

        mixer.reset();
        connectChannels(audio.useN163());
        clearBuffer();

        channelInit = false;
    }

    /**
     * <p>Connects the sound in the execution component to the track of the rendering component.
     * <p>This method temporarily determines all track numbers
     * </p>
     *
     * @param useN163 Does the audio use the N163 chip
     */
    private void connectChannels(boolean useN163) {
        mixer.detachAll();
        Set<Byte> channels = executor.allChannelSet();

        // Calculate the total number of orbits.
        // When N163 orbitals are used, but the number of orbitals is uncertain,
        // the total number + 8 is added in order to make up for the N163 orbitals.
        if (useN163 && n163ChannelCount == -1) {
            this.channels = new ChannelParam[channels.size() + 8];
        } else {
            this.channels = new ChannelParam[channels.size()];
        }

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

                // TODO Tells the mixer more information, including the output sample rate of the microphone
                //  (1.77 million for NSF, 44100 or 48000 for mpeg, etc.).
            }

            // Cache track number
            ChannelParam p = new ChannelParam();
            p.channelCode = channelCode;
            p.mixerChannel = mixerChannel;
            this.channels[index] = p;
            index++;
        }
    }

    //
    // rendering section
    //

    @Override
    protected int renderFrame() {
        int ret = countNextFrame();
        param.sampleInCurFrame = ret;
        rate.doConvert();
        mixerReady();

        int exeCount = exeCycle.tick();
        for (int i = 0; i < exeCount; i++) {
            executor.tick();
            processSounds(apuCounter.tick());
        }
        endFrame();

        // Reading data from the mixer
        readMixer();

        return ret;
    }

    @Override
    protected int skipFrame() {
        int ret = countNextFrame();
        param.sampleInCurFrame = ret;
        rate.doConvert();

        int exeCount = exeCycle.tick();
        for (int i = 0; i < exeCount; i++) {
            executor.tick();
        }
        endFrame();

        return ret;
    }

    /**
     * Reading Audio Data from the Mixer
     */
    private void readMixer() {
        mixer.finishBuffer();
        mixer.readBuffer(data, 0, data.length);
    }

    /**
     * <p>Asks if the entire song has been rendered. Since NSF does not have
     * an explicit node to end playback, this method will always return false.
     * <p>If you want to set a trigger to end the playback node, you need to scan
     * the samples returned by {@link #render(short[], int, int)} to see
     * if they are all the same, and then determine that the NSF did not emit
     * any sound during that period.
     * <p>When the NSF has been rendered for several frames in a row
     * (recommended 3 seconds, approx. 180 frames) the rendering of the song is complete.
     * </p>
     */
    @Override
    public boolean isFinished() {
        return false;
    }

    /**
     * <p>Informs the mixer that the rendering of the current frame has begun.
     * <p>This method was originally used to notify the mixer if the rendering
     * speed needed to be changed for this frame.
     * This method allows the mixer to prepare for this in advance by modifying
     * the number of samples stored and thus adjusting the playback speed.
     * </p>
     *
     * @since v0.2.9
     */
    private void mixerReady() {
        mixer.readyBuffer();
    }

    /**
     * All sound call sound.process(freqPerFrame);
     */
    private void processSounds(int freq) {
        if (channels == null) {
            return;
        }

        for (ChannelParam p : channels) {
            if (p == null) {
                continue;
            }
            executor.getSound(p.channelCode).process(freq);
        }
    }

    /**
     * All the sound calls sound.endFrame();
     */
    private void endFrame() {
        if (channels == null) {
            return;
        }

        for (ChannelParam p : channels) {
            if (p == null) {
                continue;
            }
            executor.getSound(p.channelCode).endFrame();
        }
    }

    //
    // Dashboard area
    //

    //
    // The part used to control the actual playback data.
    // Among them: control volume, control whether to play or not, control rendering components, etc.
    //

    /**
     * @return Currently playing track number
     * @since v0.2.8
     */
    @Override
    public int getCurrentTrack() {
        return executor.getCurrentTrack();
    }

    /**
     * @since v0.2.8
     */
    @Override
    public Set<Byte> allChannelSet() {
        return executor.allChannelSet();
    }

    /**
     * Setting the volume of a track
     *
     * @param channelCode channel number
     * @param level       Volume. range [0, 1]
     * @since v0.2.4
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
     * @throws NullPointerException When the track corresponding to <code>channelCode</code> does not exist
     * @since v0.2.4
     */
    @Override
    public float getLevel(byte channelCode) throws NullPointerException {
        int id = findMixerChannelByCode(channelCode);
        if (id != -1) {
            return mixer.getLevel(id);
        }
        throw new NullPointerException("No " + channelCode + " corresponding track");
    }

    /**
     * Set whether the track makes a sound or not
     *
     * @param channelCode channel number
     * @param mask        false, mute the track; true, mute the track
     * @since v0.2.4
     */
    @Override
    public void setChannelMuted(byte channelCode, boolean mask) {
        AbstractNsfSound sound = executor.getSound(channelCode);
        if (sound != null) {
            sound.setMuted(mask);
        }
    }

    /**
     * See if the track makes a sound
     *
     * @param channelCode channel number
     * @return false, indicates that the track is not blocked; true, the track is blocked
     * @throws NullPointerException When the track corresponding to <code>channelCode</code>
     *                              does not exist
     * @since v0.2.4
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

        super.resetCounterParam(frameRate, param.sampleRate);
        apuCounter.setParam(countCycle(speed), param.sampleRate);
        rate.onParamUpdate();
    }

    @Override
    public float getSpeed() {
        return param.speed;
    }

    /**
     * Get the operator (tool class) of the mixer.
     * This allows you to perform simple operations on the used mixer.
     *
     * @return channel of the mixer
     * @since v0.2.10
     */
    public IMixerHandler getMixerHandler() {
        return mixer.getHandler();
    }

    class N163ReattachListener implements IN163ReattachListener {

        @Override
        public void onReattach(int n163ChannelCount) {
            NsfRenderer.this.n163ChannelCount = n163ChannelCount;
            if (channelInit) {
                return;
            }

            for (int i = 0; i < 8; i++) {
                byte channelCode = (byte) (NesN163.CHANNEL_N163_1 + i);
                AbstractNsfSound sound = executor.getSound(channelCode);
                if (sound != null) {
                    ChannelParam p = searchParam(channelCode);
                    if (p == null) {
                        // Create Connection
                        int mixerChannel = mixer.allocateChannel(channelCode);
                        IMixerChannel mix = mixer.getMixerChannel(mixerChannel);
                        sound.setOut(mix);
                        mix.setLevel(getInitLevel(channelCode));

                        p = new ChannelParam();
                        p.channelCode = channelCode;
                        p.mixerChannel = mixerChannel;

                        putChannelParam(p);
                    }
                } else {
                    ChannelParam p = searchParam(channelCode);
                    if (p != null) {
                        // Delete connection

                        int mixerChannel = p.mixerChannel;
                        mixer.detach(mixerChannel);

                        removeChannelParam(p);
                    }
                }
            }
        }

        private ChannelParam searchParam(byte code) {
            for (ChannelParam p : channels) {
                if (p == null) {
                    continue;
                }
                if (p.channelCode == code) {
                    return p;
                }
            }

            return null;
        }

        private void putChannelParam(ChannelParam p) {
            for (int i = 0; i < channels.length; i++) {
                if (channels[i] == null) {
                    channels[i] = p;
                    return;
                }
            }

            // Arrays need to be expanded
        }

        private void removeChannelParam(ChannelParam p) {
            for (int i = 0; i < channels.length; i++) {
                if (channels[i] == p) {
                    channels[i] = null;
                    return;
                }
            }
        }
    }

    private final N163ReattachListener n163lsner = new N163ReattachListener();

    /**
     * Get the volume of each track. This value should be taken
     * from the parameter {@link NsfParameter}.
     *
     * @param channelCode
     * @return
     */
    private float getInitLevel(byte channelCode) {
        float level = switch (channelCode) {
            case CHANNEL_2A03_PULSE1 -> param.levels.level2A03Pules1;
            case CHANNEL_2A03_PULSE2 -> param.levels.level2A03Pules2;
            case CHANNEL_2A03_TRIANGLE -> param.levels.level2A03Triangle;
            case CHANNEL_2A03_NOISE -> param.levels.level2A03Noise;
            case CHANNEL_2A03_DPCM -> param.levels.level2A03DPCM;
            case CHANNEL_VRC6_PULSE1 -> param.levels.levelVRC6Pules1;
            case CHANNEL_VRC6_PULSE2 -> param.levels.levelVRC6Pules2;
            case CHANNEL_VRC6_SAWTOOTH -> param.levels.levelVRC6Sawtooth;
            case CHANNEL_MMC5_PULSE1 -> param.levels.levelMMC5Pules1;
            case CHANNEL_MMC5_PULSE2 -> param.levels.levelMMC5Pules2;
            case CHANNEL_FDS -> param.levels.levelFDS;
            case CHANNEL_N163_1 -> param.levels.levelN163Namco1;
            case CHANNEL_N163_2 -> param.levels.levelN163Namco2;
            case CHANNEL_N163_3 -> param.levels.levelN163Namco3;
            case CHANNEL_N163_4 -> param.levels.levelN163Namco4;
            case CHANNEL_N163_5 -> param.levels.levelN163Namco5;
            case CHANNEL_N163_6 -> param.levels.levelN163Namco6;
            case CHANNEL_N163_7 -> param.levels.levelN163Namco7;
            case CHANNEL_N163_8 -> param.levels.levelN163Namco8;
            case CHANNEL_VRC7_FM1 -> param.levels.levelVRC7FM1;
            case CHANNEL_VRC7_FM2 -> param.levels.levelVRC7FM2;
            case CHANNEL_VRC7_FM3 -> param.levels.levelVRC7FM3;
            case CHANNEL_VRC7_FM4 -> param.levels.levelVRC7FM4;
            case CHANNEL_VRC7_FM5 -> param.levels.levelVRC7FM5;
            case CHANNEL_VRC7_FM6 -> param.levels.levelVRC7FM6;
            case CHANNEL_S5B_SQUARE1 -> param.levels.levelS5BSquare1;
            case CHANNEL_S5B_SQUARE2 -> param.levels.levelS5BSquare2;
            case CHANNEL_S5B_SQUARE3 -> param.levels.levelS5BSquare3;
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
     * Calculate the actual number of clocks per second after
     * calculating the effect of the specified speed.
     *
     * @param speed speed
     * @return Actual number of clocks running per second
     */
    private int countCycle(float speed) {
        int cycle = executor.cycleRate();
        if (speed != 1 && speed > 0) {
            cycle = (int) (cycle / speed);
        }
        return cycle;
    }

    static class ChannelParam {

        /**
         * channel number
         */
        byte channelCode;
        /**
         * Mixer track identification number
         */
        int mixerChannel;
    }

    private ChannelParam[] channels;

    /**
     * According to channel number, find the track identification number in the Mixer.
     *
     * @param channelCode NSF-defined channel number
     * @return Mixer track identification number
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
}
