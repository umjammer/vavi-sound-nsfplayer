package zdream.nsfplayer.xgm.player;

/**
 * <p>A player that can play music files containing multiple songs
 *
 * @author Zdream
 * @date 2017-12-06
 */
public abstract class MultiSongPlayer extends Player {

    /**
     * Play the next song
     *
     * @param step Skip to the next few songs
     * @return true on success, false on failure
     */
    public boolean nextSong(int s) {
        return false;
    }

    /**
     * Play the previous song
     *
     * @param step Skip to the previous few songs
     * @return true on success, false on failure
     */
    public boolean prevSong(int s) {
        return false;
    }

    /**
     * Set which song to play
     *
     * @param song Track number
     * @return true on success, false on failure
     */
    public boolean setSong(int song) {
        return false;
    }

    /**
     * Get which song is playing now
     *
     * @return Track number
     */
    public int getSong() {
        return -1;
    }

}
