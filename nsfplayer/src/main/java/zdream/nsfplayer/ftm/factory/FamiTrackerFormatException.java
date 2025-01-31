package zdream.nsfplayer.ftm.factory;

import java.io.Serial;

import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.ftm.audio.FtmAudio;


/**
 * {@link FtmAudio} Errors during parsing, or when other structures are generated.
 *
 * @author Zdream
 * @since v0.1
 */
public class FamiTrackerFormatException extends NsfPlayerException {

    @Serial
    private static final long serialVersionUID = 6874694323781455170L;

    public FamiTrackerFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public FamiTrackerFormatException(String message) {
        super(message);
    }

    public FamiTrackerFormatException(Throwable cause) {
        super(cause);
    }
}
