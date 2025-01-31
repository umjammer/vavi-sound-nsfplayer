package zdream.nsfplayer.ftm.cmd;

import java.util.ArrayList;
import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.FtmPlayerConsole;


/**
 * <p>channel related command processor
 *
 * <p>
 * mute command:
 * <li><code>mute [channel]</code>
 * <br>Close a channel. channel can be a channel number (hexadecimal) or name
 * <li><code>mute -c [channel]</code>
 * <br>Open a channel. channel can be a channel number (in hexadecimal) or a name.
 * <li><code>mute -c</code>
 * <br>Open all channels.
 * </li>
 * <p>
 * volume command:
 * <li><code>volume</code>
 * <br>View the volume of all channels.
 * <li><code>volume [channel]</code>
 * <br>Check the volume of a channel. channel can be a channel number (hexadecimal) or name
 * <li><code>volume [channel] [level]</code>
 * <br>Set the volume of a channel. channel can be a channel number (in hexadecimal) or a name
 * <br>level is the volume, in the range [0, 1]
 * <li><code>volume -reset</code>
 * <br>Reset all channel volumes to 1.
 * <li><code>volume -set [level]</code>
 * <br>Sets the volume of all channels to level.
 * <br>level is the volume, in the range [0, 1]
 * </li>
 * </p>
 *
 * @author Zdream
 * @since v0.2.3-test
 */
public class ChannelHandler implements ICommandHandler, INsfChannelCode {

    public static final String
            CMD_MUTE = "mute",
            CMD_VOLUME = "volume";

    public ChannelHandler() {

    }

    @Override
    public String[] canHandle() {
        return new String[] {CMD_MUTE, CMD_VOLUME};
    }

    @Override
    public void handle(String[] args, FtmPlayerConsole env) {
        String cmd = args[0];
        if (CMD_MUTE.equals(cmd)) {
            handleMute(args, env);
        } else if (CMD_VOLUME.equals(cmd)) {
            handleVolume(args, env);
        }
    }

    private void handleMute(String[] args, FtmPlayerConsole env) {
        if ("-c".equals(args[1])) {
            if (args.length == 2) {
                muteClearAll(env);
            } else {
                byte channelCode = parseChannelCode(args[2].toLowerCase());
                muteClear(env, channelCode);
            }
        } else {
            byte channelCode = parseChannelCode(args[1].toLowerCase());
            mute(env, channelCode);
        }
    }

    private void handleVolume(String[] args, FtmPlayerConsole env) {
        if (args.length == 1) {
            volumePrintAll(env);
        } else if ("-reset".equals(args[1])) {
            volumeResetAll(env);
        } else if ("-set".equals(args[1])) {
            float vol = Float.parseFloat(args[2]);
            volumeSetAll(env, vol);
        } else {
            if (args.length == 2) {
                byte channelCode = parseChannelCode(args[1].toLowerCase());
                volumePrint(env, channelCode);
            } else {
                byte channelCode = parseChannelCode(args[1].toLowerCase());
                float vol = Float.parseFloat(args[2]);
                volumeSet(env, channelCode, vol);
            }
        }
    }

    /**
     * Open All channels
     */
    private void muteClearAll(FtmPlayerConsole env) {
        AbstractNsfRenderer<?> renderer = env.getRenderer();
        Set<Byte> bs = renderer.allChannelSet();

        bs.forEach(code -> renderer.setChannelMuted(code, false));
    }

    /**
     * Open a single channel
     */
    private void muteClear(FtmPlayerConsole env, byte channelCode) {
        AbstractNsfRenderer<?> renderer = env.getRenderer();
        renderer.setChannelMuted(channelCode, false);
    }

    /**
     * Turn off a single channel
     */
    private void mute(FtmPlayerConsole env, byte channelCode) {
        AbstractNsfRenderer<?> renderer = env.getRenderer();
        renderer.setChannelMuted(channelCode, true);
    }

    /**
     * View the volume of all channels
     */
    private void volumePrintAll(FtmPlayerConsole env) {
        env.printOut("[VOLUME] Lists the volume of all channels below\n     ---------- ---- ---");

        AbstractNsfRenderer<?> renderer = env.getRenderer();
        ArrayList<Byte> bs = new ArrayList<>(renderer.allChannelSet());
        bs.sort(null);

        for (byte channelCode : bs) {
            float vol = renderer.getLevel(channelCode);
            if (renderer.isChannelMuted(channelCode)) {
                vol = 0;
            }

            env.printOut("%15s [%2s] %.1f",
                    toStringChannelCode(channelCode),
                    Integer.toHexString(channelCode),
                    vol);

        }
        env.printOut("     ---------- ---- ---\nUse volume [channel] [level] to set the volume of a single channel.");
    }

    /**
     * Reset all channel volumes
     */
    private void volumeResetAll(FtmPlayerConsole env) {
        AbstractNsfRenderer<?> renderer = env.getRenderer();
        Set<Byte> bs = renderer.allChannelSet();

        bs.forEach(code -> {
            renderer.setChannelMuted(code, false);
            renderer.setLevel(code, 1.0f);
        });
    }

    /**
     * Set the volume of all channels
     */
    private void volumeSetAll(FtmPlayerConsole env, float vol) {
        AbstractNsfRenderer<?> renderer = env.getRenderer();
        Set<Byte> bs = renderer.allChannelSet();

        bs.forEach(code -> {
            renderer.setChannelMuted(code, false);
            renderer.setLevel(code, vol);
        });
    }

    /**
     * View the volume of a single channel
     */
    private void volumePrint(FtmPlayerConsole env, byte channelCode) {
        AbstractNsfRenderer<?> renderer = env.getRenderer();
        float vol = renderer.getLevel(channelCode);
        if (renderer.isChannelMuted(channelCode)) {
            vol = 0;
        }

        env.printOut("[VOLUME] channel %s [%2s] has a volume of %.1f\n" +
                        "Use volume [channel] [level] to set the volume of a single channel",
                toStringChannelCode(channelCode),
                Integer.toHexString(channelCode),
                vol);
    }

    /**
     * Set the volume of a single channel
     */
    private void volumeSet(FtmPlayerConsole env, byte channelCode, float vol) {
        AbstractNsfRenderer<?> renderer = env.getRenderer();

        renderer.setChannelMuted(channelCode, false);
        renderer.setLevel(channelCode, vol);
    }

    private static byte parseChannelCode(String c) {
        return switch (c) {
            // 2A03
            case "pulse1", "p1", "1", "01" -> CHANNEL_2A03_PULSE1;
            case "pulse2", "p2", "2", "02" -> CHANNEL_2A03_PULSE2;
            case "triangle", "tri", "3", "03" -> CHANNEL_2A03_TRIANGLE;
            case "noise", "n", "4", "04" -> CHANNEL_2A03_NOISE;
            case "dpcm", "d", "5", "05" -> CHANNEL_2A03_DPCM;

            // VRC6
            case "vrc6pulse1", "vrc6p1", "11" -> CHANNEL_VRC6_PULSE1;
            case "vrc6pulse2", "vrc6p2", "12" -> CHANNEL_VRC6_PULSE2;
            case "vrc6sawtooth", "vrc6s", "13" -> CHANNEL_VRC6_SAWTOOTH;

            // MMC5
            case "mmc5pulse1", "mmc5p1", "41" -> CHANNEL_MMC5_PULSE1;
            case "mmc5pulse2", "mmc5p2", "42" -> CHANNEL_MMC5_PULSE2;

            // FDS
            case "fds", "31" -> CHANNEL_FDS;

            // N163
            case "namco1", "n163_1", "51" -> CHANNEL_N163_1;
            case "namco2", "n163_2", "52" -> CHANNEL_N163_2;
            case "namco3", "n163_3", "53" -> CHANNEL_N163_3;
            case "namco4", "n163_4", "54" -> CHANNEL_N163_4;
            case "namco5", "n163_5", "55" -> CHANNEL_N163_5;
            case "namco6", "n163_6", "56" -> CHANNEL_N163_6;
            case "namco7", "n163_7", "57" -> CHANNEL_N163_7;
            case "namco8", "n163_8", "58" -> CHANNEL_N163_8;

            // VRC7
            case "fmchannel1", "fm1", "vrc7_1", "21" -> CHANNEL_VRC7_FM1;
            case "fmchannel2", "fm2", "vrc7_2", "22" -> CHANNEL_VRC7_FM2;
            case "fmchannel3", "fm3", "vrc7_3", "23" -> CHANNEL_VRC7_FM3;
            case "fmchannel4", "fm4", "vrc7_4", "24" -> CHANNEL_VRC7_FM4;
            case "fmchannel5", "fm5", "vrc7_5", "25" -> CHANNEL_VRC7_FM5;
            case "fmchannel6", "fm6", "vrc7_6", "26" -> CHANNEL_VRC7_FM6;

            // S5B
            case "s5bsquare1", "s5b1", "61" -> CHANNEL_S5B_SQUARE1;
            case "s5bsquare2", "s5b2", "62" -> CHANNEL_S5B_SQUARE2;
            case "s5bsquare3", "s5b3", "63" -> CHANNEL_S5B_SQUARE3;
            default -> throw new IllegalArgumentException("Unable to resolve channel number: " + c);
        };


    }

    private String toStringChannelCode(byte channelCode) {
        return switch (channelCode) {
            // 2A03
            case CHANNEL_2A03_PULSE1 -> "Pulse1";
            case CHANNEL_2A03_PULSE2 -> "Pulse2";
            case CHANNEL_2A03_TRIANGLE -> "Triangle";
            case CHANNEL_2A03_NOISE -> "Noise";
            case CHANNEL_2A03_DPCM -> "DPCM";

            // VRC6
            case CHANNEL_VRC6_PULSE1 -> "VRC6Pulse1";
            case CHANNEL_VRC6_PULSE2 -> "VRC6Pulse2";
            case CHANNEL_VRC6_SAWTOOTH -> "VRC6Sawtooth";

            // MMC5
            case CHANNEL_MMC5_PULSE1 -> "MMC5Pulse1";
            case CHANNEL_MMC5_PULSE2 -> "MMC5Pulse2";

            // FDS
            case CHANNEL_FDS -> "FDS";

            // N163
            case CHANNEL_N163_1 -> "Namco1";
            case CHANNEL_N163_2 -> "Namco2";
            case CHANNEL_N163_3 -> "Namco3";
            case CHANNEL_N163_4 -> "Namco4";
            case CHANNEL_N163_5 -> "Namco5";
            case CHANNEL_N163_6 -> "Namco6";
            case CHANNEL_N163_7 -> "Namco7";
            case CHANNEL_N163_8 -> "Namco8";

            // VRC7
            case CHANNEL_VRC7_FM1 -> "FMChannel1";
            case CHANNEL_VRC7_FM2 -> "FMChannel2";
            case CHANNEL_VRC7_FM3 -> "FMChannel3";
            case CHANNEL_VRC7_FM4 -> "FMChannel4";
            case CHANNEL_VRC7_FM5 -> "FMChannel5";
            case CHANNEL_VRC7_FM6 -> "FMChannel6";

            // S5B
            case CHANNEL_S5B_SQUARE1 -> "S5BSquare1";
            case CHANNEL_S5B_SQUARE2 -> "S5BSquare2";
            case CHANNEL_S5B_SQUARE3 -> "S5BSquare3";
            default -> "";
        };
    }
}
