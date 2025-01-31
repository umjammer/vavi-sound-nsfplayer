package zdream.nsfplayer.ftm.cmd;

import zdream.nsfplayer.ftm.FtmPlayerConsole;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.task.ChooseSongTask;
import zdream.nsfplayer.ftm.task.OpenType;


/**
 * <p>Processor on song switching
 *
 * <p>basic command:
 * <li>song &lt;song number,int&gt; switching tracks
 * <li>next: next song
 * <li>prev: previous song
 * </li>
 * </p>
 *
 * @author Zdream
 * @date 2017-09-25
 */
public class SongHandler implements ICommandHandler {

    public static final String
            CMD_CHOOSE_SONG = "song",
            CMD_NEXT_SONG = "next",
            CMD_PREV_SONG = "prev";

    @Override
    public String[] canHandle() {
        return new String[] {CMD_CHOOSE_SONG, CMD_NEXT_SONG, CMD_PREV_SONG};
    }

    @Override
    public void handle(String[] args, FtmPlayerConsole env) {
        String cmd = args[0];
        if (CMD_CHOOSE_SONG.equals(cmd)) {
            handleChoose(args, env);
        } else if (CMD_NEXT_SONG.equals(cmd)) {
            handleNext(args, env);
        } else if (CMD_PREV_SONG.equals(cmd)) {
            handlePrev(args, env);
        }
    }

    private void handleChoose(String[] args, FtmPlayerConsole env) {
        if (args.length != 2) {
            if (env.getType() == OpenType.FTM) {
                FtmAudio audio = (FtmAudio) env.getAudio();
                int song = env.getRenderer().getCurrentTrack();
                int size = audio.getTrackCount() - 1;
                env.printOut("[SONG] Use “song <track number>”. Switch the track. [ 0 - %d ]\n" +
                                "[SONG] The track %d [%s] is now playing.",
                        size, song, audio.getTrack(song).name);

                // List all tracks
                env.printOut("       ----  --------");
                for (int i = 0; i <= size; i++) {
                    env.printOut("       [%2d]  %s",
                            i, audio.getTrack(i).name);
                }
                env.printOut("       ----  --------");
            } else {
                env.printOut("[SONG] Use “song <track number>”. Switch the track. [ 0 - %d ]\n" +
                                "[SONG] Now play track %d.",
                        env.getAudio().getTrackCount() - 1, env.getRenderer().getCurrentTrack());
            }
            return;
        }

        chooseSong(Integer.parseInt(args[1]), env);
    }

    private void handleNext(String[] args, FtmPlayerConsole env) {
        chooseSong(env.getRenderer().getCurrentTrack() + 1, env);
    }

    private void handlePrev(String[] args, FtmPlayerConsole env) {
        int song = env.getRenderer().getCurrentTrack() - 1;
        if (song < 0) {
            song = env.getAudio().getTrackCount() - 1;
        }
        chooseSong(song, env);
    }

    private void chooseSong(int song, FtmPlayerConsole env) {
        ChooseSongTask t = new ChooseSongTask(song);

        t.setOption("needReset", true);
        env.putTask(t);
    }

}
