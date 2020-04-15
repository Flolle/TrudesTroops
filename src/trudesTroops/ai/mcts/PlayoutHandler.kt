package trudesTroops.ai.mcts

/**
 * This interface is represents the playout policy and the rewards based on a given terminal state.
 */
// P is player
// M is move
// S is state
interface PlayoutHandler<P, M, S : State<P, M, S>> {
    /**
     * Plays the given [State] until a terminal state is reached and returns the terminal [State].
     *
     * What sort of playout policy is used is up to the implementation.
     */
    fun playUntilTerminalStateReached(state: S): S

    /**
     * Returns the reward value for the given terminal [State] from the perspective of the given player.
     *
     * For example, if the given player won in the given [State] the reward would be 1.0, while if the given player
     * lost it would be 0.0 and if the game ended in a draw it would be 0.5. Other valuations are possible and up to
     * the implementation.
     */
    fun getTerminalStateReward(terminalState: S, playerPerspective: P): Double
}