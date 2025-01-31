package zdream.nsfplayer.core;

import java.io.Serial;


/**
 * <p>NSF Player exception
 * <p>This exception class is directly converted from the original FamiTrackerException
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public class NsfPlayerException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3361046346702576108L;

    public NsfPlayerException() {
    }

    public NsfPlayerException(String message) {
        super(message);
    }

    public NsfPlayerException(Throwable cause) {
        super(cause);
    }

    public NsfPlayerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NsfPlayerException(String message, Throwable cause, boolean enableSuppression,
                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
