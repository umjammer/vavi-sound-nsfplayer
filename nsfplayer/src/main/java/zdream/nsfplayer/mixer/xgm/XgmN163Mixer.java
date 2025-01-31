package zdream.nsfplayer.mixer.xgm;

/**
 * N163's (merged) track
 *
 * @author Zdream
 * @since v0.2.6
 */
public class XgmN163Mixer extends AbstractXgmMultiMixer {

    private final XgmAudioChannel[] n163s = new XgmAudioChannel[8];
    private final boolean[] enables = new boolean[8];

    public XgmN163Mixer() {
        for (int i = 0; i < n163s.length; i++) {
            n163s[i] = new XgmAudioChannel();
        }
    }

    @Override
    public void reset() {
        super.reset();
        for (AbstractXgmAudioChannel ch : n163s) {
            ch.reset();
        }
    }

    @Override
    public AbstractXgmAudioChannel getRemainAudioChannel(byte type) {
        if (type != CHANNEL_TYPE_N163) {
            return null;
        }

        for (int i = 0; i < enables.length; i++) {
            if (!enables[i]) {
                return n163s[i];
            }
        }

        return null;
    }

    @Override
    public void setEnable(AbstractXgmAudioChannel channel, boolean enable) {
        for (int i = 0; i < n163s.length; i++) {
            if (channel == n163s[i]) {
                enables[i] = enable;
                break;
            }
        }
    }

    @Override
    public boolean isEnable(AbstractXgmAudioChannel channel) {
        for (int i = 0; i < n163s.length; i++) {
            if (channel == n163s[i]) {
                return enables[i];
            }
        }
        return false;
    }

    @Override
    public void beforeRender() {
        super.beforeRender();
        for (int i = 0; i < enables.length; i++) {
            if (!enables[i]) {
                continue;
            }
            AbstractXgmAudioChannel ch = n163s[i];
            ch.beforeSubmit();
        }
    }

    @Override
    public int render(int index) {
        float sum = 0;
        int count = 0;
        for (int i = 0; i < n163s.length; i++) {
            XgmAudioChannel ch = n163s[i];
            if (ch == null || !enables[i]) {
                continue;
            }
            sum += (ch.read(index) * ch.getLevel());
            count++;
        }

        if (count == 0) {
            return 0;
        }

        // The original multiplication values here are:
        // 256 / 1
        // 256 / 2
        // 256 / 3
        // 256 / 4
        // 256 / 5
        // 256 / 6
        // Since the sound is more explosive when there is only one track,
        // the track sound is also corrected here
        switch (count) {
            case 1:
                sum *= 100;
                break;
            case 2:
                sum *= 85;
                break;
            case 3:
                sum *= 72;
                break;
            case 4:
                sum *= 64;
                break;
            case 5:
                sum *= 56;
                break;
            default:
                sum *= 50;
                break;
        }

        // The following comments are from the source program NsfPlayer:
        // when approximating the serial multiplex as a straight mix, once the
        // multiplex frequency gets below the nyquist frequency an average mix
        // begins to sound too quiet. To approximate this effect, I don't attenuate
        // any further after 6 channels are active.
        // If the number of tracks is greater than 6, dividing the volume by half will make
        // each track ridiculously quiet, so dividing by 6 will not make it any quieter.

        // 8 bit approximation of master volume
        // max N163 vol vs max APU square
        // unfortunately, games have been measured as low as 3.4x and as high as 8.5x
        // with higher volumes on Erika, King of Kings, and Rolling Thunder
        // and lower volumes on others. Using 5.2x as a rough "one size fits all".
        // According to the measurement results, the volume of N163 is very unstable in
        // different games, ranging from [3.4, 8.5] times of the APU rectangular pulse wave sound.
        // Here we decided to use 4.5 times.

        // In addition, the source code uses 6x, but I want it to be compatible with FTM.
        // I played FTM at 6x and found that it was still too loud, so I lowered the volume further to 4.5.

        final double MASTER_VOL = 5503.5; // 4.5 * 1223.0
        final double MAX_OUT = 57600; // max digital value: = 15.0 * 15.0 * 256.0

        int v = (int) ((MASTER_VOL / MAX_OUT) * sum);
        v = intercept(v, 1);
        return v;
    }
}
