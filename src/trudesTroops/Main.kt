package trudesTroops

import trudesTroops.ai.geneticImpl.FitnessOption
import trudesTroops.ai.geneticImpl.GeneticSim
import trudesTroops.ai.mctsImpl.MctsSim
import trudesTroops.game.Card
import trudesTroops.game.Game
import trudesTroops.game.GameResult
import java.util.*

fun main(args: Array<String>) {
    if (args.contains("-debug")) {
        debug()
        return
    }

    var difficulty = Difficulty.EASY
    if (args.contains("-difficulty"))
        difficulty = when (args[args.indexOf("-difficulty") + 1].toLowerCase(Locale.ENGLISH)) {
            "easy" -> Difficulty.EASY
            "medium" -> Difficulty.MEDIUM
            "hard" -> Difficulty.HARD
            "very_hard" -> Difficulty.VERY_HARD
            else        -> error("Invalid difficulty setting!")
        }

    PlayGameCLI.playGame(difficulty)
}

fun debug() {
    val numberOfPlayouts = 10
    val playoutResults = PlayoutResult()
    for (playoutNumber in 1..numberOfPlayouts) {
        println("Playout: $playoutNumber")
        when (runTestPlayout()) {
            GameResult.PLAYER1_WON -> playoutResults.wins++
            GameResult.PLAYER2_WON -> playoutResults.losses++
            GameResult.DRAW -> playoutResults.draws++
        }
        println()
    }

    println("\n\nPlayout results (player 1 perspective):")
    println("wins: ${playoutResults.wins}")
    println("draws: ${playoutResults.draws}")
    println("losses: ${playoutResults.losses}")
}

fun runTestPlayout(): GameResult {
    val player1 = ArrayList<Card>()
    val player2 = ArrayList<Card>()
    repeat(6) {
        val player1Pick =
            MctsSim.getBestAssumedCardPick(
                playerDeck = player1,
                opponentDeck = player2,
                runtimeInMilliseconds = 1500
            )
        val player2Pick =
            /*GeneticSim.getBestAssumedCardPick(
                numberOfGenerations = 6,
                numberOfIterationsPerGeneration = 100,
                fitnessOption = FitnessOption.WINNING_AND_DRAWS_PARTIAL,
                playerDeck = player2,
                opponentDeck = player1
            )*/
            GeneticSim.getBestAssumedCardPickTimeLimit(
                timeLimitInMilliseconds = 500,
                numberOfIterationsPerGeneration = 400,
                fitnessOption = FitnessOption.WINNING_AND_DRAWS_PARTIAL,
                playerDeck = player2,
                opponentDeck = player1
            )
        player1.add(player1Pick)
        player2.add(player2Pick)
    }

    println()
    println("player 1: $player1")
    println("player 2: $player2")
    println()

    val result = Game.computeGame(player1, player2)

    println("game result: $result")

    return result
}

data class PlayoutResult(
    var wins: Int = 0,
    var draws: Int = 0,
    var losses: Int = 0
)