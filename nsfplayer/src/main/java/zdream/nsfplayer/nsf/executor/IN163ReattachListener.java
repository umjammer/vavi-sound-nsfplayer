package zdream.nsfplayer.nsf.executor;

/**
 * <p>In NSF, the number of outputs for most chips is determined at initialization,
 * Except for the N163 chip. Only the N163 chip has the number of speakers determined at runtime.
 * <p>So, when the N163 chip determines the number of speakers to follow,
 * it reports this information to the user.
 * <p>So when the N163 chip determines the number of speakers behind it,
 * it reports this information to the user. A listener interface is used here
 * to allow an external user to sense that this is happening, and thus connect to
 * the output of the N163 track.
 * </p>
 *
 * @author Zdream
 * @since v0.3.0
 */
public interface IN163ReattachListener {

    /**
     * Signal the user according to the number of tracks in N163 after reset.
     *
     * @param n163ChannelCount Number of tracks in N163 after reset
     */
    void onReattach(int n163ChannelCount);

}
