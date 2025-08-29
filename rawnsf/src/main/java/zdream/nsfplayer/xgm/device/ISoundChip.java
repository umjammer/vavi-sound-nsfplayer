package zdream.nsfplayer.xgm.device;

import zdream.nsfplayer.nsf.device.IDevice;


/**
 * virtual sound card
 *
 * @author Zdream
 */
public interface ISoundChip extends IDevice, IRenderable0 {

    /**
     * Set the clock cycle of the chip
     *
     * @param clock clock cycles
     */
    void setClock(double clock);

    /**
     * Set speech synthesis rate
     */
    void setRate(double r);

    /**
     * Channel mask.
     */
    void setMask(int mask);

    /**
     * Stereo mix.
     *
     * @param mixl left channel = 0-256
     * @param mixr right channel = 0-256
     *             128 = neutral
     *             256 = double
     *             0 = nil
     *             <0 = inverted
     */
    void setStereoMix(int trk, int mixl, int mixr);

    /**
     * Track info for keyboard view.
     *
     * @param trk
     * @return Default is null
     */
    ITrackInfo getTrackInfo(int trk);

}
