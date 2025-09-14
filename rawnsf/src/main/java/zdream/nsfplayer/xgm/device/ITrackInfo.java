package zdream.nsfplayer.xgm.device;

public interface ITrackInfo extends Cloneable {

    /**
     * @return The current output value
     */
    int getOutput();

    /**
     * @return Frequency in Hz
     */
    double getFreqHz();

    /**
     * @return Frequency
     * [Return frequency as a device-dependent value]
     */
    int getFreq();

    /**
     * @return Volume
     */
    int getVolume();

    /**
     * @return Maximum volume
     */
    int getMaxVolumn();

    /**
     * @return Returns true if the device is off
     */
    boolean getKeyStatus();

    /**
     * @return The track number being played
     */
    int getTone();

    /**
     * @return
     * @see Cloneable
     */
    ITrackInfo clone();

}
