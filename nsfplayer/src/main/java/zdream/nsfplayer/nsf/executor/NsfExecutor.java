package zdream.nsfplayer.nsf.executor;

import java.util.HashSet;
import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfExecutor;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;

import static java.util.Objects.requireNonNull;


/**
 * <p>Execution building blocks for Nsf.
 * <p>In version 0.2.x, the execution of Nsf is written directly in the NsfRenderer.
 * As of version 0.3.0, the execution artifacts are separated from the renderer and form a separate class.
 * It takes over execution-related tasks that would otherwise be done by an NsfRuntime or NsfRenderer.
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public class NsfExecutor extends AbstractNsfExecutor<NsfAudio> {

    private final NsfRuntime runtime;

    public NsfExecutor() {
        this.runtime = new NsfRuntime();
        runtime.init();
    }

    //
    // preparatory section
    //

    /**
     * Follows the format specified in the Nsf file.
     */
    public static final int REGION_FOLLOW_AUDIO = 0;
    /**
     * Mandatory NTSC
     */
    public static final int REGION_FORCE_NTSC = 1;
    /**
     * Mandatory PAL
     */
    public static final int REGION_FORCE_PAL = 2;
    /**
     * Mandatory DENDY
     */
    public static final int REGION_FORCE_DENDY = 3;

    /**
     * Setting the format requirements for playback
     *
     * @param region Format Requirements
     * @throws IllegalArgumentException When the format requirements are not preset for these four
     * @see #REGION_FOLLOW_AUDIO
     * @see #REGION_FORCE_NTSC
     * @see #REGION_FORCE_PAL
     * @see #REGION_FORCE_DENDY
     */
    public void setRegion(int region) {
        switch (region) {
            case REGION_FOLLOW_AUDIO:
            case REGION_FORCE_NTSC:
            case REGION_FORCE_PAL:
            case REGION_FORCE_DENDY:
                runtime.param.region = region;
                break;

            default:
                throw new IllegalArgumentException("Formatting requirements: " + region + " not parsable");
        }
    }

    /**
     * Sets the rate at which tick() is executed.
     *
     * @param rate Execution rate. Typically this value is equal to sampleRate
     */
    public void setRate(int rate) {
        runtime.param.sampleRate = rate; // default: 48000
    }

    /**
     * Reads Nsf audio and prepares it with default tracks
     *
     * @param audio
     * @throws NullPointerException When audio is null
     */
    @Override
    public void ready(NsfAudio audio) throws NullPointerException {
        requireNonNull(audio, "NSF Tracks audio = null");
        ready0(audio, audio.start);
    }

    /**
     * Reads Nsf audio, prepares it with a specified track
     *
     * @param audio Nsf Audio Examples
     * @param track track number (of a song)
     * @throws NullPointerException     When audio is null
     * @throws IllegalArgumentException When the track number track is outside the range [0, audio.total_songs).
     */
    public void ready(NsfAudio audio, int track) throws NullPointerException, IllegalArgumentException {
        requireNonNull(audio, "NSF Tracks audio = null");
        if (track < 0 || track >= audio.total_songs) {
            throw new IllegalArgumentException(
                    "Track number track needs to be in range [0, " + audio.total_songs + "), " + track + " is illegal");
        }

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
    public void ready(int track) throws NullPointerException {
        NsfAudio audio = runtime.audio;
        requireNonNull(audio, "NSF Tracks audio = null");

        if (track < 0 || track >= audio.total_songs) {
            throw new IllegalArgumentException(
                    "Track number track needs to be in range [0, " + audio.total_songs + ")");
        }

        runtime.manager.setSong(track);
        runtime.reset();
    }

    private void ready0(NsfAudio audio, int track) {
        if (track < 0 || track >= audio.total_songs) {
            track = 0;
        }

        runtime.audio = audio;
        runtime.manager.setSong(track);

        runtime.reset();
    }

    //
    // rendering section
    //

    @Override
    public void tick() {
        runtime.manager.tickCPU();
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    //
    // Parameter indicators
    //

    /**
     * @return Clocks per second
     */
    public int cycleRate() {
        return runtime.param.freqPerSec;
    }

    /**
     * @return Currently playing track number
     */
    public int getCurrentTrack() {
        return runtime.manager.getSong();
    }

    /**
     * Returns the set of all track numbers. The parameter for
     * the track number is specified in {@link INsfChannelCode}.
     *
     * @return The set of all track numbers. If the ready(...)
     *         method is not called, the empty set is returned.
     */
    public Set<Byte> allChannelSet() {
        return new HashSet<>(runtime.chips.keySet());
    }

    /**
     * <p>Acquisition of a loudspeaker corresponding to the track number.
     * <p>The speaker is the final output of the actuator, and all execution
     * results are written directly to the speaker.
     * </p>
     *
     * @param channelCode orbital number
     * @return An instance of the sound generator for the corresponding track.
     *         If there is no corresponding track, returns null.
     */
    public AbstractNsfSound getSound(byte channelCode) {
        AbstractSoundChip chip = runtime.chips.get(channelCode);
        if (chip == null) {
            return null;
        }
        return chip.getSound(channelCode);
    }

    //
    // listener
    //

    /**
     * Adding a listener for N163 reconnections.
     *
     * @param listener N163 listener
     */
    public void addN163ReattachListener(IN163ReattachListener listener) {
        runtime.n163Lsners.add(listener);
    }

    /**
     * Deleting the N163 reconnected listener.
     *
     * @param listener N163 listener
     */
    public void removeReattachListener(IN163ReattachListener listener) {
        runtime.n163Lsners.remove(listener);
    }

    /**
     * Empty N163 reconnected listener.
     */
    public void clearReattachListeners() {
        runtime.n163Lsners.clear();
    }
}
