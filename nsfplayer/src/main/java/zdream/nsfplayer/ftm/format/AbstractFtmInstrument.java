package zdream.nsfplayer.ftm.format;

import zdream.nsfplayer.core.FtmChipType;


/**
 * <p>Musical instrument interface.
 *
 * <p>Note that since these classes are frequently accessed,
 * encapsulation methods such as get and set are no longer set.
 *
 * @author Zdream
 */
public abstract class AbstractFtmInstrument {

    /**
     * Identifies the type of instrument. For example, 2A03
     *
     * @return
     */
    public abstract FtmChipType instType();

    /**
     * Sequence number. In the same NsfAudio, the seq of the same type of instrument is different.
     * This value starts at 0
     */
    public int seq;

    public String name;
}
