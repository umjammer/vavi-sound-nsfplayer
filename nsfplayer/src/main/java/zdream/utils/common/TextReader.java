package zdream.utils.common;

import java.util.Scanner;


/**
 * byte array reader
 *
 * @author Zdream
 * @date 2018-04-25
 * @since v0.1
 */
public class TextReader {

    final String text;

    /**
     * line number
     */
    int l = 0;

    final Scanner scanner;

    /**
     * Store this line of text
     */
    String lineBuf;

    public TextReader(String text) {
        this.text = text;
        scanner = new Scanner(text);
    }

    // basic operation

    /**
     * next line of text
     *
     * @return
     */
    public String nextLine() {
        if (!scanner.hasNextLine()) {
            lineBuf = null;
            l = 0;
        }

        lineBuf = scanner.nextLine();
        l++;

        return lineBuf;
    }

    /**
     * This line of text
     *
     * @return
     */
    public String thisLine() {
        return lineBuf;
    }

    /**
     * @return The line number of this line, the first line is 1.
     *         0 if the line has not been read or has already been read.
     */
    public int line() {
        return l;
    }

    public void close() {
        scanner.close();
    }

    public boolean isFinished() {
        return !scanner.hasNextLine();
    }

    // advanced operation

    /**
     * Ask if this line is now a valid line.
     * Blank lines and lines beginning with # are not valid lines.
     */
    public boolean isValidLine() {
        if (lineBuf.trim().isEmpty()) {
            return false;
        }
        if (lineBuf.charAt(0) == '#') {
            return false;
        }
        return true;
    }

    /**
     * Jump to the next valid line.
     *
     * @return Returns 0 when there is no next valid line.
     */
    public int toNextValidLine() {
        while (scanner.hasNextLine()) {
            nextLine();

            if (isValidLine()) {
                return l;
            }
        }
        return 0;
    }
}
