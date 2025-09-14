package zdream.nsfplayer.nsf.renderer;

import java.util.Set;

import javax.sound.midi.Instrument;

import vavi.util.win32.WAVE.data;
import zdream.nsfplayer.core.CycleCounter;
import zdream.nsfplayer.core.FloatCycleCounter;
import zdream.nsfplayer.core.INsfRendererHandler;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfRateConverter;
import zdream.nsfplayer.core.NsfStatic;
import zdream.nsfplayer.mixer.IMixerChannel;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.chip.NesN163;
import zdream.nsfplayer.nsf.executor.IN163ReattachListener;
import zdream.nsfplayer.nsf.executor.NsfExecutor;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.SoundPulse;


/**
 * <p>NSF Recorder.
 * <p>The NSF content is recorded and exported in accordance with certain contents
 * for subsequent broadcasting and processing.
 * This class is used in scenarios where you need to output NSF playback data on the server side,
 * and transfer it to the front-end browser for rendering and playback.
 * The requirements here are that the data transferred should not be too high (so {@link NsfRenderer}
 * won't work), and that it should be fast ({@link NsfRenderer}'s audio compression
 * into .mp3, .ogg, .m4a formats is obviously too slow).
 * <p>Current Program Support: 2A03.
 * </p>
 *
 * @author Zdream
 * @since v0.3.2
 */
public class NsfRecorder implements INsfRendererHandler<NsfAudio> {

    private final NsfExecutor executor = new NsfExecutor();

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
     * <p>N163 Number of tracks .
     * <p>If not determined, the value is -1.
     * </p>
     * @since v0.3.2
     */
//	private int n163ChannelCount = -1;

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
//	private boolean channelInit;
    public NsfRecorder() {
        this(new NsfRendererConfig());
    }

    public NsfRecorder(NsfRendererConfig config) {
        param.sampleRate = config.sampleRate;
        param.frameRate = frameRate;
        param.levels.copyFrom(config.channelLevels);

        executor.setRegion(config.region);
        executor.setRate(config.sampleRate);

        rate = new NsfRateConverter(param);
        exeCycle.setParam(config.sampleRate, this.frameRate);
    }

    //
    // Preparatory segment
    //

    /**
     * It doesn't really matter if you choose 50 or 60, because
     * the Nsf player calculates in steps of one sample, not frames.
     */
    private final int frameRate = NsfStatic.FRAME_RATE_NTSC;

    /**
     * Reads Nsf audio and prepares it with default tracks
     *
     * @param audio
     * @throws NullPointerException When audio is null
     */
    public void ready(NsfAudio audio) {
        ready0(audio, audio.getStart());
    }

    /**
     * Reads Nsf audio, prepares it with a specified track
     *
     * @param audio Nsf Audio Examples
     * @param track track number (of a song)
     * @throws NullPointerException     When audio is null
     * @throws IllegalArgumentException When the track number track is outside
     *                                  the range [0, audio.total_songs).
     */
    @Override
    public void ready(NsfAudio audio, int track) {
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
     * @throws IllegalArgumentException When the track number track is outside
     *                                  the range [0, audio.total_songs).
     */
    @Override
    public void ready(int track) throws NullPointerException {
        executor.ready(track);
    }

    private void ready0(NsfAudio audio, int track) {
//		channelInit = true;
        executor.ready(audio, track);

        resetCounterParam(frameRate, param.sampleRate);
        rate.onParamUpdate(frameRate, executor.cycleRate());
        apuCounter.setParam(countCycle(param.speed), param.sampleRate);

        connectChannels(audio.useN163());
//		clearBuffer();

//		channelInit = false;
    }

    /**
     * <p>Connects the sound in the execution component to the track of the rendering component.
     * <p>This method temporarily determines all track numbers
     * </p>
     *
     * @param useN163 Does the audio use the N163 chip
     */
    private void connectChannels(boolean useN163) {
//		mixer.detachAll();
        Set<Byte> channels = executor.allChannelSet();

        // Calculate the total number of orbits.
        // When N163 orbitals are used, but the number of orbitals is uncertain,
        // the total number + 8 is added in order to make up for the N163 orbitals.
//		if (useN163 && n163ChannelCount == -1) {
//			this.channels = new ChannelParam[channels.size() + 8];
//		} else {
//			this.channels = new ChannelParam[channels.size()];
//		}

        for (byte channelCode : channels) {
            AbstractNsfSound sound = executor.getSound(channelCode);
            if (sound != null) {
//				mixerChannel = mixer.allocateChannel(channelCode);
//				IMixerChannel mix = mixer.getMixerChannel(mixerChannel);
//				sound.setOut(mix);

                sound.setMuted(true); // Otherwise, a null pointer error is reported.

                // TODO Insert logger here

                // loudness
//				mix.setLevel(getInitLevel(channelCode));

                // TODO Tells the mixer more information, including the output sample rate of
                //  the microphone (1.77 million for NSF, 44100 or 48000 for mpeg, etc.).
            }

            // Cache track number
            ChannelParam p = new ChannelParam();
            p.channelCode = channelCode;
            p.mixerChannel = -1;
        }
    }

    //
    // rendering section
    //

    int ic = 0;
    boolean lastEnable = false;
    int lastPeriod = -1;
    int lastDuty = -1;
    int lastVolume = -1;

    public int renderFrame() {
        int ret = countNextFrame();
        param.sampleInCurFrame = ret;
        rate.doConvert();
//		mixerReady();

        SoundPulse p = (SoundPulse) executor.getSound(CHANNEL_2A03_PULSE1);
        int exeCount = exeCycle.tick();
        for (int i = 0; i < exeCount; i++) {
            executor.tick();
            processSounds(apuCounter.tick());

            if (lastPeriod != p.period || lastDuty != p.dutyLength || lastVolume != p.fixedVolume || lastEnable != p.isEnable()) {
                System.out.println(String.format("%6x:%s,%3x,%d,%x", ic++,
                        p.isEnable() ? '1' : '0', p.period, p.dutyLength, p.fixedVolume));
                lastPeriod = p.period;
                lastDuty = p.dutyLength;
                lastVolume = p.fixedVolume;
                lastEnable = p.isEnable();
            }
            ic++;
        }
        endFrame();

        ic = ((ic & 0x7FFFF000) + 0x1000);

        // Reading data from the mixer
//		readMixer();

        return ret;
    }

//    /**
//     * Reading Audio Data from the Mixer
//     */
//	private void readMixer() {
//		mixer.finishBuffer();
//		mixer.readBuffer(data, 0, data.length);
//	}

    /**
     * <p>Asks if the entire song has been rendered. Since NSF does not have an explicit node
     * to end playback, this method will always return false.
     * <p>If you want to set a trigger to end the playback node, you need to scan the samples
     * returned by {@link #render(short[], int, int)} to see if they are all the same,
     * and then determine that the NSF did not emit any sound during that period.
     * <p>When the NSF has been rendered for several frames in a row (recommended 3 seconds,
     * approx. 180 frames) the rendering of the song is complete.
     * </p>
     */
    public boolean isFinished() {
        return false;
    }

//	/**
//	 * <p>Informs the mixer that the rendering of the current frame has begun.
//	 * <p>This method was originally used to notify the mixer that if the rendering speed
//	 * of the current frame needed to be changed, this method could be used to allow
//	 * the mixer to prepare for this in advance by modifying the number of samples stored
//	 * to adjust the playback speed.
//	 * </p>
//	 * @since v0.2.9
//	 */
//	private void mixerReady() {
//		mixer.readyBuffer();
//	}

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
            executor.getSound(p.channelCode).process(freq); // I don't know if I need it.
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
    // Instrument panel area
    //

    //
    // The part used to control the actual playback data .
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
     * Setting the volume of a channel is not supported
     */
    @Override
    public void setLevel(byte channelCode, float level) {
        // It's useless.
    }

    /**
     * Getting the volume of a channel is not supported
     */
    @Override
    public float getLevel(byte channelCode) throws NullPointerException {
        return 1f;
    }

    /**
     * Set whether the channel makes a sound or not
     *
     * @param channelCode channel number
     * @param mask        false, mute the channel; true, mute the channel
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
     * See if the channel makes a sound
     *
     * @param channelCode channel number
     * @return false, indicates that the channel is not blocked; true, the channel is blocked
     * @throws NullPointerException When the track corresponding to <code>channelCode</code> does not exist
     * @since v0.2.4
     */
    @Override
    public boolean isChannelMuted(byte channelCode) throws NullPointerException {
        return executor.getSound(channelCode).isMuted();
    }

    public void setSpeed(float speed) {
        if (speed > 10) {
            speed = 10;
        } else if (speed < 0.1f) {
            speed = 0.1f;
        }

        param.speed = speed;

        resetCounterParam(frameRate, param.sampleRate);
        apuCounter.setParam(countCycle(speed), param.sampleRate);
        rate.onParamUpdate();
    }

    public float getSpeed() {
        return param.speed;
    }

//    class N163ReattachListener implements IN163ReattachListener {
//
//        @Override
//        public void onReattach(int n163ChannelCount) {
//            NsfRenderer.this.n163ChannelCount = n163ChannelCount;
//            if (channelInit) {
//                return;
//            }
//
//            for (int i = 0; i < 8; i++) {
//                byte channelCode = (byte) (NesN163.CHANNEL_N163_1 + i);
//                AbstractNsfSound sound = executor.getSound(channelCode);
//                if (sound != null) {
//                    ChannelParam p = searchParam(channelCode);
//                    if (p == null) {
//                        // Create Connection
//                        int mixerChannel = mixer.allocateChannel(channelCode);
//                        IMixerChannel mix = mixer.getMixerChannel(mixerChannel);
//                        sound.setOut(mix);
//                        mix.setLevel(getInitLevel(channelCode));
//
//                        p = new ChannelParam();
//                        p.channelCode = channelCode;
//                        p.mixerChannel = mixerChannel;
//
//                        putChannelParam(p);
//                    }
//                } else {
//                    ChannelParam p = searchParam(channelCode);
//                    if (p != null) {
//                        // Deleting a connection
//
//                        int mixerChannel = p.mixerChannel;
//                        mixer.detach(mixerChannel);
//
//                        removeChannelParam(p);
//                    }
//                }
//            }
//        }
//
//        private ChannelParam searchParam(byte code) {
//            for (ChannelParam p : channels) {
//                if (p == null) {
//                    continue;
//                }
//                if (p.channelCode == code) {
//                    return p;
//                }
//            }
//
//            return null;
//        }
//
//        private void putChannelParam(ChannelParam p) {
//            for (int i = 0; i < channels.length; i++) {
//                if (channels[i] == null) {
//                    channels[i] = p;
//                    return;
//                }
//            }
//
//            // The array needs to be expanded
//        }
//
//        private void removeChannelParam(ChannelParam p) {
//            for (int i = 0; i < channels.length; i++) {
//                if (channels[i] == p) {
//                    channels[i] = null;
//                    return;
//                }
//            }
//        }
//
//    }
//
//    private final N163ReattachListener n163lsner = new N163ReattachListener();

    /**
     * Calculate the actual number of clocks per second after calculating
     * the effect of the specified speed.
     *
     * @param speed tempo
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
         * Mixer channel identifier
         */
        int mixerChannel;
    }

    private ChannelParam[] channels;

    //
    // replenishment
    //

    /**
     * Sample rate counter.
     *
     * @since v0.2.5
     */
    protected final CycleCounter counter = new CycleCounter();

    private void resetCounterParam(int maxFrameCount, int maxSampleCount) {
        float speed = getSpeed();
        int cycle = maxSampleCount;
        if (speed != 1) {
            cycle = (int) (cycle / speed);
        }

        // Reset Counter
        counter.setParam(cycle, maxFrameCount);
    }

    /**
     * Calculates the number of samples needed for the next frame (per channel),
     * taking into account the effect of playback speed.
     *
     * @return Number of samples needed for the next frame
     */
    protected int countNextFrame() {
        return counter.tick();
    }
}
