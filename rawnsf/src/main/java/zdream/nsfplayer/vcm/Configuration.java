package zdream.nsfplayer.vcm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class Configuration extends Observable {

    protected final Map<String, Value> data = new HashMap<>();

    /**
     * Read value. If not, return null.
     *
     * @param id
     * @return
     */
    public synchronized Value get(String id) {
        return data.get(id);
    }

    /**
     * Create value. If id already exists, the call fails and nothing is done.
     *
     * @param id key
     * @return If id already exists, return false
     */
    public synchronized boolean createValue(String id, Value value) {
        if (data.containsKey(id)) {
            return false;
        } else {
            data.put(id, value);
            return true;
        }
    }

    /**
     * Create value. If id already exists, the call fails and nothing is done.
     *
     * @param id key
     * @return If id already exists, return false
     */
    public boolean createValue(String id, int value) {
        return createValue(id, new Value(value));
    }

    /**
     * Create value. If id already exists, the call fails and nothing is done.
     *
     * @param id key
     * @return If id already exists, return false
     */
    public boolean createValue(String id, String value) {
        return createValue(id, new Value(value));
    }

    /**
     * set value
     */
    public synchronized void setValue(String id, Value value) {
        data.put(id, value);
    }

    /**
     * get value
     */
    public synchronized int getIntValue(String id) {
        return data.get(id).toInt();
    }

    /**
     * clear value
     */
    public synchronized void clear() {
        data.clear();
    }

    public synchronized void read(Configuration src) {
        Iterator<Entry<String, Value>> it = src.data.entrySet().iterator();

        for (; it.hasNext(); ) {
            Entry<String, Value> e = it.next();
            data.put(e.getKey(), e.getValue());
        }
    }

    public synchronized void write(Configuration src) {
        for (Entry<String, Value> e : data.entrySet()) {
            src.data.put(e.getKey(), e.getValue());
        }
    }

}
