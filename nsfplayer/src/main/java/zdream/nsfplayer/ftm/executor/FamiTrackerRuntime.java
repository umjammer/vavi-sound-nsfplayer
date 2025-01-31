package zdream.nsfplayer.ftm.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import zdream.nsfplayer.ftm.audio.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.context.ChannelDeviceSelector;
import zdream.nsfplayer.ftm.executor.context.DefaultFtmEffectConverter;
import zdream.nsfplayer.ftm.executor.context.IFtmEffectConverter;
import zdream.nsfplayer.ftm.executor.effect.FtmEffectType;
import zdream.nsfplayer.ftm.executor.effect.IFtmEffect;
import zdream.nsfplayer.ftm.executor.hook.IFtmExecutedListener;
import zdream.nsfplayer.ftm.executor.hook.IFtmFetchListener;
import zdream.nsfplayer.ftm.executor.tools.FtmRowFetcher;
import zdream.nsfplayer.ftm.format.FtmNote;

import static zdream.nsfplayer.core.NsfChannelCode.typeOfChannel;


/**
 * Famitracker Runtime Status
 *
 * @author Zdream
 * @since v0.2.1
 */
public class FamiTrackerRuntime {

    //
    // members
    //

    public final FamiTrackerParameter param = new FamiTrackerParameter();

    /**
     * <p>FTM channel.
     * <p>The speaker is in the track, and can be obtained using the {@link AbstractFtmChannel#getSound()} method.
     * </p>
     */
    public final HashMap<Byte, AbstractFtmChannel> channels = new HashMap<>();

    /**
     * This frame is labeled for manual switching of the execution position.
     */
    public boolean switchFlag;
    /**
     * The frame is labeled for position switching. Includes automatic line feeds and manual position switching.
     */
    public boolean updateFlag;

    /*
     * initialization
     */

    public void init() {
        selector = new ChannelDeviceSelector();
        fetcher = new FtmRowFetcher(param);
        converter = new DefaultFtmEffectConverter();
    }

    /**
     * Reset Playback Tracks, Position
     *
     * @param audio   Tracks played
     * @param track   Track number, from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     * @since v0.2.9
     */
    public void ready(FtmAudio audio, int track, int section, int row) {
        querier = new FamiTrackerQuerier(audio);
        fetcher.ready(querier, track, section, row);
        fetcher.clearJump();

        // Add map to runtime.effects
        effects.clear();
        int len = querier.channelCount();
        for (int i = 0; i < len; i++) {
            byte code = querier.channelCode(i);
            effects.put(code, new HashMap<>());
        }
    }

    /**
     * Reset playback position, no track reset
     *
     * @param track   Track number, from 0
     * @param section Segment number, starting from 0
     * @param row     Line number, starting from 0
     * @since v0.2.9
     */
    public void ready(int track, int section, int row) {
        fetcher.ready(track, section, row);
        fetcher.clearJump();
        for (Map<FtmEffectType, IFtmEffect> map : effects.values()) {
            map.clear();
        }
        geffect.clear();
    }

    //
    // tool
    //

    /**
     * Row Data Acquisition and Playback Position Resolution Tool
     */
    public FtmRowFetcher fetcher;

    /**
     * Key - Effect Converter
     */
    public IFtmEffectConverter converter;

    /**
     * checker
     */
    public FamiTrackerQuerier querier;

    /**
     * environmental memory
     */
    public ChannelDeviceSelector selector;

    //
    // digital
    //

    /**
     * <p>The set of effects of all the keys in the line being interpreted,
     * which have been processed and sorted.
     * <p>Structure: track number - set of effects (may be empty)
     * </p>
     */
    public final HashMap<Byte, Map<FtmEffectType, IFtmEffect>> effects = new HashMap<>();

    /**
     * Global Scope Effects Set
     */
    public final Map<FtmEffectType, IFtmEffect> geffect = new HashMap<>();

    public void resetAllChannels() {
        channels.forEach((channelCode, ch) -> ch.reset());
    }

    /**
     * Empty effects set
     *
     * @since v0.2.9
     */
    public void clearEffects() {
        for (Map<FtmEffectType, IFtmEffect> effect : effects.values()) {
            effect.clear();
        }
        geffect.clear();
    }

    //
    // manipulate
    //

    /**
     * The music runs forward one frame. Let's see what line of Ftm we're on.
     */
    public void runFrame() {
        // reprovision
        param.finished = false;
        this.clearEffects();

        if (fetcher.doFrameUpdate()) {
            storeRow();
        }
    }

    /**
     * Determine which line is playing now, and let {@link IFtmEffectConverter} get it and process it.
     */
    public void storeRow() {
        int len = querier.channelCount();

        int trackIdx = param.trackIdx;
        int section = param.curSection;
        int row = param.curRow;

        for (int i = 0; i < len; i++) {
            byte channel = querier.channelCode(i);
            byte channelType = typeOfChannel(channel);

            FtmNote note = querier.getNote(trackIdx, section, i, row);
            note = onFetcher(note, channel);

            converter.convert(note, channelType, effects.get(channel), geffect, querier);
        }
    }

    /**
     * Updates the line break indicator. Called at the end of each frame.
     *
     * @since v0.3.1
     */
    public void updateFlag() {
        if (switchFlag) {
            updateFlag = true;
        } else {
            updateFlag = fetcher.isRowUpdated();
        }

        this.switchFlag = false;
    }

    /*
     * listener
     */

    public final ArrayList<IFtmFetchListener> flners = new ArrayList<>();
    public final ArrayList<IFtmExecutedListener> elners = new ArrayList<>();

    /**
     * Calling a tone key to get a listener
     *
     * @param note        Original key, may be null
     * @param channelCode current orbital number
     * @return
     */
    FtmNote onFetcher(FtmNote note, byte channelCode) {
        if (flners.isEmpty()) {
            return note;
        }

        FtmNote n = (note != null) ? note.clone() : null;
        FamiTrackerExecutorHandler h = new FamiTrackerExecutorHandler(this);

        for (IFtmFetchListener l : flners) {
            try {
                n = l.onFetch(n, channelCode, h);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        h.destroy();

        return n;
    }

    /**
     * Calling the execution completion listener
     */
    void onExecuteFinished() {
        if (elners.isEmpty()) {
            return;
        }

        FamiTrackerExecutorHandler h = new FamiTrackerExecutorHandler(this);
        elners.forEach(l -> l.onExecuteFinished(h));
    }
}
