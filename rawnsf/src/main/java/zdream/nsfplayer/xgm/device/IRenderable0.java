package zdream.nsfplayer.xgm.device;

/**
 * Renderable data interface
 *
 * @author Zdream
 */
public interface IRenderable0 {

    /**
     * Sound rendering
     *
     * @param bs Sound data for left and right channels, must be int[2]
     * @return The size of the synthesized data.
     * 1: Monaural
     * 2: Stereo
     * 0: Synthesis failed
     */
    int render(int[] bs);

    /**
     * Chip update/operation is now bound to CPU rendering (using the method <code>render()</code>)
     * Simply mix and output sound<br>
     * chip update/operation is now bound to CPU clocks
     * Render() now simply mixes and outputs sound
     */
    void tick(int clocks);

}
