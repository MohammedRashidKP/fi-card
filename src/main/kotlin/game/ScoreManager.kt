package com.closemates.games.game

import com.closemates.games.models.PlayerScore
import com.closemates.games.models.RoundScore
import com.closemates.games.models.ScoreCard
import com.closemates.games.models.ScoreFile
import kotlinx.serialization.json.Json
import java.io.File

object ScoreManager {
    private val json = Json { prettyPrint = true }

    private fun getScoreFile(roomId: String): ScoreFile {
        val file = File("scores/$roomId.json")
        return if (file.exists()) {
            json.decodeFromString(ScoreFile.serializer(), file.readText())
        } else {
            ScoreFile()
        }
    }

    fun saveRoundScore(roomId: String, roundNumber: Int, scoreCard: ScoreCard) {

        val roundScores = scoreCard.roundScore.map { score ->
            PlayerScore(
                playerId = score.playerId,
                playerName = score.playerName,
                score = score.score
            )
        }
        val scoreFile = getScoreFile(roomId)
        scoreFile.rounds.add(RoundScore(roundNumber, roundScores))
        val file = File("scores/$roomId.json")
        file.parentFile.mkdirs() // make sure folder exists
        file.writeText(json.encodeToString(ScoreFile.serializer(), scoreFile))
    }

    fun getRoundHistoryPerRound(roomId: String): List<RoundScore> {
        val totals = mutableMapOf<String, Int>() // running totals per player
        val scoreFile = getScoreFile(roomId)
        val rounds =  scoreFile.rounds.map {
            RoundScore(
                roundNumber = it.roundNumber,
                scores = it.scores.sortedBy {score -> score.playerId }
            )
        }

        return rounds.map { round ->
            val newScores = round.scores.map { ps ->
                val runningTotal = totals.getOrDefault(ps.playerId, 0) + ps.score
                totals[ps.playerId] = runningTotal
                ps.copy(total = runningTotal)
            }

            // Compute rank per round based on running total
            val ranked = newScores.sortedByDescending { it.total }
            ranked.forEachIndexed { idx, ps -> ps.rank = idx + 1 }

            round.copy(scores = ranked)
        }
    }


    fun getGlobalLeaderboard(): List<PlayerScore>? {
        val folder = File("scores")
        if (!folder.exists() || !folder.isDirectory) return null

        val globalScores = mutableListOf<PlayerScore>()

        folder.listFiles { f -> f.extension == "json" }?.forEach { file ->
            val scoreFile = json.decodeFromString(ScoreFile.serializer(), file.readText())

            val globalScores: List<PlayerScore> = scoreFile.rounds
                .flatMap { it.scores }                  // flatten all round scores
                .groupBy { it.playerId }                // group by player
                .map { (playerId, scores) ->
                    PlayerScore(
                        playerId = playerId,
                        playerName = scores.first().playerName, // take name from first occurrence
                        score = scores.sumOf { it.score }      // total score
                    )
                }
                .sortedByDescending { it.score }        // highest score first

        }

        return globalScores.sortedBy { it.score }
    }

}
