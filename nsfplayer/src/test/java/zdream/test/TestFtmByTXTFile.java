package zdream.test;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;
import zdream.utils.common.BytesPlayer;


/**
 * <p>This use case shows how to read a txt file to play audio
 * </p>
 *
 * @author Zdream
 * @since v0.2.6-test
 */
@PropsEntity(url = "file:local.properties")
public class TestFtmByTXTFile {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "txt")
    String path = "src/test/resources/assets/Editor_05.txt";
//				"src/test/resources/assets/mm10nsf.txt"

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    static final boolean onIde = System.getProperty("vavi.test", "").equals("ide");
    static final long time = onIde ? 3600 : 15;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
Debug.println("volume: " + volume);
    }

    @Test
    void test1() throws Exception {
        FtmAudioFactory factory = new FtmAudioFactory();
        FtmAudio audio = factory.createFromTextPath(path);

        // Playing part
        FamiTrackerRenderer renderer = new FamiTrackerRenderer();
        renderer.ready(audio);

        BytesPlayer player = new BytesPlayer();
        player.setVolume(volume);
        byte[] bs = new byte[2400];

        for (int i = 0; i < time; i++) {
            int size = renderer.render(bs, 0, 2400);
            player.writeSamples(bs, 0, size);
            if (renderer.isFinished()) {
                break;
            }
        }
    }
}
