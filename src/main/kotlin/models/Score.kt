package com.closemates.games.models

import kotlinx.serialization.Serializable

@Serializable
data class RoundScore(
    val roundNumber: Int,
    val scores: List<PlayerScore>
)

@Serializable
data class ScoreFile(
    val rounds: MutableList<RoundScore> = mutableListOf()
)
