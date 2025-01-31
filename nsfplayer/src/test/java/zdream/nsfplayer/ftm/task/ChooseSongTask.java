package zdream.nsfplayer.ftm.task;

import zdream.nsfplayer.core.AbstractNsfAudio;
import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.ftm.FtmPlayerConsole;
import zdream.nsfplayer.ftm.audio.FtmAudio;


/**
 * The task of initiating the song switching action
 *
 * @author Zdream
 * @date 2017-09-22
 */
public class ChooseSongTask implements IFtmTask {

    int song;

    /**
     * Do you need to reset the player parameters when switching songs?<br>
     * Now generally speaking, if a music file has just been opened and has not yet been played
     * (OpenTask has just been completed), this value is the default value false;
     * If it has already been played, you need to reset it. In this case, please set
     * this parameter to true.
     */
    boolean needReset;

    public ChooseSongTask() {
    }

    public ChooseSongTask(int song) {
        super();
        this.song = song;
    }

    @Override
    public void setOption(String key, Object arg) {
        if ("song".equals(key)) {
            song = (Integer) arg;
        } else if ("needReset".equals(key)) {
            needReset = (Boolean) arg;
        }
    }

    @Override
    public void execute(FtmPlayerConsole env) {
        AbstractNsfRenderer<?> renderer = env.getRenderer();
        AbstractNsfAudio audio = env.getAudio();

        if (song < 0) {
            song = 0;
        } else if (song >= audio.getTrackCount()) {
            song = song % audio.getTrackCount();
        }

        renderer.ready(song);

        // Output prompt text
        if (env.getType() == OpenType.FTM) {
            env.printOut("[SONG] Switch to song: %d [%s]", song, ((FtmAudio) audio).getTrack(song).name);
        } else {
            env.printOut("[SONG] Switch to song: %d", song);
        }

        // Add the playback task
        env.putTask(PlayTask.getOne());
    }
}
