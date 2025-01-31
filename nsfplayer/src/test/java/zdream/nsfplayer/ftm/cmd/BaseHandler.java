package zdream.nsfplayer.ftm.cmd;

import zdream.nsfplayer.ftm.FtmPlayerConsole;
import zdream.nsfplayer.ftm.task.OpenTask;
import zdream.nsfplayer.ftm.task.PauseTask;
import zdream.nsfplayer.ftm.task.PlayTask;


/**
 * <p>Basic command processor
 *
 * <p>
 * <b>open</b> command:
 * <li><code>open [filePath]</code>
 * <br>Open a file.
 * <li><code>open [filePath] --beginSong [song]</code>
 * <li><code>open [filePath] -s [song]</code>
 * <br>Open a file and specify the track number to start playing
 * <li><code>open [filePath] --format txt</code>
 * <li><code>open [filePath] -fm txt</code>
 * <br>Open a txt file
 * </li>
 * <br>Note: The above options can be used in combination
 * </p>
 *
 * <p>
 * <b>pause command:
 * <br>pause
 * </p>
 *
 * <p>
 * <b>play</b> command:
 * <br>Play after Pause
 * </p>
 *
 * @author Zdream
 * @version v0.2.5-test
 * <br>Added support for opening txt files
 * @date 2017-09-23
 * @since v0.2
 */
public class BaseHandler implements ICommandHandler {

    public static final String
            CMD_OPEN = "open",
            CMD_PAUSE = "pause",
            CMD_PLAY = "play";

    @Override
    public String[] canHandle() {
        return new String[] {CMD_OPEN, CMD_PAUSE, CMD_PLAY};
    }

    @Override
    public void handle(String[] args, FtmPlayerConsole env) {
        String cmd = args[0];
        if (CMD_OPEN.equals(cmd)) {
            handleOpen(args, env);
        } else if (CMD_PAUSE.equals(cmd)) {
            handlePause(args, env);
        } else if (CMD_PLAY.equals(cmd)) {
            handlePlay(args, env);
        }
    }

    private void handleOpen(String[] args, FtmPlayerConsole env) {
        if (args.length < 2) {
            return;
        }

        OpenTask t = new OpenTask(args[1]);

A:
        {
            if (args.length == 2) {
                break A;
            }

            for (int i = 2; i < args.length; ) {
                if ("-s".equals(args[i]) || "--beginSong".equals(args[i])) {
                    t.setOption("beginSong", Integer.valueOf(args[i + 1]));
                    i += 2;
                } else if ("-fm".equals(args[i]) || "--format".equals(args[i])) {
                    t.setOption("format", args[i + 1]);
                    i += 2;
                } else {
                    i++;
                }
            }
        }

        env.putTask(t);
    }

    private static void handlePause(String[] args, FtmPlayerConsole env) {
        env.putTask(new PauseTask());
    }

    private static void handlePlay(String[] args, FtmPlayerConsole env) {
        PlayTask t = PlayTask.getOne();
        t.setOption(PlayTask.OPT_REPLAY, true);
        env.putTask(t);
    }
}
