package zdream.test;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    String path = "test\\assets\\test\\mm10nsf.ftm";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
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
