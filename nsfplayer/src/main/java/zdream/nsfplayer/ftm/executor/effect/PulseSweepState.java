package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;
import zdream.nsfplayer.ftm.executor.channel.Channel2A03Pulse;


/**
 * <p>Sweep effect status
 * <p>When a channel has this state, the pitch will change over time.
 * During this time, the channel will not respond to any other effects that
 * modify the pitch or key of the track.
 * </p>
 *
 * @author Zdream
 * @see PulseSweepEffect
 * @since 0.2.9
 */
public class PulseSweepState implements IFtmState {

    public static final String NAME = "Sweep";

    @Override
    public String name() {
        return "Sweep";
    }

    @Override
    public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
        Channel2A03Pulse ch = (Channel2A03Pulse) runtime.channels.get(channelCode);

        if (ch.isNoteUpdated()) {
            ch.clearSweep();

            // Delete yourself
            ch.removeState(this);
        }
    }

    /**
     * Low priority. Needs to be triggered after most of the master note
     * {@link AbstractFtmChannel#setMasterNote(int)} calls have been completed
     */
    @Override
    public int priority() {
        return -5;
    }
}
