package zdream.nsfplayer.ftm.process.agreement;

import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.ftm.process.base.FtmPosition;
import zdream.nsfplayer.ftm.renderer.FamiTrackerSyncRenderer;

import static java.util.Objects.requireNonNull;


/**
 * <p>One-way wait synchronization protocol
 * <p>When {@link FamiTrackerSyncRenderer} starts multiple executors at the same time,
 * multiple executors need to be synchronized.
 * This protocol handles a situation where, when actuator A executes to position a,
 * it checks the execution position of actuator B.
 * When actuator B reaches a certain specified position, actuator A is released;
 * If actuator B has not reached a certain position,
 * A waits for actuator B until it reaches the specified position.
 * <p>This protocol is a one-way constraint, that is, A waits for B to arrive at the specified location,
 * which is transparent to B.
 * B does not need to wait for A to arrive at the specified location,
 * and the protocol will not make B wait.
 * <p>This protocol simulates the work of semaphores in multi-threaded concurrency.
 *
 * <p>The protocol supports the executor to exit the protocol midway. If in the above example,
 * the A or B executor is deleted during the execution, then the protocol is automatically canceled.
 * </p>
 *
 * @author Zdream
 * @since v0.3.1
 */
public class WaitingAgreement extends AbstractAgreement {

    public static final String NAME = "WAITING";

    /**
     * @throws NsfPlayerException When <code>waitExeId == dependExeId</code>
     */
    public WaitingAgreement(
            int waitExeId,
            FtmPosition waitPos,
            int dependExeId,
            FtmPosition dependPos) {
        requireNonNull(waitPos, "waitPos == null");
        requireNonNull(dependPos, "dependPos == null");
        if (waitExeId == dependExeId) {
            throw new NsfPlayerException("waitExeId == dependExeId");
        }
        this.waitExeId = waitExeId;
        this.waitPos = waitPos;
        this.dependExeId = dependExeId;
        this.dependPos = dependPos;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public WaitingAgreementEntry createEntry() {
        return WaitingAgreementEntry.create(this);
    }

    public final int waitExeId;
    public final FtmPosition waitPos;
    public final int dependExeId;
    public final FtmPosition dependPos;
}
