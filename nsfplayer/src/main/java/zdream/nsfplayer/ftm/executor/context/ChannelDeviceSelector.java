package zdream.nsfplayer.ftm.executor.context;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.channel.Channel2A03Pulse;
import zdream.nsfplayer.ftm.executor.channel.ChannelDPCM;
import zdream.nsfplayer.ftm.executor.channel.ChannelFDS;
import zdream.nsfplayer.ftm.executor.channel.ChannelMMC5Pulse;
import zdream.nsfplayer.ftm.executor.channel.ChannelN163;
import zdream.nsfplayer.ftm.executor.channel.ChannelNoise;
import zdream.nsfplayer.ftm.executor.channel.ChannelTriangle;
import zdream.nsfplayer.ftm.executor.channel.ChannelVRC6Pulse;
import zdream.nsfplayer.ftm.executor.channel.ChannelVRC6Sawtooth;
import zdream.nsfplayer.ftm.executor.channel.ChannelVRC7;
import zdream.nsfplayer.ftm.executor.channel.EmptyFtmChannel;
import zdream.nsfplayer.sound.vrc7.OPLL;


/**
 * <p>Multi-orbital environment memory, and orbital equipment selection, generation tools
 * <p>Originally named <code>zdream.nsfplayer.ftm.renderer.channel.ChannalFactory</code>
 * Later it was realized that for each different track, not only the Ftm track,
 * but also the vocal and audio tracks were different, so it was enhanced to select various
 * devices and classes for each different track.
 * <p>For the VRC7 track, where the same environment data set (OPLL) is shared among several tracks,
 * a separate place is needed to store the common environment data. So this is the place to go
 * </p>
 *
 * @author Zdream
 * @version v0.2.7
 * Transforms the class from a mere selection tool for channel equipment to a memory for the environment.
 * The methods used in it have also been changed from static to non-static
 * @since 0.2.1
 */
public class ChannelDeviceSelector implements INsfChannelCode, IResetable {

    /**
     * Establishment of individual tracks
     *
     * @param code channel number
     * @return
     */
    public AbstractFtmChannel selectFtmChannel(byte code) {
        return switch (code) {
            // 2A03
            case CHANNEL_2A03_PULSE1 -> {
                Channel2A03Pulse s = new Channel2A03Pulse(true);
                yield s;
            }
            case CHANNEL_2A03_PULSE2 -> {
                Channel2A03Pulse s = new Channel2A03Pulse(false);
                yield s;
            }
            case CHANNEL_2A03_TRIANGLE -> {
                ChannelTriangle s = new ChannelTriangle();
                yield s;
            }
            case CHANNEL_2A03_NOISE -> {
                ChannelNoise s = new ChannelNoise();
                yield s;
            }
            case CHANNEL_2A03_DPCM -> {
                ChannelDPCM s = new ChannelDPCM();
                yield s;
            }

            // VRC6
            case CHANNEL_VRC6_PULSE1 -> new ChannelVRC6Pulse(true);
            case CHANNEL_VRC6_PULSE2 -> new ChannelVRC6Pulse(false);
            case CHANNEL_VRC6_SAWTOOTH -> new ChannelVRC6Sawtooth();

            // MMC5
            case CHANNEL_MMC5_PULSE1 -> new ChannelMMC5Pulse(true);
            case CHANNEL_MMC5_PULSE2 -> new ChannelMMC5Pulse(false);

            // FDS
            case CHANNEL_FDS -> new ChannelFDS();

            // N163
            case CHANNEL_N163_1 -> new ChannelN163(0);
            case CHANNEL_N163_2 -> new ChannelN163(1);
            case CHANNEL_N163_3 -> new ChannelN163(2);
            case CHANNEL_N163_4 -> new ChannelN163(3);
            case CHANNEL_N163_5 -> new ChannelN163(4);
            case CHANNEL_N163_6 -> new ChannelN163(5);
            case CHANNEL_N163_7 -> new ChannelN163(6);
            case CHANNEL_N163_8 -> new ChannelN163(7);

            // VRC7
            case CHANNEL_VRC7_FM1 -> createVRC7Channel(0);
            case CHANNEL_VRC7_FM2 -> createVRC7Channel(1);
            case CHANNEL_VRC7_FM3 -> createVRC7Channel(2);
            case CHANNEL_VRC7_FM4 -> createVRC7Channel(3);
            case CHANNEL_VRC7_FM5 -> createVRC7Channel(4);
            case CHANNEL_VRC7_FM6 -> createVRC7Channel(5);
            default -> new EmptyFtmChannel(code);
        };

    }

    /*
     * Public method
     */

    @Override
    public void reset() {
        opll = null;

    }

    //
    // matrix
    //

    OPLL opll;

    public OPLL getOpll() {
        return opll;
    }

    private ChannelVRC7 createVRC7Channel(int index) {
        if (opll == null) {
            opll = new OPLL();
        }

        return new ChannelVRC7(index, opll);
    }
}
