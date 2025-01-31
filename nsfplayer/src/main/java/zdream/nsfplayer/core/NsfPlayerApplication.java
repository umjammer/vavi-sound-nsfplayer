package zdream.nsfplayer.core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FamiTrackerFormatException;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.mixer.factory.NsfSoundMixerFactory;


/**
 * <p>NsfPlayer Application
 * <p>Replace the original FamiTrackerApplication
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public class NsfPlayerApplication {

    public static final NsfPlayerApplication app;

    public static final Charset defCharset;

    static {
        defCharset = StandardCharsets.UTF_8;
        app = new NsfPlayerApplication();
    }

    public NsfPlayerApplication() {
        ftmFactory = new FtmAudioFactory();
        mixerFactory = new NsfSoundMixerFactory();
    }

    //
    // FTM
    //

    public final FtmAudioFactory ftmFactory;

    /**
     * Load FamiTracker (.ftm) file and generate {@link FtmAudio} instance
     *
     * @param filePath File Path
     */
    public FtmAudio open(String filePath) throws IOException, FamiTrackerFormatException {
        return ftmFactory.create(filePath);
    }

    /**
     * Load the text file (.txt) exported by FamiTracker and generate an instance of {@link FtmAudio}
     *
     * @param filePath File Path
     * @since v0.2.5
     */
    public FtmAudio openWithTxt(String filePath) throws IOException, FamiTrackerFormatException {
        return ftmFactory.createFromTextPath(filePath);
    }

    //
    // NSF
    //

    /*
     * Mixer
     */
    public final NsfSoundMixerFactory mixerFactory;
}
