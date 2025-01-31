package zdream.nsfplayer.ftm.cmd;

import zdream.nsfplayer.ftm.FtmPlayerConsole;
import zdream.nsfplayer.ftm.task.SpeedTask;


/**
 * <p>About the processor for playback
 *
 * <p>
 * speed command:
 * <li><code>speed [s]</code>
 * <br>Change the speed. s Floating point number, range [0.1, 10], default 1
 * <li><code>speed --reset</code>
 * <br>Reset playback speed to 1
 * <li><code>speed</code>
 * <br>View current playback speed
 * </li>
 * <p>
 *
 * @author Zdream
 * @since v0.2.9-test
 */
public class PlayHandler implements ICommandHandler {

    public static final String CMD_SPEED = "speed";

    public PlayHandler() {
    }

    @Override
    public String[] canHandle() {
        return new String[] {CMD_SPEED};
    }

    @Override
    public void handle(String[] args, FtmPlayerConsole env) {
        String cmd = args[0];
        if (CMD_SPEED.equals(cmd)) {
            handleSpeed(args, env);
        }
    }

    private void handleSpeed(String[] args, FtmPlayerConsole env) {
        if (args.length == 1) {
            // View current playback speed
            env.printOut("[SPEED] The current playback speed is %f", env.getRenderer().getSpeed());
            env.printOut("[SPEED] Use speed [s] to set the playback speed");
        } else {
            SpeedTask t = new SpeedTask();
            if ("--reset".equals(args[1])) {
                // Reset playback speed
                t.setOption("speed", 1.0f);
            } else {
                // Setting the playback speed
                float speed = Float.parseFloat(args[1]);
                t.setOption("speed", speed);
            }
            env.putTask(t);
        }
    }
}
