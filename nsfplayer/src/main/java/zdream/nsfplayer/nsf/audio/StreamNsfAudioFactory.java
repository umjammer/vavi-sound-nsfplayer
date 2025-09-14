package zdream.nsfplayer.nsf.audio;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * <p>Used to generate NSF audio structures (NSFe is not supported in principle)
 *
 * @author Zdream
 * @apiNote umjammer
 * @version 2025-09-13
 * @since 0.2.12
 */
public class StreamNsfAudioFactory {

    private static final Logger logger = getLogger(StreamNsfAudioFactory.class.getName());

    public StreamNsfAudioFactory() {
    }

    public StreamNsfAudio create(InputStream image) throws NsfAudioFormatException, IOException {
        return create(image, 0);
    }

    /**
     * Read and generate NSF data from a byte array. NSFe is not supported
     *
     * @param is  Mirror byte array
     * @param offset The mirror byte data starts from the array, default is 0
     * @return
     * @throws NsfAudioFormatException Reading failed due to mismatched file formats
     */
    public StreamNsfAudio create(InputStream is, int offset) throws NsfAudioFormatException, IOException {

        if (is.available() < 0x80) // This is equivalent to checking is == null
            throw new NsfAudioFormatException("Mirror array is too small");

        // Index pointing to is
        is.skipNBytes(offset);

        StreamNsfAudio audio = new StreamNsfAudio();

        // Check the first 4 bytes
        byte[] HEAD = {'N', 'E', 'S', 'M'};
        for (byte b : HEAD) {
            if (b != (is.read() & 0xff)) {
                throw new NsfAudioFormatException("The file header identifier is incorrect");
            }
        }
        is.skipNBytes(1); // Byte 5 is ignored

        audio.setVersion((short) (is.read() & 0xFF));
        audio.setTrackCount((short) (is.read() & 0xFF));
logger.log(Level.DEBUG, "tracks: " + audio.getTrackCount());
        audio.setStart((short) ((is.read() & 0xFF) - 1));

        audio.setLoadAddress((is.read() & 0xFF) | ((is.read() & 0xFF) << 8));
        audio.setInitAddress((is.read() & 0xFF) | ((is.read() & 0xFF) << 8));
        audio.setPlayAddress((is.read() & 0xFF) | ((is.read() & 0xFF) << 8));

        int end;

        // Title section
        byte[] b = new byte[32]; // [ptr, nextPtr) This part is to specify the title data
        is.readNBytes(b, 0, 32);
        audio.setTitle(new String(b, 0, 32).replace("\0", ""));
logger.log(Level.DEBUG, "title: " + audio.getTitle());

        // Artist Section
        b = new byte[32];
        is.readNBytes(b, 0, 32);
        audio.setAuthor(new String(b, 0, 32).replace("\0", ""));
logger.log(Level.DEBUG, "author: " + audio.getAuthor());

        // Copyright Statement Section
        is.readNBytes(b, 0, 32);
        audio.setCopyright(new String(b, 0, 32).replace("\0", ""));
logger.log(Level.DEBUG, "copyright: " + audio.getCopyright());

        audio.setSpeedNtsc((is.read() & 0xff) | ((is.read() & 0xff) << 8));

        // 0x70
        for (int i = 0; i < 8; i++) {
            audio.setBankSwitch(i, (short) (is.read() & 0xff));
        }

        audio.setSpeedPal((is.read() & 0xff) | ((is.read() & 0xff) << 8));
        audio.setPalNtsc((byte) (is.read() & 0xff));

        if (audio.getSpeedPal() == 0)
            audio.setSpeedPal(19997);
        if (audio.getSpeedNtsc() == 0)
            audio.setSpeedNtsc(16639);
        audio.setSoundChip((byte) (is.read() & 0xff)); // 0x7b

        // Extra occupies 4 bytes
        is.skipNBytes(4);

        audio.setBody(is);

        return audio;
    }
}
