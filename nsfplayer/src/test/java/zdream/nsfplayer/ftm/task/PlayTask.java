package zdream.nsfplayer.ftm.task;

import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.ftm.FtmPlayerConsole;


/**
 * Playback Tasks
 *
 * @author Zdream
 */
public class PlayTask implements IFtmTask {

    // Simulation Play Pool
    private static final PlayTask[] POOL = new PlayTask[5];
    private static int getPtr = 0;

    static {
        for (int i = 0; i < POOL.length; i++) {
            POOL[i] = new PlayTask();
        }
    }

    public static PlayTask getOne() {
        getPtr = (getPtr + 1) % POOL.length;
        PlayTask t = POOL[getPtr];
        return t;
    }

    public static final String OPT_REPLAY = "replay";

    private PlayTask() {
    }

    @Override
    public void setOption(String key, Object arg) {
    }

    @Override
    public void execute(FtmPlayerConsole env) {
        AbstractNsfRenderer<?> renderer = env.getRenderer();

        short[] bs = env.getLastSampleBytes();
        int size = renderer.render(bs, 0, bs.length);
        env.writeSamples(0, size);

        // When the song is over, switch to the next one
        if (renderer.isFinished()) {
            ChooseSongTask t = new ChooseSongTask(renderer.getCurrentTrack() + 1);
            t.setOption("needReset", true);
            env.putTask(t);
        }

        // Loop playback, so add another task
        IFtmTask t = env.nextTask();
        if (t == null || t.getClass() != getClass()) {
            PlayTask p = getOne();
            env.putTask(p);
        }
    }
}
