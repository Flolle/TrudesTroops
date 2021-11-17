package trudesTroops.game

import kotlin.math.ceil
import kotlin.math.max

class Game(player1Deck: List<Card>, player2Deck: List<Card>) {
    companion object {
        fun addNewRank(
            player1Card: Card,
            player1CurrentDeck: MutableList<Card>,
            player2Card: Card,
            player2CurrentDeck: MutableList<Card>
        ) {
            if (player1CurrentDeck.isEmpty()) {
                player1CurrentDeck += player1Card
                player2CurrentDeck += player2Card
                return
            }

            if (player1Card == Card.Horseman || player2Card == Card.Siren) {
                val lastCard = player1CurrentDeck.removeLast()
                player1CurrentDeck += player1Card
                player1CurrentDeck += lastCard
            } else {
                player1CurrentDeck += player1Card
            }
            if (player2Card == Card.Horseman || player1Card == Card.Siren) {
                val lastCard = player2CurrentDeck.removeLast()
                player2CurrentDeck += player2Card
                player2CurrentDeck += lastCard
            } else {
                player2CurrentDeck += player2Card
            }
        }

        fun computeGame(player1Deck: List<Card>, player2Deck: List<Card>): GameResult {
            val deck1 = ArrayList<Card>()
            val deck2 = ArrayList<Card>()
            for (i in player1Deck.indices)
                addNewRank(player1Deck[i], deck1, player2Deck[i], deck2)

            return Game(deck1, deck2).playGame()
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
                Game(deck1, deck2).playGame(),
                Game(deck1, deck2).getTurnByTurnSummary()
            )
        }
    }

    private val deck1: Deck = Deck(player1Deck)

    private val deck2: Deck = Deck(player2Deck)

    private var turnNumber: Int = 1

    private val isGameFinished: Boolean
        get() = deck1.isEmpty || deck2.isEmpty

    private val isFirstPlayerWon: Boolean
        get() = !deck1.isEmpty && deck2.isEmpty

    private val isSecondPlayerWon: Boolean
        get() = deck1.isEmpty && !deck2.isEmpty

    fun playGame(): GameResult {
        if (isGameFinished)
            throw IllegalStateException("The game object is already in a finished state!")

        // Cannonball handling happens before the actual turns start.
        cannonballHandling()

        // Do the actual turn loop, but use an escape hatch in case both decks cancel each other out.
        var p1Ranks = deck1.ranks
        var p2Ranks = deck2.ranks
        var noCardDiedCounter = 0
        while (!isGameFinished) {
            playTurn()

            if (deck1.ranks == p1Ranks && deck2.ranks == p2Ranks) {
                noCardDiedCounter++
            } else {
                noCardDiedCounter = 0
                p1Ranks = deck1.ranks
                p2Ranks = deck2.ranks
            }

            if (noCardDiedCounter > 10) {
                deck1.removeCardAtRank(1)
                deck2.removeCardAtRank(1)
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
        gameStates.add(GameState(deck1.toList(), deck2.toList()))

        // Cannonball handling happens before the actual turns start.
        cannonballHandling()

        // Do the actual turn loop, but use an escape hatch in case both decks cancel each other out.
        var p1Ranks = deck1.ranks
        var p2Ranks = deck2.ranks
        var noCardDiedCounter = 0
        while (!isGameFinished) {
            playTurn()

            if (deck1.ranks == p1Ranks && deck2.ranks == p2Ranks) {
                noCardDiedCounter++
            } else {
                noCardDiedCounter = 0
                p1Ranks = deck1.ranks
                p2Ranks = deck2.ranks
            }

            if (noCardDiedCounter > 10) {
                deck1.removeCardAtRank(1)
                deck2.removeCardAtRank(1)
                noCardDiedCounter = 0
            }

            gameStates.add(GameState(deck1.toList(), deck2.toList()))
        }

        return gameStates
    }

    private fun cannonballHandling() {
        val cball1Rank = deck1.findRankOfType<Cannonball>()
        if (cball1Rank != -1) {
            deck1.removeCardAtRank(cball1Rank)
            deck2.removeCardAtRank(cball1Rank)
        }
        val cball2Rank = deck2.findRankOfType<Cannonball>()
        if (cball2Rank != -1) {
            deck1.removeCardAtRank(cball2Rank)
            deck2.removeCardAtRank(cball2Rank)
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

        applySpecialAttack(deck1, deck2)
        applySpecialAttack(deck2, deck1)

        if (deck1.any { it is Sniper })
            applyAttackToDefendingTargetRank(1, deck2.ranks, deck2)
        if (deck2.any { it is Sniper })
            applyAttackToDefendingTargetRank(1, deck1.ranks, deck1)

        deck1.removeKilledCards()
        deck2.removeKilledCards()
        if (isGameFinished)
            return

        applyHealing(deck1)
        applyHealing(deck2)

        turnNumber++
    }

    private fun applyBaseAttacks() {
        val player1BaseAttack = calculateBaseAttack(deck1)
        val player2BaseAttack = calculateBaseAttack(deck2)
        applyAttackToDefendingTargetRank(player1BaseAttack, 1, deck2)
        applyAttackToDefendingTargetRank(player2BaseAttack, 1, deck1)

        val player1First = deck1.firstRank!!.thisOrMartyr().card
        val player2First = deck2.firstRank!!.thisOrMartyr().card

        if (player1First is Gladiator)
            applyAttackToDefendingTargetRank(ceil(player2BaseAttack / 2.0).toInt(), 1, deck2)
        if (player2First is Gladiator)
            applyAttackToDefendingTargetRank(ceil(player1BaseAttack / 2.0).toInt(), 1, deck1)
    }

    private fun calculateBaseAttack(attackerCards: Deck): Int {
        val attackerRank1Card = attackerCards.firstRank!!.card

        var attackerBaseAttack = attackerRank1Card.baseAttack
        attackerCards.firstRank?.nextRank?.also {
            val p1Rank2Card = it.card
            if (p1Rank2Card is Page) {
                attackerBaseAttack++
            } else if (attackerBaseAttack > 0 && p1Rank2Card is MadScientist) {
                attackerBaseAttack *= 2
                attackerRank1Card.currentHP--
            }
        }

        return attackerBaseAttack
    }

    private fun applySpecialAttack(attackerCards: Deck, defenderCards: Deck) {
        val attackerRank1Card = attackerCards.firstRank?.card as? AttackSpecial ?: return

        when (attackerRank1Card) {
            is Alchemist ->
                if (turnNumber % 2 == 1)
                    applyAttackToDefendingTargetRank(2, 1, defenderCards)
                else
                    applyAttackToDefendingTargetRank(4, 1, defenderCards)
            is Hammerman ->
                applyAttackToDefendingTargetRank(
                    ceil(defenderCards.firstRank!!.card.maxHP / 2.0).toInt(),
                    1,
                    defenderCards
                )
            is Bowman    ->
                if (defenderCards.ranks > 1)
                    applyAttackToDefendingTargetRank(3, 2, defenderCards)
            is Lanceman  -> {
                if (defenderCards.ranks > 1)
                    applyAttackToDefendingTargetRank(1, 2, defenderCards)
                if (defenderCards.ranks > 2)
                    applyAttackToDefendingTargetRank(1, 3, defenderCards)
            }
            is Mage      -> {
                applyAttackToDefendingTargetRank(3, 1, defenderCards)
                if (defenderCards.ranks > 1)
                    applyAttackToDefendingTargetRank(2, 2, defenderCards)
                if (defenderCards.ranks > 2)
                    applyAttackToDefendingTargetRank(2, 3, defenderCards)
            }
            is Dervish   -> {
                if (attackerCards.ranks > 1)
                    applyAttackToDefendingTargetRank(3, 2, attackerCards)
                if (defenderCards.ranks > 1)
                    applyAttackToDefendingTargetRank(3, 2, defenderCards)
            }
            is MadBomber -> {
                var attackerIndex = attackerCards.ranks
                while (attackerIndex > 0)
                    applyAttackToDefendingTargetRank(1, attackerIndex--, attackerCards)

                var defenderIndex = defenderCards.ranks
                while (defenderIndex > 0)
                    applyAttackToDefendingTargetRank(1, defenderIndex--, defenderCards)
            }
            else         -> error("Unhandled AttackSpecial card: $attackerRank1Card")
        }
    }

    private fun applyAttackToDefendingTargetRank(
        attackBaseValue: Int,
        defendingTargetRank: Int,
        defenderCards: Deck
    ) {
        val defendingCardElement = defenderCards.getCardAtRank(defendingTargetRank)!!.thisOrMartyr()

        var attackValue = attackBaseValue
        val defendingCard = defendingCardElement.card

        if (attackValue > 0) {
            val nextCard = defendingCardElement.nextRank
            if (nextCard != null && nextCard.card is Shieldmaiden)
                attackValue = max(attackValue - 1, 1)

            if (defendingCard is Guard)
                attackValue = max(attackValue - 1, 1)

            if (defendingCard is Shieldbot && defendingCard.hasShield)
                defendingCard.hasShield = false
            else
                defendingCard.currentHP -= attackValue
        }
    }

    private fun applyHealing(playerCards: Deck) {
        playerCards.forEachCardElement { cardElement ->
            if (cardElement.card is HealingSpecial) {
                when (cardElement.card) {
                    is Vampire -> {
                        if (playerCards.firstRank == cardElement)
                            cardElement.card.heal(1)
                    }
                    is Nurse   -> {
                        cardElement.previousRank?.card?.heal(1)
                        cardElement.nextRank?.card?.heal(1)
                    }
                    is Medic   -> {
                        var index = playerCards.firstRank
                        while (index != null && index.card.currentHP == index.card.maxHP)
                            index = index.nextRank

                        index?.card?.heal(1)
                    }
                    is Cleric  -> {
                        var index = playerCards.lastRank
                        while (index != null && index.card.currentHP == index.card.maxHP)
                            index = index.previousRank

                        index?.card?.heal(1)
                    }
                    else       -> error("Unhandled HealingSpecial card: ${cardElement.card}")
                }
            }
        }
    }
}

enum class GameResult {
    PLAYER1_WON, PLAYER2_WON, DRAW
}

data class GameState(
    val player1Deck: List<CardInstance>,
    val player2Deck: List<CardInstance>
)