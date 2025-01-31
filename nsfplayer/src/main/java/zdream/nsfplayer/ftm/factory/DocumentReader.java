package zdream.nsfplayer.ftm.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import zdream.utils.common.BytesReader;


public class DocumentReader extends BytesReader {

    public DocumentReader(String fileName) {
        file = new File(fileName);
    }

    final File file;

    /**
     * Open and create a file, and read all the data in the file as a byte array.
     *
     * @throws IOException
     */
    public void open() throws IOException {
        FileInputStream reader = new FileInputStream(file);
        bs = new byte[(int) file.length()];
        reader.read(bs);
        offset = 0;

        reader.close();
    }

    @Override
    public int length() {
        return (int) file.length();
    }

    @Override
    public void set(byte[] bs) {
        // do-nothing
    }
}
