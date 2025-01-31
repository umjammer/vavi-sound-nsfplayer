package zdream.nsfplayer.ftm.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import zdream.nsfplayer.ftm.process.agreement.AbstractAgreementEntry;
import zdream.nsfplayer.ftm.process.agreement.BarrierAgreementEntry;
import zdream.nsfplayer.ftm.process.agreement.WaitingAgreement;
import zdream.nsfplayer.ftm.process.agreement.WaitingAgreementEntry;
import zdream.nsfplayer.ftm.process.base.FtmPosition;
import zdream.nsfplayer.ftm.renderer.FamiTrackerSyncRenderer;


/**
 * <p>A manager who coordinates the execution position and speed of each actuator
 * according to the protocol content
 * <p>Used in {@link FamiTrackerSyncRenderer}.
 * Supports signal protocol. Fence protocol is not supported in version v0.3.1
 * </p>
 *
 * @author Zdream
 * @since v0.3.1
 */
public class SyncProcessManager {

    final ArrayList<ExecutorProcessState> states = new ArrayList<>();

    private ExecutorProcessState getState(int exeId) {
        for (ExecutorProcessState state : states) {
            if (state.id == exeId) {
                return state;
            }
        }
        throw new NullPointerException("SyncProcessManager: There is no state corresponding to " + exeId);
    }

    /*
     * Actuator
     */

    /**
     * Adding an executor
     *
     * @param exeId Actuator identification number
     * @param pos   Initial position
     */
    public void addExecutor(int exeId, FtmPosition pos) {
        ExecutorProcessState state = new ExecutorProcessState(exeId);
        state.pos = pos;
        states.add(state);
    }

    /**
     * Deleting an executor
     *
     * @param exeId Actuator identification number
     */
    public void removeExecutor(int exeId) {
        clearAgreement(exeId);
        states.remove(getState(exeId));
    }

    /**
     * Update position for actuator
     *
     * @param exeId Actuator identification number
     * @param pos   Updated location
     */
    public void updatePosition(int exeId, FtmPosition pos) {
        ExecutorProcessState state = getState(exeId);
        FtmPosition oldPos = state.pos;

        if (oldPos.equals(pos)) {
            return;
        }

        state.pos = pos;
        updateExecutorBound(state);
    }

    /**
     * Updated bondage list
     *
     * @param state
     */
    private void updateExecutorBound(ExecutorProcessState state) {
        state.bounds.forEach(AbstractAgreementEntry::reset); // TODO Synchronous waits can do this, but fences cannot.
        state.bounds.clear();
        List<AbstractAgreementEntry> agreements = state.agreements.get(state.pos);

        if (agreements == null) {
            return;
        }

        for (AbstractAgreementEntry entry : agreements) {
            state.bounds.add(entry);
        }
    }

    /**
     * Update status every frame
     */
    public void updateStates() {
        HashSet<BarrierAgreementEntry> barriers = null;

        for (ExecutorProcessState state : states) {
            if (state.bounds.isEmpty()) {
                continue;
            }

            ListIterator<AbstractAgreementEntry> it = state.bounds.listIterator();

            while (it.hasNext()) {
                AbstractAgreementEntry e0 = it.next();
                if (e0 instanceof WaitingAgreementEntry e) {

                    ExecutorProcessState depend = getState(e.dependExeId);
                    if (e.dependPos.equals(depend.pos)) {
                        // End of the wait
                        e.countdown = -1;
                        it.remove();
                        continue;
                    }

                    // Need to wait
                    if (e.countdown == -1) {
                        e.countdown = e.baseTimeout;
                    } else if (e.countdown == 0) {
                        // Timeout, release
                        it.remove();
                        continue;
                    } else {
                        e.countdown--;
                    }
                } else if (e0 instanceof BarrierAgreementEntry) {
                    if (barriers == null) {
                        barriers = new HashSet<>();
                    }
                    barriers.add((BarrierAgreementEntry) e0);
                }
            }
        }

        // Update all fences
        if (barriers != null) {
            for (BarrierAgreementEntry entry : barriers) {
                // TODO

                if (entry.countdown == -1) {
                    entry.countdown = entry.baseTimeout;
                } else if (entry.countdown == 0) {
                    // Timeout, release TODO
//					state.bounds.remove(e);
                    continue;
                } else {
                    entry.countdown--;
                }
            }
        }
    }

    /**
     * Check the status of the actuator to see if it needs to wait
     *
     * @param exeId Actuator identification number
     */
    public boolean isWaiting(int exeId) {
        return !getState(exeId).bounds.isEmpty();
    }

    //
    // protocol
    //

    /**
     * Add Wait Protocol
     *
     * @param a Protocol Data
     */
    public void addWaitingAgreement(WaitingAgreement a) {
        WaitingAgreementEntry entry = a.createEntry();

        int waitExeId = entry.waitExeId;
        int dependExeId = entry.dependExeId;

        ExecutorProcessState waitExe = getState(waitExeId);
        ExecutorProcessState dependExe = getState(dependExeId);

        List<AbstractAgreementEntry> list = waitExe.agreements.get(entry.waitPos);
        if (list == null) {
            list = new ArrayList<>();
            list.add(entry);
            waitExe.agreements.put(entry.waitPos, list);
        } else {
            list.add(entry);
        }

        dependExe.refs.add(entry);

        // Check whether the agreement is about to be triggered now
        if (waitExe.pos.equals(entry.waitPos)) {
            waitExe.bounds.add(entry);
        }
    }

    /**
     * Delete waiting protocol
     *
     * @param a Protocol Data
     */
    public void removeWaitingAgreement(WaitingAgreement a) {
        int dependExeId = a.dependExeId;
        ExecutorProcessState dependExe = getState(dependExeId);

        int len = dependExe.refs.size();
        for (int i = 0; i < len; i++) {
            AbstractAgreementEntry e = dependExe.refs.get(i);
            if (e.is(a)) {
                removeWaitingAgreement((WaitingAgreementEntry) e);
                break;
            }
        }
    }

    private void removeWaitingAgreement(WaitingAgreementEntry entry) {
        // Remove the reference in dependExe
        int dependExeId = entry.dependExeId;
        ExecutorProcessState dependExe = getState(dependExeId);
        dependExe.refs.remove(entry);

        // Remove the reference in waitExe
        int waitExeId = entry.waitExeId;
        ExecutorProcessState waitExe = getState(waitExeId);
        List<AbstractAgreementEntry> list = waitExe.agreements.get(entry.waitPos);
        if (list != null) {
            list.remove(entry);
        }
        if (list.isEmpty()) {
            waitExe.agreements.remove(entry.waitPos);
        }

        if (entry.waitPos.equals(waitExe.pos)) {
            waitExe.bounds.remove(entry);
        }
    }

    /**
     * <p>Clear all protocol contents of the specified executor.
     * <p>If the executor has signed a waiting agreement, the agreement will be canceled
     * regardless of whether it is the waiting party or the relying party.
     * The protocol in the other object will also be deleted
     * </p>
     *
     * @param exeId
     */
    private void clearAgreement(int exeId) {
        ExecutorProcessState exe = getState(exeId);

        ArrayList<AbstractAgreementEntry> lists =
                new ArrayList<>(exe.agreements.size() + (exe.agreements.size() << 1));

        for (AbstractAgreementEntry entry : lists) {
            if (entry instanceof WaitingAgreementEntry) {
                removeWaitingAgreement((WaitingAgreementEntry) entry);
            }
        }
    }
}
