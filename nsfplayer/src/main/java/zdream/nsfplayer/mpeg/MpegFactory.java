package zdream.nsfplayer.mpeg;

import java.io.IOException;

import zdream.utils.common.FileUtils;


/**
 * <p>A factory for generating audio data in Mpeg format
 *
 * @author Zdream
 * @date 2018-01-16
 * @since v0.1
 */
public class MpegFactory {

    public MpegAudio createFromFile(String path) throws IOException, MpegAudioException {
        return create(FileUtils.readFile(path));
    }

    /**
     * Read and generate mpeg data from a byte array
     *
     * @param image Mirror byte array
     * @return
     * @throws MpegAudioException Reading failed due to mismatched file formats
     */
    public MpegAudio create(byte[] image) throws MpegAudioException {
        MpegAudio audio = new MpegAudio();

        int offset = handleId3v2(image);

        System.out.println(System.currentTimeMillis());
        if (offset == 0) {
            audio.data = image;
        } else {
            audio.data = new byte[image.length - offset];
            System.out.println(audio.data.length);
            System.arraycopy(image, offset, audio.data, 0, audio.data.length);
        }
        System.out.println(System.currentTimeMillis());

        return audio;
    }

    /**
     * Read and skip ID3V2 section
     *
     * @param image
     * @return The data length of the ID3V2 part, or 0 if there is no ID3V2 part.
     */
    int handleId3v2(byte[] image) {
        if (image[0] == 'I' && image[1] == 'D' && image[2] == '3') {
            int size = (image[6] & 0x7F) << 21 | (image[7] & 0x7F) << 14 | (image[8] & 0x7F) << 7 | (image[9] & 0x7F);
            return size + 10; // 10 is the size of the ID3V2 frame header, size does not include it
        }
        return 0;
    }
}

