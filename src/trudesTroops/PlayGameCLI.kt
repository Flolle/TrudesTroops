package trudesTroops

import trudesTroops.ai.geneticImpl.FitnessOption
import trudesTroops.ai.geneticImpl.GeneticSim
import trudesTroops.ai.mctsImpl.MctsSim
import trudesTroops.game.Card
import trudesTroops.game.Game
import java.util.concurrent.Executors
import kotlin.system.exitProcess

object PlayGameCLI {
    fun playGame(aiMillisecondsPerTurn: Int, aiPlayer: AIPlayer) {
        println("AI allotted milliseconds per turn: $aiMillisecondsPerTurn")
        println("AI algorithm: $aiPlayer\n")
        Card.values().forEach { println("${it.ordinal}: $it") }
        println()

        val executor = Executors.newFixedThreadPool(1)
        val playerDeck = ArrayList<Card>(6)
        val aiDeck = ArrayList<Card>(6)
        try {
            repeat(6) {
                val aiResult = executor.submit<Card> {
                    if (aiPlayer == AIPlayer.MCTS_ALGORITHM)
                        MctsSim.getBestAssumedCardPick(
                            playerDeck = aiDeck,
                            opponentDeck = playerDeck,
                            runtimeInMilliseconds = aiMillisecondsPerTurn
                        )
                    else
                        GeneticSim.getBestAssumedCardPickTimeLimit(
                            timeLimitInMilliseconds = aiMillisecondsPerTurn,
                            numberOfIterationsPerGeneration = 400,
                            fitnessOption = FitnessOption.WINNING_AND_DRAWS_PARTIAL,
                            playerDeck = aiDeck,
                            opponentDeck = playerDeck
                        )
                }

                println("\n##################################################\n")

                var input: String? = null
                while (input == null) {
                    print("Select your next unit:")
                    input = readLine()
                    // Ctrl+C seems to result in a null value, so we print a short message and exit the program in that case.
                    if (input == null) {
                        println("\nInvalid or null input, ending program.")
                        exitProcess(0)
                    }

                    val inputNumber = input.trim().toInt()
                    if (Card.values()[inputNumber] in playerDeck) {
                        println("\nThe given card has already been selected. Choose a different one!")
                        input = null
                    }
                }

                val inputNumber = input.trim().toInt()
                aiDeck.add(aiResult.get())
                playerDeck.add(Card.values()[inputNumber])

                println()
                printRanks(playerDeck, aiDeck)
            }
        } finally {
            executor.shutdown()
        }

        println("\n##################################################\n")
        printGameResult(playerDeck, aiDeck)
    }

    private fun printRanks(playerDeck: List<Card>, aiDeck: List<Card>) {
        if (playerDeck.size != aiDeck.size)
            throw IllegalArgumentException("Player and AI decks must have the same size!")

        val playerRanks = ArrayList<Card>(playerDeck.size)
        val aiRanks = ArrayList<Card>(playerDeck.size)
        repeat(playerDeck.size) { rank ->
            Game.addNewRank(playerDeck[rank], playerRanks, aiDeck[rank], aiRanks)
        }

        println("Player      vs      AI\n")
        repeat(6) { rank ->
            if (rank < playerRanks.size) {
                val playerCardName = playerRanks[rank].toString()
                println(playerCardName.padEnd(20) + aiRanks[rank])
            } else {
                println("empty               empty")
            }
        }
        println()
    }

    private fun printGameResult(playerDeck: List<Card>, aiDeck: List<Card>) {
        val (gameResult, turnByTurnSummary) = Game.computeGameWithTurnByTurnSummary(playerDeck, aiDeck)

        turnByTurnSummary.forEachIndexed { turnNumber, gameState ->
            println("End of turn: $turnNumber")
            println("Player      vs      AI\n")
            repeat(6) { rank ->
                val playerCardString =
                    if (rank < gameState.player1Deck.size)
                        "${gameState.player1Deck[rank]} (${gameState.player1Deck[rank].currentHP})"
                    else
                        "empty"
                val aiCardString =
                    if (rank < gameState.player2Deck.size)
                        "${gameState.player2Deck[rank]} (${gameState.player2Deck[rank].currentHP})"
                    else
                        "empty"

                println(playerCardString.padEnd(20) + aiCardString)
            }
            println("\n----\n")
        }

        println("Game result: $gameResult")
    }
}

enum class AIPlayer {
    MCTS_ALGORITHM, GENETIC_ALGORITHM
}