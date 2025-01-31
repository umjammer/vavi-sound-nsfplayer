package zdream.nsfplayer.xgm.device.misc;

import zdream.nsfplayer.nsf.device.IDevice;


public interface ILoopDetector extends IDevice {

    boolean isLooped(int timeInMs, int matchSecond, int matchInterval);

    int getLoopStart();

    int getLoopEnd();

    boolean isEmpty();

}
