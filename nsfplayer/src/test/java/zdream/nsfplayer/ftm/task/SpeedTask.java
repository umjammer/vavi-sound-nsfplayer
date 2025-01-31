package zdream.nsfplayer.ftm.task;

import zdream.nsfplayer.ftm.FtmPlayerConsole;


/**
 * Modify playback speed task
 *
 * @author Zdream
 * @since v0.2.10-test
 */
public class SpeedTask implements IFtmTask {

    float speed;

    @Override
    public void setOption(String key, Object arg) {
        if ("speed".equals(key)) {
            speed = ((Number) arg).floatValue();
        }
    }

    @Override
    public void execute(FtmPlayerConsole env) {
        env.getRenderer().setSpeed(speed);
    }
}
