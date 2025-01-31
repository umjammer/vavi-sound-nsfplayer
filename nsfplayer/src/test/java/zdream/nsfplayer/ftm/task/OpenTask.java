package zdream.nsfplayer.ftm.task;

import java.io.IOException;

import zdream.nsfplayer.core.NsfPlayerApplication;
import zdream.nsfplayer.ftm.FtmPlayerConsole;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.audio.NsfAudioFactory;

import static zdream.nsfplayer.ftm.task.OpenType.FTM;
import static zdream.nsfplayer.ftm.task.OpenType.NSF;
import static zdream.nsfplayer.ftm.task.OpenType.TXT;
import static zdream.nsfplayer.ftm.task.OpenType.UNKNOWED;


/**
 * Open File Task
 *
 * @author Zdream
 */
public class OpenTask implements IFtmTask {

    static final NsfAudioFactory nsfFactory = new NsfAudioFactory();

    String filename;
    int beginSong = -1;

    /**
     * Whether to open the file in txt format
     *
     * @since v0.2.5-test
     */
    OpenType type = UNKNOWED;

    public OpenTask() {
    }

    /**
     * By default, the song starts playing from song 0.
     *
     * @param filename
     */
    public OpenTask(String filename) {
        super();
        this.filename = filename;
    }

    @Override
    public void setOption(String key, Object arg) {
        if ("filename".equals(key) || "f".equals(key)) {
            filename = arg.toString();
        } else if ("beginSong".equals(key) || "song".equals(key)) {
            beginSong = (Integer) arg;
        } else if ("format".equals(key)) {
            switch (arg.toString().toLowerCase()) {
                case "txt":
                    type = TXT;
                    break;
                case "nsf":
                    type = NSF;
                    break;
                default:
                    type = FTM;
                    break;
            }
        }
    }

    @Override
    public void execute(FtmPlayerConsole env) {
        if (filename == null || filename.isEmpty()) {
            env.printOut("[OPEN] Use open [file] to read the file");
            return;
        }

        if (type == UNKNOWED) {
            if (filename.endsWith(".ftm")) {
                type = FTM;
            } else if (filename.endsWith(".nsf")) {
                type = NSF;
            } else if (filename.endsWith(".txt")) {
                type = TXT;
            } else { // Default ftm
                type = FTM;
            }
        }

        try {
            switch (type) {
                case FTM: {
                    FtmAudio audio = NsfPlayerApplication.app.open(filename);
                    env.setFtmAudio(audio);
                    env.getFamiTrackerRenderer().ready(audio);
                }
                break;

                case TXT: {
                    FtmAudio audio = NsfPlayerApplication.app.openWithTxt(filename);
                    env.setFtmAudio(audio);
                    env.getFamiTrackerRenderer().ready(audio);
                }
                break;

                case NSF: {
                    NsfAudio audio = nsfFactory.createFromFile(filename);
                    env.setNsfAudio(audio);
                    env.getNsfRenderer().ready(audio);
                }
                break;

                default:
                    break;
            }

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            env.printOut("[OPEN] Failed to read file: %s. Continue playing original audio", filename);
            return;
        }

        // Output prompt text
        env.printOut("[OPEN] Trying to open file: %s", filename);

        if (beginSong <= 0) {
            // Do not change the track number, directly put it into the playback task
            env.putTask(PlayTask.getOne());
        } else {
            // Change track number
            IFtmTask t = new ChooseSongTask(beginSong);
            t.setOption("needReset", true);
            env.putTask(t);
        }
    }
}
