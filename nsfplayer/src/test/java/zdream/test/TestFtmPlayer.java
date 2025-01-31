package zdream.test;

import java.io.IOException;
import java.util.Properties;

import zdream.nsfplayer.ftm.FtmPlayerConsole;


/**
 * @author Zdream
 * @since v0.2.3-test
 */
public class TestFtmPlayer {

    /**
     * @param args for example: -mixer:blip
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();

        if (args.length > 0) {
            for (String arg : args) {
                if (arg.startsWith("-mixer:")) {
                    prop.setProperty("mixer", arg.substring("-mixer:".length()));
                }
            }
        }

        FtmPlayerConsole r = new FtmPlayerConsole(prop);
        r.go();
    }
}
