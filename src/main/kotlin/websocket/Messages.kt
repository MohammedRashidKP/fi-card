package com.closemates.games.websocket

import com.closemates.games.game.Card
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ClientMessage(
    val action: String,
    val roomId: String,
    val playerId: String,
    val name: String? = null,
    val index: Int? = null,
    val cardsToDiscard: List<Card> = emptyList(),
    val targetId: String? = null,
    val payload: JsonElement? = null,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class CreateRoomResponse(
    val success: Boolean = false,
    var roomId: String? = null
)