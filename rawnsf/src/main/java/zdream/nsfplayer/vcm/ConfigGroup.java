package zdream.nsfplayer.vcm;

import java.util.Deque;
import java.util.List;


public class ConfigGroup {

    static class Pair {

        final String k;
        final ValueCtrl v;

        public Pair(String _k, ValueCtrl _v) {
            this.k = _k;
            this.v = _v;
        }
    }

    public Deque<Pair> members;
    public final String label;
    public final String desc;

    public List<ConfigGroup> subGroup;

    public ConfigGroup(String l, String d, Configuration b/*=NULL*/) {
        this.label = l;
        this.desc = d;
    }

    public boolean addSubGroup(ConfigGroup sub) {
        subGroup.add(sub);
        return true;
    }

    public boolean insert(String id, ValueCtrl ctrl, ValueConv conv /* = null */) {
        if (conv != null)
            ctrl.addConv(conv);
        this.members.add(new Pair(id, ctrl));
        return true;
    }

    public boolean insert(String id, ValueCtrl ctrl, List<ValueConv> convs) {
        ctrl.addConv(convs);
        this.members.add(new Pair(id, ctrl));
        return true;
    }

    public void clear() {
        members.clear();
    }
}
