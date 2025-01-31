package zdream.nsfplayer.xgm.device;

public interface ITrackInfo extends Cloneable {

    /**
     * @return 把现在的输出值
     */
    int getOutput();

    /**
     * @return 以 Hz 为单位的频率数
     */
    double getFreqHz();

    /**
     * @return 频率
     * [周波数をデバイス依存値で返す]
     */
    int getFreq();

    /**
     * @return 音量
     */
    int getVolume();

    /**
     * @return 最大音量
     */
    int getMaxVolumn();

    /**
     * @return 如果设备被关闭了, 返回 true
     */
    boolean getKeyStatus();

    /**
     * @return 播放的曲目号
     */
    int getTone();

    /**
     * @return
     * @see Cloneable
     */
    ITrackInfo clone();

}
