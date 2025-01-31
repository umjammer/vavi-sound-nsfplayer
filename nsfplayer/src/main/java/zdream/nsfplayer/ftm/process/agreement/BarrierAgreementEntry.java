package zdream.nsfplayer.ftm.process.agreement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import zdream.nsfplayer.ftm.process.base.FtmPosition;


/**
 * <p>Content data of the fence synchronization protocol
 * </p>
 *
 * @author Zdream
 * @since v0.3.1
 */
public class BarrierAgreementEntry extends AbstractAgreementEntry {

    public static BarrierAgreementEntry create(BarrierAgreement ref) {
        HashMap<Integer, FtmPosition> map = ref.getPoses();
        int len = map.size();

        if (len <= 1) {
            return null;
        }

        return new BarrierAgreementEntry(ref, map);
    }

    private BarrierAgreementEntry(BarrierAgreement ref, HashMap<Integer, FtmPosition> map) {
        super(ref);

        int len = map.size();
        exeIds = new int[len];
        poses = new FtmPosition[len];

        Iterator<Entry<Integer, FtmPosition>> it = map.entrySet().iterator();
        for (int i = 0; i < exeIds.length; i++) {
            Entry<Integer, FtmPosition> e = it.next();
            exeIds[i] = e.getKey();
            poses[i] = e.getValue();
        }

    }

    /**
     * List of actuator identification numbers. If invalid,
     * the identification number will be set to -1.
     */
    final int[] exeIds;
    final FtmPosition[] poses;
}
