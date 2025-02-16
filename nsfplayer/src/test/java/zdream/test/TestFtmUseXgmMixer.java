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
import zdream.nsfplayer.ftm.renderer.FamiTrackerConfig;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.utils.common.BytesPlayer;


/**
 * <p>This use case shows how to use the Xgm mixer to play FTM audio
 * <p>(Xgm is the default mixer)
 * </p>
 *
 * @author Zdream
 * @since v0.2.6-test
 */
@PropsEntity(url = "file:local.properties")
public class TestFtmUseXgmMixer {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "ftm")
    String path = "src/test/resources/assets/mm10nsf.ftm";

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
        FtmAudio audio = factory.create(path);

        FamiTrackerConfig c = new FamiTrackerConfig();
        c.mixerConfig = new XgmMixerConfig();

        // Playing part
        FamiTrackerRenderer renderer = new FamiTrackerRenderer(c);
        renderer.ready(audio, 44);

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
