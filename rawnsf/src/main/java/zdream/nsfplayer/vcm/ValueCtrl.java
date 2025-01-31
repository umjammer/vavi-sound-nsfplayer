package zdream.nsfplayer.vcm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;


public class ValueCtrl {

    public static final int
            CT_INVALID = 0,
            CT_CHECK = 1,
            CT_SPIN = 2,
            CT_SLIDER = 3,
            CT_TEXT = 4,
            CT_COMBO = 5,
            CT_ENUM = 6,
            CT_NOBREAK = 7,
            CT_USER = 8;

    public final int ctrlType;
    public final String label;
    public final String desc;
    public final List<ValueConv> vcs = new ArrayList<>();

    /**
     * @param ct 在类中的 CT_* 的常数中选择
     */
    public ValueCtrl(String l, String d, int ct) {
        ctrlType = ct;
        label = l;
        desc = d;
    }

    public void addConv(ValueConv vc) {
        vcs.add(vc);
    }

    public void addConv(Collection<ValueConv> convs) {
        vcs.addAll(convs);
    }

    public void clearConv() {
        vcs.clear();
    }

    public boolean export(Configuration cfg, String id, Value result) {
        result = cfg.get(id);
        for (ListIterator<ValueConv> it = vcs.listIterator(vcs.size()); it.hasPrevious(); ) {
            Value tmp = result;
            ValueConv v = it.previous();
            if (!v.getExportValue(this, cfg, id, tmp, result))
                return false;
        }

        return true;
    }

    public boolean import_(Configuration cfg, String id, Value result) {

        for (ValueConv vc : vcs) {
            Value tmp = result;
            ValueConv v = vc;
            if (!v.getImportValue(this, cfg, id, tmp, result))
                return false;
        }

        cfg.setValue(id, result);
        return true;
    }
}
