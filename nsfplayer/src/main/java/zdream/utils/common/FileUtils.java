package zdream.utils.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Tool class, used to read, write files
 *
 * @author Zdream
 */
public class FileUtils {

    public static void writeFile(String path, String content) {
        File file = new File(path);
        FileWriter w = null;
        try {
            w = new FileWriter(file);
            w.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Reads the file as a byte[] stream.
     *
     * @param fn file name
     * @return
     * @throws IOException
     */
    public static byte[] readFile(String fn) throws IOException {
        File f = new File(fn);
        FileInputStream r = new FileInputStream(f);
        byte[] bs = new byte[(int) f.length()];
        r.read(bs);
        r.close();

        return bs;
    }

    /**
     * Reads a file as a String. Character set defaults to UTF-8
     *
     * @param fn file name
     * @return
     * @throws IOException
     */
    public static String readFileAsString(String fn) throws IOException {
        return readFileAsString(fn, "UTF-8");
    }

    /**
     * Reads a file as a String. Specify character set
     *
     * @param fn      file name
     * @param charset character set
     * @return
     * @throws IOException
     */
    public static String readFileAsString(String fn, String charset) throws IOException {
        byte[] bs = readFile(fn);
        return new String(bs, charset);
    }
}
