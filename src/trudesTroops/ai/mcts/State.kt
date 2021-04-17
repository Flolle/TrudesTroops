package trudesTroops.ai.mcts

/**
 * This interface defines a game state.
 *
 * The whole interface is defined as read-only, any methods that would mutate a game's state will return new State
 * instances. Any implementations of this interface need to heed this behaviour.
 */
// A is agent
// M is move
// S is state
interface State<A, M, S : State<A, M, S>> {
    /**
     * The agent that can make the next move.
     *
     * By convention, if this State reaches a terminal game state, this property should return the last agent that
     * made an action.
     */
    val currentAgent: A

    /**
     * The moves that the currently active agent can make.
     *
     * Please note that skipping the turn or not being able/allowed to make a move need to be specified moves since an
     * empty collection will mean that the state is terminal.
     *
     * Implementation note: This property is called a couple of times per MCTS iteration, as such it is recommended to
     * not compute this collection anew every time this property is called for performance reasons.
     */
    val possibleMoves: Collection<M>

    /**
     * Returns true if this State is in a terminal state.
     *
     * Default behaviour is to treat an empty [possibleMoves] collection as a terminal state.
     */
    val isTerminal: Boolean
        get() = possibleMoves.isEmpty()

    /**
     * Returns a new State instance with the given move made by this State's instance [currentAgent] as its new game
     * state.
     */
    fun makeMove(move: M): S

    operator fun plus(move: M): S = makeMove(move)
}