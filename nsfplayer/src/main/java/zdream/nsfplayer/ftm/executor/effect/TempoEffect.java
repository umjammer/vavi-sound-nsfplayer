package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * <p>Effects of changing tempo, Fxx
 * <p>Global effect
 * </p>
 *
 * @author Zdream
 * @since 0.2.1
 */
public class TempoEffect implements IFtmEffect {

    public final int tempo;

    private TempoEffect(int tempo) {
        this.tempo = tempo;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.TEMPO;
    }

    /**
     * Create an effect that modifies the rhythm value
     *
     * @param tempo Tempo value. Tempo value must be a positive number.
     * @return Effect Examples
     * @throws IllegalArgumentException When the tempo value <code>tempo</code> is not within the specified range
     */
    public static TempoEffect of(int tempo) throws IllegalArgumentException {
        if (tempo <= 0) {
            throw new IllegalArgumentException("The volume must be an integer value between 0 and 15");
        }
        return new TempoEffect(tempo);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        runtime.fetcher.setTempo(tempo);
    }

    @Override
    public String toString() {
        return "Tempo:" + tempo;
    }
}
