package zdream.nsfplayer.ftm.process.agreement;

import java.util.HashMap;

import zdream.nsfplayer.ftm.process.base.AgreementCommitedException;
import zdream.nsfplayer.ftm.process.base.FtmPosition;
import zdream.nsfplayer.ftm.renderer.FamiTrackerSyncRenderer;

import static java.util.Objects.requireNonNull;


/**
 * <p>Fence Synchronization Protocol
 * <p>When {@link FamiTrackerSyncRenderer} starts multiple executors at the same time,
 * multiple executors need to be synchronized.
 * This protocol handles a situation where when actuator A executes to position a,
 * or actuator B executes to position b,
 * when two or more actuators execute to the specified position, they must wait for other actuators to
 * execute to the specified position before they can be released and executed simultaneously.
 * <p>This protocol simulates the work of barriers in multi-threaded concurrency.
 *
 * <p>The protocol supports an executor to exit the protocol midway.
 * If executor A is deleted during the execution,
 * then executor A and related data will be removed from the protocol.
 * If the number of remaining executors in the protocol is less than or equal to 1,
 * the protocol is automatically canceled.
 * </p>
 *
 * @author Zdream
 * @since v0.3.1
 */
public class BarrierAgreement extends AbstractAgreement {

    public static final String NAME = "BARRIER";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public AbstractAgreementEntry createEntry() {
        return BarrierAgreementEntry.create(this);
    }

    /*
     * Agreement Content
     */

    /**
     * exeId - pos
     */
    private final HashMap<Integer, FtmPosition> poses = new HashMap<>();

    public HashMap<Integer, FtmPosition> getPoses() {
        return new HashMap<>(poses);
    }

    /**
     * Add sync position to the protocol.
     *
     * @param exeId Executor ID
     * @param pos   The execution location where the fence is needed for synchronization
     * @throws AgreementCommitedException If this protocol has been submitted when this method is called,
     *         the modification will be rejected and this exception will be thrown
     */
    public void put(int exeId, FtmPosition pos) throws AgreementCommitedException {
        requireNonNull(pos, "pos == null");
        synchronized (this) {
            if (isCommited()) {
                throw new AgreementCommitedException("The agreement has been submitted, no modification is allowed: " + this);
            } else {
                poses.put(exeId, pos);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("protocol %s: %s", NAME, poses);
    }
}
