package zdream.utils.common;

import java.util.ArrayList;
import java.util.List;


/**
 * Slice the string according to the command line <br>
 *
 * @author Zdream
 * @version v0.1
 * @date 2016-04-16
 * @since v0.1
 */
public class CodeSpliter {

    public static String[] split(String str) {
        List<String> list = new ArrayList<>();
        char[] chars = str.toCharArray();

        int index = 0; // Index of chars
        int size = str.length(); // Total length of chars

        int head = 0; // Beginning index of the substring
        int tail = 0; // End index of the substring
        int status = 0;

        for (; index < size; index++) {
            char ch = chars[index];
            switch (status) {
                case 0: // No search started.
                    if (Character.isSpaceChar(ch)) {
                        continue;
                    } else if (ch == '\"') {
                        head = index;
                        status = 3;
                    } else {
                        head = index;
                        status = 1;
                    }
                    break;

                case 1: // Already found the header (not starting with double quotes)
                    if (Character.isSpaceChar(ch)) {
                        // Can be truncated
                        tail = index;
                        list.add(new String(chars, head, tail - head));
                        status = 0;
                    } else {
                        continue;
                    }

                case 3: // Already found the header (starts with double quotes)
                    if (ch == '\"') {
                        if (index > 0 && chars[index - 1] != '\\') {
                            // Can be truncated
                            tail = index;
                            String ss = new String(chars, head + 1, tail - head - 1);
                            ss.replace("\\\"", "\"");
                            list.add(ss);
                            status = 0;
                        }
                    } else {
                        continue;
                    }
            }
        }

        if (status == 1) {
            tail = index;
            list.add(new String(chars, head, tail - head));
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * For text like “123”, remove the quotation marks at both ends,
     * and after processing the intermediate format, it returns
     *
     * @since v0.2.5
     */
    public static String extract(String raw) {
        int head = 0; // including through
        int tail = raw.length(); // exclusive of

        if (raw.charAt(0) == '"') {
            head++;
        }
        if (raw.charAt(tail - 1) == '"') {
            tail--;
        }
        if (tail <= head) {
            return "";
        }

        char[] chars = raw.toCharArray();
        int status = 0;
        int index = head;
        StringBuilder b = new StringBuilder(tail - head);

        for (; index < tail; index++) {
            char ch = chars[index];
            switch (status) {
                case 0: // No search started.
                    if (ch == '\\') {
                        status = 1;
                    } else {
                        b.append(ch);
                    }
                    break;

                case 1: // We have found '\', with the following matches.
                    // '\"' convert '"'
                    // '\\' convert '\'
                    // '\ ' convert ' '
                    // In other cases, '\' is still considered a valid character '\'.

                    if (Character.isSpaceChar(ch) || ch == '"' || ch == '\\') {
                        b.append(ch);
                    } else {
                        b.append('\\').append(ch);
                    }
                    status = 0;
            }
        }

        // ending
        if (status == 1) {
            b.append('\\');
        }

        return b.toString();
    }
}
