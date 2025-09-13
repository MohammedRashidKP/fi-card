package com.closemates.games.websocket

import com.closemates.games.game.Card
import kotlinx.serialization.Serializable

@Serializable
data class ClientMessage(
    val action: String,
    val roomId: String,
    val playerId: String,
    val name: String? = null,
    val index: Int? = null,
    val cardsToDiscard: List<Card> = emptyList()
)

@Serializable
data class CreateRoomResponse(
    val success: Boolean = false,
    var roomId: String? = null,
    var joinUrl: String? = null
)