package zdream.nsfplayer.vcm;

/**
 * Value. in vcm.h
 *
 * @author Zdream
 */
public class Value {

    public String data; // Any type can be saved
    public boolean update; // True if updated

    public Value(int i) {
        this.data = Integer.toString(i);
    }

    public Value(String o) {
        this.data = o;
    }

    public int toInt() {
        return Integer.parseInt(data);
    }

    public String toString() {
        return data;
    }

    public void set(String o) {
        this.data = o;
        this.update = true;
    }

    public void set(int i) {
        this.data = Integer.toString(i);
        this.update = true;
    }
}
