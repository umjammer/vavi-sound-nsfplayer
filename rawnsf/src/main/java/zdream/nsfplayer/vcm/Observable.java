package zdream.nsfplayer.vcm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * in vcm.h
 *
 * @author Zdream
 */
public class Observable {

    protected final Set<ObserverI> oblist = new HashSet<>();

    public void attachObserver(ObserverI p) {
        oblist.add(p);
    }

    public void detachObserver(ObserverI p) {
        oblist.remove(p);
    }

    public int getObserverNum() {
        return oblist.size();
    }

    public Iterator<ObserverI> getObserver() {
        if (!oblist.isEmpty())
            return oblist.iterator();
        else
            return null;
    }

    public void notify(int id) {
        for (ObserverI o : oblist) {
            o.notify(id);
        }
    }

}
