package zdream.test3;

import java.io.IOException;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FamiTrackerFormatException;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.process.agreement.WaitingAgreement;
import zdream.nsfplayer.ftm.process.base.FtmPosition;
import zdream.nsfplayer.ftm.renderer.FamiTrackerConfig;
import zdream.nsfplayer.ftm.renderer.FamiTrackerSyncRenderer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.utils.common.BytesPlayer;

import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_2A03_DPCM;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_2A03_NOISE;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_2A03_PULSE1;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_2A03_PULSE2;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_N163_1;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_N163_2;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_N163_3;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_N163_4;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM1;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM2;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM3;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM4;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM5;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM6;


/**
 * This use case shows how to use WaitingAgreement to control
 * the synchronization of multiple FamiTracker audios.
 *
 * @author Zdream
 * @since v0.3.1-test
 */
public class TestWaitingAgreement {

    public static void main(String[] args) throws FamiTrackerFormatException, IOException {
        String path =
                "src/test/resources/assets/N163 - Enigma of Aqua (Sync Play Version).ftm";
        String path2 =
                "src/test/resources/assets/VRC7 - Enigma of Aqua (Sync Play Version).ftm";

        FtmAudioFactory factory = new FtmAudioFactory();
        FtmAudio audio = factory.create(path);
        FtmAudio audio2 = factory.create(path2);

        // this use case show show to use waiting agreement to control the
        // synchronization of multiple fa fan tracker audios.
        FamiTrackerConfig c = new FamiTrackerConfig();
        XgmMixerConfig cc = new XgmMixerConfig();
        cc.channelType = XgmMixerConfig.TYPE_SINGER;
        c.mixerConfig = cc;
        // Select Mixer END

        // Select the track you want to render
        FamiTrackerSyncRenderer renderer = new FamiTrackerSyncRenderer(c);
        int exeId1 = renderer.allocateAll(audio);
        int exeId2 = renderer.allocateAll(audio2, 0, 5); // Track 2 starts at segment 5

        renderer.setSpeed(1f);
        renderer.free(exeId1, CHANNEL_2A03_DPCM);
        renderer.free(exeId2, CHANNEL_2A03_PULSE1);
        renderer.free(exeId2, CHANNEL_2A03_PULSE2);
        renderer.free(exeId2, CHANNEL_2A03_NOISE);

        // Setting the volume
        renderer.setLevel(exeId1, CHANNEL_N163_1, 0.6f);
        renderer.setLevel(exeId1, CHANNEL_N163_2, 0.6f);
        renderer.setLevel(exeId1, CHANNEL_N163_3, 0.6f);
        renderer.setLevel(exeId1, CHANNEL_N163_4, 0.9f);
        renderer.setLevel(exeId2, CHANNEL_VRC7_FM1, 0.4f);
        renderer.setLevel(exeId2, CHANNEL_VRC7_FM2, 0.5f);
        renderer.setLevel(exeId2, CHANNEL_VRC7_FM3, 0.5f);
        renderer.setLevel(exeId2, CHANNEL_VRC7_FM4, 0.4f);
        renderer.setLevel(exeId2, CHANNEL_VRC7_FM5, 0.4f);
        renderer.setLevel(exeId2, CHANNEL_VRC7_FM6, 0.4f);

        // At the beginning, track 2 is waiting,
        // and when track 1 reaches the beginning of segment 3, track 2 starts playing.
        WaitingAgreement a = new WaitingAgreement(exeId2, new FtmPosition(5), exeId1, new FtmPosition(3));
        a.setTimeoutForever(); // Never time out
        renderer.addWaitingAgreement(a);

        BytesPlayer player = new BytesPlayer();
        byte[] bs = new byte[3600];

        for (int i = 0; i < 60000; i++) {
            int size = renderer.render(bs, 0, bs.length);
            player.writeSamples(bs, 0, size);
            if (renderer.isFinished()) {
                break;
            }
        }
    }
}
