package zdream.nsfplayer.xgm.player;

/**
 * 标准 MIDI 音源的接口
 *
 * @author Zdream
 */
public interface MIDIDeviceI {

    /**
     * @param velocity=0 的时候是消音
     */
    void NoteOn(int ch, int note, int velocity);

    /**
     * @param velocity 是音消失的速度
     */
    void NoteOff(int ch, int note, int velocity);

    /**
     * 模拟按下复音键
     */
    void PolyKeyPressure(int ch, int note, int pressure);

    /**
     * 模拟按下通道
     */
    void ChannelPressure(int ch, int pressure);

    /**
     * 音高弯
     */
    void PitchBendChange(int ch, int data);

    /**
     * 控制变更
     */
    void ControlChange(int ch, int ctrl_no, int data);

    /**
     * 计划变更
     */
    void ProgramChange(int ch, int prg_no);

    /**
     * 模式消息
     */
    void ModeMessage(int ch, int mode, int data);

    /**
     * 独家消息
     */
    void ExclusiveMessage(int id, int[] data, int offset, int size);
}
