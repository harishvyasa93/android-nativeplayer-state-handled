package com.harish.player.wrapper;

/**
 * @author HARISH.
 *         <p>
 *         Helper class which provides ncecessary methods to see if the player has the capability to handle certain states.
 */
public final class PlayerHelper {
    private static final String TAG = PlayerHelper.class.getSimpleName();

    /**
     * Helper method to see if the player is ready to start playback.
     *
     * @param player The native player.
     * @return TRUE if ready for playback, FALSE otherwise.
     */
    public static boolean isReady(Player player) {
        return player != null
                && player.getCurrentState() == Player.STATE_PREPARED;
    }

    /**
     * Helper method to see if the player is initialized with a data source.
     *
     * @param player The native player.
     * @return TRUE if initialized, FALSE otherwise.
     */
    public static boolean isInitialized(Player player) {
        return player != null
                && (player.getCurrentState() >= Player.STATE_INITIALIZED
                && player.getCurrentState() <= Player.STATE_COMPLETED);
    }
}
