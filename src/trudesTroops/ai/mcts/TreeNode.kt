package trudesTroops.ai.mcts

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

// A is agent
// M is move
// S is state
class TreeNode<A, M, S : State<A, M, S>> private constructor(
    val parent: TreeNode<A, M, S>?,
    val children: MutableList<TreeNode<A, M, S>> = ArrayList(),
    val actionTaken: M?,
    val actionByAgent: A,
    val state: S
) {
    companion object {
        fun <A, M, S : State<A, M, S>> rootNode(startingState: S): TreeNode<A, M, S> =
            TreeNode(
                parent = null,
                actionTaken = null,
                actionByAgent = startingState.currentAgent,
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

    fun addChildWithAction(action: M): TreeNode<A, M, S> {
        val childNode =
            TreeNode(
                parent = this,
                actionTaken = action,
                actionByAgent = state.currentAgent,
                state = state + action
            )
        childrenLock.write { children += childNode }

        return childNode
    }

    fun updateNodeValue(valueToAdd: Double) {
        nodeValueLock.write {
            visitCount++
            totalReward += valueToAdd
        }
    }
}