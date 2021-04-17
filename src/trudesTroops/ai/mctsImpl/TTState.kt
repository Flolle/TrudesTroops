package trudesTroops.ai.mctsImpl

import trudesTroops.ai.GameHelper
import trudesTroops.ai.mcts.State
import trudesTroops.game.Card
import java.util.*

class TTState(
    override val currentAgent: Player,
    val currentPlayerDeck: List<Card>,
    val nextPlayerDeck: List<Card>
) : State<Player, Card, TTState> {
    override val isTerminal: Boolean
        get() = currentPlayerDeck.size == 6 && nextPlayerDeck.size == 6

    override val possibleMoves: EnumSet<Card> = when {
        currentPlayerDeck.size == 6 -> EnumSet.noneOf(Card::class.java)
        currentPlayerDeck.isEmpty() -> GameHelper.reasonableFirstPositionCards
        else                        -> EnumSet.complementOf(EnumSet.copyOf(currentPlayerDeck))
    }

    override fun makeMove(move: Card): TTState =
        TTState(
            if (currentAgent == Player.PLAYER1) Player.PLAYER2 else Player.PLAYER1,
            nextPlayerDeck,
            currentPlayerDeck + move
        )
}

enum class Player {
    PLAYER1, PLAYER2
}