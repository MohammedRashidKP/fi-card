package com.closemates.games.models

import com.closemates.games.game.Card
import kotlinx.serialization.Serializable

@Serializable
data class HandInfo(
    val playerId: String,
    val hand: List<Card>,
    val openCard: Card? = null,
    val canCall: Boolean = false
)