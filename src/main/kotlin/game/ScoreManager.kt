package com.closemates.games.game

import com.closemates.games.models.*
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Comparator

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

    fun saveLeaderBoardPoint(roomId: String, roundNumber: Int, scoreCard: ScoreCard) {
        val scores = scoreCard.roundScore

        // Start with empty points map
        val pointsMap = mutableMapOf<String, Int>()

        // 1️⃣ Winner or striker points
        scores.forEach { ps ->
            pointsMap[ps.playerId] = when {
                scoreCard.strikerId != null && ps.playerId == scoreCard.strikerId -> 4
                scoreCard.strikerId == null && ps.playerId == scoreCard.winnerId -> 5
                else -> 0
            }
        }

        // 2️⃣ Compute second and third least scores
        val sortedScores = scores.sortedBy { it.score }
        val secondLeastScore = sortedScores.getOrNull(1)?.score
        val thirdLeastScore = sortedScores.getOrNull(2)?.score

        // Assign 2 points to second least
        secondLeastScore?.let { secScore ->
            sortedScores.filter { it.score == secScore }
                .forEach { pointsMap[it.playerId] = (pointsMap[it.playerId] ?: 0) + 2 }
        }

        // Assign 1 point to third least
        thirdLeastScore?.let { thirdScore ->
            sortedScores.filter { it.score == thirdScore }
                .forEach { pointsMap[it.playerId] = (pointsMap[it.playerId] ?: 0) + 1 }
        }

        // Convert to list
        val pointMap = scores.map { PlayerPoints(it.playerId, pointsMap[it.playerId] ?: 0) }
        val scoreFile = getScoreFile(roomId)
        scoreFile.rounds.add(RoundPoint(roundNumber, pointMap))
        val file = File("scores/$roomId.json")
        file.parentFile.mkdirs() // make sure folder exists
        file.writeText(json.encodeToString(ScoreFile.serializer(), scoreFile))
    }

    fun getGlobalLeaderboard(): List<PlayerPoints> {
        val folder = File("scores")
        if (!folder.exists() || !folder.isDirectory) return emptyList()
        val leaderBoard = mutableListOf<PlayerPoints>()
        folder.listFiles { f -> f.extension == "json" }?.forEach { file ->
            val scoreFile = json.decodeFromString(ScoreFile.serializer(), file.readText())

            leaderBoard.addAll(scoreFile.rounds
                .flatMap { it.scores }                  // flatten all round scores
                .groupBy { it.playerId }                // group by player
                .map { (playerId, scores) ->
                    PlayerPoints(
                        playerId = playerId,
                        points = scores.sumOf { it.points }
                    )
                }
                .sortedBy { it.points }
            )     // highest score first
        }
        return leaderBoard
            .groupBy { it.playerId }
            .map { (playerId, entries) ->
                PlayerPoints(
                    playerId = playerId,
                    points = entries.sumOf { it.points }
                )
            }
    }

}
