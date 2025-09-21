package com.closemates.games.models

import kotlinx.serialization.Serializable

@Serializable
data class SoundCue(
    val cue: SoundEvent,
    val volume: Double = 1.0,
    val loop: Boolean = false
)

enum class SoundEvent {
    PLAYER_TURN, CALL, STRIKE, CALLER_WIN, STRIKER_WIN
}