package zdream.nsfplayer.nsf.audio;

import java.io.IOException;

import zdream.utils.common.FileUtils;


/**
 * <p>Used to generate NSF audio structures (NSFe is not supported in principle)
 *
 * @author Zdream
 * @since  0.1
 * @version 2018-01-16
 */
public class NsfAudioFactory {

    public NsfAudioFactory() {
    }

    public NsfAudio createFromFile(String path) throws IOException, NsfAudioFormatException {
        return create(FileUtils.readFile(path));
    }

    public NsfAudio create(byte[] image) throws NsfAudioFormatException {
        return create(image, 0);
    }

    /**
     * Read and generate NSF data from a byte array. NSFe is not supported
     *
     * @param image  Mirror byte array
     * @param offset The mirror byte data starts from the array, default is 0
     * @return nsf audio source
     * @throws NsfAudioFormatException Reading failed due to mismatched file formats
     */
    public NsfAudio create(byte[] image, int offset) throws NsfAudioFormatException {

        if (image.length < 0x80) // This is equivalent to checking image == null
            throw new NsfAudioFormatException("Mirror array is too small");

        // Index pointing to image
        int ptr = offset;

        NsfAudio audio = new NsfAudio();

        // Check the first 4 bytes
        byte[] HEAD = {'N', 'E', 'S', 'M'};
        for (byte b : HEAD) {
            if (b != image[ptr++]) {
                throw new NsfAudioFormatException("The file header identifier is incorrect");
            }
        }
        ptr++; // Byte 5 is ignored

        audio.version = (short) (image[ptr++] & 0xFF);
        audio.total_songs = (short) (image[ptr++] & 0xFF);
        audio.start = (short) ((image[ptr++] & 0xFF) - 1);

        audio.load_address = (image[ptr] & 0xFF) | ((image[ptr + 1] & 0xFF) << 8);
        ptr += 2;
        audio.init_address = (image[ptr] & 0xFF) | ((image[ptr + 1] & 0xFF) << 8);
        ptr += 2;
        audio.play_address = (image[ptr] & 0xFF) | ((image[ptr + 1] & 0xFF) << 8);
        ptr += 2;

        int end;

        // Title section
        int nextPtr = ptr + 32; // [ptr, nextPtr) This part is to specify the title data
        for (end = ptr; end < nextPtr; end++) {
            if (image[end] == 0)
                break;
        }
        audio.title = new String(image, ptr, end - ptr);

        // Artist Section
        ptr = nextPtr;
        nextPtr = ptr + 32;
        for (end = ptr; end < nextPtr; end++) {
            if (image[end] == 0)
                break;
        }
        audio.artist = new String(image, ptr, end - ptr);

        // Copyright Statement Section
        ptr = nextPtr;
        nextPtr = ptr + 32;
        for (end = ptr; end < nextPtr; end++) {
            if (image[end] == 0)
                break;
        }
        audio.copyright = new String(image, ptr, end - ptr);

        ptr = nextPtr;
        audio.speed_ntsc = (image[ptr] & 0xff) | ((image[ptr + 1] & 0xff) << 8);
        ptr += 2;

        // 0x70
        for (int i = 0; i < 8; i++) {
            audio.bankswitch[i] = (short) (image[ptr++] & 0xff);
        }

        audio.speed_pal = (image[ptr] & 0xff) | ((image[ptr + 1] & 0xff) << 8);
        ptr += 2;
        audio.pal_ntsc = (byte) (image[ptr++] & 0xff);

        if (audio.speed_pal == 0)
            audio.speed_pal = 19997;
        if (audio.speed_ntsc == 0)
            audio.speed_ntsc = 16639;
        audio.soundChip = (byte) (image[ptr++] & 0xff); // 0x7b

        // Extra occupies 4 bytes
        ptr += 4;

        byte[] body = new byte[image.length - ptr]; // ptr == 0x80
        System.arraycopy(image, 0x80, body, 0, body.length);
        audio.body = body;

        return audio;
    }
}
