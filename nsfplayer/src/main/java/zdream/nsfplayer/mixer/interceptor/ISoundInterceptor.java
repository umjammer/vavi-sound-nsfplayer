package zdream.nsfplayer.mixer.interceptor;

import zdream.nsfplayer.core.IEnable;
import zdream.nsfplayer.core.IResetable;


/**
 * Audio data interceptor
 *
 * @author Zdream
 * @since v0.2.3
 */
public interface ISoundInterceptor extends IResetable, IEnable {

    int execute(int value, int time);
}
