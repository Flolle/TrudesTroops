package trudesTroops.ai.geneticImpl

import trudesTroops.game.Card
import trudesTroops.game.Game
import trudesTroops.game.GameResult
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class Generation(initialFitness: GenerationFitness) {
    private val playerCardRoulette = createCardRoulette(initialFitness.playerFitness)

    private val opponentCardRoulette = createCardRoulette(initialFitness.opponentFitness)

    private val rng = ThreadLocalRandom.current()

    private fun createCardRoulette(cardFitness: Array<Map<Card, Int>>): CardRoulette {
        val results = Array(6) { rank ->
            val roulette = IntArray(Card.values.size)
            var rouletteSize = 0

            if (cardFitness[rank].size == 1) {
                roulette[cardFitness[rank].keys.first().ordinal] = 1
                rouletteSize = 1
            } else {
                for ((card, fitness) in cardFitness[rank]) {
                    roulette[card.ordinal] = fitness
                    rouletteSize += fitness
                }

                // Add at least one entry for every still selectable card when the roulette is empty so that
                // the roulette always contains entries.
                if (rouletteSize == 0) {
                    // It is assumed that maps that only contain one single card are preselected cards.
                    val alreadySelectedCards = EnumSet.noneOf(Card::class.java).apply {
                        for (fitnessMap in cardFitness)
                            if (fitnessMap.size == 1)
                                addAll(fitnessMap.keys)
                    }

                    for (card in Card.values) {
                        if (card !in alreadySelectedCards) {
                            roulette[card.ordinal] = 1
                            rouletteSize += 1
                        }
                    }
                }
            }

            roulette to rouletteSize
        }

        return CardRoulette(results[0], results[1], results[2], results[3], results[4], results[5])
    }

    fun calculateGenerationFitness(numberOfHands: Int, fitnessOption: FitnessOption): GenerationFitness {
        val genPlayerHands = createHands(numberOfHands, playerCardRoulette)
        val genOpponentHands = createHands(numberOfHands, opponentCardRoulette)

        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val taskList = ArrayList<Future<Array<GameResult>>>(numberOfHands)
        repeat(numberOfHands) { playerHandIndex ->
            taskList.add(executor.submit<Array<GameResult>> {
                Array(numberOfHands) { opponentHandIndex ->
                    Game.computeGame(genPlayerHands[playerHandIndex], genOpponentHands[opponentHandIndex])
                }
            })
        }

        executor.shutdown()

        val playerHandsWins = DoubleArray(numberOfHands)
        val opponentHandsWins = DoubleArray(numberOfHands)
        taskList.forEachIndexed { playerHandIndex, playerHandResults ->
            playerHandResults.get().forEachIndexed { opponentHandIndex, gameResult ->
                when (gameResult) {
                    GameResult.PLAYER1_WON -> playerHandsWins[playerHandIndex]++
                    GameResult.PLAYER2_WON -> opponentHandsWins[opponentHandIndex]++
                    GameResult.DRAW        -> when (fitnessOption) {
                        FitnessOption.WINNING                   -> Unit //Do nothing
                        FitnessOption.WINNING_AND_DRAWS         -> {
                            playerHandsWins[playerHandIndex]++
                            opponentHandsWins[opponentHandIndex]++
                        }
                        FitnessOption.WINNING_AND_DRAWS_PARTIAL -> {
                            playerHandsWins[playerHandIndex] += 0.6
                            opponentHandsWins[opponentHandIndex] += 0.6
                        }
                    }
                }
            }
        }

        return GenerationFitness(
            createHandFitness(genPlayerHands, playerHandsWins),
            createHandFitness(genOpponentHands, opponentHandsWins)
        )
    }

    private fun createHands(numberOfHands: Int, cardRoulette: CardRoulette): Array<List<Card>> =
        Array(numberOfHands) {
            val hand = ArrayList<Card>(6)
            for (rank in 1..6) {
                val (roulette, rouletteSize) = cardRoulette[rank]
                val card = getNthCardInArray(rng.nextInt(rouletteSize), roulette)

                hand.add(card)
            }

            hand
        }

    private fun getNthCardInArray(n: Int, cardArray: IntArray): Card {
        var counter = n
        cardArray.forEachIndexed { cardOrdinal, amount ->
            counter -= amount
            if (counter < 0)
                return Card.values[cardOrdinal]
        }

        error("n is bigger than collection!")
    }

    private fun createHandFitness(hands: Array<List<Card>>, wins: DoubleArray): Array<Map<Card, Int>> {
        val results = Array<Map<Card, Int>>(6) { emptyMap() }
        val zeroPair = Pair(0.0, 0)

        repeat(6) { rank ->
            // numbers in pair = Pair(winValue, totalGames)
            val gameResultsMap = EnumMap<Card, Pair<Double, Int>>(Card::class.java)
            for (i in hands.indices) {
                val card = hands[i][rank]
                val previousMapValue = gameResultsMap.getOrDefault(card, zeroPair)

                gameResultsMap[card] = Pair(previousMapValue.first + wins[i], previousMapValue.second + hands.size)
            }

            val cardFitnessMap = EnumMap<Card, Int>(Card::class.java)
            for ((card, valuePair) in gameResultsMap) {
                val (winValue, totalGames) = valuePair
                val fitness = (winValue / totalGames * 100).roundToInt()

                cardFitnessMap[card] = fitness
            }

            results[rank] = cardFitnessMap
        }

        return results
    }
}

private class CardRoulette(
    val rank1RouletteAndSize: Pair<IntArray, Int>,
    val rank2RouletteAndSize: Pair<IntArray, Int>,
    val rank3RouletteAndSize: Pair<IntArray, Int>,
    val rank4RouletteAndSize: Pair<IntArray, Int>,
    val rank5RouletteAndSize: Pair<IntArray, Int>,
    val rank6RouletteAndSize: Pair<IntArray, Int>
) {
    operator fun get(rank: Int): Pair<IntArray, Int> = when (rank) {
        1    -> rank1RouletteAndSize
        2    -> rank2RouletteAndSize
        3    -> rank3RouletteAndSize
        4    -> rank4RouletteAndSize
        5    -> rank5RouletteAndSize
        6    -> rank6RouletteAndSize
        else -> throw IllegalArgumentException("Rank value must be between 1 and 5!")
    }
}

class GenerationFitness(
    val playerFitness: Array<Map<Card, Int>>,
    val opponentFitness: Array<Map<Card, Int>>
)

enum class FitnessOption {
    WINNING, WINNING_AND_DRAWS, WINNING_AND_DRAWS_PARTIAL
}