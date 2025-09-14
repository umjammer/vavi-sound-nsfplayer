package zdream.nsfplayer.xgm.device;

import java.util.Arrays;


/**
 * Buffer
 *
 * @author Zdream
 */
public class InfoBuffer {

    int index;
    final Entry[] buffer;

    static class Entry {

        int i;
        ITrackInfo t;

        public Entry() {
        }

        public Entry(int i, ITrackInfo t) {
            this.i = i;
            this.t = t;
        }

        @Override
        public String toString() {
            return i + ":" + ((t == null) ? "null" : t.toString());
        }
    }

    public InfoBuffer(int bufmax) {
        index = 0;
        buffer = new Entry[bufmax];
        for (int i = 0; i < bufmax; i++) {
            buffer[i] = new Entry(0, null);
        }
    }

    public InfoBuffer() {
        this(600); // 60 * 10 = 600
    }

    public void clear() {
        for (Entry entry : buffer) {
            entry.i = 0;
            entry.t = null;
        }
    }

    public void addInfo(int pos, ITrackInfo t) {
        if (t != null) {
            buffer[index].i = pos;
            buffer[index].t = t.clone();
            index = (index + 1) % buffer.length;
        }
    }

    public ITrackInfo getInfo(int pos) {
        int bufmax = buffer.length;
        if (pos == -1)
            return buffer[(index + bufmax - 1) % bufmax].t;

        for (int i = (index + bufmax - 1) % bufmax; i != index; i = (i + bufmax - 1) % bufmax) {
            if (buffer[i].i <= pos)
                return buffer[i].t;
        }

        return null;
    }

    @Override
    public String toString() {
        return "InfoBuffer#" + index + Arrays.toString(this.buffer);
    }

}
