package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;


/**
 * Dedicated to the noise track, the effect of modifying the noise value
 *
 * @author Zdream
 * @since 0.2.2
 */
public class NoiseEffect implements IFtmEffect {

    /**
     * Noise value
     */
    public final int noise;

    private NoiseEffect(int noise) {
        this.noise = noise;
    }

    @Override
    public FtmEffectType type() {
        return FtmEffectType.NOTE;
    }

    /**
     * Creates an effect of modifying the noise value
     *
     * @param noise Noise value. Must be in the range [1, 16], 0 is an illegal value
     * @return Effect Examples
     * @throws IllegalArgumentException When <code>noise</code> is not within the specified range
     */
    public static NoiseEffect of(int noise) throws IllegalArgumentException {
        if (noise > 16 || noise < 1) {
            throw new IllegalArgumentException("The noise value must be an integer between 1 and 16.");
        }
        return new NoiseEffect(noise);
    }

    @Override
    public void execute(byte channelCode, FamiTrackerRuntime runtime) {
        AbstractFtmChannel ch = runtime.channels.get(channelCode);

        ch.setMasterNote(noise);
        ch.turnOn();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Noise:");

        switch (noise) {
            case 1:
                b.append("0-#");
                break;
            case 2:
                b.append("1-#");
                break;
            case 3:
                b.append("2-#");
                break;
            case 4:
                b.append("3-#");
                break;
            case 5:
                b.append("4-#");
                break;
            case 6:
                b.append("5-#");
                break;
            case 7:
                b.append("6-#");
                break;
            case 8:
                b.append("7-#");
                break;
            case 9:
                b.append("8-#");
                break;
            case 10:
                b.append("9-#");
                break;
            case 11:
                b.append("A-#");
                break;
            case 12:
                b.append("B-#");
                break;
            case 13:
                b.append("C-#");
                break;
            case 14:
                b.append("D-#");
                break;
            case 15:
                b.append("E-#");
                break;
            case 16:
                b.append("F-#");
                break;
        }

        return b.toString();
    }
}
