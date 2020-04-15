package trudesTroops.ai.mcts

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

// P is player
// M is move
// S is state
class TreeNode<P, M, S : State<P, M, S>> private constructor(
    val parent: TreeNode<P, M, S>?,
    val children: MutableList<TreeNode<P, M, S>> = ArrayList(),
    val actionTaken: M?,
    val actionByPlayer: P,
    val state: S
) {
    companion object {
        fun <P, M, S : State<P, M, S>> rootNode(startingState: S): TreeNode<P, M, S> =
            TreeNode(
                parent = null,
                actionTaken = null,
                actionByPlayer = startingState.currentPlayer,
                state = startingState
            )
    }

    val childrenLock = ReentrantReadWriteLock()

    private val nodeValueLock = ReentrantReadWriteLock()

    private var totalReward: Double = 0.0

    var visitCount: Int = 0
        get() = nodeValueLock.read { field }
        private set

    val isFullyExpanded: Boolean
        get() = triedActionCounter == untriedActions.size

    val parentVisitCount: Int
        get() = parent?.visitCount ?: 0

    val nodeValue: Double
        get() = nodeValueLock.read { totalReward / visitCount }

    private var triedActionCounter: Int = 0

    private val untriedActions: List<M> = ArrayList(state.possibleMoves).apply { shuffle() }

    fun getUntriedAction(): M = untriedActions[triedActionCounter++]

    fun addChildWithAction(action: M): TreeNode<P, M, S> {
        val newState = state.playMove(action)
        val childNode =
            TreeNode(
                parent = this,
                actionTaken = action,
                actionByPlayer = state.currentPlayer,
                state = newState
            )
        childrenLock.write { children.add(childNode) }

        return childNode
    }

    fun updateNodeValue(valueToAdd: Double) {
        nodeValueLock.write {
            visitCount++
            totalReward += valueToAdd
        }
    }
}