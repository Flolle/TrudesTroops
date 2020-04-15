package trudesTroops.game

import kotlin.math.ceil
import kotlin.math.max

class Game(
    private var firstPlayerCards: List<CardInstance>,
    private var secondPlayerCards: List<CardInstance>
) {

    companion object {
        fun addNewRank(
            player1Card: Card,
            player1CurrentDeck: MutableList<Card>,
            player2Card: Card,
            player2CurrentDeck: MutableList<Card>
        ) {
            if (player1CurrentDeck.isEmpty()) {
                player1CurrentDeck.add(player1Card)
                player2CurrentDeck.add(player2Card)
                return
            }

            if (player1Card == Card.Horseman || player2Card == Card.Siren) {
                val lastCard = player1CurrentDeck.removeAt(player1CurrentDeck.lastIndex)
                player1CurrentDeck.add(player1Card)
                player1CurrentDeck.add(lastCard)
            } else {
                player1CurrentDeck.add(player1Card)
            }
            if (player2Card == Card.Horseman || player1Card == Card.Siren) {
                val lastCard = player2CurrentDeck.removeAt(player2CurrentDeck.lastIndex)
                player2CurrentDeck.add(player2Card)
                player2CurrentDeck.add(lastCard)
            } else {
                player2CurrentDeck.add(player2Card)
            }
        }

        fun computeGame(player1Deck: List<Card>, player2Deck: List<Card>): GameResult {
            val deck1 = ArrayList<Card>()
            val deck2 = ArrayList<Card>()
            for (i in player1Deck.indices)
                addNewRank(player1Deck[i], deck1, player2Deck[i], deck2)

            return Game(deck1.map { it.getCardInstance() }, deck2.map { it.getCardInstance() }).playGame()
        }

        fun computeGameWithTurnByTurnSummary(
            player1Deck: List<Card>,
            player2Deck: List<Card>
        ): Pair<GameResult, List<GameState>> {
            val deck1 = ArrayList<Card>()
            val deck2 = ArrayList<Card>()
            for (i in player1Deck.indices)
                addNewRank(player1Deck[i], deck1, player2Deck[i], deck2)


            return Pair(
                Game(deck1.map { it.getCardInstance() }, deck2.map { it.getCardInstance() }).playGame(),
                Game(deck1.map { it.getCardInstance() }, deck2.map { it.getCardInstance() }).getTurnByTurnSummary()
            )
        }
    }

    private var turnNumber: Int = 1

    private val isGameFinished: Boolean
        get() = firstPlayerCards.isEmpty() || secondPlayerCards.isEmpty()

    private val isFirstPlayerWon: Boolean
        get() = firstPlayerCards.isNotEmpty() && secondPlayerCards.isEmpty()

    private val isSecondPlayerWon: Boolean
        get() = firstPlayerCards.isEmpty() && secondPlayerCards.isNotEmpty()

    fun playGame(): GameResult {
        if (isGameFinished)
            throw IllegalStateException("The game object is already in a finished state!")

        // Cannonball handling happens before the actual turns start.
        cannonballHandling()

        // Do the actual turn loop, but use an escape hatch in case both decks cancel each other out.
        var p1Ranks = firstPlayerCards.size
        var p2Ranks = secondPlayerCards.size
        var noCardDiedCounter = 0
        while (!isGameFinished) {
            playTurn()

            if (firstPlayerCards.size == p1Ranks && secondPlayerCards.size == p2Ranks) {
                noCardDiedCounter++
            } else {
                noCardDiedCounter = 0
                p1Ranks = firstPlayerCards.size
                p2Ranks = secondPlayerCards.size
            }

            if (noCardDiedCounter > 10) {
                firstPlayerCards = firstPlayerCards.drop(1)
                secondPlayerCards = secondPlayerCards.drop(1)
                noCardDiedCounter = 0
            }
        }

        return when {
            isFirstPlayerWon  -> GameResult.PLAYER1_WON
            isSecondPlayerWon -> GameResult.PLAYER2_WON
            else              -> GameResult.DRAW
        }
    }

    /**
     * The element index corresponds with the game state after the given turn was finished. Index 0 is a special case
     * that shows the game state before any actions have been taken.
     */
    fun getTurnByTurnSummary(): List<GameState> {
        if (isGameFinished)
            throw IllegalStateException("Cannot create turn by turn summary for finished game states!")

        val gameStates = ArrayList<GameState>()
        gameStates.add(GameState(firstPlayerCards.map { it.copy() }, secondPlayerCards.map { it.copy() }))

        // Cannonball handling happens before the actual turns start.
        cannonballHandling()

        // Do the actual turn loop, but use an escape hatch in case both decks cancel each other out.
        var p1Ranks = firstPlayerCards.size
        var p2Ranks = secondPlayerCards.size
        var noCardDiedCounter = 0
        while (!isGameFinished) {
            playTurn()

            if (firstPlayerCards.size == p1Ranks && secondPlayerCards.size == p2Ranks) {
                noCardDiedCounter++
            } else {
                noCardDiedCounter = 0
                p1Ranks = firstPlayerCards.size
                p2Ranks = secondPlayerCards.size
            }

            if (noCardDiedCounter > 10) {
                firstPlayerCards = firstPlayerCards.drop(1)
                secondPlayerCards = secondPlayerCards.drop(1)
                noCardDiedCounter = 0
            }

            gameStates.add(GameState(firstPlayerCards.map { it.copy() }, secondPlayerCards.map { it.copy() }))
        }

        return gameStates
    }

    private fun cannonballHandling() {
        val player1CannonballIndex = firstPlayerCards.indexOfFirst { it is Cannonball }
        if (player1CannonballIndex >= 0) {
            firstPlayerCards = firstPlayerCards.filterIndexed { index, _ -> index != player1CannonballIndex }
            secondPlayerCards = secondPlayerCards.filterIndexed { index, _ -> index != player1CannonballIndex }
        }
        val player2CannonballIndex = secondPlayerCards.indexOfFirst { it is Cannonball }
        if (player2CannonballIndex >= 0) {
            firstPlayerCards = firstPlayerCards.filterIndexed { index, _ -> index != player2CannonballIndex }
            secondPlayerCards = secondPlayerCards.filterIndexed { index, _ -> index != player2CannonballIndex }
        }
    }

    /**
     * How a turn resolves:
     *
     * - Rank 1 cards attack each other
     * - Rank 1 cards perform special attacks (if applicable)
     * - Sniper shoots
     * - Cards at or below 0 hp are removed
     * - Healing cards apply their abilities. Whichever is in lower rank heals first.
     */
    private fun playTurn() {
        applyBaseAttacks()

        applySpecialAttack(firstPlayerCards, secondPlayerCards)
        applySpecialAttack(secondPlayerCards, firstPlayerCards)

        if (firstPlayerCards.any { it is Sniper })
            applyAttackToDefendingTargetPosition(1, secondPlayerCards.lastIndex, secondPlayerCards)
        if (secondPlayerCards.any { it is Sniper })
            applyAttackToDefendingTargetPosition(1, firstPlayerCards.lastIndex, firstPlayerCards)

        removeKilledCards()
        if (isGameFinished)
            return

        applyHealing(firstPlayerCards)
        applyHealing(secondPlayerCards)

        turnNumber++
    }

    private fun applyBaseAttacks() {
        val player1BaseAttack = calculateBaseAttack(firstPlayerCards)
        val player2BaseAttack = calculateBaseAttack(secondPlayerCards)
        applyAttackToDefendingTargetPosition(player1BaseAttack, 0, secondPlayerCards)
        applyAttackToDefendingTargetPosition(player2BaseAttack, 0, firstPlayerCards)

        val player1First = firstPlayerCards[0]
        val player2First = secondPlayerCards[0]

        if (player1First is Gladiator)
            applyAttackToDefendingTargetPosition(ceil(player2BaseAttack / 2.0).toInt(), 0, secondPlayerCards)
        if (player2First is Gladiator)
            applyAttackToDefendingTargetPosition(ceil(player1BaseAttack / 2.0).toInt(), 0, firstPlayerCards)
    }

    private fun calculateBaseAttack(attackerCards: List<CardInstance>): Int {
        val attackerRank1Card = attackerCards[0]

        var attackerBaseAttack = attackerRank1Card.baseAttack
        if (attackerCards.size > 1) {
            val p1Rank2Card = attackerCards[1]
            if (p1Rank2Card is Page) {
                attackerBaseAttack++
            } else if (attackerBaseAttack > 0 && p1Rank2Card is MadScientist) {
                attackerBaseAttack *= 2
                attackerRank1Card.currentHP--
            }
        }

        return attackerBaseAttack
    }

    private fun applySpecialAttack(attackerCards: List<CardInstance>, defenderCards: List<CardInstance>) {
        val attackerRank1Card = attackerCards[0] as? AttackSpecial ?: return

        when (attackerRank1Card) {
            is Alchemist ->
                if (turnNumber % 2 == 1)
                    applyAttackToDefendingTargetPosition(2, 0, defenderCards)
                else
                    applyAttackToDefendingTargetPosition(4, 0, defenderCards)
            is Hammerman ->
                applyAttackToDefendingTargetPosition(
                    ceil(defenderCards[0].maxHP / 2.0).toInt(),
                    0,
                    defenderCards
                )
            is Bowman    ->
                if (defenderCards.size > 1)
                    applyAttackToDefendingTargetPosition(3, 1, defenderCards)
            is Lanceman  ->
                if (defenderCards.size == 2)
                    applyAttackToDefendingTargetPosition(1, 1, defenderCards)
                else if (defenderCards.size > 2) {
                    applyAttackToDefendingTargetPosition(1, 1, defenderCards)
                    applyAttackToDefendingTargetPosition(1, 2, defenderCards)
                }
            is Mage      -> {
                applyAttackToDefendingTargetPosition(3, 0, defenderCards)
                if (defenderCards.size > 1)
                    applyAttackToDefendingTargetPosition(2, 1, defenderCards)
                if (defenderCards.size > 2)
                    applyAttackToDefendingTargetPosition(2, 2, defenderCards)
            }
            is Dervish   -> {
                if (attackerCards.size > 1)
                    applyAttackToDefendingTargetPosition(3, 1, attackerCards)
                if (defenderCards.size > 1)
                    applyAttackToDefendingTargetPosition(3, 1, defenderCards)
            }
            is MadBomber -> {
                var attackerIndex = attackerCards.lastIndex
                while (attackerIndex >= 0)
                    applyAttackToDefendingTargetPosition(1, attackerIndex--, attackerCards)

                var defenderIndex = defenderCards.lastIndex
                while (defenderIndex >= 0)
                    applyAttackToDefendingTargetPosition(1, defenderIndex--, defenderCards)
            }
            else         -> error("Unhandled AttackSpecial card: $attackerRank1Card")
        }
    }

    private fun applyAttackToDefendingTargetPosition(
        attackBaseValue: Int,
        defendingTargetPosition: Int,
        defenderCards: List<CardInstance>
    ) {
        val positionBehind = defendingTargetPosition + 1
        val actualTargetPosition =
            if (defenderCards.size > positionBehind && defenderCards[positionBehind] is Martyr)
                positionBehind
            else
                defendingTargetPosition

        var attackValue = attackBaseValue
        val defendingCard = defenderCards[actualTargetPosition]

        if (attackValue > 0) {
            if (defenderCards.size > actualTargetPosition + 1) {
                val cardBehindDefender = defenderCards[actualTargetPosition + 1]
                if (cardBehindDefender is Shieldmaiden)
                    attackValue = max(attackValue - 1, 1)
            }

            if (defendingCard is Shieldbot && defendingCard.hasShield)
                defendingCard.hasShield = false
            else
                defendingCard.currentHP -= attackValue
        }
    }

    private fun applyHealing(playerCards: List<CardInstance>) {
        playerCards.forEachIndexed { index, card ->
            if (card is HealingSpecial)
                when (card) {
                    is Vampire -> {
                        if (index == 0)
                            card.heal(1)
                    }
                    is Nurse   -> {
                        val previousCard = if (index > 0) playerCards[index - 1] else null
                        val nextCard = if (index < playerCards.lastIndex) playerCards[index + 1] else null
                        previousCard?.heal(1)
                        nextCard?.heal(1)
                    }
                    is Medic   -> {
                        var i = 0
                        while (i < playerCards.size && playerCards[i].currentHP == playerCards[i].maxHP)
                            i++
                        if (i < playerCards.size)
                            playerCards[i].heal(1)
                    }
                    is Cleric  -> {
                        var i = playerCards.lastIndex
                        while (i >= 0 && playerCards[i].currentHP == playerCards[i].maxHP)
                            i--
                        if (i >= 0)
                            playerCards[i].heal(1)
                    }
                    else       -> error("Unhandled HealingSpecial card: $card")
                }
        }
    }

    private fun removeKilledCards() {
        val firstPlayer = ArrayList<CardInstance>(5)
        for (card in firstPlayerCards)
            if (card.currentHP > 0)
                firstPlayer.add(card)
        firstPlayerCards = firstPlayer

        val secondPlayer = ArrayList<CardInstance>(5)
        for (card in secondPlayerCards)
            if (card.currentHP > 0)
                secondPlayer.add(card)
        secondPlayerCards = secondPlayer
    }
}

enum class GameResult {
    PLAYER1_WON, PLAYER2_WON, DRAW
}

data class GameState(
    val player1Deck: List<CardInstance>,
    val player2Deck: List<CardInstance>
)