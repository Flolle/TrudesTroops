package trudesTroops.ai.mctsImpl

import trudesTroops.ai.mcts.PlayoutHandler
import trudesTroops.game.Card
import trudesTroops.game.Game
import trudesTroops.game.GameResult
import java.util.concurrent.ThreadLocalRandom

object AAPlayoutHandler : PlayoutHandler<Player, Card, AAState> {
    override fun playUntilTerminalStateReached(state: AAState): AAState {
        val rng = ThreadLocalRandom.current()
        var currentState = state
        while (!currentState.isTerminal) {
            val move = findNthMoveInCollection(rng.nextInt(currentState.possibleMoves.size), currentState.possibleMoves)
            currentState = currentState.playMove(move)
        }

        return currentState
    }

    private fun findNthMoveInCollection(n: Int, collection: Collection<Card>): Card {
        collection.forEachIndexed { counter, possibleMove ->
            if (counter == n)
                return possibleMove
        }

        error("n is bigger than collection!")
    }

    override fun getTerminalStateReward(terminalState: AAState, playerPerspective: Player): Double {
        val playerDeck =
            if (terminalState.currentPlayer == playerPerspective)
                terminalState.currentPlayerDeck
            else
                terminalState.nextPlayerDeck
        val opponentDeck =
            if (terminalState.currentPlayer == playerPerspective)
                terminalState.nextPlayerDeck
            else
                terminalState.currentPlayerDeck

        return when (Game.computeGame(playerDeck, opponentDeck)) {
            GameResult.PLAYER1_WON -> 1.0
            GameResult.DRAW        -> 0.5
            GameResult.PLAYER2_WON -> 0.0
        }
    }
}