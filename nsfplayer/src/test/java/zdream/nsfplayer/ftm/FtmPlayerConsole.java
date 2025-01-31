package zdream.nsfplayer.ftm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import zdream.nsfplayer.core.AbstractNsfAudio;
import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.cmd.BaseHandler;
import zdream.nsfplayer.ftm.cmd.ChannelHandler;
import zdream.nsfplayer.ftm.cmd.ICommandHandler;
import zdream.nsfplayer.ftm.cmd.PlayHandler;
import zdream.nsfplayer.ftm.cmd.SongHandler;
import zdream.nsfplayer.ftm.renderer.FamiTrackerConfig;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;
import zdream.nsfplayer.ftm.task.ChooseSongTask;
import zdream.nsfplayer.ftm.task.IFtmTask;
import zdream.nsfplayer.ftm.task.OpenTask;
import zdream.nsfplayer.ftm.task.OpenType;
import zdream.nsfplayer.ftm.task.PauseTask;
import zdream.nsfplayer.ftm.task.PlayTask;
import zdream.nsfplayer.mixer.IMixerConfig;
import zdream.nsfplayer.mixer.blip.BlipMixerConfig;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.renderer.NsfRenderer;
import zdream.nsfplayer.nsf.renderer.NsfRendererConfig;
import zdream.utils.common.BytesPlayer;
import zdream.utils.common.CodeSpliter;


/**
 * <p>Control panel for testing audio playback of FamiTracker files
 * <p>Although it uses the command line / similar to Shell
 * </p>
 *
 * @author Zdream
 * @version v0.2.8-test
 * Added support for NSF
 * @since v0.2.3-test
 */
public class FtmPlayerConsole {

    // FTM
    final FamiTrackerRenderer ftmRenderer;
    FtmAudio ftm;
    // NSF
    final NsfRenderer nsfRenderer;
    NsfAudio nsf;

    // Player
    OpenType type = OpenType.FTM;
    final BytesPlayer player;

    PlayThreadForFtm thread;

    // cache
    private short[] samples;

    // Instruction parsing
    final Map<String, ICommandHandler> handlers = new HashMap<>();

    public FtmPlayerConsole() {
        this(new Properties());
    }

    public FtmPlayerConsole(Properties prop) {
        String mixerProp = prop.getProperty("mixer");
        IMixerConfig c;
        if (mixerProp != null && mixerProp.equals("blip")) {
            c = new BlipMixerConfig();
        } else {
            c = new XgmMixerConfig();
            ((XgmMixerConfig) c).channelType = XgmMixerConfig.TYPE_MULTI;
        }

        FamiTrackerConfig config1 = new FamiTrackerConfig();
        config1.mixerConfig = c;
        ftmRenderer = new FamiTrackerRenderer(config1);

        NsfRendererConfig config2 = new NsfRendererConfig();
        config2.mixerConfig = c;
        nsfRenderer = new NsfRenderer(config2);
        player = new BytesPlayer();

        attachHandler(new BaseHandler());
        attachHandler(new SongHandler());
        attachHandler(new ChannelHandler());
        attachHandler(new PlayHandler());
    }

    public FamiTrackerRenderer getFamiTrackerRenderer() {
        return ftmRenderer;
    }

    public NsfRenderer getNsfRenderer() {
        return nsfRenderer;
    }

    public AbstractNsfRenderer<?> getRenderer() {
        return (type == OpenType.FTM) ? ftmRenderer : nsfRenderer;
    }

    public FtmAudio getFtmAudio() {
        return ftm;
    }

    public NsfAudio getNsfAudio() {
        return nsf;
    }

    public AbstractNsfAudio getAudio() {
        return (type == OpenType.FTM) ? ftm : nsf;
    }

    public void setFtmAudio(FtmAudio audio) {
        this.ftm = audio;
        this.type = OpenType.FTM;
    }

    public void setNsfAudio(NsfAudio audio) {
        this.nsf = audio;
        this.type = OpenType.NSF;
    }

    public void setAudio(AbstractNsfAudio audio) {
        if (audio instanceof FtmAudio) {
            setFtmAudio((FtmAudio) audio);
        } else if (audio instanceof NsfAudio) {
            setNsfAudio((NsfAudio) audio);
        }
    }

    public OpenType getType() {
        return type;
    }

    public void setType(OpenType type) {
        this.type = type;
    }

    public void attachHandler(ICommandHandler h) {
        String[] cmds = h.canHandle();
        for (String cmd : cmds) {
            handlers.put(cmd, h);
        }
    }

    /**
     * Start the player thread, which listens for input events
     */
    public void go() {
        this.thread = new PlayThreadForFtm(this);
        OpenTask t = new OpenTask("test\\assets\\test\\mm10nsf.ftm");
        t.setOption("beginSong", 8);
        putTask(t);

        Thread thread = new Thread(this.thread, "player");
        thread.setDaemon(true); // The playback process is a daemon process
        thread.start();

        Scanner scan = new Scanner(System.in);
        String text;
        boolean exits = false;

        while (!exits) {
            text = scan.nextLine();

            String[] args = CodeSpliter.split(text);
            if (args.length < 1) {
                continue;
            }

            try {
                String cmd = args[0] = args[0].toLowerCase();
                ICommandHandler h = handlers.get(cmd);
                if (h != null) {
                    h.handle(args, this);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        // Generally I won't come here
        scan.close();
    }

    boolean audio_print = false;

    /**
     * Opens NSF files, but does not play them.<br>
     *
     * @param fn
     * @throws IOException
     */
    public void open(String fn) throws IOException {
        OpenTask t = new OpenTask(fn);
        putTask(t);
    }

    /**
     * Loop the playback of the opened NSF file.<br>
     * If the <code>open()</code> method has not been used successfully before playing,
     * this method will fail;<br>
     *
     * @throws IOException
     */
    public void play() {
        IFtmTask t = PlayTask.getOne();
        putTask(t);
    }

    /**
     * Pause playback
     */
    public void pause() {
        putTask(new PauseTask());
    }

    /**
     * Switch to track
     */
    public void chooseSong(int song) {
        ChooseSongTask t = new ChooseSongTask(song);
        t.setOption("needReset", true);
        putTask(t);
    }

    public short[] getLastSampleBytes() {
        if (samples == null) {
            samples = new short[1200];
        }
        return samples;
    }

    public int writeSamples(int off, int len) {
        return player.writeSamples(samples, off, len);
    }

    public void putTask(IFtmTask task) {
        thread.putTask(task);
    }

    public IFtmTask nextTask() {
        return thread.nextTask();
    }

    public void clearTask() {
        thread.queue.clear();
    }

    public void printOut(String text, Object... args) {
        if (args == null || args.length == 0) {
            System.out.println(text);
        } else {
            System.out.println(String.format(text, args));
        }
    }
}
