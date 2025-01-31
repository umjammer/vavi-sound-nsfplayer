package zdream.test;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @Property(name = "nsf")
    String path = "test/assets/test/Editor_05.txt";
//				"test/assets/test/mm10nsf.txt"

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test1() throws Exception {
        FtmAudioFactory factory = new FtmAudioFactory();
        FtmAudio audio = factory.createFromTextPath(path);

        // Playing part
        FamiTrackerRenderer renderer = new FamiTrackerRenderer();
        renderer.ready(audio);

        BytesPlayer player = new BytesPlayer();
        byte[] bs = new byte[2400];

        for (int i = 0; i < 3600; i++) {
            int size = renderer.render(bs, 0, 2400);
            player.writeSamples(bs, 0, size);
            if (renderer.isFinished()) {
                break;
            }
        }
    }
}
