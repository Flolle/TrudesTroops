package trudesTroops.ai.mcts

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.read
import kotlin.math.ln
import kotlin.math.sqrt

// A is agent
// M is move
// S is state
class Mcts<A, M, S : State<A, M, S>>(
    private val startingState: S,
    private val playoutHandler: PlayoutHandler<A, M, S>,
    private val explorationParameterValue: Double = sqrt(2.0)
) {
    companion object {
        private const val NO_EXPLORATION: Double = 0.0

        fun <A, M, S : State<A, M, S>> getBestAssumedMoveWithTimeLimit(
            startingState: S,
            playoutHandler: PlayoutHandler<A, M, S>,
            timeLimitInMilliseconds: Int
        ): M {
            val calculationContinues = AtomicBoolean(true)
            val timer = Executors.newScheduledThreadPool(1)
            timer.schedule({ calculationContinues.set(false) }, timeLimitInMilliseconds.toLong(), TimeUnit.MILLISECONDS)
            timer.shutdown()

            return Mcts(startingState, playoutHandler).uctSearchForMostPromisingAction { calculationContinues.get() }
        }
    }

    fun uctSearchForMostPromisingAction(continueIterationChecker: () -> Boolean): M {
        if (startingState.isTerminal)
            throw IllegalArgumentException("Cannot choose an action from a terminal state!")

        val rootNode = TreeNode.rootNode(startingState)
        val numberOfThreads = Runtime.getRuntime().availableProcessors()
        val executor = Executors.newFixedThreadPool(numberOfThreads)

        repeat(numberOfThreads) {
            executor.execute {
                while (continueIterationChecker()) {
                    val selectedNode = selectAndExpand(rootNode)
                    backPropagate(selectedNode)
                }
            }
        }

        executor.shutdown()
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)

        return getMostPromisingAction(rootNode)
    }

    private fun selectAndExpand(node: TreeNode<A, M, S>): TreeNode<A, M, S> {
        var currentNode = node
        while (!currentNode.state.isTerminal) {
            synchronized(currentNode) {
                if (!currentNode.isFullyExpanded)
                    currentNode.getUntriedAction()
                else
                    null
            }?.let { action ->
                return currentNode.addChildWithAction(action)
            }

            currentNode = getBestChild(currentNode, explorationParameterValue)
        }

        return currentNode
    }

    private fun backPropagate(node: TreeNode<A, M, S>) {
        val terminalState = playoutHandler.playUntilTerminalStateReached(node.state)
        var currentNode: TreeNode<A, M, S>? = node
        while (currentNode != null) {
            val agentPerspective = currentNode.actionByAgent // The agent that made the last move.
            val reward = playoutHandler.getTerminalStateReward(terminalState, agentPerspective)
            currentNode.updateNodeValue(reward)

            currentNode = currentNode.parent
        }
    }

    private fun getMostPromisingAction(rootNode: TreeNode<A, M, S>): M {
        // No need to acquire locks, this method is only ever called at the end by a single thread.
        if (rootNode.children.isEmpty())
            throw UnsupportedOperationException("Operation not supported if child nodes empty!")
        else if (!rootNode.isFullyExpanded)
            throw UnsupportedOperationException("Operation not supported if node not fully expanded!")
        else if (rootNode.children.any { it.visitCount == 0 })
            throw UnsupportedOperationException("Operation not supported if node contains an unvisited child!")

        return getBestChild(rootNode, NO_EXPLORATION).actionTaken!!
    }

    private fun getBestChild(node: TreeNode<A, M, S>, explorationParameter: Double): TreeNode<A, M, S> =
        node.childrenLock.read {
            node.children.maxBy { calculateUctValue(it, explorationParameter) } ?: throw IllegalStateException()
        }

    private fun calculateUctValue(node: TreeNode<A, M, S>, explorationParameter: Double): Double =
        node.nodeValue + explorationParameter * sqrt(2 * ln(node.parentVisitCount.toDouble()) / node.visitCount)
}