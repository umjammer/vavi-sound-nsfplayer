package zdream.nsfplayer.mixer.blip;

import zdream.nsfplayer.core.IExpression;
import zdream.nsfplayer.mixer.IMixerChannel;


/**
 * FTM default audio pipeline
 *
 * @author Zdream
 * @since 0.2.1
 */
public class BlipMixerChannel implements IMixerChannel {

    BlipSynth synth;
    final BlipSoundMixer mixer;
    /**
     * Volume correction. Range [0, 1.0], default 1.0
     */
    float level = 1;

    /**
     * Flag indicating whether it is opened
     */
    boolean enable = true;

    /**
     * The expression to set
     */
    IExpression expression;

    /**
     * Previous value
     * <li>lastInValue: passed in from sound
     * <li>lastMixValue: calculated in the expression
     * </li>
     */
    int lastInValue, lastMixValue;

    public BlipMixerChannel(BlipSoundMixer mixer) {
        this.mixer = mixer;
    }

    public void updateSetting(int quality, int range) {
        synth = new BlipSynth(quality, range);
    }

    @Override
    public void setLevel(float level) {
        this.level = level;
    }

    @Override
    public float getLevel() {
        return level;
    }

    /**
     * Setting the expression
     *
     * @param expression
     */
    public void setExpression(IExpression expression) {
        this.expression = expression;
    }

    @Override
    public void mix(int value, int time) {
        if (value == lastInValue) {
            return;
        }

        int mv = expression.f(value);

        synth.offset(time, (int) ((mv - lastMixValue) * level), mixer.buffer);
        lastInValue = value;
        lastMixValue = mv;
    }

    @Override
    public void reset() {
        lastInValue = lastMixValue = 0;
    }
}
