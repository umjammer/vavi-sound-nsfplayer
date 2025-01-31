package zdream.nsfplayer.ftm.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import zdream.nsfplayer.ftm.process.agreement.AbstractAgreementEntry;
import zdream.nsfplayer.ftm.process.base.FtmPosition;


/**
 * <p>Executor execution status
 * </p>
 *
 * @author Zdream
 * @since v0.3.1
 */
class ExecutorProcessState {

    /**
     * Executor ID
     */
    final int id;

    ExecutorProcessState(int id) {
        this.id = id;
    }

    /**
     * All synchronization protocols of this executor, that is,
     * the protocols that will cause this executor to wait
     */
    final HashMap<FtmPosition, List<AbstractAgreementEntry>> agreements = new HashMap<>();

    /**
     * The synchronization protocol depends on the executor.
     * If the executor is removed or changed, the protocol will change.
     */
    final ArrayList<AbstractAgreementEntry> refs = new ArrayList<>();

    /**
     * Current location
     */
    FtmPosition pos;

    /**
     * The protocol that is binding the executor and making it wait
     */
    final ArrayList<AbstractAgreementEntry> bounds = new ArrayList<>();
}
