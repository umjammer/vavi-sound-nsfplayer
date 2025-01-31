package zdream.nsfplayer.ftm.executor.effect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmSchedule;
import zdream.nsfplayer.ftm.executor.IFtmState;


/**
 * <p>Delayed playback effects, Gxx
 * </p>
 *
 * @author Zdream
 * @since v0.2.1
 */
public class DelayEffect implements IFtmEffect {

    public final int duration;

    private final HashSet<IFtmEffect> effects = new HashSet<>();

    private DelayEffect(int duration) {
        this.duration = duration;

        state = new DelayState(duration);
        schedule = new DelaySchedule();
        tracer = new DelayTraceSchedule();
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.DELAY;
    }

    /**
     * Creates a delayed playback effect
     *
     * @param duration Number of delayed frames. Only positive numbers are allowed.
     * @param effects  The set of all effects after the delay is triggered. Cannot be null
     * @return Examples of effects
     * @throws IllegalArgumentException When the number of delayed frames <code>duration</code> is not in the specified range
     * @throws NullPointerException     when effects = null
     */
    public static DelayEffect of(int duration, Collection<IFtmEffect> effects)
            throws IllegalArgumentException, NullPointerException {
        if (duration < 0) {
            throw new IllegalArgumentException("The number of delayed frames must be a positive value");
        }
        if (effects == null) {
            throw new NullPointerException("Effects collection effects = null");
        }
        DelayEffect d = new DelayEffect(duration);
        d.effects.addAll(effects);
        return d;
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel channel = runtime.channels.get(channelCode);

        // If the current channel has a delayed state, it is triggered immediately.
        HashSet<IFtmState> set = channel.filterStates(state.name());
        if (!set.isEmpty()) {
            for (IFtmState s : set) {
                // Trigger immediately: puts the effect into the effects of the environment.
                ((DelayState) s).triggerNow(channelCode, runtime);
            }
        }

        channel.addState(state);
    }

    @Override
    public String toString() {
        return "Delay:" + duration;
    }

    /**
     * <p>Internal Class, Delay Counter
     * </p>
     *
     * <p><b>complementary effect</b>
     * <li>(v0.2.3) When the state realizes that an effect has been triggered on a frame,
     * it is ready to execute the content of the delayed frame; the rules for execution are as follows:
     * <br>
     * <br>1. If the frame is created in the state, the supplemental effect will not be executed.
     * <br>2. All delayed effects need to be triggered before the effect is triggered on that frame.
     * </li>
     * </p>
     *
     * @author Zdream
     * @since v0.2.1
     */
    class DelayState implements IFtmState {

        /**
         * Deduct 1 for each frame, delayed key playback when deducted to 0.
         */
        private int delayCounter;

        public DelayState(int duration) {
            this.delayCounter = duration;
        }

        @Override
        public String name() {
            return "DELAY";
        }

        @Override
        public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
            AbstractFtmChannel channel = runtime.channels.get(channelCode);

            if (delayCounter > 1) {
                delayCounter--;
                channel.addSchedule(tracer);
            } else {
                // delayCounter = 1
                channel.addSchedule(schedule);
                // Delete this state
                channel.removeState(this);
            }
        }

        @Override
        public String toString() {
            return name() + delayCounter;
        }

        /**
         * Now immediately trigger off the keys in the state, and delete the state.
         *
         * @since v0.2.5
         */
        public void triggerNow(byte channelCode, FamiTrackerRuntime runtime) {
            Map<FtmEffectType, IFtmEffect> map = runtime.effects.get(channelCode);

            ArrayList<IFtmEffect> list = new ArrayList<>(effects);
            list.sort(null);

            for (IFtmEffect eff : list) {
                if (!map.containsKey(eff.type()))
                    map.put(eff.type(), eff);
            }

            AbstractFtmChannel channel = runtime.channels.get(channelCode);
            channel.removeState(this);
        }

        /**
         * lowest priority
         */
        @Override
        public final int priority() {
            return -99;
        }
    }

    class DelaySchedule implements IFtmSchedule {

        @Override
        public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
            // trigger
            Map<FtmEffectType, IFtmEffect> map = runtime.effects.get(channelCode);

            ArrayList<IFtmEffect> list = new ArrayList<>(effects);
            list.sort(null);

            for (IFtmEffect eff : list) {
                if (!map.containsKey(eff.type()))
                    map.put(eff.type(), eff);
            }
        }

    }

    /**
     * When there is a delayed state, if you see that the frame needs to trigger another effect,
     * then trigger the delayed effect in advance.
     *
     * @author Zdream
     * @since v0.2.3
     */
    class DelayTraceSchedule implements IFtmSchedule {

        @Override
        public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
            Map<FtmEffectType, IFtmEffect> map = runtime.effects.get(channelCode);
            if (!map.isEmpty()) {
                AbstractFtmChannel channel = runtime.channels.get(channelCode);

                // prepare to trigger
                channel.forceEffect(effects);

                // Delete this state
                channel.removeState(state);
            }
        }

    }

    final DelayState state;
    final DelaySchedule schedule;
    final DelayTraceSchedule tracer;

    /**
     * highest priority
     */
    @Override
    public final int priority() {
        return 99;
    }
}
