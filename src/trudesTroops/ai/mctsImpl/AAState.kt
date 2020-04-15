package trudesTroops.ai.mctsImpl

import trudesTroops.ai.mcts.State
import trudesTroops.game.Card
import java.util.*
import kotlin.collections.ArrayList

class AAState(
    override val currentPlayer: Player,
    val currentPlayerDeck: List<Card>,
    val nextPlayerDeck: List<Card>
) : State<Player, Card, AAState> {
    override val isTerminal: Boolean
        get() = currentPlayerDeck.size == 6 && nextPlayerDeck.size == 6

    override val possibleMoves: EnumSet<Card> = when {
        currentPlayerDeck.size == 6 -> EnumSet.noneOf(Card::class.java)
        currentPlayerDeck.isEmpty() -> reasonableFirstPositionCards
        else                        -> EnumSet.complementOf(EnumSet.copyOf(currentPlayerDeck))
    }

    override fun playMove(move: Card): AAState =
        AAState(
            if (currentPlayer == Player.PLAYER1) Player.PLAYER2 else Player.PLAYER1,
            nextPlayerDeck,
            ArrayList(currentPlayerDeck).apply { add(move) }
        )

    companion object {
        private val cardsNeverOptimalInFirstPosition = EnumSet.of(
            Card.Wallman,
            Card.Cleric,
            Card.Medic,
            Card.Nurse,
            Card.Shieldmaiden,
            Card.MadScientist,
            Card.Page,
            Card.Martyr,
            Card.Sniper,
            Card.Horseman,
            Card.Siren
        )

        private val reasonableFirstPositionCards = EnumSet.complementOf(cardsNeverOptimalInFirstPosition)
    }
}

enum class Player {
    PLAYER1, PLAYER2
}