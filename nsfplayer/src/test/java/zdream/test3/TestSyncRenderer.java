package zdream.test3;

import java.io.IOException;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FamiTrackerFormatException;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.renderer.FamiTrackerConfig;
import zdream.nsfplayer.ftm.renderer.FamiTrackerSyncRenderer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.utils.common.BytesPlayer;


/**
 * This use case shows how to use FamiTrackerSyncRenderer to play multiple FTM audios simultaneously.
 *
 * @author Zdream
 * @since v0.3.1-test
 */
public class TestSyncRenderer {

    public static void main(String[] args) throws FamiTrackerFormatException, IOException {
        String path =
                "src/test/resources/assets/Hornet 2xVRC7.ftm";
        String path2 =
                "src/test/resources/assets/mm9nsf.ftm";

        FtmAudioFactory factory = new FtmAudioFactory();
        FtmAudio audio = factory.create(path);
        FtmAudio audio2 = factory.create(path2);

        // This section selects the mixer and can be deleted
        FamiTrackerConfig c = new FamiTrackerConfig();
        XgmMixerConfig cc = new XgmMixerConfig();
        cc.channelType = XgmMixerConfig.TYPE_SINGER;
        c.mixerConfig = cc;
        // Select Mixer END

        // Select the track you want to render
        FamiTrackerSyncRenderer renderer = new FamiTrackerSyncRenderer(c);
        renderer.allocate(audio, 0,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM1,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM2,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM3,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM4,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM5,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM6);
        renderer.allocate(audio, 1,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM1,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM2,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM3,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM4,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM5,
                FamiTrackerSyncRenderer.CHANNEL_VRC7_FM6);
        renderer.allocate(audio2, 12,
                FamiTrackerSyncRenderer.CHANNEL_2A03_PULSE1,
                FamiTrackerSyncRenderer.CHANNEL_2A03_PULSE2,
                FamiTrackerSyncRenderer.CHANNEL_2A03_TRIANGLE,
                FamiTrackerSyncRenderer.CHANNEL_2A03_NOISE);

        BytesPlayer player = new BytesPlayer();
        byte[] bs = new byte[3200];

        for (int i = 0; i < 6000; i++) {
            int size = renderer.render(bs, 0, 3201);
            player.writeSamples(bs, 0, size);
            if (renderer.isFinished()) {
                break;
            }
        }
    }
}
