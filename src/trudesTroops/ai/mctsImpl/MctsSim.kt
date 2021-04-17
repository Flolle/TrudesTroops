package trudesTroops.ai.mctsImpl

import trudesTroops.ai.mcts.Mcts
import trudesTroops.game.Card

object MctsSim {
    fun getBestAssumedCardPick(
        playerDeck: List<Card> = emptyList(),
        opponentDeck: List<Card> = emptyList(),
        runtimeInMilliseconds: Int
    ): Card =
        Mcts.getBestAssumedMoveWithTimeLimit(
            TTState(Player.PLAYER1, playerDeck, opponentDeck),
            TTPlayoutHandler,
            runtimeInMilliseconds
        )
}