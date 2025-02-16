package zdream.test;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.audio.NsfAudioFactory;
import zdream.nsfplayer.nsf.renderer.NsfRenderer;
import zdream.utils.common.BytesPlayer;


@PropsEntity(url = "file:local.properties")
public class TestNsf {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "nsf")
    String in = "src/test/resources/assets/test/mm10nsf.nsf";

    @Property(name = "track")
    int track = 0;

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
Debug.println("volume: " + volume);
    }

    static final boolean onIde = System.getProperty("vavi.test", "").equals("ide");
    static final long time = onIde ? 3600 : 15;

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    public void test1() throws Exception {
Debug.println(in);
        NsfAudioFactory factory = new NsfAudioFactory();
        NsfAudio nsf = factory.createFromFile(in);

        NsfRenderer renderer = new NsfRenderer();
        renderer.ready(nsf, track);

        BytesPlayer player = new BytesPlayer();
        player.setVolume(volume);
        byte[] bs = new byte[1600];

        for (int i = 0; i < time; i++) {
            int len = renderer.renderOneFrame(bs, 0, bs.length);
            player.writeSamples(bs, 0, len);
        }
    }
}
