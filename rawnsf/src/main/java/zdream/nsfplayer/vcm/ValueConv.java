package zdream.nsfplayer.vcm;

public class ValueConv {

    /**
     * Converts internal values to public values. Returns false if the conversion fails
     */
    public boolean getExportValue(ValueCtrl vt, Configuration cfg, String id, Value src_value, Value result) {
        result = src_value;
        return true;
    }

    /**
     * Converts public values to internal values. Returns false if the conversion fails
     */
    public boolean getImportValue(ValueCtrl vt, Configuration cfg, String id, Value src_value, Value result) {
        result = src_value;
        return true;
    }
}
