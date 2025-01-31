package zdream.nsfplayer.ftm.process.agreement;

import java.lang.ref.WeakReference;

import zdream.nsfplayer.core.IResetable;

import static java.util.Objects.requireNonNull;


/**
 * <p>Contents of the agreement
 * </p>
 *
 * @author Zdream
 * @since v0.3.1
 */
public abstract class AbstractAgreementEntry implements IResetable {

    /**
     * A weak reference to the original instance.
     */
    private final WeakReference<AbstractAgreement> ref;

    /**
     * Timeout
     */
    public final int baseTimeout;

    /**
     * If the trigger is in progress, the value is greater than or equal to 0,
     * and the value is the remaining timeout period; if the trigger is not in progress,
     * the value is -1
     */
    public int countdown = -1;

    public AbstractAgreementEntry(AbstractAgreement ref) {
        requireNonNull(ref, "agreement == null");
        this.ref = new WeakReference<>(ref);
        this.baseTimeout = ref.getTimeout();
    }

    public boolean is(AbstractAgreement a) {
        return a == ref.get();
    }

    @Override
    public void reset() {
        countdown = baseTimeout;
    }
}
