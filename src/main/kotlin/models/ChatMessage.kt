package com.closemates.games.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val playerId: String,
    val playerName: String? = null,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)