package trudesTroops.ai.geneticImpl

import trudesTroops.ai.GameHelper
import trudesTroops.game.Card
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object GeneticSim {
    fun getBestAssumedCardPickTimeLimit(
        timeLimitInMilliseconds: Int,
        numberOfIterationsPerGeneration: Int = 100,
        fitnessOption: FitnessOption = FitnessOption.WINNING_AND_DRAWS_PARTIAL,
        playerDeck: List<Card> = emptyList(),
        opponentDeck: List<Card> = emptyList()
    ): Card {
        if (playerDeck.size > 5 || opponentDeck.size > 5)
            throw IllegalArgumentException("The decks must contain 5 or less cards!")

        var playerFitness = createInitialFitness(playerDeck)
        var opponentFitness = createInitialFitness(opponentDeck)

        val calculationContinues = AtomicBoolean(true)
        val timer = Executors.newScheduledThreadPool(1)
        timer.schedule({ calculationContinues.set(false) }, timeLimitInMilliseconds.toLong(), TimeUnit.MILLISECONDS)
        timer.shutdown()

        while (calculationContinues.get()) {
            val generation = Generation(GenerationFitness(playerFitness, opponentFitness))
            val newFitness = generation.calculateGenerationFitness(numberOfIterationsPerGeneration, fitnessOption)
            playerFitness = newFitness.playerFitness
            opponentFitness = newFitness.opponentFitness
        }

        return playerFitness[playerDeck.size].maxByOrNull { it.value }!!.key
    }

    fun getBestAssumedCardPick(
        numberOfIterationsPerGeneration: Int = 100,
        numberOfGenerations: Int = 10,
        fitnessOption: FitnessOption = FitnessOption.WINNING_AND_DRAWS_PARTIAL,
        playerDeck: List<Card> = emptyList(),
        opponentDeck: List<Card> = emptyList()
    ): Card {
        if (playerDeck.size > 5 || opponentDeck.size > 5)
            throw IllegalArgumentException("The decks must contain 5 or less cards!")

        var playerFitness = createInitialFitness(playerDeck)
        var opponentFitness = createInitialFitness(opponentDeck)

        repeat(numberOfGenerations) {
            val generation = Generation(GenerationFitness(playerFitness, opponentFitness))
            val newFitness = generation.calculateGenerationFitness(numberOfIterationsPerGeneration, fitnessOption)
            playerFitness = newFitness.playerFitness
            opponentFitness = newFitness.opponentFitness
        }

        return playerFitness[playerDeck.size].maxByOrNull { it.value }!!.key
    }

    private fun createInitialFitness(preselectedDeck: List<Card>): Array<Map<Card, Int>> {
        val deckFitness = ArrayList<Map<Card, Int>>(6)
        val cardPool =
            if (preselectedDeck.isEmpty())
                EnumSet.allOf(Card::class.java)
            else
                EnumSet.complementOf(EnumSet.copyOf(preselectedDeck))

        for (preselectedCard in preselectedDeck)
            deckFitness.add(EnumMap<Card, Int>(Card::class.java).apply { put(preselectedCard, 1) })

        val fitnessMap = EnumMap<Card, Int>(Card::class.java)
        cardPool.forEach { fitnessMap[it] = 1 }
        for (rank in preselectedDeck.size..5) {
            if (rank > 0)
                deckFitness.add(fitnessMap)
            else
                deckFitness.add(EnumMap(fitnessMap).apply {
                    GameHelper.cardsNeverOptimalInFirstPosition.forEach { remove(it) }
                })
        }

        return deckFitness.toTypedArray()
    }
}