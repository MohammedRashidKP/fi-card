package com.closemates.games.models

import com.closemates.games.game.Card
import kotlinx.serialization.Serializable

@Serializable
data class GameSnapshot(
    val roomId: String,
    val state: GameState, // "lobby", "inProgress", "finished"
    val players: List<PlayerInfo>,
    val currentPlayerId: String?,
    val currentPlayerName: String?,
    val currentDealerId: String?,
    val currentDealerName: String?,
    val deckCount: Int = 0,
    val jokerCard: Card?,
    val scoreCard: ScoreCard?,
    var callerId: String? = null,
    var callValue: Int? = null,
    var strikerId: String? = null,
    var strikeValue: Int? = null,
    var roundHistory: MutableList<RoundScore> = mutableListOf(),
    var globalLeaderBoard: List<PlayerPoints>? = mutableListOf()
)

@Serializable
data class ScoreCard(
    val winnerId: String,
    val roundScore: List<PlayerScore>,
    val strikerId: String? = null
)

@Serializable
data class PlayerScore(
    val playerId: String,
    val playerName: String,
    val score: Int
)

enum class GameState {
    LOBBY, IN_PROGRESS, CALLED, FINISHED
}

@Serializable
data class PlayerInfo(
    val id: String,
    val name: String,
    val cardCount: Int,
    val openCard: Card? = null
)