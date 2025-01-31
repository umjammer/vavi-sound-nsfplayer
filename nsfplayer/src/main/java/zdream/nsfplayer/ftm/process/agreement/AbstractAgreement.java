package zdream.nsfplayer.ftm.process.agreement;

import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.ftm.process.base.AgreementCommitedException;


/**
 * <p>Abstract protocol with timeout
 * </p>
 *
 * @author Zdream
 * @since v0.3.1
 */
public abstract class AbstractAgreement {

    public abstract String name();

    /**
     * Generate content entity
     *
     * @return
     */
    public abstract AbstractAgreementEntry createEntry();

    /*
     * Timeout
     */

    /**
     * <p>Timeout. If some actuators arrive at the specified position,
     * the waiting time of the first actuator to arrive is recorded.
     * If the timeout period has expired and the actuator specified in the protocol
     * has not reached the specified position, all the actuators waiting in the protocol
     * will be released at the same time.
     * <p>The time unit is one frame, which is a positive number.
     * </p>
     */
    private int timeout = 60;

    /**
     * @return Timeout
     * @see #timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Setting Timeout
     *
     * @param timeout Timeout, Positive number.
     * @throws AgreementCommitedException If this protocol has been submitted when this method is called,
     *         the modification will be rejected and this exception will be thrown
     * @see #timeout
     */
    public void setTimeout(int timeout) {
        synchronized (this) {
            if (commited) {
                throw new AgreementCommitedException("The agreement has been submitted, no modification is allowed: " + this);
            }
        }

        if (timeout < 1) {
            throw new NsfPlayerException("Timeout: " + timeout + " must be a positive number");
        }
        this.timeout = timeout;
    }

    /**
     * Set Timeout to forever, which means it will never time out.
     *
     * @see #setTimeout(int)
     */
    public void setTimeoutForever() {
        setTimeout(Integer.MAX_VALUE);
    }

    //
    // submit
    //

    /*
     * Once the agreement is submitted, it cannot be modified.
     */
    private volatile boolean commited = false;

    public synchronized void commit() {
        setCommited(true);
    }

    /**
     * <p>Asks whether the agreement has been submitted for processing.
     * <p>Once the protocol has been submitted, any modification to its basic
     * properties will be rejected and an exception will be thrown.
     * </p>
     */
    public synchronized boolean isCommited() {
        return commited;
    }

    protected synchronized void setCommited(boolean commited) {
        this.commited = commited;
    }
}
