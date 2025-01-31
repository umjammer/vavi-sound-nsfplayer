package zdream.nsfplayer.ftm.factory;

import java.io.IOException;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.utils.common.BytesReader;
import zdream.utils.common.FileUtils;
import zdream.utils.common.TextReader;


/**
 * <p>Famitracker text file factory, responsible for parsing FTM txt and generating FtmAudio class.
 * <p>Generally speaking, the usage is to input a text {@link String}, {@link CharSequence} or byte[]
 * stream as a parameter. As for how to read the file, it is not the job of this factory class.
 * <p>Parse the text file exported by Ftm, using the method {@link #create(String)}
 *
 * @author Zdream
 */
public class FtmAudioFactory {

    /**
     * Parse Ftm files and generate Ftm-Audio.
     *
     * @param bs File data
     * @return
     */
    public FtmAudio create(byte[] bs) throws FamiTrackerFormatException {
        BytesReader reader = new BytesReader(bs);
        return createFtm(reader);
    }

    /**
     * Parse Ftm files and generate Ftm-Audio.
     *
     * @param filepath File Path
     * @return
     */
    public FtmAudio create(String filepath) throws IOException, FamiTrackerFormatException {
        DocumentReader openFile = new DocumentReader(filepath);

        openFile.open();

        // If it is an empty file, it will report an error directly.
        if (openFile.length() == 0) {
            throw new IOException("file: " + filepath + " is an empty file");
        }

        return createFtm(openFile);
    }

    private FtmAudio createFtm(BytesReader reader) throws FamiTrackerFormatException {
        FamiTrackerCreater creater = new FamiTrackerCreater();

        FtmAudio audio = new FtmAudio();
        creater.doCreate(reader, audio.handler);

        return audio;
    }

    /**
     * Parse the text file exported by Ftm and generate Ftm-Audio.
     *
     * @param txt File data, Any string type
     * @return
     */
    public FtmAudio createFromText(String txt) throws FamiTrackerFormatException {
        FamiTrackerTextCreater creater = new FamiTrackerTextCreater();

        TextReader reader = new TextReader(txt);
        FtmAudio audio = new FtmAudio();
        creater.doCreate(reader, audio.handler);

        return audio;
    }

    /**
     * Parse the text file exported by Ftm and generate Ftm-Audio.
     *
     * @param path File Path
     * @return
     */
    public FtmAudio createFromTextPath(String path) throws IOException, FamiTrackerFormatException {
        return createFromText(FileUtils.readFileAsString(path));
    }
}
