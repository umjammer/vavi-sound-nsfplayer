package zdream.nsfplayer.xgm.player;

/**
 * Performance data with multiple songs
 *
 * @author Zdream
 */
public abstract class SoundDataMSP extends SoundData {

    public boolean enable_multi_tracks = false;

    /**
     * @return The track number being played
     */
    public abstract int getSong();

    public abstract void setSong(int song);

    /**
     * @return Total number of songs
     */
    public abstract int getSongNum();

}
