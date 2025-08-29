package zdream.nsfplayer.xgm.player;

/**
 * Interface of standard MIDI sound source
 *
 * @author Zdream
 */
public interface MIDIDeviceI {

    /**
     * @param velocity=0 is mute
     */
    void NoteOn(int ch, int note, int velocity);

    /**
     * @param velocity is the speed at which the sound disappears
     */
    void NoteOff(int ch, int note, int velocity);

    /**
     * Simulate pressing a polyphonic key
     */
    void PolyKeyPressure(int ch, int note, int pressure);

    /**
     * Simulate pressing a channel
     */
    void ChannelPressure(int ch, int pressure);

    /**
     * Pitch bend
     */
    void PitchBendChange(int ch, int data);

    /**
     * Control change
     */
    void ControlChange(int ch, int ctrl_no, int data);

    /**
     * Program change
     */
    void ProgramChange(int ch, int prg_no);

    /**
     * Mode message
     */
    void ModeMessage(int ch, int mode, int data);

    /**
     * Exclusive message
     */
    void ExclusiveMessage(int id, int[] data, int offset, int size);
}
