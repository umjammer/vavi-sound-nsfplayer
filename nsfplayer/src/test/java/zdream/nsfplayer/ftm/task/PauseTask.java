package zdream.nsfplayer.ftm.task;

import zdream.nsfplayer.ftm.FtmPlayerConsole;


/**
 * Pause playback task
 *
 * @author Zdream
 * @date 2017-09-23
 */
public class PauseTask implements IFtmTask {

    @Override
    public void setOption(String key, Object arg) {

    }

    @Override
    public void execute(FtmPlayerConsole env) {
        env.clearTask(); // Just clear the PlayTask
    }
}
