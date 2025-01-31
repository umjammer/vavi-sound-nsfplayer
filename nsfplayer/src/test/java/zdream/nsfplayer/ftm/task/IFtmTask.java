package zdream.nsfplayer.ftm.task;

import zdream.nsfplayer.ftm.FtmPlayerConsole;


/**
 * The main thread assigns tasks to the playback thread
 *
 * @author Zdream
 */
public interface IFtmTask {

    /**
     * Setting parameters
     *
     * @param key key
     * @param arg value
     */
    void setOption(String key, Object arg);

    /**
     * Execute the task
     *
     * @param env environment
     */
    void execute(FtmPlayerConsole env);
}
