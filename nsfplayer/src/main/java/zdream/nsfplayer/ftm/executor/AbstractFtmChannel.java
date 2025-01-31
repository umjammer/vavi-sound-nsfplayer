package zdream.nsfplayer.ftm.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.ftm.executor.effect.IFtmEffect;
import zdream.nsfplayer.sound.AbstractNsfSound;


/**
 * Abstract Famitracker tracks, used to store playback localization parameters for each track,
 * such as local pitch, etc.
 *
 * <p>In the original project, it is a combination of TrackerChannel and ChannelHandler.
 * </p>
 *
 * @author Zdream
 * @data 2018-06-09
 * @since 0.2.1
 */
public abstract class AbstractFtmChannel implements INsfChannelCode, IFtmRuntimeHolder, IResetable {

    /**
     * channel number
     */
    public final byte channelCode;

    /**
     * Runtime data
     */
    private FamiTrackerRuntime runtime;

    @Override
    public FamiTrackerRuntime getRuntime() {
        return runtime;
    }

    /**
     * Setting up the environment
     *
     * @param runtime
     */
    void setRuntime(FamiTrackerRuntime runtime) {
        this.runtime = runtime;
    }

    public AbstractFtmChannel(byte channelCode) {
        this.channelCode = channelCode;
    }

    /**
     * Get Audio Speaker
     *
     * @return Examples of Audio Loudspeakers
     */
    public abstract AbstractNsfSound getSound();

    /**
     * <p>Write data from the channel to the transmitter
     * <p>Originally, subclasses had similar functions, but starting with version v0.2.9,
     * they have been moved to the parent class.
     * </p>
     *
     * @since v0.2.9
     */
    public abstract void writeToSound();

    /*
     * external interface
     */

    /**
     * <p>Execute note
     * <p>The execution part and the playback part have been split into
     * two functions since v0.2.9.
     * This function is the executable, and all this method does is handle
     * the triggering of effects and states.
     * The result of this function modifies the parameters of the channel instance,
     * but does not modify sound in any way.
     * <p>original program: ChannelHandler.playNote
     * </p>
     *
     * @version v0.2.9
     * <br>From completing both [Execute] and [Play] tasks, only [Execute] tasks will be completed.
     * The playback task is moved to the function {@link #triggerSound()}.
     * @see #triggerSound()
     */
    public void playNote() {
        // initialization
        startFrame();

        // effect
        forceEffect(runtime.effects.get(this.channelCode).values());

        // statuses
        triggleState();
    }

    //
    // parameters
    //

    /*
     * Here's a quick note. The following parameters, such as volume, have two values,
     * curVolume and masterVolume.
     * The final value used is curVolume, with the stipulation that when the effect and
     * state are modified, masterVolume is the master volume and curVolume is the offset,
     * i.e., curVolume floats around 0; and
     * When the total curVolume is finally calculated, both the master volume and the offset
     * are entered into the calculation, and the resulting curVolume rewrites the value of
     * curVolume. At this point, the meaning of curVolume changes from offset to current volume.
     *
     * Pitch period, key note also follow this rule.
     */

    /**
     * instruments
     */
    protected int instrument;

    /**
     * If or not the instrument has been updated in the current frame.
     * This value is true when the key or instrument is changed in the frame.
     */
    protected boolean instrumentUpdated;

    /**
     * Whether the keys have been updated in the current frame.
     * This value is true when the key is changed in this frame.
     */
    protected boolean noteUpdated;

    /**
     * Keys, with notes and pitches
     * <p>curNote: current key
     * <p>masterNote: Master key.
     * Valid values for the master key are [1, 96] ([1, 16] for the Noise track),
     * 0 is an invalid value, meaning no sound is emitted.
     * </p>
     */
    protected int curNote, masterNote;

    /**
     * <p>volume
     * <p>curVolume: Current volume, need to calculate instrument variables.
     * Since I was trying to increase the precision here, 16 times the original volume,
     * and allowed for negative numbers and so on to exceed the bounds in the middle of
     * the modification process, I ended up limiting the range to [0, 240] before writing
     * it to the microphone.
     * <p>masterVolume: master volume [0, 15]
     * </p>
     */
    protected int curVolume, masterVolume = 15;

    /**
     * <p>pitch
     * <p>curPeriod: Current wavelength, need to calculate other things like vibrato etc.
     * <p>masterPitch: Master pitch. This value is called finePitch in the original
     * C++ program and is controlled by the Pxx effect. The default value is 0
     * <p>The relationship between wavelength and pitch is negative, in the same units.
     * The longer the wavelength, the lower the pitch.
     * </p>
     */
    protected int curPeriod, masterPitch;

    /**
     * <p>tone
     * <p>curDuty: Current Tone
     * <p>masterDuty: Master Tone. This value is controlled by the Vxx effect. Default value 0
     * </p>
     */
    protected int curDuty, masterDuty;

    /**
     * Whether or not it is playing
     */
    protected boolean playing = false;

    /**
     * @return {@link #instrument}
     */
    public int getInstrument() {
        return instrument;
    }

    /**
     * @param instrument {@link #instrument}
     */
    public void setInstrument(int instrument) {
        instrumentUpdated = true;
        this.instrument = instrument;
    }

    /**
     * @return {@link #curNote}
     */
    public int getCurrentNote() {
        return curNote;
    }

    /**
     * @param note {@link #curNote}
     */
    public void setCurrentNote(int note) {
        this.curNote = note;
    }

    /**
     * Adding a value to {@link #curNote}
     *
     * @param delta increment, can be positive, negative or 0
     */
    public void addCurrentNote(int delta) {
        this.curNote += delta;
    }

    /**
     * @return {@link #curVolume}
     */
    public int getCurrentVolume() {
        return curVolume;
    }

    /**
     * @param volume {@link #curVolume}
     */
    public void setCurrentVolume(int volume) {
        this.curVolume = volume;
    }

    /**
     * Adding a value to {@link #curVolume}
     *
     * @param delta increment, can be positive, negative or 0
     */
    public void addCurrentVolume(int delta) {
        this.curVolume += delta;
    }

    /**
     * @return {@link #curPeriod}
     */
    public int getCurrentPeriod() {
        return curPeriod;
    }

    /**
     * @param period {@link #curPeriod}
     */
    public void setCurrentPeriod(int period) {
        this.curPeriod = period;
    }

    /**
     * Adding a value to {@link #curPeriod}
     *
     * @param delta increment, can be positive, negative or 0
     */
    public void addCurrentPeriod(int delta) {
        this.curPeriod += delta;
    }

    /**
     * @return {@link #curDuty}
     */
    public int getCurrentDuty() {
        return curDuty;
    }

    /**
     * Setting and resetting the current tone key
     *
     * @param note {@link #masterNote}
     */
    public void setMasterNote(int note) {
        instrumentUpdated = true;
        noteUpdated = true;
        this.masterNote = note;
    }

    /**
     * <p>Sets and resets the current key, but does not set instrumentUpdated to true.
     * <p>The instrumentUpdated is not triggered when the effect Rxy and Qxy set the keys.
     * </p>
     *
     * @param note {@link #masterNote}
     * @since v0.2.3
     */
    public void setMasterNoteWithoutUpdate(int note) {
        this.masterNote = note;
    }

    /**
     * @return {@link #masterNote}
     */
    public int getMasterNote() {
        return masterNote;
    }

    /**
     * Set and reset the current volume
     *
     * @param masterVolume {@link #masterVolume}
     */
    public void setMasterVolume(int masterVolume) {
        this.masterVolume = masterVolume;
    }

    /**
     * @return {@link #masterVolume}
     */
    public int getMasterVolume() {
        return masterVolume;
    }

    /**
     * Setting and resetting the current pitch
     *
     * @param masterPitch {@link #masterPitch}
     */
    public void setMasterPitch(int masterPitch) {
        this.masterPitch = masterPitch;
    }

    /**
     * @return {@link #masterPitch}
     */
    public int getMasterPitch() {
        return masterPitch;
    }

    /**
     * Setting and resetting the current tone
     *
     * @param masterDuty {@link #masterDuty}
     */
    public void setMasterDuty(int masterDuty) {
        this.masterDuty = masterDuty;
    }

    /**
     * @return {@link #masterDuty}
     */
    public int getMasterDuty() {
        return masterDuty;
    }

    /**
     * @return {@link #instrumentUpdated}
     * @since v0.2.9
     */
    public boolean isInstrumentUpdated() {
        return instrumentUpdated;
    }

    /**
     * @return {@link #noteUpdated}
     * @since v0.2.9
     */
    public boolean isNoteUpdated() {
        return noteUpdated;
    }

    /**
     * Turn it on, let the channel play.
     * It is called so that when the channel receives a new note, it starts playing the new note.
     */
    public void turnOn() {
        playing = true;
    }

    /**
     * <p>Ask if the current channel is playing.
     * <p>Note that a volume of 0 does not mean that it is not playing.
     * Only halt effects, Sxx effects, etc. can set <code>playing = false</code>.
     * </p>
     *
     * @return Whether the current channel is playing or not
     */
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void reset() {
        playing = true;
        masterNote = 0;
        masterVolume = 15;
        masterDuty = 0;
        masterPitch = 0;
        instrument = 0;

        schedules.clear();
        states.clear();
    }

    /**
     * Called at the beginning of each frame
     */
    protected void startFrame() {
        curNote = 0;
        curDuty = 0;
        curPeriod = 0;
        curVolume = 0;
        instrumentUpdated = false;
        noteUpdated = false;

        for (IFtmSchedule s : schedules) {
            s.trigger(channelCode, runtime);
        }
        schedules.clear();
    }

    /*
     * Enforcement
     */

    /**
     * <p>The set of states.
     * <p>Originally, the delayed state, etc., was considered a state.
     * The state is triggered after the effect occurs. If you want the state to be
     * triggered before it happens, you need to put the state in schedules.
     * </p>
     */
    final HashSet<IFtmState> states = new HashSet<>();

    /**
     * The set of states triggered during the preparation phase. e.g. delay states
     * triggered before the effect is triggered, put them here.
     * The state of the preparation phase is called only once and is automatically
     * deleted after the call.
     */
    final HashSet<IFtmSchedule> schedules = new HashSet<>();

    /**
     * Add Status
     *
     * @param state
     */
    public void addState(IFtmState state) {
        states.add(state);
        state.onAttach(channelCode, runtime);
    }

    /**
     * Adding statuses triggered by the preparation phase
     *
     * @param schedule
     */
    public void addSchedule(IFtmSchedule schedule) {
        schedules.add(schedule);
    }

    /**
     * Delete Status
     *
     * @param state
     */
    public void removeState(IFtmState state) {
        state.onDetach(channelCode, runtime);
        states.remove(state);
    }

    /**
     * Delete all states with matching names
     *
     * @param name
     */
    public void removeStates(String name) {
        states.removeIf((s) -> {
            boolean b = s.name().equals(name);
            if (b) {
                s.onDetach(channelCode, runtime);
            }
            return b;
        });
    }

    /**
     * Filter out all state collections with matching names
     *
     * @param name
     * @return
     */
    public HashSet<IFtmState> filterStates(String name) {
        HashSet<IFtmState> set = new HashSet<>();
        for (IFtmState s : states) {
            if (s.name().equals(name)) {
                set.add(s);
            }
        }
        return set;
    }

    /**
     * Compulsory, immediate enforcement effect pooling
     *
     * @param effs Collection of effects (valid for non-global effects)
     */
    public void forceEffect(Collection<IFtmEffect> effs) {
        ArrayList<IFtmEffect> list = new ArrayList<>(effs);
        list.sort(null); // Effect classes have a natural prioritization

        for (IFtmEffect eff : list) {
            eff.execute(channelCode, runtime);
        }
    }

    /**
     * Forces existing states to be triggered according to their priority.
     */
    private void triggleState() {
        ArrayList<IFtmState> list = new ArrayList<>(this.states);
        list.sort(null); // State classes have a natural prioritization

        list.forEach((state) -> state.trigger(channelCode, runtime));
    }

    /**
     * Pause sound playback. The effect of a note displayed as “---” in ftm.
     */
    public void doHalt() {
        playing = false;
    }

    /**
     * Play the release of the instrument. The effect of a note that appears as “===” in ftm.
     */
    public void doRelease() {
        // Requires subclass completion
    }


    /**
     * <p>Query the wavelength value according to the tone key.
     * <p>Tool methods, which need to be overridden by subclasses as needed.
     * </p>
     *
     * @param note
     * @return
     */
    public int periodTable(int note) {
        throw new IllegalStateException("This track does not support the function of querying key wavelengths");
    }
}
